def signal(history):
    return len(history) >= 20 and history[-1] < sum(history[-20:]) / 20 * .98
