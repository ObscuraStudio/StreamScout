package org.obscura.backend.exception;

/**
 * Thrown when OMDb reports no matching movie (its response carries
 * {@code "Response":"False"}, e.g. "Movie not found!").
 *
 * <p>Translated into an HTTP 404 by the exception handler in this package.
 */
public class StreamNotFoundException extends RuntimeException {

    public StreamNotFoundException(String message) {
        super(message);
    }
}
