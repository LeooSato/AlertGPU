package com.sato.alertsgpu.api.exception;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(String message) {
        super(message);
    }
}
