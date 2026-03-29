package io.springui.compiler;

import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolException;
import org.teavm.tooling.sources.SourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TeaVMCompiler — real TeaVM integration for SpringUI.
 *
 * This class wraps TeaVM's TeaVMTool API to compile
 * SpringUI components (Java bytecode) into WebAssembly.
 *
 * This is the real Phase 2 implementation — not a simulation.
 * TeaVM actually compiles Java to WASM/JS that runs in browsers.
 */
public class TeaVMCompiler {

    // ===========================
    // Configuration
    // ===========================

    private final TeaVMCompilerConfig config;

    public TeaVMCompiler(TeaVMCompilerConfig config) {
        this.config = config;
    }

    public static TeaVMCompiler withDefaults() {
        return new TeaVMCompiler(TeaVMCompilerConfig.defaults());
    }

    // ===========================
    // Compile
    // ===========================

    /**
     * Compiles a SpringUI component entry point to WebAssembly
     * using TeaVM's real compilation pipeline.
     *
     * @param mainClass The entry point class containing
     *                  the SpringUI bootstrap code
     * @return TeaVMCompilationResult with output file paths
     */
    public TeaVMCompilationResult compile(String mainClass) {
        log("Starting TeaVM compilation...");
        log("Entry point: " + mainClass);
        log("Target: " + config.getTarget());
        log("Output: " + config.getOutputDir());

        long startTime = System.currentTimeMillis();

        try {
            // Prepare output directory
            File outputDir = new File(config.getOutputDir());
            if (!outputDir.exists()) outputDir.mkdirs();

            // Configure TeaVMTool
            TeaVMTool tool = new TeaVMTool();

            // Set main class — the SpringUI bootstrap entry point
            tool.setMainClass(mainClass);

            // Set output directory
            tool.setTargetDirectory(outputDir);

            // Set output file name
            tool.setTargetFileName(config.getOutputFileName());

            // Set optimization level
            tool.setOptimizationLevel(
                    config.isMinify()
                            ? TeaVMOptimizationLevel.FULL
                            : TeaVMOptimizationLevel.SIMPLE
            );

            // Enable/disable source maps
            tool.setSourceMapsFileGenerated(config.isSourceMaps());

            // Set classloader — uses current thread's classloader
            tool.setClassLoader(
                    Thread.currentThread().getContextClassLoader()
            );

            // Run compilation
            log("Running TeaVM compilation pipeline...");
            tool.generate();

            // Check for compilation problems
            List<String> problems = new ArrayList<>();
            tool.getProblemProvider().getProblems().forEach(p ->
                    problems.add(p.getText()));

            long duration = System.currentTimeMillis() - startTime;

            if (problems.isEmpty()) {
                log("✓ Compilation successful in " + duration + "ms");
                log("  Output: " + config.getOutputDir() +
                        "/" + config.getOutputFileName());
            } else {
                log("⚠ Compilation completed with " +
                        problems.size() + " warning(s)");
                problems.forEach(p -> log("  Warning: " + p));
            }

            return new TeaVMCompilationResult(
                    mainClass,
                    config.getOutputDir(),
                    config.getOutputFileName(),
                    true,
                    problems,
                    duration
            );

        } catch (TeaVMToolException e) {
            long duration = System.currentTimeMillis() - startTime;
            log("✗ Compilation failed: " + e.getMessage());
            return new TeaVMCompilationResult(
                    mainClass,
                    config.getOutputDir(),
                    config.getOutputFileName(),
                    false,
                    List.of(e.getMessage()),
                    duration
            );
        }
    }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[TeaVMCompiler] " + message);
    }
}