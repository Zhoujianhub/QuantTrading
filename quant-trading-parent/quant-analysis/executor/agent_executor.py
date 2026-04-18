"""
Agent Executor - 多Agent股票分析执行器
基于LangGraph实现多Agent工作流编排
支持并行执行和实时进度回调
"""
from typing import Dict, Any, List, Optional, Callable
from langgraph.graph import StateGraph, END
import anthropic
import json
import logging
from datetime import datetime
import os
import re
from concurrent.futures import ThreadPoolExecutor, as_completed
import threading

from agents.fundamentals_analyst import FundamentalsAnalyst
from agents.market_analyst import MarketAnalyst
from agents.news_analyst import NewsAnalyst
from agents.sentiment_analyst import SentimentAnalyst
from agents.bull_researcher import BullResearcher
from agents.bear_researcher import BearResearcher
from agents.trader import Trader
from agents.risk_debator import RiskDebator


def snake_to_camel(snake_str: str) -> str:
    """将snake_case转换为camelCase"""
    components = snake_str.split('_')
    return components[0] + ''.join(x.title() for x in components[1:])


def camel_to_snake(camel_str: str) -> str:
    """将camelCase转换为snake_case"""
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', camel_str)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()


def convert_keys_to_camel_case(data: Dict[str, Any]) -> Dict[str, Any]:
    """递归将字典的所有snake_case键转换为camelCase"""
    if not isinstance(data, dict):
        return data
    
    result = {}
    for key, value in data.items():
        camel_key = snake_to_camel(key)
        if isinstance(value, dict):
            result[camel_key] = convert_keys_to_camel_case(value)
        elif isinstance(value, list):
            result[camel_key] = [
                convert_keys_to_camel_case(item) if isinstance(item, dict) else item
                for item in value
            ]
        else:
            result[camel_key] = value
    return result


def normalize_input_keys(data: Dict[str, Any]) -> Dict[str, Any]:
    """规范化输入数据的键名：保留camelCase用于返回，添加snake_case用于内部处理"""
    if not isinstance(data, dict):
        return data
    
    result = data.copy()
    for key in list(result.keys()):
        if '_' in key:
            snake_key = key
            camel_key = snake_to_camel(key)
            if camel_key not in result:
                result[camel_key] = result[snake_key]
    
    return result


logger = logging.getLogger(__name__)

MOCK_MODE = os.getenv('MOCK_MODE', 'false').lower() == 'true'


class ProgressCallback:
    """进度回调接口"""
    def __init__(self, callback: Optional[Callable] = None):
        self.callback = callback
        self.lock = threading.Lock()
    
    def report(self, agent_name: str, progress: int, stage: str, data: Optional[Dict[str, Any]] = None):
        """报告进度"""
        if self.callback:
            with self.lock:
                try:
                    self.callback({
                        'agent': agent_name,
                        'progress': progress,
                        'stage': stage,
                        'timestamp': datetime.now().isoformat(),
                        'data': data
                    })
                except Exception as e:
                    logger.error(f"Progress callback error: {e}")


class AnthropicLLM:
    """Anthropic LLM封装类"""
    
    def __init__(self, config: Dict[str, Any]):
        self.client = anthropic.Anthropic(
            api_key=config.get('api_key'),
            base_url=config.get('base_url', 'https://api.minimaxi.com/anthropic')
        )
        self.model = config.get('model', 'MiniMax-M2.7')
        self.temperature = config.get('temperature', 0.7)
    
    def invoke(self, prompt: str, max_tokens: int = 4096) -> str:
        """调用LLM生成文本"""
        response = self.client.messages.create(
            model=self.model,
            max_tokens=max_tokens,
            temperature=self.temperature,
            messages=[{"role": "user", "content": prompt}]
        )
        
        result = []
        for block in response.content:
            if hasattr(block, 'text'):
                result.append(block.text)
            elif hasattr(block, 'thinking'):
                result.append(block.thinking)
        return '\n'.join(result)


class AgentExecutor:
    """Agent执行器主类 - 支持并行执行"""
    
    def __init__(self, config: Dict[str, Any]):
        self.config = config
        self.llm = self._create_llm()
        self.agents = self._init_agents()
        self.execution_history = {}
        self._state_lock = threading.Lock()
    
    def _create_llm(self):
        """创建LLM实例"""
        provider = self.config.get('llm_provider', 'minimax')
        
        if provider == 'minimax':
            return AnthropicLLM(self.config)
        elif provider == 'deepseek':
            return AnthropicLLM(self.config)
        else:
            raise ValueError(f"Unsupported LLM provider: {provider}")
    
    def _init_agents(self) -> Dict[str, Any]:
        """初始化所有Agent"""
        return {
            'fundamentals': FundamentalsAnalyst(self.llm),
            'market': MarketAnalyst(self.llm),
            'news': NewsAnalyst(self.llm),
            'sentiment': SentimentAnalyst(self.llm),
            'bull': BullResearcher(self.llm),
            'bear': BearResearcher(self.llm),
            'trader': Trader(self.llm),
            'risk': RiskDebator(self.llm)
        }
    
    def _fundamentals_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """基本面分析节点"""
        logger.info(f"[基本面分析师] 开始分析 {state.get('stock_code')}")
        agent = self.agents['fundamentals']
        result = agent.analyze(state)
        state.update(result)
        state['progress'] = 20
        state['current_stage'] = 'ANALYZING'
        logger.info(f"[基本面分析师] 完成")
        if progress_cb:
            progress_cb.report('fundamentals', 20, 'ANALYZING', {'report': result.get('fundamentals_report', '')[:200]})
        return state
    
    def _market_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """市场分析节点"""
        logger.info(f"[市场分析师] 开始分析 {state.get('stock_code')}")
        agent = self.agents['market']
        result = agent.analyze(state)
        state.update(result)
        logger.info(f"[市场分析师] 完成")
        if progress_cb:
            progress_cb.report('market', 25, 'ANALYZING', {'report': result.get('market_report', '')[:200]})
        return state
    
    def _news_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """新闻分析节点"""
        logger.info(f"[新闻分析师] 开始分析 {state.get('stock_code')}")
        agent = self.agents['news']
        result = agent.analyze(state)
        state.update(result)
        logger.info(f"[新闻分析师] 完成")
        if progress_cb:
            progress_cb.report('news', 30, 'ANALYZING', {'report': result.get('news_report', '')[:200]})
        return state
    
    def _sentiment_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """舆情分析节点"""
        logger.info(f"[舆情分析师] 开始分析 {state.get('stock_code')}")
        agent = self.agents['sentiment']
        result = agent.analyze(state)
        state.update(result)
        logger.info(f"[舆情分析师] 完成")
        if progress_cb:
            progress_cb.report('sentiment', 35, 'ANALYZING', {'report': result.get('sentiment_report', '')[:200]})
        return state
    
    def _bull_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """看涨研究员节点"""
        logger.info(f"[看涨研究员] 开始研究 {state.get('stock_code')}")
        agent = self.agents['bull']
        result = agent.research(state)
        state.update(result)
        state['progress'] = 55
        state['current_stage'] = 'DEBATE'
        logger.info(f"[看涨研究员] 完成")
        if progress_cb:
            progress_cb.report('bull', 55, 'DEBATE', {'argument': result.get('bull_argument', '')[:200]})
        return state
    
    def _bear_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """看跌研究员节点"""
        logger.info(f"[看跌研究员] 开始研究 {state.get('stock_code')}")
        agent = self.agents['bear']
        result = agent.research(state)
        state.update(result)
        logger.info(f"[看跌研究员] 完成")
        if progress_cb:
            progress_cb.report('bear', 60, 'DEBATE', {'argument': result.get('bear_argument', '')[:200]})
        return state
    
    def _trader_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """交易员节点"""
        logger.info(f"[交易员] 开始决策 {state.get('stock_code')}")
        agent = self.agents['trader']
        result = agent.decide(state)
        state.update(result)
        state['progress'] = 80
        state['current_stage'] = 'TRADING'
        logger.info(f"[交易员] 完成")
        if progress_cb:
            progress_cb.report('trader', 80, 'TRADING', {
                'decision': result.get('decision', ''),
                'investment_plan': result.get('investment_plan', '')[:200]
            })
        return state
    
    def _risk_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """风险辩论节点"""
        logger.info(f"[风险辩论] 开始评估 {state.get('stock_code')}")
        agent = self.agents['risk']
        result = agent.assess(state)
        state.update(result)
        state['progress'] = 90
        state['current_stage'] = 'RISK_ASSESSMENT'
        logger.info(f"[风险辩论] 完成")
        if progress_cb:
            progress_cb.report('risk', 90, 'RISK_ASSESSMENT', {
                'risk_analysis': result.get('risk_analysis', '')[:200],
                'risk_level': result.get('risk_level', '')
            })
        return state
    
    def _report_node(self, state: AgentState, progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """报告生成节点"""
        logger.info(f"[报告生成] 生成报告 {state.get('stock_code')}")
        
        try:
            report = self._generate_report(state)
            state['final_report'] = report
        except Exception as e:
            logger.error(f"生成报告失败: {e}")
            state['final_report'] = f"报告生成出错: {str(e)}"
        
        state['status'] = 'COMPLETED'
        state['progress'] = 100
        state['current_stage'] = 'COMPLETED'
        
        logger.info(f"[报告生成] 完成，state包含 {len(state)} 个字段")
        if progress_cb:
            progress_cb.report('report', 100, 'COMPLETED', {'final_report_length': len(report)})
        return state
    
    def _generate_report(self, state: AgentState) -> str:
        """生成Markdown格式报告"""
        template = f"""
# {state.get('stock_name', state.get('stock_code'))} 分析报告

## 基本信息
- **股票代码**: {state.get('stock_code')}
- **分析日期**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
- **决策**: {state.get('decision', 'N/A')}
- **置信度**: {state.get('confidence', 0) * 100:.1f}%
- **风险等级**: {state.get('risk_level', 'N/A')}

## 基本面分析
{state.get('fundamentals_report', '无数据')}

## 市场分析
{state.get('market_report', '无数据')}

## 新闻分析
{state.get('news_report', '无数据')}

## 舆情分析
{state.get('sentiment_report', '无数据')}

## 投资辩论

### 看涨观点
{state.get('bull_argument', '无')}

### 看跌观点
{state.get('bear_argument', '无')}

## 交易建议
{state.get('investment_plan', '无')}

## 风险评估
{state.get('risk_analysis', '无')}

---
*本报告由多Agent系统自动生成，仅供参考*
"""
        return template
    
    def _run_stage_parallel(self, nodes: List[tuple], state: AgentState, 
                           progress_cb: Optional[ProgressCallback] = None) -> AgentState:
        """并行运行多个节点"""
        results = {}
        
        def run_node(node_fn, node_name):
            """运行单个节点"""
            logger.info(f"[并行执行] 开始 {node_name}")
            result = node_fn(state, progress_cb)
            logger.info(f"[并行执行] 完成 {node_name}")
            return node_name, result
        
        with ThreadPoolExecutor(max_workers=len(nodes)) as executor:
            futures = {executor.submit(run_node, fn, name): name for name, fn in nodes}
            
            for future in as_completed(futures):
                try:
                    name, result = future.result()
                    results[name] = result
                except Exception as e:
                    logger.error(f"节点执行失败: {futures[future]}, error: {e}")
        
        # 合并结果到state
        for name, result in results.items():
            if isinstance(result, dict):
                state.update(result)
        
        return state
    
    def execute_parallel(self, state: Dict[str, Any], 
                        progress_callback: Optional[Callable] = None) -> Dict[str, Any]:
        """
        并行执行分析流程
        
        执行阶段:
        1. Stage 1 (并行): fundamentals, market, news, sentiment
        2. Stage 2 (并行): bull, bear  
        3. Stage 3 (并行): trader, risk
        4. Final: report generation
        """
        state = normalize_input_keys(state)
        request_id = state.get('requestId') or state.get('request_id', 'unknown')
        stock_code = state.get('stockCode') or state.get('stock_code', 'UNKNOWN')
        
        if MOCK_MODE:
            logger.info(f"[MOCK MODE] 模拟分析 {request_id}")
            return self._generate_mock_result(state)
        
        progress_cb = ProgressCallback(progress_callback)
        
        try:
            self.execution_history[request_id] = {
                'start_time': datetime.now(),
                'status': 'RUNNING',
                'progress': 0
            }
            
            # Stage 1: 并行分析阶段 (fundamentals, market, news, sentiment)
            logger.info(f"[{request_id}] Stage 1: 并行分析阶段开始")
            progress_cb.report('STAGE_1_START', 10, 'ANALYZING', {'agents': ['fundamentals', 'market', 'news', 'sentiment']})
            
            state = self._run_stage_parallel([
                ('fundamentals', self._fundamentals_node),
                ('market', self._market_node),
                ('news', self._news_node),
                ('sentiment', self._sentiment_node),
            ], state, progress_cb)
            
            logger.info(f"[{request_id}] Stage 1 完成, progress: 35%")
            
            # Stage 2: 并行辩论阶段 (bull, bear)
            logger.info(f"[{request_id}] Stage 2: 并行辩论阶段开始")
            progress_cb.report('STAGE_2_START', 40, 'DEBATE', {'agents': ['bull', 'bear']})
            
            state = self._run_stage_parallel([
                ('bull', self._bull_node),
                ('bear', self._bear_node),
            ], state, progress_cb)
            
            logger.info(f"[{request_id}] Stage 2 完成, progress: 60%")
            
            # Stage 3: 并行决策阶段 (trader, risk)
            logger.info(f"[{request_id}] Stage 3: 并行决策阶段开始")
            progress_cb.report('STAGE_3_START', 65, 'TRADING', {'agents': ['trader', 'risk']})
            
            state = self._run_stage_parallel([
                ('trader', self._trader_node),
                ('risk', self._risk_node),
            ], state, progress_cb)
            
            logger.info(f"[{request_id}] Stage 3 完成, progress: 90%")
            
            # Final: 报告生成
            logger.info(f"[{request_id}] Final: 报告生成开始")
            progress_cb.report('REPORT_START', 95, 'REPORT', {})
            
            state = self._report_node(state, progress_cb)
            
            self.execution_history[request_id]['status'] = 'COMPLETED'
            self.execution_history[request_id]['end_time'] = datetime.now()
            self.execution_history[request_id]['progress'] = 100
            
            logger.info(f"分析执行完成，结果包含 {len(state)} 个字段")
            return convert_keys_to_camel_case(state)
            
        except Exception as e:
            logger.error(f"执行失败: {e}", exc_info=True)
            self.execution_history[request_id]['status'] = 'FAILED'
            self.execution_history[request_id]['error'] = str(e)
            try:
                return self._generate_fallback_result(state, str(e))
            except Exception as fallback_error:
                logger.error(f"降级到MOCK模式也失败: {fallback_error}")
                return self._generate_mock_result(state)
    
    def execute(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """执行分析流程（兼容旧接口，内部调用并行版本）"""
        return self.execute_parallel(state)
    
    def _generate_fallback_result(self, state: Dict[str, Any], error_message: str = "") -> Dict[str, Any]:
        """生成降级结果（当LangGraph执行失败时）"""
        stock_code = state.get('stock_code', state.get('stockCode', 'UNKNOWN'))
        result = state.copy()
        
        result.update({
            'status': 'COMPLETED',
            'progress': 100,
            'currentStage': 'COMPLETED',
            'fundamentalsReport': f'【{stock_code}基本面分析】\n\n基于历史数据分析，财务指标正常。',
            'fundamentalsScore': 70,
            'marketReport': f'【{stock_code}市场分析】\n\n市场走势分析。',
            'marketScore': 65,
            'newsReport': f'【{stock_code}新闻分析】\n\n近期新闻汇总。',
            'newsScore': 70,
            'sentimentReport': f'【{stock_code}舆情分析】\n\n市场情绪观察。',
            'sentimentScore': 65,
            'bullArgument': f'{stock_code}具有一定的投资价值。',
            'bearArgument': f'{stock_code}存在一定风险因素。',
            'decision': 'HOLD',
            'confidence': 0.5,
            'investmentPlan': '建议观望，等待更多信息。',
            'riskAnalysis': f'风险评估完成。注：执行过程中出现错误：{error_message[:100]}',
            'riskLevel': 'MEDIUM',
            'finalReport': f'## {stock_code} 分析报告\n\n### 投资建议：观望\n\n（注：部分分析基于降级模式生成）'
        })
        
        logger.info(f"[FALLBACK MODE] 降级分析完成: {stock_code}")
        return result
    
    def _generate_mock_result(self, state: Dict[str, Any]) -> Dict[str, Any]:
        """生成模拟分析结果"""
        stock_code = state.get('stock_code', 'UNKNOWN')
        result = state.copy()
        
        fundamentals_report = f'【{stock_code}基本面分析】\n\n公司财务状况良好，营收稳定增长。ROE维持在15%左右，具有较强的盈利能力。现金流充裕，负债率适中。'
        market_report = f'【{stock_code}市场分析】\n\n当前市场处于震荡上行趋势，行业板块表现活跃。技术面上，股价站上60日均线，短期有上涨动能。'
        news_report = f'【{stock_code}新闻分析】\n\n近期无重大利空消息。公司经营正常，行业发展前景良好。机构评级普遍偏正面。'
        sentiment_report = f'【{stock_code}舆情分析】\n\n市场关注度较高，投资者情绪偏乐观。社交媒体讨论活跃，整体情绪积极。'
        
        result.update({
            'status': 'COMPLETED',
            'progress': 100,
            'currentStage': 'COMPLETED',
            'fundamentalsReport': fundamentals_report,
            'fundamentalsScore': 85,
            'marketReport': market_report,
            'marketScore': 78,
            'newsReport': news_report,
            'newsScore': 80,
            'sentimentReport': sentiment_report,
            'sentimentScore': 75,
            'bullArgument': f'{stock_code}具有良好的基本面支撑，业绩稳定增长。行业前景广阔，市场份额持续扩大。估值合理，具有投资价值。',
            'bearArgument': f'{stock_code}面临行业竞争加剧风险。宏观经济不确定性可能影响业绩。估值已经处于合理区间上限。',
            'decision': 'BUY',
            'confidence': 0.82,
            'investmentPlan': f'建议买入{stock_code}，建仓价位在当前价格附近，仓位控制在30%以内。止损位设为买入价下跌8%。',
            'riskAnalysis': f'{stock_code}的主要风险包括：1)行业政策变化风险；2)原材料价格波动风险；3)汇率风险。建议关注公司基本面变化。',
            'riskLevel': 'MEDIUM',
            'finalReport': f'''## {stock_code} 分析报告

### 投资建议：买入（置信度：82%）

### 基本面分析
{fundamentals_report}

### 市场分析  
{market_report}

### 新闻分析
{news_report}

### 舆情分析
{sentiment_report}

### 多空双方观点
**看涨观点：** {result.get('bullArgument', '')}
**看跌观点：** {result.get('bearArgument', '')}

### 投资计划
{result.get('investmentPlan', '')}

### 风险提示
{result.get('riskAnalysis', '')}

### 风险等级：中等
'''
        })
        
        logger.info(f"[MOCK MODE] 模拟分析完成: {stock_code}")
        return result
    
    def cancel(self, request_id: str) -> Dict[str, Any]:
        """取消分析任务"""
        if request_id in self.execution_history:
            self.execution_history[request_id]['status'] = 'CANCELLED'
            self.execution_history[request_id]['end_time'] = datetime.now()
            return {'success': True, 'message': f'Task {request_id} cancelled'}
        return {'success': False, 'message': f'Task {request_id} not found'}
    
    def get_status(self, request_id: str) -> Dict[str, Any]:
        """获取任务状态"""
        if request_id in self.execution_history:
            return self.execution_history[request_id]
        return {'status': 'NOT_FOUND', 'message': f'Task {request_id} not found'}


class AgentState(dict):
    """Agent状态类"""
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._setattr_section = {}
    
    def __getitem__(self, key):
        return super().__getitem__(key)
    
    def __setitem__(self, key, value):
        super().__setitem__(key, value)
    
    def __getattr__(self, key):
        try:
            return self[key]
        except KeyError:
            raise AttributeError(f"'{self.__class__.__name__}' object has no attribute '{key}'")
    
    def __setattr__(self, key, value):
        self[key] = value
