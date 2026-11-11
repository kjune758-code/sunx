package com.quant.entity;

import java.time.Instant;

public class Signal {

    public Long id;

    public String symbol;
    public String strategy;
    public String side;
    public String status;
    public String riskMessage;
    public double price;
    public int quantity;
    public Instant createdAt;

    public Long version;

    public Signal() {
    }
}
