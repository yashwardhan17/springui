package io.springui.cli;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DevCommand — handles {@code springui dev}.
 *
 * Starts the SpringUI hot-reload development server ({@code HotReloadServer}
 * from springui-devtools) and optionally opens the browser.
 *
 * In test / offline mode, server startup is simulated.
 */
public class DevCommand {

    static final int DEFAULT_PORT = 8080;

    private final CliPrinter printer;
    private final boolean simulateServer;   // true in unit tests

    public DevCommand(CliPrinter printer) {
        this(printer, false);
    }

    /** Test constructor — skips real HotReloadServer startup. */
    DevCommand(CliPrinter printer, boolean simulateServer) {
        this.printer        = printer;
        this.simulateServer = simulateServer;
    }

    // ─── Result ──────────────────────────────────────────────────────────────

    public record DevResult(
            boolean success,
            int port,
            String message
    ) {
        public static DevResult ok(int port) {
            return new DevResult(true, port, "Dev server started on port " + port);
        }
        public static DevResult fail(String reason) {
            return new DevResult(false, -1, reason);
        }
    }

    // ─── Execute ─────────────────────────────────────────────────────────────

    /**
     * Starts the dev server.
     *
     * @param projectDir   root of the SpringUI project
     * @param port         WebSocket port for hot reload (default 8080)
     * @param openBrowser  whether to open the browser automatically
     * @return result describing success or failure
     */
    public DevResult execute(Path projectDir, int port, boolean openBrowser) {

        // ── Validation ────────────────────────────────────────────────────────
        if (!Files.exists(projectDir)) {
            return DevResult.fail(
                    "Project directory not found: " + projectDir +
                            ". Run springui dev from inside a SpringUI project.");
        }
        if (!Files.exists(projectDir.resolve("pom.xml"))) {
            return DevResult.fail(
                    "No pom.xml found in " + projectDir +
                            ". Are you inside a SpringUI project?");
        }
        if (port < 1 || port > 65535) {
            return DevResult.fail(
                    "Invalid port " + port + ". Port must be between 1 and 65535.");
        }

        printer.header("Starting dev server");

        try {
            if (simulateServer) {
                printer.step("watch", "Source files  [simulated]");
                printer.step("start", "Hot reload WebSocket on :" + port + "  [simulated]");
            } else {
                startRealServer(projectDir, port);
            }

            if (openBrowser) {
                tryOpenBrowser(port);
            }

            printer.devServerStarted(port);
            return DevResult.ok(port);

        } catch (Exception e) {
            return DevResult.fail("Failed to start dev server: " + e.getMessage());
        }
    }

    // ─── Server startup ──────────────────────────────────────────────────────

    private void startRealServer(Path projectDir, int port) throws Exception {
        printer.step("watch", "Source files");
        printer.step("start", "Hot reload WebSocket on :" + port);

        // Invoke HotReloadServer reflectively — same pattern as BuildCommand
        Class<?> serverClass = Class.forName("io.springui.devtools.HotReloadServer");
        Object server = serverClass.getDeclaredConstructor(int.class).newInstance(port);
        java.lang.reflect.Method startMethod = serverClass.getMethod("start");
        startMethod.invoke(server);
    }

    private void tryOpenBrowser(int port) {
        String url = "http://localhost:" + port;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
            printer.step("open", "browser → " + url);
        } catch (Exception e) {
            printer.warn("Could not open browser automatically. Visit " + url);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    static boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }
}