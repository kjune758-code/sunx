from strategies import breakout, mean_reversion
STRATEGIES = {'breakout': breakout.signal, 'mean_reversion': mean_reversion.signal}
def evaluate(history, strategy):
    return 'BUY' if STRATEGIES[strategy](history) else 'HOLD'
