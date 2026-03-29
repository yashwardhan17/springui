package io.springui.core;

/**
 * SpringUIContextException — thrown when SpringUIContext fails to start.
 */
public class SpringUIContextException extends RuntimeException {

    public SpringUIContextException(String message) {
        super(message);
    }

    public SpringUIContextException(String message, Throwable cause) {
        super(message, cause);
    }
}