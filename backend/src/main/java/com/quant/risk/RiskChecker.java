package com.quant.risk;

import org.springframework.stereotype.Component;

@Component
public class RiskChecker {

    public String check(String side, double price, int quantity) {
        if (!"BUY".equals(side)) {
            return "No actionable BUY signal";
        }
        if (!Double.isFinite(price) || price <= 0 || quantity <= 0) {
            return "Invalid price or quantity";
        }
        if (price * quantity > 10000) {
            return "Exceeds 10% of simulated 100000 quote-currency equity";
        }
        return "PASS";
    }
}
