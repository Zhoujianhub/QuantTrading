import os
os.environ['MOCK_MODE'] = 'false'

import sys
sys.path.insert(0, 'E:/Trae/QuantTradingProject/QuantTrading/quant-trading-parent/quant-analysis/executor')

from flask import Flask, request, jsonify
from flask_cors import CORS
from agent_executor import AgentExecutor
import logging
import time

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

config = {
    'llm_provider': 'minimax',
    'api_key': 'sk-cp-B0xJA2MwSZq67EwYZYXBJCFBvDw5txcE-rKeYiy0j6ZsQAQbHSl2cw5CX5mBdEPrI4NHLHXTfsNIrgQ-T4mC56q939-Dk9WQLF5TSwbXwWQhduEbKn8EJus',
    'base_url': 'https://api.minimaxi.com/anthropic',
    'model': 'MiniMax-M2.7'
}
executor = AgentExecutor(config)
print('Executor created successfully')

def progress_handler(progress_data):
    print('[PROGRESS] Agent: {}, Progress: {}%, Stage: {}'.format(
        progress_data.get('agent'),
        progress_data.get('progress'),
        progress_data.get('stage')
    ))

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'message': 'Executor is running'})

@app.route('/api/execute', methods=['POST'])
def execute():
    try:
        state = request.get_json()
        if not state:
            return jsonify({'error': 'Invalid request body'}), 400
        
        request_id = state.get('requestId') or state.get('request_id', 'unknown')
        print('='*60)
        print('RECEIVED REQUEST: {}'.format(request_id))
        print('Stock: {} ({})'.format(state.get('stockName'), state.get('stockCode')))
        print('='*60)
        
        start = time.time()
        result = executor.execute_parallel(state, progress_handler)
        elapsed = time.time() - start
        
        print('='*60)
        print('COMPLETED in {:.1f}s'.format(elapsed))
        print('Decision: {}'.format(result.get('decision')))
        print('Confidence: {}%'.format(result.get('confidence', 0)*100))
        print('='*60)
        
        return jsonify(result)
        
    except Exception as e:
        logger.error('Error: %s', str(e), exc_info=True)
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    print('Starting server on port 5000...')
    # Use threaded=True to handle concurrent requests
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)
