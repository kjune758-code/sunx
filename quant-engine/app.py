import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs
from data import prices
from signals import evaluate, STRATEGIES
from backtest import run

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        path = urlparse(self.path)
        try:
            q = parse_qs(path.query)
            symbol = q.get('symbol', ['AAPL'])[0]; strategy = q.get('strategy', ['breakout'])[0]
            if path.path == '/health': result = {'status': 'UP', 'mode': 'SIMULATION'}
            elif path.path in ('/signal', '/backtest'):
                if strategy not in STRATEGIES: raise ValueError('Unknown strategy')
                history = prices(symbol)
                result = run(history, strategy) if path.path == '/backtest' else {'symbol': symbol, 'strategy': strategy, 'side': evaluate(history, strategy), 'price': history[-1], 'dataSource': 'SYNTHETIC'}
            else: self.respond(404, {'message': 'Not found'}); return
            self.respond(200, result)
        except ValueError as e: self.respond(400, {'message': str(e)})
    def respond(self, status, result):
        body = json.dumps(result, allow_nan=False).encode()
        self.send_response(status); self.send_header('Content-Type','application/json'); self.send_header('Content-Length', str(len(body))); self.end_headers(); self.wfile.write(body)
if __name__ == '__main__': ThreadingHTTPServer(('0.0.0.0', 8000), Handler).serve_forever()
