"""
Prompts Package
包含各Agent的Prompt模板
"""
from .fundamentals_prompt import FUNDAMENTALS_ANALYSIS_PROMPT
from .market_prompt import MARKET_ANALYSIS_PROMPT
from .news_prompt import NEWS_ANALYSIS_PROMPT
from .debate_prompt import BULL_ARGUMENT_PROMPT, BEAR_ARGUMENT_PROMPT
from .trading_prompt import TRADING_DECISION_PROMPT
from .risk_prompt import RISK_ASSESSMENT_PROMPT

__all__ = [
    'FUNDAMENTALS_ANALYSIS_PROMPT',
    'MARKET_ANALYSIS_PROMPT',
    'NEWS_ANALYSIS_PROMPT',
    'BULL_ARGUMENT_PROMPT',
    'BEAR_ARGUMENT_PROMPT',
    'TRADING_DECISION_PROMPT',
    'RISK_ASSESSMENT_PROMPT'
]