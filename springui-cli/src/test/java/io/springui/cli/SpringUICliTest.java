package io.springui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SpringUICli — argument parsing and command routing.
 *
 * Uses simulation modes for BuildCommand and DevCommand so tests
 * don't require a real project on disk or TeaVM.
 */
class SpringUICliTest {

    @TempDir
    Path tempDir;

    private SpringUICli cli;

    @BeforeEach
    void setUp() throws IOException {
        // Place pom.xml so build/dev commands pass validation
        Files.writeString(tempDir.resolve("pom.xml"), "<project/>");
        System.setProperty("user.dir", tempDir.toString());

        CliPrinter printer   = new CliPrinter(false);
        NewCommand newCmd    = new NewCommand(new TemplateEngine(), printer);
        BuildCommand buildCmd = new BuildCommand(printer, true);
        DevCommand devCmd    = new DevCommand(printer, true);

        cli = new SpringUICli(printer, newCmd, buildCmd, devCmd);
    }

    // ─── No-arg / help ───────────────────────────────────────────────────────

    @Test
    void noArgs_returnsZero() {
        assertEquals(0, cli.run(new String[]{}));
    }

    @Test
    void nullArgs_returnsZero() {
        assertEquals(0, cli.run(null));
    }

    @Test
    void helpFlag_returnsZero() {
        assertEquals(0, cli.run(new String[]{"--help"}));
    }

    @Test
    void helpShort_returnsZero() {
        assertEquals(0, cli.run(new String[]{"-h"}));
    }

    @Test
    void versionFlag_returnsZero() {
        assertEquals(0, cli.run(new String[]{"--version"}));
    }

    @Test
    void unknownCommand_returnsOne() {
        assertEquals(1, cli.run(new String[]{"unknown-cmd"}));
    }

    // ─── new command ─────────────────────────────────────────────────────────

    @Test
    void newCommand_missingName_returnsOne() {
        assertEquals(1, cli.run(new String[]{"new"}));
    }

    @Test
    void newCommand_validName_returnsZero() {
        assertEquals(0, cli.run(new String[]{"new", "my-test-app"}));
    }

    @Test
    void newCommand_withTemplate_returnsZero() {
        assertEquals(0, cli.run(new String[]{"new", "counter-app", "--template", "counter"}));
    }

    @Test
    void newCommand_invalidTemplate_returnsOne() {
        assertEquals(1, cli.run(new String[]{"new", "my-app2", "--template", "angular"}));
    }

    @Test
    void newCommand_invalidName_returnsOne() {
        assertEquals(1, cli.run(new String[]{"new", "MyApp"}));
    }

    // ─── build command ───────────────────────────────────────────────────────

    @Test
    void buildCommand_missingEntry_returnsOne() {
        assertEquals(1, cli.run(new String[]{"build"}));
    }

    @Test
    void buildCommand_withEntry_returnsZero() {
        assertEquals(0, cli.run(new String[]{"build", "--entry", "io.myapp.AppComponent"}));
    }

    @Test
    void buildCommand_withEntryAndOutput_returnsZero() {
        assertEquals(0, cli.run(new String[]{
                "build", "--entry", "io.myapp.AppComponent", "--output", "dist"}));
    }

    // ─── dev command ─────────────────────────────────────────────────────────

    @Test
    void devCommand_defaultPort_returnsZero() {
        assertEquals(0, cli.run(new String[]{"dev", "--no-open"}));
    }

    @Test
    void devCommand_customPort_returnsZero() {
        assertEquals(0, cli.run(new String[]{"dev", "--port", "3000", "--no-open"}));
    }

    @Test
    void devCommand_invalidPort_returnsOne() {
        assertEquals(1, cli.run(new String[]{"dev", "--port", "notANumber"}));
    }
}