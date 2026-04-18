"""
Production WSGI server using waitress for parallel execution
"""
import os
os.environ.setdefault('MOCK_MODE', os.getenv('MOCK_MODE', 'false'))

from flask import Flask, request, jsonify
from flask_cors import CORS
from agent_executor import AgentExecutor
import logging
import time

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Create Flask app
app = Flask(__name__)
CORS(app)

# Initialize executor
config = {
    'llm_provider': os.getenv('LLM_PROVIDER', 'minimax'),
    'api_key': os.getenv('MINIMAX_API_KEY', 'sk-cp-B0xJA2MwSZq67EwYZYXBJCFBvDw5txcE-rKeYiy0j6ZsQAQbHSl2cw5CX5mBdEPrI4NHLHXTfsNIrgQ-T4mC56q939-Dk9WQLF5TSwbXwWQhduEbKn8EJus'),
    'base_url': os.getenv('LLM_BASE_URL', 'https://api.minimaxi.com/anthropic'),
    'model': os.getenv('LLM_MODEL', 'MiniMax-M2.7')
}
executor = AgentExecutor(config)
logger.info('AgentExecutor initialized: provider=%s, model=%s', config['llm_provider'], config['model'])


def progress_handler(progress_data):
    """Handle progress updates from parallel execution"""
    print('[PROGRESS] Agent: {agent}, Progress: {progress}%, Stage: {stage}'.format(
        agent=progress_data.get('agent', ''),
        progress=progress_data.get('progress', 0),
        stage=progress_data.get('stage', '')
    ))


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({'status': 'ok', 'message': 'Executor is running'})


@app.route('/api/execute', methods=['POST'])
def execute_analysis():
    """Execute stock analysis with parallel agent execution"""
    try:
        state = request.get_json()
        if not state:
            return jsonify({'error': 'Invalid request body'}), 400
        
        request_id = state.get('requestId') or state.get('request_id', 'unknown')
        stock_code = state.get('stockCode') or state.get('stockCode', 'UNKNOWN')
        
        logger.info('='*60)
        logger.info('RECEIVED REQUEST: %s', request_id)
        logger.info('Stock: %s', stock_code)
        logger.info('='*60)
        
        start_time = time.time()
        
        # Execute with parallel agents and progress reporting
        result = executor.execute_parallel(state, progress_handler)
        
        elapsed = time.time() - start_time
        
        logger.info('='*60)
        logger.info('COMPLETED in %.1f seconds', elapsed)
        logger.info('Decision: %s', result.get('decision'))
        logger.info('Confidence: %s', result.get('confidence'))
        logger.info('='*60)
        
        return jsonify(result)
        
    except Exception as e:
        logger.error('Analysis failed: %s', str(e), exc_info=True)
        return jsonify({'error': str(e)}), 500


@app.route('/api/status/<request_id>', methods=['GET'])
def get_status(request_id):
    """Get execution status"""
    status = executor.get_status(request_id)
    return jsonify(status)


@app.route('/api/cancel/<request_id>', methods=['POST'])
def cancel_analysis(request_id):
    """Cancel running analysis"""
    result = executor.cancel(request_id)
    return jsonify(result)


if __name__ == '__main__':
    from waitress import serve
    logger.info('Starting production server on port 5000...')
    serve(app, host='0.0.0.0', port=5000, threads=4)
