package io.springui.compiler;

import java.util.List;

/**
 * TeaVMCompilationResult — result of a real TeaVM compilation.
 */
public class TeaVMCompilationResult {

    private final String mainClass;
    private final String outputDir;
    private final String outputFileName;
    private final boolean success;
    private final List<String> problems;
    private final long durationMs;

    public TeaVMCompilationResult(String mainClass, String outputDir,
                                  String outputFileName, boolean success,
                                  List<String> problems, long durationMs) {
        this.mainClass = mainClass;
        this.outputDir = outputDir;
        this.outputFileName = outputFileName;
        this.success = success;
        this.problems = problems;
        this.durationMs = durationMs;
    }

    public boolean isSuccess() { return success; }
    public boolean hasProblems() { return !problems.isEmpty(); }
    public String getMainClass() { return mainClass; }
    public String getOutputDir() { return outputDir; }
    public String getOutputFileName() { return outputFileName; }
    public String getFullOutputPath() { return outputDir + "/" + outputFileName; }
    public List<String> getProblems() { return problems; }
    public long getDurationMs() { return durationMs; }

    @Override
    public String toString() {
        return "TeaVMCompilationResult{" +
                "mainClass='" + mainClass + "'" +
                ", success=" + success +
                ", problems=" + problems.size() +
                ", duration=" + durationMs + "ms}";
    }
}