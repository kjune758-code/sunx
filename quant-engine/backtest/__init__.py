from signals import STRATEGIES

def run(prices, strategy):
    # Signal at close t, fill at next synthetic close t+1. Long only, 3-bar hold.
    cash, shares, entry, trades = 100000., 0, 0, 0
    curve = []
    for i, price in enumerate(prices):
        if shares and i - entry >= 3:
            cash += shares * price * .999; shares = 0
        elif not shares and i >= 21 and STRATEGIES[strategy](prices[:i]):
            shares = int(cash * .1 / price)
            cash -= shares * price * 1.001; entry = i; trades += 1
        curve.append(round(cash + shares * price, 2))
    peak, drawdown = curve[0], 0.
    for value in curve:
        peak = max(peak, value); drawdown = max(drawdown, (peak-value)/peak)
    return {'equity': curve, 'returnPct': round((curve[-1]/100000-1)*100, 4), 'maxDrawdownPct': round(drawdown*100, 4), 'trades': trades, 'dataSource': 'SYNTHETIC', 'feeBps': 10, 'fillModel': 'next synthetic close; 3-bar hold'}
