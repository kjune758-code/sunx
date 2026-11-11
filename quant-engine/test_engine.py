import unittest
from data import prices
from strategies.breakout import signal
from backtest import run
from risk import check
class EngineTest(unittest.TestCase):
    def test_no_lookahead(self):
        self.assertFalse(signal([1]*21)); self.assertTrue(signal([1]*20+[2]))
        self.assertEqual(run([1]*30, 'breakout')['trades'], 0)
    def test_limits(self):
        self.assertFalse(check(100,101)); self.assertTrue(check(100,100))
    def test_deterministic(self):
        self.assertEqual(prices('AAPL'), prices('AAPL'))
        self.assertEqual(len(run(prices('AAPL'),'breakout')['equity']),120)
if __name__ == '__main__': unittest.main()
