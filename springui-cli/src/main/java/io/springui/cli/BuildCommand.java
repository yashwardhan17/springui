package io.springui.cli;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * BuildCommand — handles {@code springui build}.
 *
 * Resolves the project's entry class and delegates to {@code SpringUICompiler}
 * (from springui-compiler) to produce:
 * <pre>
 *   springui-out/
 *   ├── index.html
 *   ├── springui.js
 *   └── springui.wasm
 * </pre>
 *
 * In test / offline mode, compilation is simulated so tests don't need TeaVM.
 */
public class BuildCommand {

    static final String DEFAULT_OUTPUT_DIR = "springui-out";

    private final CliPrinter printer;
    private final boolean simulateCompile;   // true in unit tests

    public BuildCommand(CliPrinter printer) {
        this(printer, false);
    }

    /** Test constructor — skips real TeaVM invocation. */
    BuildCommand(CliPrinter printer, boolean simulateCompile) {
        this.printer         = printer;
        this.simulateCompile = simulateCompile;
    }

    // ─── Result ──────────────────────────────────────────────────────────────

    public record BuildResult(
            boolean success,
            String outputDir,
            String message,
            long durationMs
    ) {
        public static BuildResult ok(String outputDir, long ms) {
            return new BuildResult(true, outputDir, "Build succeeded", ms);
        }
        public static BuildResult fail(String reason) {
            return new BuildResult(false, null, reason, 0);
        }
    }

    // ─── Execute ─────────────────────────────────────────────────────────────

    /**
     * Executes the build.
     *
     * @param projectDir   root of the SpringUI project (contains pom.xml)
     * @param entryClass   fully-qualified main class, e.g. {@code io.myapp.AppComponent}
     * @param outputDir    output directory name (default: "springui-out")
     * @return result describing success or failure
     */
    public BuildResult execute(Path projectDir, String entryClass, String outputDir) {

        // ── Validation ────────────────────────────────────────────────────────
        if (!Files.exists(projectDir)) {
            return BuildResult.fail(
                    "Project directory not found: " + projectDir +
                            ". Run springui build from inside a SpringUI project.");
        }
        if (!Files.exists(projectDir.resolve("pom.xml"))) {
            return BuildResult.fail(
                    "No pom.xml found in " + projectDir +
                            ". Are you inside a SpringUI project?");
        }
        if (entryClass == null || entryClass.isBlank()) {
            return BuildResult.fail(
                    "Entry class cannot be blank. " +
                            "Usage: springui build --entry io.myapp.AppComponent");
        }
        if (!isValidClassName(entryClass)) {
            return BuildResult.fail("Invalid entry class name: '" + entryClass + "'");
        }

        String out = (outputDir != null && !outputDir.isBlank())
                ? outputDir : DEFAULT_OUTPUT_DIR;
        Path outputPath = projectDir.resolve(out);

        printer.header("Building " + entryClass);
        long start = System.currentTimeMillis();

        try {
            if (simulateCompile) {
                // Simulate compilation steps for testing
                simulateCompilationSteps(outputPath, entryClass);
            } else {
                realCompile(projectDir, entryClass, outputPath);
            }

            long elapsed = System.currentTimeMillis() - start;
            printer.buildDone(outputPath.toString());
            return BuildResult.ok(outputPath.toString(), elapsed);

        } catch (Exception e) {
            return BuildResult.fail("Build failed: " + e.getMessage());
        }
    }

    // ─── Compilation ─────────────────────────────────────────────────────────

    private void realCompile(Path projectDir, String entryClass, Path outputPath)
            throws Exception {
        // SpringUICompiler is the facade from springui-compiler module.
        // We call it reflectively so the CLI can still compile even when
        // the compiler module isn't on the test classpath.
        printer.step("compile", "Java → WASM  (TeaVM)");
        printer.step("generate", "springui.js  (JS interop glue)");
        printer.step("generate", "index.html   (browser entry point)");

        Class<?> compilerClass = Class.forName("io.springui.compiler.SpringUICompiler");
        Object compiler = compilerClass.getDeclaredConstructor().newInstance();
        java.lang.reflect.Method compile = compilerClass.getMethod(
                "compile", String.class, String.class);
        compile.invoke(compiler, entryClass, outputPath.toString());
    }

    private void simulateCompilationSteps(Path outputPath, String entryClass)
            throws java.io.IOException {
        printer.step("compile", "Java → WASM  (TeaVM)  [simulated]");
        printer.step("generate", "springui.js  [simulated]");
        printer.step("generate", "index.html   [simulated]");
        Files.createDirectories(outputPath);
        Files.writeString(outputPath.resolve("index.html"),
                "<!-- SpringUI output for " + entryClass + " -->");
        Files.writeString(outputPath.resolve("springui.js"), "/* springui js glue */");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    static boolean isValidClassName(String name) {
        // Basic check: package.ClassName segments, letters/digits/underscore
        return name.matches("[a-zA-Z_][a-zA-Z0-9_.]*[a-zA-Z0-9_]");
    }
}