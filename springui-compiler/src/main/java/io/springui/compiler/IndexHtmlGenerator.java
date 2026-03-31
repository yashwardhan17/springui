package io.springui.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * IndexHtmlGenerator — generates the browser entry point for SpringUI apps.
 *
 * <p>This is the equivalent of Create React App's {@code index.html}.
 * Called by {@link SpringUICompiler} after WASM compilation to produce
 * the HTML file that bootstraps the entire SpringUI runtime in the browser.
 *
 * <p>Generated file structure:
 * <ul>
 *   <li>Loads the WASM module via the JS glue file from {@link JSInteropGenerator}</li>
 *   <li>Provides the mount target {@code <div>} for the root component</li>
 *   <li>Shows a loading spinner while WASM initializes</li>
 *   <li>Handles load errors gracefully with a user-facing message</li>
 *   <li>Injects dev mode tooling when {@code devMode=true}</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * IndexHtmlGenerator generator = new IndexHtmlGenerator(
 *     IndexHtmlConfig.builder()
 *         .title("My SpringUI App")
 *         .mountTargetId("root")
 *         .wasmJsFile("springui.js")
 *         .outputDir("./springui-out")
 *         .devMode(true)
 *         .build()
 * );
 * IndexHtmlResult result = generator.generate();
 * }</pre>
 *
 * @see JSInteropGenerator
 * @see SpringUICompiler
 */
public class IndexHtmlGenerator {

    private final IndexHtmlConfig config;

    public IndexHtmlGenerator(IndexHtmlConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("IndexHtmlConfig must not be null");
        }
        this.config = config;
    }

    /**
     * Generates the index.html content and optionally writes it to disk.
     *
     * @return {@link IndexHtmlResult} containing the generated HTML and metadata
     */
    public IndexHtmlResult generate() {
        String html = buildHtml();
        IndexHtmlResult result = new IndexHtmlResult(html, config);

        if (config.isWriteToDisk()) {
            writeToDisk(html);
        }

        return result;
    }

    /**
     * Assembles the full HTML document from its parts.
     */
    String buildHtml() {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"" + config.getLang() + "\">\n"
                + buildHead()
                + buildBody()
                + "</html>\n";
    }

    private String buildHead() {
        StringBuilder head = new StringBuilder();
        head.append("<head>\n");
        head.append("  <meta charset=\"UTF-8\">\n");
        head.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        head.append("  <title>").append(escapeHtml(config.getTitle())).append("</title>\n");
        head.append(buildStyles());
        head.append("</head>\n");
        return head.toString();
    }

    private String buildStyles() {
        return "  <style>\n"
                + "    * { margin: 0; padding: 0; box-sizing: border-box; }\n"
                + "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }\n"
                + "\n"
                + "    /* SpringUI loading overlay */\n"
                + "    #springui-loading {\n"
                + "      display: flex;\n"
                + "      flex-direction: column;\n"
                + "      align-items: center;\n"
                + "      justify-content: center;\n"
                + "      height: 100vh;\n"
                + "      gap: 16px;\n"
                + "      color: #555;\n"
                + "      font-size: 14px;\n"
                + "    }\n"
                + "    .springui-spinner {\n"
                + "      width: 36px;\n"
                + "      height: 36px;\n"
                + "      border: 3px solid #e0e0e0;\n"
                + "      border-top-color: #6db33f;\n"
                + "      border-radius: 50%;\n"
                + "      animation: springui-spin 0.8s linear infinite;\n"
                + "    }\n"
                + "    @keyframes springui-spin {\n"
                + "      to { transform: rotate(360deg); }\n"
                + "    }\n"
                + "\n"
                + "    /* SpringUI error overlay */\n"
                + "    #springui-error {\n"
                + "      display: none;\n"
                + "      flex-direction: column;\n"
                + "      align-items: center;\n"
                + "      justify-content: center;\n"
                + "      height: 100vh;\n"
                + "      gap: 12px;\n"
                + "      color: #c0392b;\n"
                + "      font-size: 14px;\n"
                + "    }\n"
                + "    #springui-error h2 { font-size: 18px; }\n"
                + "    #springui-error code {\n"
                + "      background: #fff3f3;\n"
                + "      padding: 8px 14px;\n"
                + "      border-radius: 4px;\n"
                + "      font-size: 12px;\n"
                + "      max-width: 600px;\n"
                + "      overflow-wrap: break-word;\n"
                + "    }\n"
                + (config.isDevMode() ? buildDevModeStyles() : "")
                + "  </style>\n";
    }

    private String buildDevModeStyles() {
        return "\n"
                + "    /* SpringUI DevTools badge (dev mode only) */\n"
                + "    #springui-devtools-badge {\n"
                + "      position: fixed;\n"
                + "      bottom: 12px;\n"
                + "      right: 12px;\n"
                + "      background: #6db33f;\n"
                + "      color: white;\n"
                + "      font-size: 11px;\n"
                + "      font-weight: 600;\n"
                + "      padding: 4px 10px;\n"
                + "      border-radius: 12px;\n"
                + "      letter-spacing: 0.5px;\n"
                + "      z-index: 9999;\n"
                + "      cursor: pointer;\n"
                + "      user-select: none;\n"
                + "    }\n";
    }

    private String buildBody() {
        StringBuilder body = new StringBuilder();
        body.append("<body>\n");
        body.append("\n");
        body.append("  <!-- SpringUI loading state -->\n");
        body.append("  <div id=\"springui-loading\">\n");
        body.append("    <div class=\"springui-spinner\"></div>\n");
        body.append("    <span>Loading SpringUI...</span>\n");
        body.append("  </div>\n");
        body.append("\n");
        body.append("  <!-- SpringUI error state -->\n");
        body.append("  <div id=\"springui-error\">\n");
        body.append("    <h2>SpringUI failed to load</h2>\n");
        body.append("    <p>Check the browser console for details.</p>\n");
        body.append("    <code id=\"springui-error-message\"></code>\n");
        body.append("  </div>\n");
        body.append("\n");
        body.append("  <!-- Root mount target — SpringUI renders here -->\n");
        body.append("  <div id=\"").append(escapeHtml(config.getMountTargetId())).append("\"></div>\n");
        body.append("\n");
        if (config.isDevMode()) {
            body.append("  <!-- DevTools badge (dev mode only) -->\n");
            body.append("  <div id=\"springui-devtools-badge\" title=\"SpringUI DevTools\">&#9889; SpringUI DEV</div>\n");
            body.append("\n");
        }
        body.append("  <!-- SpringUI WASM bootstrap -->\n");
        body.append("  <script src=\"").append(escapeHtml(config.getWasmJsFile())).append("\" defer></script>\n");
        body.append("  <script>\n");
        body.append(buildBootstrapScript());
        body.append("  </script>\n");
        body.append("\n");
        body.append("</body>\n");
        return body.toString();
    }

    private String buildBootstrapScript() {
        StringBuilder script = new StringBuilder();
        script.append("    // SpringUI bootstrap — runs after WASM JS glue is loaded\n");
        script.append("    window.__SPRINGUI_CONFIG__ = {\n");
        script.append("      mountTarget: '").append(config.getMountTargetId()).append("',\n");
        script.append("      devMode: ").append(config.isDevMode()).append(",\n");
        script.append("      version: '").append(config.getVersion()).append("'\n");
        script.append("    };\n");
        script.append("\n");
        script.append("    function springUIShowError(message) {\n");
        script.append("      document.getElementById('springui-loading').style.display = 'none';\n");
        script.append("      var errorEl = document.getElementById('springui-error');\n");
        script.append("      errorEl.style.display = 'flex';\n");
        script.append("      document.getElementById('springui-error-message').textContent = message;\n");
        script.append("    }\n");
        script.append("\n");
        script.append("    function springUIOnReady() {\n");
        script.append("      document.getElementById('springui-loading').style.display = 'none';\n");
        if (config.isDevMode()) {
            script.append("      console.log('[SpringUI] Mounted in DEV mode on #")
                    .append(config.getMountTargetId()).append("');\n");
        }
        script.append("    }\n");
        script.append("\n");
        script.append("    window.addEventListener('springui:ready', springUIOnReady);\n");
        script.append("    window.addEventListener('springui:error', function(e) {\n");
        script.append("      springUIShowError(e.detail || 'Unknown error');\n");
        script.append("    });\n");
        if (config.isDevMode()) {
            script.append("\n");
            script.append("    // Dev mode: hot reload via WebSocket\n");
            script.append("    (function() {\n");
            script.append("      var ws = new WebSocket('ws://localhost:")
                    .append(config.getDevServerPort()).append("/springui-hot-reload');\n");
            script.append("      ws.onmessage = function(e) {\n");
            script.append("        if (e.data === 'reload') {\n");
            script.append("          console.log('[SpringUI] Hot reload triggered');\n");
            script.append("          window.location.reload();\n");
            script.append("        }\n");
            script.append("      };\n");
            script.append("      ws.onerror = function() {\n");
            script.append("        console.warn('[SpringUI] Hot reload server not reachable');\n");
            script.append("      };\n");
            script.append("    })();\n");
        }
        return script.toString();
    }

    private void writeToDisk(String html) {
        try {
            Path outputPath = Paths.get(config.getOutputDir());
            Files.createDirectories(outputPath);
            Path filePath = outputPath.resolve("index.html");
            Files.writeString(filePath, html);
        } catch (IOException e) {
            throw new IndexHtmlGenerationException(
                    "Failed to write index.html to: " + config.getOutputDir(), e);
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // -------------------------------------------------------------------------
    // Config
    // -------------------------------------------------------------------------

    /**
     * Configuration for {@link IndexHtmlGenerator}.
     */
    public static class IndexHtmlConfig {

        private final String title;
        private final String mountTargetId;
        private final String wasmJsFile;
        private final String outputDir;
        private final boolean devMode;
        private final boolean writeToDisk;
        private final String lang;
        private final String version;
        private final int devServerPort;

        private IndexHtmlConfig(Builder builder) {
            this.title = builder.title;
            this.mountTargetId = builder.mountTargetId;
            this.wasmJsFile = builder.wasmJsFile;
            this.outputDir = builder.outputDir;
            this.devMode = builder.devMode;
            this.writeToDisk = builder.writeToDisk;
            this.lang = builder.lang;
            this.version = builder.version;
            this.devServerPort = builder.devServerPort;
        }

        public String getTitle() { return title; }
        public String getMountTargetId() { return mountTargetId; }
        public String getWasmJsFile() { return wasmJsFile; }
        public String getOutputDir() { return outputDir; }
        public boolean isDevMode() { return devMode; }
        public boolean isWriteToDisk() { return writeToDisk; }
        public String getLang() { return lang; }
        public String getVersion() { return version; }
        public int getDevServerPort() { return devServerPort; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String title = "SpringUI App";
            private String mountTargetId = "root";
            private String wasmJsFile = "springui.js";
            private String outputDir = "./springui-out";
            private boolean devMode = false;
            private boolean writeToDisk = false;
            private String lang = "en";
            private String version = "0.1.0-alpha";
            private int devServerPort = 8080;

            public Builder title(String title) { this.title = title; return this; }
            public Builder mountTargetId(String id) { this.mountTargetId = id; return this; }
            public Builder wasmJsFile(String file) { this.wasmJsFile = file; return this; }
            public Builder outputDir(String dir) { this.outputDir = dir; return this; }
            public Builder devMode(boolean devMode) { this.devMode = devMode; return this; }
            public Builder writeToDisk(boolean write) { this.writeToDisk = write; return this; }
            public Builder lang(String lang) { this.lang = lang; return this; }
            public Builder version(String version) { this.version = version; return this; }
            public Builder devServerPort(int port) { this.devServerPort = port; return this; }

            public IndexHtmlConfig build() {
                if (title == null || title.isBlank()) {
                    throw new IllegalArgumentException("title must not be blank");
                }
                if (mountTargetId == null || mountTargetId.isBlank()) {
                    throw new IllegalArgumentException("mountTargetId must not be blank");
                }
                if (wasmJsFile == null || wasmJsFile.isBlank()) {
                    throw new IllegalArgumentException("wasmJsFile must not be blank");
                }
                return new IndexHtmlConfig(this);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Result
    // -------------------------------------------------------------------------

    /**
     * Result of {@link IndexHtmlGenerator#generate()}.
     */
    public static class IndexHtmlResult {

        private final String html;
        private final IndexHtmlConfig config;

        public IndexHtmlResult(String html, IndexHtmlConfig config) {
            this.html = html;
            this.config = config;
        }

        public String getHtml() { return html; }
        public IndexHtmlConfig getConfig() { return config; }
        public boolean isSuccess() { return html != null && !html.isBlank(); }

        /** Byte size of the generated HTML. */
        public long getSizeBytes() {
            return html == null ? 0 : html.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        }

        @Override
        public String toString() {
            return "IndexHtmlResult{size=" + getSizeBytes() + "B, devMode=" + config.isDevMode() + "}";
        }
    }

    // -------------------------------------------------------------------------
    // Exception
    // -------------------------------------------------------------------------

    public static class IndexHtmlGenerationException extends RuntimeException {
        public IndexHtmlGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
        public IndexHtmlGenerationException(String message) {
            super(message);
        }
    }
}