package io.springui.compiler;

import io.springui.core.UIComponent;
import io.springui.core.VNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpringUICompilerTest {

    // A valid test component
    static class ValidComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div").child(VNode.text("Hello"));
        }
    }

    // A class that does NOT extend UIComponent
    static class InvalidComponent {
        public void render() {}
    }

    // A component with no default constructor
    static class NoDefaultConstructorComponent extends UIComponent {
        public NoDefaultConstructorComponent(String name) {}

        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    private SpringUICompiler compiler;

    @BeforeEach
    void setUp() {
        CompilerConfig config = new CompilerConfig.Builder()
                .outputDir("./target/springui-test-out")
                .target(CompilerTarget.WASM)
                .build();
        compiler = new SpringUICompiler(config);
    }

    @Test
    void shouldCompileValidComponent() {
        CompilationResult result = compiler.compile(ValidComponent.class);
        assertTrue(result.isSuccess());
        assertNotNull(result.getWasmFile());
        assertNotNull(result.getJsGlueFile());
    }

    @Test
    void shouldContainComponentClassInResult() {
        CompilationResult result = compiler.compile(ValidComponent.class);
        assertTrue(result.getComponentClass()
                .contains("ValidComponent"));
    }

    @Test
    void shouldThrowForNonUIComponent() {
        assertThrows(CompilationException.class, () ->
                compiler.compile(InvalidComponent.class));
    }

    @Test
    void shouldProduceWasmFilePath() {
        CompilationResult result = compiler.compile(ValidComponent.class);
        assertTrue(result.getWasmFile().toString().endsWith(".wasm"));
    }

    @Test
    void shouldProduceJsGlueFilePath() {
        CompilationResult result = compiler.compile(ValidComponent.class);
        assertTrue(result.getJsGlueFile().toString().endsWith(".js"));
    }

    @Test
    void shouldUseDefaultConfig() {
        SpringUICompiler defaultCompiler = SpringUICompiler.withDefaults();
        CompilationResult result = defaultCompiler.compile(ValidComponent.class);
        assertTrue(result.isSuccess());
    }

    @Test

    void shouldBuildConfigWithBuilder() {
        CompilerConfig config = new CompilerConfig.Builder()
                .outputDir("./out")
                .target(CompilerTarget.JAVASCRIPT)
                .minify(true)
                .sourceMaps(false)
                .build();
        assertEquals("./out", config.getOutputDir());
        assertEquals(CompilerTarget.JAVASCRIPT, config.getTarget());
        assertTrue(config.isMinify());
        assertFalse(config.isSourceMaps());
    }

    @Test
    void shouldHaveThreeCompilerTargets() {
        assertEquals(3, CompilerTarget.values().length);
    }
}