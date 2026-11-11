package com.quant.controller;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.quant.entity.Signal;
import com.quant.repository.SignalRepository;
import com.quant.service.TradingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TradingController {

    private final TradingService service;
    private final SignalRepository repository;

    public TradingController(TradingService service, SignalRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "mode", "SIMULATION", "realOrdersEnabled", false);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
                "mode", "SIMULATION",
                "signals", repository.count(),
                "positions", positions().size(),
                "note", "Separate USD/HKD demo accounts; no FX conversion or real holdings");
    }

    @GetMapping("/stocks")
    public List<?> stocks() {
        return List.of(
                Map.of("symbol", "AAPL", "market", "US", "currency", "USD"),
                Map.of("symbol", "MSFT", "market", "US", "currency", "USD"),
                Map.of("symbol", "00700.HK", "market", "HK", "currency", "HKD"));
    }

    @GetMapping("/strategies")
    public List<?> strategies() {
        return List.of(
                Map.of("id", "breakout", "name", "20-bar breakout"),
                Map.of("id", "mean_reversion", "name", "20-bar mean reversion (-2%)"));
    }

    @GetMapping("/positions")
    public List<?> positions() {
        var quantities = new TreeMap<String, Long>();
        for (var fill : repository.findByStatus("FILLED")) {
            quantities.merge(fill.symbol, (long) fill.quantity, Long::sum);
        }
        return quantities.entrySet().stream()
                .map(entry -> Map.of(
                        "symbol", entry.getKey(),
                        "currency", entry.getKey().endsWith(".HK") ? "HKD" : "USD",
                        "quantity", entry.getValue()))
                .toList();
    }

    @GetMapping("/signals")
    public List<Signal> signals() {
        return repository.findAll();
    }

    public record Request(String symbol, String strategy, int quantity) {
    }

    @PostMapping("/signals")
    public Signal generate(@RequestBody Request request) {
        return service.generate(request.symbol(), request.strategy(), request.quantity());
    }

    @PostMapping("/signals/{id}/confirm")
    public Signal confirm(@PathVariable long id) {
        return service.confirm(id);
    }

    @PostMapping("/signals/{id}/fill")
    public Signal fill(@PathVariable long id) {
        return service.fill(id);
    }

    @PostMapping("/signals/{id}/skip")
    public Signal skip(@PathVariable long id) {
        return service.skip(id);
    }

    @GetMapping("/backtest")
    public Map<?, ?> backtest(
            @RequestParam(defaultValue = "AAPL") String symbol,
            @RequestParam(defaultValue = "breakout") String strategy) {
        return service.backtest(symbol, strategy);
    }
}
