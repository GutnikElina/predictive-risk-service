package com.innowise.predictiveriskservice.exception;

public class IllegalRiskScore extends RuntimeException {
    public IllegalRiskScore(String message) {
        super(message);
    }
}
