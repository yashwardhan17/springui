package io.springui.compiler;

/**
 * CompilerConfig — configuration for the SpringUI compiler.
 */
public class CompilerConfig {

    private final String outputDir;
    private final CompilerTarget target;
    private final boolean minify;
    private final boolean sourceMaps;

    private CompilerConfig(Builder builder) {
        this.outputDir = builder.outputDir;
        this.target = builder.target;
        this.minify = builder.minify;
        this.sourceMaps = builder.sourceMaps;
    }

    // ===========================
    // Defaults
    // ===========================

    public static CompilerConfig defaults() {
        return new Builder().build();
    }

    // ===========================
    // Getters
    // ===========================

    public String getOutputDir() { return outputDir; }
    public CompilerTarget getTarget() { return target; }
    public boolean isMinify() { return minify; }
    public boolean isSourceMaps() { return sourceMaps; }

    // ===========================
    // Builder
    // ===========================

    public static class Builder {
        private String outputDir = "./springui-out";
        private CompilerTarget target = CompilerTarget.WASM;
        private boolean minify = false;
        private boolean sourceMaps = true;

        public Builder outputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder target(CompilerTarget target) {
            this.target = target;
            return this;
        }

        public Builder minify(boolean minify) {
            this.minify = minify;
            return this;
        }

        public Builder sourceMaps(boolean sourceMaps) {
            this.sourceMaps = sourceMaps;
            return this;
        }

        public CompilerConfig build() {
            return new CompilerConfig(this);
        }
    }

    @Override
    public String toString() {
        return "CompilerConfig{outputDir='" + outputDir + "', target=" +
                target + ", minify=" + minify + ", sourceMaps=" + sourceMaps + "}";
    }
}