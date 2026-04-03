package io.springui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DevCommandTest {

    @TempDir
    Path tempDir;

    private DevCommand command;

    @BeforeEach
    void setUp() throws IOException {
        CliPrinter printer = new CliPrinter(false);
        // simulateServer=true — no real HotReloadServer started in tests
        command = new DevCommand(printer, true);

        Files.writeString(tempDir.resolve("pom.xml"), "<project/>");
    }

    // ─── Happy path ──────────────────────────────────────────────────────────

    @Test
    void execute_succeeds_withDefaultPort() {
        DevCommand.DevResult result = command.execute(tempDir, 8080, false);
        assertTrue(result.success());
    }

    @Test
    void execute_returnsPort() {
        DevCommand.DevResult result = command.execute(tempDir, 9090, false);
        assertEquals(9090, result.port());
    }

    @Test
    void execute_customPort_succeeds() {
        DevCommand.DevResult result = command.execute(tempDir, 3000, false);
        assertTrue(result.success());
        assertEquals(3000, result.port());
    }

    @Test
    void execute_messageContainsPort() {
        DevCommand.DevResult result = command.execute(tempDir, 8080, false);
        assertTrue(result.message().contains("8080"));
    }

    // ─── Validation failures ─────────────────────────────────────────────────

    @Test
    void execute_missingProjectDir_fails() {
        Path nonExistent = tempDir.resolve("no-such-dir");
        DevCommand.DevResult result = command.execute(nonExistent, 8080, false);
        assertFalse(result.success());
        assertTrue(result.message().contains("not found"));
    }

    @Test
    void execute_missingPomXml_fails() throws IOException {
        Path noPom = tempDir.resolve("no-pom");
        Files.createDirectories(noPom);
        DevCommand.DevResult result = command.execute(noPom, 8080, false);
        assertFalse(result.success());
        assertTrue(result.message().contains("pom.xml"));
    }

    @Test
    void execute_portZero_fails() {
        DevCommand.DevResult result = command.execute(tempDir, 0, false);
        assertFalse(result.success());
        assertTrue(result.message().contains("port") || result.message().contains("Port"));
    }

    @Test
    void execute_portTooHigh_fails() {
        DevCommand.DevResult result = command.execute(tempDir, 99999, false);
        assertFalse(result.success());
    }

    // ─── isValidPort ─────────────────────────────────────────────────────────

    @Test
    void isValidPort_acceptsValidRange() {
        assertTrue(DevCommand.isValidPort(1));
        assertTrue(DevCommand.isValidPort(8080));
        assertTrue(DevCommand.isValidPort(65535));
    }

    @Test
    void isValidPort_rejectsOutOfRange() {
        assertFalse(DevCommand.isValidPort(0));
        assertFalse(DevCommand.isValidPort(-1));
        assertFalse(DevCommand.isValidPort(65536));
    }
}