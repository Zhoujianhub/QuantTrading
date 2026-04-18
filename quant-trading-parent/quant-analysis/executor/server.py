"""
Quant Analysis Executor - Flask HTTP Server
提供HTTP接口供Java调用Python Agent执行器
支持SSE实时进度推送
"""
import os
# 确保MOCK_MODE从环境变量读取（必须在导入agent_executor之前设置）
os.environ.setdefault('MOCK_MODE', os.getenv('MOCK_MODE', 'false'))

from flask import Flask, request, jsonify, Response
from flask_cors import CORS
from agent_executor import AgentExecutor, convert_keys_to_camel_case
import logging
import json
import threading
import queue

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建Flask应用
app = Flask(__name__)
CORS(app)

# 初始化执行器
executor = None

# 存储正在执行的任务及其进度队列
task_progress_queues = {}
task_progress_lock = threading.Lock()


def init_executor():
    """初始化执行器"""
    global executor
    try:
        config = {
            'llm_provider': os.getenv('LLM_PROVIDER', 'minimax'),
            'api_key': os.getenv('MINIMAX_API_KEY', 'sk-cp-B0xJA2MwSZq67EwYZYXBJCFBvDw5txcE-rKeYiy0j6ZsQAQbHSl2cw5CX5mBdEPrI4NHLHXTfsNIrgQ-T4mC56q939-Dk9WQLF5TSwbXwWQhduEbKn8EJus'),
            'base_url': os.getenv('LLM_BASE_URL', 'https://api.minimaxi.com/anthropic'),
            'model': os.getenv('LLM_MODEL', 'MiniMax-M2.7')
        }
        executor = AgentExecutor(config)
        logger.info(f"Agent执行器初始化成功: provider={config['llm_provider']}, model={config['model']}")
    except Exception as e:
        logger.error(f"Agent执行器初始化失败: {e}")
        raise


def progress_callback_factory(request_id: str):
    """创建进度回调工厂函数"""
    def progress_callback(progress_data):
        """进度回调函数"""
        with task_progress_lock:
            if request_id in task_progress_queues:
                try:
                    task_progress_queues[request_id].put_nowait(progress_data)
                except queue.Full:
                    logger.warning(f"Progress queue full for {request_id}")
    
    return progress_callback


@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    if executor is None:
        return jsonify({'status': 'error', 'message': 'Executor not initialized'}), 500
    return jsonify({'status': 'ok', 'message': 'Executor is running'})


@app.route('/api/execute', methods=['POST'])
def execute_analysis():
    """
    执行股票分析（同步版本，兼容旧客户端）
    """
    try:
        if executor is None:
            return jsonify({'error': 'Executor not initialized'}), 500
        
        state = request.get_json()
        if not state:
            return jsonify({'error': 'Invalid request body'}), 400
        
        request_id = state.get('requestId') or state.get('request_id', 'unknown')
        logger.info(f"收到分析请求: {request_id}")
        
        # 执行分析
        try:
            result = executor.execute(state)
        except Exception as e:
            logger.error(f"执行器异常: {e}", exc_info=True)
            return jsonify({'error': f'Executor error: {str(e)}'}), 500
        
        if result is None:
            logger.error("分析执行返回None")
            return jsonify({'error': 'Analysis execution returned no result'}), 500
        
        logger.info(f"分析完成: {request_id}, keys: {list(result.keys())}")
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"分析执行失败: {e}", exc_info=True)
        return jsonify({'error': str(e)}), 500


@app.route('/api/execute/stream', methods=['POST'])
def execute_analysis_stream():
    """
    执行股票分析（SSE流式版本，实时推送进度到前端）
    
    前端可以通过EventSource接收进度更新:
    - 每个agent完成时推送进度
    - 最终返回完整结果
    """
    try:
        if executor is None:
            return jsonify({'error': 'Executor not initialized'}), 500
        
        state = request.get_json()
        if not state:
            return jsonify({'error': 'Invalid request body'}), 400
        
        request_id = state.get('requestId') or state.get('request_id', 'unknown')
        logger.info(f"[SSE] 收到分析请求: {request_id}")
        
        # 创建进度队列
        progress_queue = queue.Queue(maxsize=100)
        with task_progress_lock:
            task_progress_queues[request_id] = progress_queue
        
        def generate():
            """SSE生成器"""
            try:
                # 首先发送连接成功消息
                yield f"data: {json.dumps({'type': 'connected', 'requestId': request_id}, ensure_ascii=False)}\n\n"
                
                # 创建进度回调
                def progress_callback(progress_data):
                    try:
                        progress_data['requestId'] = request_id
                        progress_data['type'] = 'progress'
                        yield_data = f"data: {json.dumps(progress_data, ensure_ascii=False)}\n\n"
                        # 由于在子线程中，需要通过队列传递
                        with task_progress_lock:
                            if request_id in task_progress_queues:
                                try:
                                    task_progress_queues[request_id].put_nowait(yield_data)
                                except queue.Full:
                                    pass
                    except Exception as e:
                        logger.error(f"Progress callback error: {e}")
                
                # 在后台线程中执行分析
                result_holder = [None]
                error_holder = [None]
                
                def run_analysis():
                    try:
                        result_holder[0] = executor.execute_parallel(state, progress_callback)
                    except Exception as e:
                        logger.error(f"Analysis error: {e}")
                        error_holder[0] = str(e)
                
                analysis_thread = threading.Thread(target=run_analysis)
                analysis_thread.start()
                
                # 持续发送进度更新直到分析完成
                while analysis_thread.is_alive() or not progress_queue.empty():
                    try:
                        item = progress_queue.get(timeout=1.0)
                        yield item
                    except queue.Empty:
                        continue
                
                analysis_thread.join()
                
                # 发送最终结果
                if error_holder[0]:
                    yield f"data: {json.dumps({'type': 'error', 'requestId': request_id, 'error': error_holder[0]}, ensure_ascii=False)}\n\n"
                else:
                    result = result_holder[0]
                    if result:
                        # 发送完成消息
                        yield f"data: {json.dumps({'type': 'completed', 'requestId': request_id, 'progress': 100}, ensure_ascii=False)}\n\n"
                        # 发送最终结果
                        yield f"data: {json.dumps({'type': 'result', 'requestId': request_id, 'data': convert_keys_to_camel_case(result)}, ensure_ascii=False)}\n\n"
                
            finally:
                # 清理进度队列
                with task_progress_lock:
                    if request_id in task_progress_queues:
                        del task_progress_queues[request_id]
                logger.info(f"[SSE] 请求 {request_id} 完成")
        
        return Response(
            generate(),
            mimetype='text/event-stream',
            headers={
                'Cache-Control': 'no-cache',
                'Connection': 'keep-alive',
                'X-Accel-Buffering': 'no'  # 禁用nginx缓冲
            }
        )
        
    except Exception as e:
        logger.error(f"SSE分析执行失败: {e}", exc_info=True)
        return jsonify({'error': str(e)}), 500


@app.route('/api/execute/poll', methods=['POST'])
def execute_analysis_poll():
    """
    执行股票分析（异步轮询版本）
    
    工作流程:
    1. POST /api/execute/poll - 提交任务，返回request_id
    2. GET /api/progress/{request_id} - 轮询进度
    3. GET /api/result/{request_id} - 获取最终结果
    """
    try:
        if executor is None:
            return jsonify({'error': 'Executor not initialized'}), 500
        
        state = request.get_json()
        if not state:
            return jsonify({'error': 'Invalid request body'}), 400
        
        request_id = state.get('requestId') or state.get('request_id', 'unknown')
        logger.info(f"[POLL] 收到分析请求: {request_id}")
        
        # 创建进度队列和结果存储
        progress_queue = queue.Queue(maxsize=100)
        result_holder = [None]
        error_holder = [None]
        status_holder = [{'status': 'PENDING', 'progress': 0}]
        
        with task_progress_lock:
            task_progress_queues[request_id] = progress_queue
        
        def progress_callback(progress_data):
            """进度回调 - 更新状态"""
            try:
                status_holder[0].update({
                    'status': 'RUNNING',
                    'progress': progress_data.get('progress', 0),
                    'stage': progress_data.get('stage', ''),
                    'agent': progress_data.get('agent', ''),
                    'lastUpdate': progress_data.get('timestamp', '')
                })
                # 同时放入队列供后续GET使用
                with task_progress_lock:
                    if request_id in task_progress_queues:
                        try:
                            task_progress_queues[request_id].put_nowait(progress_data)
                        except queue.Full:
                            pass
            except Exception as e:
                logger.error(f"Progress callback error: {e}")
        
        def run_analysis():
            try:
                result_holder[0] = executor.execute_parallel(state, progress_callback)
                status_holder[0]['status'] = 'COMPLETED'
                status_holder[0]['progress'] = 100
            except Exception as e:
                logger.error(f"Analysis error: {e}")
                error_holder[0] = str(e)
                status_holder[0]['status'] = 'FAILED'
                status_holder[0]['error'] = str(e)
        
        # 启动后台分析线程
        analysis_thread = threading.Thread(target=run_analysis)
        analysis_thread.start()
        
        # 立即返回request_id供后续轮询
        return jsonify({
            'requestId': request_id,
            'status': 'PENDING',
            'message': '任务已提交，请使用GET /api/progress/' + request_id + ' 轮询进度'
        })
        
    except Exception as e:
        logger.error(f"POLL分析提交失败: {e}", exc_info=True)
        return jsonify({'error': str(e)}), 500


@app.route('/api/progress/<request_id>', methods=['GET'])
def get_progress(request_id):
    """获取任务进度（供轮询使用）"""
    with task_progress_lock:
        if request_id in task_progress_queues:
            # 返回队列中的最新进度
            latest_progress = {}
            while not task_progress_queues[request_id].empty():
                try:
                    latest_progress = task_progress_queues[request_id].get_nowait()
                except queue.Empty:
                    break
            return jsonify(latest_progress)
        
        # 检查是否有已完成的结果
        # 这里简化处理，实际应该从结果存储中获取
        return jsonify({'status': 'NOT_FOUND', 'message': f'任务 {request_id} 不存在'})


@app.route('/api/cancel/<request_id>', methods=['POST'])
def cancel_analysis(request_id):
    """取消分析任务"""
    try:
        if executor is None:
            return jsonify({'error': 'Executor not initialized'}), 500
        
        result = executor.cancel(request_id)
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"取消分析失败: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/status/<request_id>', methods=['GET'])
def get_status(request_id):
    """获取分析状态"""
    try:
        if executor is None:
            return jsonify({'error': 'Executor not initialized'}), 500
        
        status = executor.get_status(request_id)
        return jsonify(status)
        
    except Exception as e:
        logger.error(f"获取状态失败: {e}")
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    init_executor()
    port = int(os.getenv('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=False)
