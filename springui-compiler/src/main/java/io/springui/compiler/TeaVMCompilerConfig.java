package io.springui.compiler;

/**
 * TeaVMCompilerConfig — configuration for the TeaVM compiler.
 */
public class TeaVMCompilerConfig {

    public enum Target {
        WEBASSEMBLY,
        JAVASCRIPT,
        WEBASSEMBLY_WASI
    }

    private final String outputDir;
    private final String outputFileName;
    private final Target target;
    private final boolean minify;
    private final boolean sourceMaps;
    private final boolean incremental;

    private TeaVMCompilerConfig(Builder builder) {
        this.outputDir = builder.outputDir;
        this.outputFileName = builder.outputFileName;
        this.target = builder.target;
        this.minify = builder.minify;
        this.sourceMaps = builder.sourceMaps;
        this.incremental = builder.incremental;
    }

    public static TeaVMCompilerConfig defaults() {
        return new Builder().build();
    }

    public String getOutputDir() { return outputDir; }
    public String getOutputFileName() { return outputFileName; }
    public Target getTarget() { return target; }
    public boolean isMinify() { return minify; }
    public boolean isSourceMaps() { return sourceMaps; }
    public boolean isIncremental() { return incremental; }

    public static class Builder {
        private String outputDir = "./springui-out";
        private String outputFileName = "springui.js";
        private Target target = Target.JAVASCRIPT;
        private boolean minify = false;
        private boolean sourceMaps = true;
        private boolean incremental = true;

        public Builder outputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder outputFileName(String name) {
            this.outputFileName = name;
            return this;
        }

        public Builder target(Target target) {
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

        public Builder incremental(boolean incremental) {
            this.incremental = incremental;
            return this;
        }

        public TeaVMCompilerConfig build() {
            return new TeaVMCompilerConfig(this);
        }
    }
}