package com.quant.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.quant.entity.Signal;
import com.quant.repository.SignalRepository;
import com.quant.risk.RiskChecker;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TradingService {

    private final SignalRepository repository;
    private final RestClient engine;
    private final RiskChecker risk;

    public TradingService(SignalRepository repository, RestClient engine, RiskChecker risk) {
        this.repository = repository;
        this.engine = engine;
        this.risk = risk;
    }

    public void validate(String symbol, String strategy) {
        if (symbol == null || strategy == null
                || !List.of("AAPL", "MSFT", "00700.HK").contains(symbol)
                || !List.of("breakout", "mean_reversion").contains(strategy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown symbol or strategy");
        }
    }

    public Map<?, ?> backtest(String symbol, String strategy) {
        validate(symbol, strategy);
        return engine.get()
                .uri("/backtest?symbol={s}&strategy={t}", symbol, strategy)
                .retrieve()
                .body(Map.class);
    }

    public Signal generate(String symbol, String strategy, int quantity) {
        validate(symbol, strategy);
        if (quantity < 1 || quantity > 100000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be 1..100000");
        }

        var data = engine.get()
                .uri("/signal?symbol={s}&strategy={t}", symbol, strategy)
                .retrieve()
                .body(Map.class);

        var signal = new Signal();
        signal.symbol = symbol;
        signal.strategy = strategy;
        signal.side = (String) data.get("side");
        signal.price = ((Number) data.get("price")).doubleValue();
        signal.quantity = quantity;
        signal.createdAt = Instant.now();
        signal.riskMessage = risk.check(signal.side, signal.price, quantity);
        signal.status = "PASS".equals(signal.riskMessage) ? "PENDING" : "BLOCKED";
        signal.version = 0L;
        repository.insert(signal);
        return signal;
    }

    @Transactional
    public Signal confirm(long id) {
        return resolve(id, "CONFIRMED");
    }

    @Transactional
    public Signal fill(long id) {
        return resolve(id, "FILLED");
    }

    @Transactional
    public Signal skip(long id) {
        return resolve(id, "SKIPPED");
    }

    private Signal resolve(long id, String status) {
        var signal = repository.findById(id);
        if (signal == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Signal not found");
        }

        if (!"PENDING".equals(signal.status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending signals can be processed");
        }
        if (!"SKIPPED".equals(status)) {
            if (signal.createdAt.isBefore(Instant.now().minusSeconds(86400))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Signal expired");
            }
            var result = risk.check(signal.side, signal.price, signal.quantity);
            if (!"PASS".equals(result)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, result);
            }
        }

        long version = signal.version == null ? 0 : signal.version;
        if (repository.updatePendingStatus(id, status, version) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Signal was already processed");
        }
        signal.status = status;
        signal.version = version + 1;
        return signal;
    }
}
