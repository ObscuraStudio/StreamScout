package org.obscura.backend.exception;

public class SteamApiException extends RuntimeException {

    public SteamApiException(String message) {
        super(message);
    }
}
