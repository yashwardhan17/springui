package io.springui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CliPrinterTest {

    private CliPrinter printer;
    private CliPrinter noColorPrinter;

    @BeforeEach
    void setUp() {
        printer        = new CliPrinter(true);   // color on
        noColorPrinter = new CliPrinter(false);  // color off
    }

    @Test
    void colorEnabled_wrapsTextWithAnsiCodes() {
        String result = printer.color("\u001B[32m", "hello");
        assertTrue(result.startsWith("\u001B[32m"), "Should start with ANSI green");
        assertTrue(result.contains("hello"));
        assertTrue(result.endsWith("\u001B[0m"), "Should end with ANSI reset");
    }

    @Test
    void colorDisabled_returnsPlainText() {
        String result = noColorPrinter.color("\u001B[32m", "hello");
        assertEquals("hello", result, "Color disabled — should return plain text");
    }

    @Test
    void isColorEnabled_reflectsConstructorArg() {
        assertTrue(printer.isColorEnabled());
        assertFalse(noColorPrinter.isColorEnabled());
    }

    @Test
    void banner_doesNotThrow() {
        assertDoesNotThrow(() -> printer.banner());
    }

    @Test
    void info_doesNotThrow() {
        assertDoesNotThrow(() -> printer.info("some info message"));
    }

    @Test
    void success_doesNotThrow() {
        assertDoesNotThrow(() -> printer.success("operation complete"));
    }

    @Test
    void warn_doesNotThrow() {
        assertDoesNotThrow(() -> printer.warn("something might be wrong"));
    }

    @Test
    void error_doesNotThrow() {
        assertDoesNotThrow(() -> printer.error("something went wrong"));
    }

    @Test
    void usage_doesNotThrow() {
        assertDoesNotThrow(() -> printer.usage());
    }

    @Test
    void scaffoldDone_doesNotThrow() {
        assertDoesNotThrow(() -> printer.scaffoldDone("my-app"));
    }

    @Test
    void buildDone_doesNotThrow() {
        assertDoesNotThrow(() -> printer.buildDone("springui-out"));
    }

    @Test
    void devServerStarted_doesNotThrow() {
        assertDoesNotThrow(() -> printer.devServerStarted(8080));
    }
}