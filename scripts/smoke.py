"""Run against already started backend + engine; stdlib only."""
import json, urllib.request, urllib.error, os
urllib.request.install_opener(urllib.request.build_opener(urllib.request.ProxyHandler({})))
BASE = os.environ.get('API_URL', 'http://127.0.0.1:8080/api')
def request(path, payload=None, expected=200):
    data = None if payload is None else json.dumps(payload).encode()
    req = urllib.request.Request(BASE+path, data=data, headers={'Content-Type':'application/json'})
    try:
        with urllib.request.urlopen(req, timeout=20) as res:
            assert res.status == expected
            return json.load(res)
    except urllib.error.HTTPError as e:
        assert e.code == expected, (e.code, e.read().decode())
        return None
assert request('/health')['realOrdersEnabled'] is False
for path in ['/dashboard','/stocks','/strategies','/positions','/signals']: request(path)
assert len(request('/backtest')['equity']) == 120
request('/backtest?symbol=INVALID', expected=400)
request('/signals', {'symbol':'AAPL','strategy':'breakout','quantity':0}, 400)
s = request('/signals', {'symbol':'AAPL','strategy':'breakout','quantity':10})
assert s['status']=='PENDING', s
assert request('/signals/'+str(s['id'])+'/confirm', {})['status']=='CONFIRMED'
request('/signals/'+str(s['id'])+'/confirm', {}, 409)
s = request('/signals', {'symbol':'AAPL','strategy':'breakout','quantity':1000})
assert s['status']=='BLOCKED'
request('/signals/'+str(s['id'])+'/confirm', {}, 409)
request('/signals/99999999/confirm', {}, 404)
assert request('/positions')==[]
print('PASS: API routes, engine integration, backtest, validation, risk block, manual confirmation, duplicate confirmation, no orders/positions')
