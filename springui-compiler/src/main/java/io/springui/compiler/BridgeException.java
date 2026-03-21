package io.springui.compiler;

/**
 * BridgeException — thrown when the WasmBridge encounters an error.
 */
public class BridgeException extends RuntimeException {

    public BridgeException(String message) {
        super(message);
    }

    public BridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}