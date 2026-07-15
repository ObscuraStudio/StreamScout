package org.obscura.backend.exception;

public class StreamNotFoundException extends RuntimeException {

    public StreamNotFoundException(String message) {
        super(message);
    }
}
