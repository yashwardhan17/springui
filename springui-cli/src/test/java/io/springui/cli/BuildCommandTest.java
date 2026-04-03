package io.springui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BuildCommandTest {

    @TempDir
    Path tempDir;

    private BuildCommand command;

    @BeforeEach
    void setUp() throws IOException {
        CliPrinter printer = new CliPrinter(false);
        // simulateCompile=true so tests don't need TeaVM on the classpath
        command = new BuildCommand(printer, true);

        // Place a pom.xml so validation passes
        Files.writeString(tempDir.resolve("pom.xml"), "<project/>");
    }

    // ─── Happy path ──────────────────────────────────────────────────────────

    @Test
    void execute_succeeds_withValidArgs() {
        BuildCommand.BuildResult result = command.execute(
                tempDir, "io.myapp.AppComponent", "springui-out");
        assertTrue(result.success());
    }

    @Test
    void execute_createsOutputDirectory() {
        command.execute(tempDir, "io.myapp.AppComponent", "springui-out");
        assertTrue(Files.exists(tempDir.resolve("springui-out")));
    }

    @Test
    void execute_generatesIndexHtml() {
        command.execute(tempDir, "io.myapp.AppComponent", "springui-out");
        assertTrue(Files.exists(tempDir.resolve("springui-out/index.html")));
    }

    @Test
    void execute_generatesSpringUiJs() {
        command.execute(tempDir, "io.myapp.AppComponent", "springui-out");
        assertTrue(Files.exists(tempDir.resolve("springui-out/springui.js")));
    }

    @Test
    void execute_usesDefaultOutputDir_whenNullProvided() {
        BuildCommand.BuildResult result = command.execute(
                tempDir, "io.myapp.AppComponent", null);
        assertTrue(result.success());
        assertEquals(
                tempDir.resolve(BuildCommand.DEFAULT_OUTPUT_DIR).toString(),
                result.outputDir());
    }

    @Test
    void execute_recordsDuration() {
        BuildCommand.BuildResult result = command.execute(
                tempDir, "io.myapp.AppComponent", "springui-out");
        assertTrue(result.durationMs() >= 0);
    }

    // ─── Validation failures ─────────────────────────────────────────────────

    @Test
    void execute_missingProjectDir_fails() {
        Path nonExistent = tempDir.resolve("does-not-exist");
        BuildCommand.BuildResult result = command.execute(
                nonExistent, "io.myapp.App", "out");
        assertFalse(result.success());
        assertTrue(result.message().contains("not found"));
    }

    @Test
    void execute_missingPomXml_fails() throws IOException {
        Path noPom = tempDir.resolve("no-pom");
        Files.createDirectories(noPom);
        BuildCommand.BuildResult result = command.execute(
                noPom, "io.myapp.App", "out");
        assertFalse(result.success());
        assertTrue(result.message().contains("pom.xml"));
    }

    @Test
    void execute_nullEntryClass_fails() {
        BuildCommand.BuildResult result = command.execute(tempDir, null, "out");
        assertFalse(result.success());
        assertTrue(result.message().contains("Entry class"));
    }

    @Test
    void execute_blankEntryClass_fails() {
        BuildCommand.BuildResult result = command.execute(tempDir, "", "out");
        assertFalse(result.success());
    }

    @Test
    void isValidClassName_acceptsFullyQualified() {
        assertTrue(BuildCommand.isValidClassName("io.myapp.AppComponent"));
        assertTrue(BuildCommand.isValidClassName("AppComponent"));
    }

    @Test
    void isValidClassName_rejectsInvalid() {
        assertFalse(BuildCommand.isValidClassName(""));
        assertFalse(BuildCommand.isValidClassName(".StartWithDot"));
    }
}