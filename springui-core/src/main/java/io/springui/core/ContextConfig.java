package io.springui.core;

/**
 * ContextConfig — configuration for SpringUIContext.
 */
public class ContextConfig {

    private final String basePackage;
    private final boolean devMode;
    private final boolean failOnError;

    private ContextConfig(Builder builder) {
        this.basePackage = builder.basePackage;
        this.devMode = builder.devMode;
        this.failOnError = builder.failOnError;
    }

    public String getBasePackage() { return basePackage; }
    public boolean isDevMode() { return devMode; }
    public boolean isFailOnError() { return failOnError; }

    public static class Builder {
        private String basePackage = "io.springui";
        private boolean devMode = false;
        private boolean failOnError = true;

        public Builder basePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }

        public Builder devMode(boolean devMode) {
            this.devMode = devMode;
            return this;
        }

        public Builder failOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        public ContextConfig build() {
            return new ContextConfig(this);
        }
    }
}