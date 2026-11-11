def check(price, quantity, equity=100000):
    return price > 0 and quantity > 0 and price * quantity <= equity * .1
