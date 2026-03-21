package io.springui.devtools;

/**
 * HotReloadConfig — configuration for the HotReloadServer.
 */
public class HotReloadConfig {

    private final String watchDir;
    private final String outputDir;
    private final int port;
    private final long pollIntervalMs;

    private HotReloadConfig(Builder builder) {
        this.watchDir = builder.watchDir;
        this.outputDir = builder.outputDir;
        this.port = builder.port;
        this.pollIntervalMs = builder.pollIntervalMs;
    }

    public static HotReloadConfig defaults() {
        return new Builder().build();
    }

    public String getWatchDir() { return watchDir; }
    public String getOutputDir() { return outputDir; }
    public int getPort() { return port; }
    public long getPollIntervalMs() { return pollIntervalMs; }

    public static class Builder {
        private String watchDir = "./src/main/java";
        private String outputDir = "./springui-out";
        private int port = 8081;
        private long pollIntervalMs = 500;

        public Builder watchDir(String watchDir) {
            this.watchDir = watchDir;
            return this;
        }

        public Builder outputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder pollIntervalMs(long ms) {
            this.pollIntervalMs = ms;
            return this;
        }

        public HotReloadConfig build() {
            return new HotReloadConfig(this);
        }
    }
}