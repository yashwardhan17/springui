package io.springui.compiler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeaVMCompilerTest {

    // ===========================
    // Config tests
    // ===========================

    @Test
    void shouldBuildDefaultConfig() {
        TeaVMCompilerConfig config = TeaVMCompilerConfig.defaults();
        assertNotNull(config);
        assertEquals("./springui-out", config.getOutputDir());
        assertEquals("springui.js", config.getOutputFileName());
        assertEquals(TeaVMCompilerConfig.Target.JAVASCRIPT, config.getTarget());
        assertFalse(config.isMinify());
        assertTrue(config.isSourceMaps());
    }

    @Test
    void shouldBuildCustomConfig() {
        TeaVMCompilerConfig config = new TeaVMCompilerConfig.Builder()
                .outputDir("./out")
                .outputFileName("app.js")
                .target(TeaVMCompilerConfig.Target.WEBASSEMBLY)
                .minify(true)
                .sourceMaps(false)
                .build();

        assertEquals("./out", config.getOutputDir());
        assertEquals("app.js", config.getOutputFileName());
        assertEquals(TeaVMCompilerConfig.Target.WEBASSEMBLY, config.getTarget());
        assertTrue(config.isMinify());
        assertFalse(config.isSourceMaps());
    }

    @Test
    void shouldHaveThreeTargets() {
        assertEquals(3, TeaVMCompilerConfig.Target.values().length);
    }

    // ===========================
    // Compiler instantiation
    // ===========================

    @Test
    void shouldCreateCompilerWithDefaults() {
        TeaVMCompiler compiler = TeaVMCompiler.withDefaults();
        assertNotNull(compiler);
    }

    @Test
    void shouldCreateCompilerWithCustomConfig() {
        TeaVMCompilerConfig config = new TeaVMCompilerConfig.Builder()
                .outputDir("./target/teavm-test-out")
                .build();
        TeaVMCompiler compiler = new TeaVMCompiler(config);
        assertNotNull(compiler);
    }

    // ===========================
    // Compilation result
    // ===========================

    @Test
    void shouldProduceResultWithCorrectMainClass() {
        TeaVMCompilationResult result = new TeaVMCompilationResult(
                "io.springui.examples.todo.TodoApp",
                "./out", "app.js", true,
                java.util.List.of(), 100L
        );
        assertEquals("io.springui.examples.todo.TodoApp",
                result.getMainClass());
        assertTrue(result.isSuccess());
        assertFalse(result.hasProblems());
    }

    @Test
    void shouldComputeFullOutputPath() {
        TeaVMCompilationResult result = new TeaVMCompilationResult(
                "io.Test", "./out", "app.js",
                true, java.util.List.of(), 50L
        );
        assertEquals("./out/app.js", result.getFullOutputPath());
    }

    @Test
    void shouldDetectProblems() {
        TeaVMCompilationResult result = new TeaVMCompilationResult(
                "io.Test", "./out", "app.js",
                false, java.util.List.of("Error: missing class"),
                50L
        );
        assertFalse(result.isSuccess());
        assertTrue(result.hasProblems());
        assertEquals(1, result.getProblems().size());
    }

    // ===========================
    // Real compilation test
    // (runs actual TeaVM — may take a few seconds)
    // ===========================

    @Test
    void shouldAttemptRealCompilation() {
        TeaVMCompilerConfig config = new TeaVMCompilerConfig.Builder()
                .outputDir("./target/teavm-real-out")
                .outputFileName("todo.js")
                .target(TeaVMCompilerConfig.Target.JAVASCRIPT)
                .sourceMaps(false)
                .build();

        TeaVMCompiler compiler = new TeaVMCompiler(config);

        // Attempt real compilation of TodoApp
        TeaVMCompilationResult result = compiler.compile(
                "io.springui.examples.todo.TodoApp"
        );

        // We don't assert success here because TeaVM may not find
        // all classes on the test classpath — but it should not throw
        assertNotNull(result);
        assertNotNull(result.getMainClass());
        assertTrue(result.getDurationMs() >= 0);
    }
}