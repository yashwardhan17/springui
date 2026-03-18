package io.springui.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JSInteropGeneratorTest {

    private JSInteropGenerator generator;
    private JSInteropConfig config;

    @BeforeEach
    void setUp() {
        generator = new JSInteropGenerator();
        config = new JSInteropConfig.Builder()
                .componentName("TodoApp")
                .wasmFileName("todoapp.wasm")
                .mountElementId("root")
                .frameworkVersion("0.1.0-alpha")
                .build();
    }

    @Test
    void shouldGenerateNonEmptyOutput() {
        String js = generator.generate(config);
        assertNotNull(js);
        assertFalse(js.isEmpty());
    }

    @Test
    void shouldContainFrameworkVersion() {
        String js = generator.generate(config);
        assertTrue(js.contains("0.1.0-alpha"));
    }

    @Test
    void shouldContainComponentName() {
        String js = generator.generate(config);
        assertTrue(js.contains("TodoApp"));
    }

    @Test
    void shouldContainWasmFileName() {
        String js = generator.generate(config);
        assertTrue(js.contains("todoapp.wasm"));
    }

    @Test
    void shouldContainMountElementId() {
        String js = generator.generate(config);
        assertTrue(js.contains("root"));
    }

    @Test
    void shouldContainDOMBindings() {
        String js = generator.generate(config);
        assertTrue(js.contains("createElement"));
        assertTrue(js.contains("appendChild"));
        assertTrue(js.contains("removeChild"));
        assertTrue(js.contains("setAttribute"));
        assertTrue(js.contains("setTextContent"));
    }

    @Test
    void shouldContainWasmLoader() {
        String js = generator.generate(config);
        assertTrue(js.contains("WebAssembly.instantiate"));
        assertTrue(js.contains("loadSpringUIWasm"));
    }

    @Test
    void shouldContainSpringUIRuntime() {
        String js = generator.generate(config);
        assertTrue(js.contains("springui_runtime"));
        assertTrue(js.contains("springui_runtime.init"));
        assertTrue(js.contains("springui_runtime.mount"));
    }

    @Test
    void shouldContainBootstrap() {
        String js = generator.generate(config);
        assertTrue(js.contains("DOMContentLoaded"));
    }

    @Test
    void shouldContainUseStrict() {
        String js = generator.generate(config);
        assertTrue(js.contains("'use strict'"));
    }

    @Test
    void shouldContainDoNotEditComment() {
        String js = generator.generate(config);
        assertTrue(js.contains("DO NOT EDIT"));
    }
}