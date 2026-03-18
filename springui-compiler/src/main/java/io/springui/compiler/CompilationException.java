package io.springui.compiler;

/**
 * CompilationException — thrown when SpringUI compilation fails.
 */
public class CompilationException extends RuntimeException {

    public CompilationException(String message) {
        super(message);
    }

    public CompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}