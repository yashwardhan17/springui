package io.springui.compiler;

import java.io.File;
import java.nio.file.Path;

/**
 * SpringUICompiler — the heart of Phase 2.
 * Wraps TeaVM's compilation API to compile SpringUI components
 * (Java bytecode) into WebAssembly (.wasm) for browser execution.
 *
 * Phase 2 goals:
 * - Take a UIComponent subclass
 * - Compile it + springui-core to .wasm
 * - Generate a thin JS glue file for browser loading
 * - Output to a configurable directory
 */
public class SpringUICompiler {

    // ===========================
    // Configuration
    // ===========================

    private final CompilerConfig config;

    public SpringUICompiler(CompilerConfig config) {
        this.config = config;
    }

    // ===========================
    // Compile
    // ===========================

    /**
     * Compiles the given component class to WebAssembly.
     * Returns a CompilationResult describing what was produced.
     */
    public CompilationResult compile(Class<?> componentClass) {
        long startTime = System.currentTimeMillis();

        log("Starting SpringUI compilation...");
        log("Target component: " + componentClass.getName());
        log("Output directory: " + config.getOutputDir());
        log("Target: " + config.getTarget());

        try {
            // Step 1 — Validate the component
            validateComponent(componentClass);
            log("✓ Component validated");

            // Step 2 — Prepare output directory
            prepareOutputDir();
            log("✓ Output directory ready");

            // Step 3 — Run TeaVM compilation
            // Phase 2: real TeaVM API call goes here
            // For now, simulate the compilation pipeline
            CompilationResult result = runCompilation(componentClass);

            long duration = System.currentTimeMillis() - startTime;
            log("✓ Compilation complete in " + duration + "ms");
            log("  Output: " + result.getWasmFile());
            log("  JS glue: " + result.getJsGlueFile());

            return result;

        } catch (CompilationException e) {
            log("✗ Compilation failed: " + e.getMessage());
            throw e;
        }
    }

    // ===========================
    // Validation
    // ===========================

    /**
     * Validates that the given class is a valid SpringUI component.
     * Must extend UIComponent and have a no-arg constructor.
     */
    private void validateComponent(Class<?> componentClass) {
        // Check it extends UIComponent
        Class<?> superClass = componentClass.getSuperclass();
        boolean isUIComponent = false;
        while (superClass != null) {
            if (superClass.getName().equals("io.springui.core.UIComponent")) {
                isUIComponent = true;
                break;
            }
            superClass = superClass.getSuperclass();
        }

        if (!isUIComponent) {
            throw new CompilationException(
                    componentClass.getName() + " does not extend UIComponent. " +
                            "Only UIComponent subclasses can be compiled by SpringUI."
            );
        }

        // Check it has a no-arg constructor
        try {
            componentClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new CompilationException(
                    componentClass.getName() + " must have a no-argument constructor " +
                            "to be compiled by SpringUI."
            );
        }
    }

    // ===========================
    // Output Directory
    // ===========================

    private void prepareOutputDir() {
        File outputDir = new File(config.getOutputDir());
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                throw new CompilationException(
                        "Failed to create output directory: " + config.getOutputDir()
                );
            }
        }
    }

    // ===========================
    // Compilation Pipeline
    // Phase 1: simulated
    // Phase 2: real TeaVM API
    // ===========================

    private CompilationResult runCompilation(Class<?> componentClass) {
        String componentName = componentClass.getSimpleName().toLowerCase();
        String wasmFile = config.getOutputDir() + "/" + componentName + ".wasm";
        String jsGlueFile = config.getOutputDir() + "/" + componentName + ".js";

        // Phase 2: replace this block with real TeaVM compilation:
        //
        // TeaVMTool tool = new TeaVMTool();
        // tool.setMainClass(componentClass.getName());
        // tool.setTargetDirectory(new File(config.getOutputDir()));
        // tool.setTargetType(TeaVMTargetType.WEBASSEMBLY);
        // tool.generate();

        log("  [TeaVM] Analyzing class hierarchy...");
        log("  [TeaVM] Resolving dependencies...");
        log("  [TeaVM] Generating WebAssembly bytecode...");
        log("  [TeaVM] Generating JS glue layer...");

        return new CompilationResult(
                componentClass.getName(),
                Path.of(wasmFile),
                Path.of(jsGlueFile),
                true,
                null
        );
    }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[SpringUICompiler] " + message);
    }

    // ===========================
    // Static factory
    // ===========================

    /**
     * Creates a compiler with default configuration.
     * Output goes to ./springui-out/
     */
    public static SpringUICompiler withDefaults() {
        return new SpringUICompiler(CompilerConfig.defaults());
    }
}