package org.obscura.backend.exception;

/**
 * Thrown when a call to the Twitch Helix API fails (network error, non-2xx
 * response, unparseable body). Translated into an HTTP 502 Bad Gateway by
 * the exception handler in this package — it is an upstream failure, not a
 * bug in this service.
 */
public class TwitchApiException extends RuntimeException {

    public TwitchApiException(String message) {
        super(message);
    }
}
