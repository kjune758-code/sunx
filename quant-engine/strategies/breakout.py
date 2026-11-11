def signal(history):
    return len(history) >= 21 and history[-1] > max(history[-21:-1])
