package io.springui.core.annotation;

public class AnnotationProcessingException extends RuntimeException {
    public AnnotationProcessingException(String message) {
        super(message);
    }

    public AnnotationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}