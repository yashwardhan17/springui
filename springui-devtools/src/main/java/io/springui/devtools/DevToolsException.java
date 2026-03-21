package io.springui.devtools;

/**
 * DevToolsException — thrown when SpringUI DevTools encounters an error.
 */
public class DevToolsException extends RuntimeException {

    public DevToolsException(String message) {
        super(message);
    }

    public DevToolsException(String message, Throwable cause) {
        super(message, cause);
    }
}