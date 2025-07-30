package com.thr.tuchat.exception;

public class ServiceDeniedException extends RuntimeException {
    public ServiceDeniedException(String message) {
        super(message);
    }
}