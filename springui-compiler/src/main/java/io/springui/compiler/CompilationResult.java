package io.springui.compiler;

import java.nio.file.Path;

/**
 * CompilationResult — describes the output of a SpringUI compilation.
 */
public class CompilationResult {

    private final String componentClass;
    private final Path wasmFile;
    private final Path jsGlueFile;
    private final boolean success;
    private final String errorMessage;

    public CompilationResult(String componentClass, Path wasmFile,
                             Path jsGlueFile, boolean success,
                             String errorMessage) {
        this.componentClass = componentClass;
        this.wasmFile = wasmFile;
        this.jsGlueFile = jsGlueFile;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public String getComponentClass() { return componentClass; }
    public Path getWasmFile() { return wasmFile; }
    public Path getJsGlueFile() { return jsGlueFile; }
    public String getErrorMessage() { return errorMessage; }

    @Override
    public String toString() {
        return "CompilationResult{" +
                "component='" + componentClass + "'" +
                ", wasm=" + wasmFile +
                ", js=" + jsGlueFile +
                ", success=" + success +
                "}";
    }
}