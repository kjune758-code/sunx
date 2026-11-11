package com.quant.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class ApiErrors {

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<?> engine(RestClientException e) {
        return ResponseEntity.status(502)
                .body(Map.of("message", "Quant engine unavailable or invalid response"));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> conflict(Exception e) {
        return ResponseEntity.status(409)
                .body(Map.of("message", "Signal changed; refresh and retry"));
    }
}
