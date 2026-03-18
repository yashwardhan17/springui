package io.springui.compiler;

/**
 * CompilerTarget — the output format for SpringUI compilation.
 */
public enum CompilerTarget {

    /**
     * WebAssembly — runs natively in the browser via WASM runtime.
     * Primary target for SpringUI.
     */
    WASM,

    /**
     * JavaScript — fallback for browsers without WASM support.
     * TeaVM can compile Java to JS as well.
     */
    JAVASCRIPT,

    /**
     * Both WASM and JS — WASM with JS fallback.
     * Recommended for production.
     */
    WASM_WITH_JS_FALLBACK
}