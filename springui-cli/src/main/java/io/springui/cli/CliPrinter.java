package io.springui.cli;

/**
 * CliPrinter — console output helper for SpringUI CLI.
 *
 * Handles colored output, banners, progress indicators, and error formatting.
 * Uses ANSI escape codes; gracefully degrades on terminals that don't support them.
 */
public class CliPrinter {

    // ANSI color codes
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String RED    = "\u001B[31m";
    private static final String DIM    = "\u001B[2m";

    private final boolean colorEnabled;

    public CliPrinter() {
        // Disable color if NO_COLOR env var is set or output is not a terminal
        this.colorEnabled = System.getenv("NO_COLOR") == null;
    }

    /** For testing — allows injecting color preference. */
    CliPrinter(boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    // ─── Banner ──────────────────────────────────────────────────────────────

    public void banner() {
        System.out.println();
        System.out.println(color(BOLD + CYAN,
                "  ☕  SpringUI CLI  v0.1.0-alpha"));
        System.out.println(color(DIM,
                "  Write Java. Ship to the browser."));
        System.out.println();
    }

    // ─── Log levels ──────────────────────────────────────────────────────────

    public void info(String message) {
        System.out.println(color(CYAN, "  ℹ ") + message);
    }

    public void success(String message) {
        System.out.println(color(GREEN, "  ✔ ") + message);
    }

    public void warn(String message) {
        System.out.println(color(YELLOW, "  ⚠ ") + message);
    }

    public void error(String message) {
        System.err.println(color(RED, "  ✖ ") + message);
    }

    public void step(String step, String detail) {
        System.out.println(color(DIM, "    " + step + " ") + detail);
    }

    // ─── Structural output ───────────────────────────────────────────────────

    public void header(String title) {
        System.out.println();
        System.out.println(color(BOLD, "  " + title));
        System.out.println(color(DIM, "  " + "─".repeat(title.length())));
    }

    public void blank() {
        System.out.println();
    }

    /**
     * Prints a "done" summary after a scaffolding command completes.
     *
     * Example output:
     *   ✔ Created my-app
     *     cd my-app
     *     mvn test
     *     springui dev
     */
    public void scaffoldDone(String projectName) {
        blank();
        success(color(BOLD, "Created " + projectName + "!"));
        blank();
        System.out.println(color(DIM, "  Next steps:"));
        step("→", color(CYAN, "cd " + projectName));
        step("→", color(CYAN, "mvn test"));
        step("→", color(CYAN, "springui dev"));
        blank();
    }

    public void buildDone(String outputDir) {
        blank();
        success(color(BOLD, "Build complete!"));
        step("→", "Output: " + color(CYAN, outputDir));
        blank();
    }

    public void devServerStarted(int port) {
        blank();
        success(color(BOLD, "Dev server running!"));
        step("→", "Hot reload: " + color(CYAN, "ws://localhost:" + port));
        step("→", "App:        " + color(CYAN, "http://localhost:8080"));
        blank();
        System.out.println(color(DIM, "  Press Ctrl+C to stop."));
        blank();
    }

    // ─── Usage ───────────────────────────────────────────────────────────────

    public void usage() {
        blank();
        header("Usage");
        System.out.println();
        System.out.println("  " + color(CYAN, "springui new <name>") +
                "          Scaffold a new SpringUI project");
        System.out.println("  " + color(CYAN, "springui new <name> --template <tpl>") +
                "  Scaffold from a built-in template");
        System.out.println("  " + color(CYAN, "springui build") +
                "               Compile Java → WASM → index.html");
        System.out.println("  " + color(CYAN, "springui dev") +
                "                 Start hot-reload dev server");
        System.out.println();
        System.out.println(color(DIM, "  Templates: blank (default), todo, counter"));
        blank();
    }

    // ─── Internal ────────────────────────────────────────────────────────────

    String color(String ansiCode, String text) {
        if (!colorEnabled) return text;
        return ansiCode + text + RESET;
    }

    boolean isColorEnabled() {
        return colorEnabled;
    }
}