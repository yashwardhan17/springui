package io.springui.compiler;

import io.springui.compiler.IndexHtmlGenerator.IndexHtmlConfig;
import io.springui.compiler.IndexHtmlGenerator.IndexHtmlResult;
import io.springui.compiler.IndexHtmlGenerator.IndexHtmlGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link IndexHtmlGenerator}.
 *
 * Coverage:
 *  1. Default config generates valid HTML document
 *  2. Custom title appears in generated HTML
 *  3. Custom mount target ID appears in generated HTML
 *  4. Custom WASM JS file name appears in script tag
 *  5. Loading overlay is always present
 *  6. Error overlay is always present
 *  7. Dev mode injects DevTools badge and hot reload script
 *  8. Dev mode is absent in production mode
 *  9. window.__SPRINGUI_CONFIG__ is injected with correct values
 * 10. writeToDisk writes index.html to output directory
 * 11. Null config throws IllegalArgumentException
 */
class IndexHtmlGeneratorTest {

    // 1 -----------------------------------------------------------------------
    @Test
    void defaultConfig_generatesValidHtmlDocument() {
        IndexHtmlConfig config = IndexHtmlConfig.builder().build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        IndexHtmlResult result = generator.generate();

        assertTrue(result.isSuccess());
        String html = result.getHtml();
        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.contains("<html lang=\"en\">"));
        assertTrue(html.contains("</html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(result.getSizeBytes() > 0);
    }

    // 2 -----------------------------------------------------------------------
    @Test
    void customTitle_appearsInTitleTag() {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .title("My Awesome App")
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("<title>My Awesome App</title>"));
    }

    // 3 -----------------------------------------------------------------------
    @Test
    void customMountTargetId_appearsInRootDiv() {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .mountTargetId("app")
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("<div id=\"app\">"));
        assertTrue(html.contains("mountTarget: 'app'"));
    }

    // 4 -----------------------------------------------------------------------
    @Test
    void customWasmJsFile_appearsInScriptTag() {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .wasmJsFile("myapp.wasm.js")
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("<script src=\"myapp.wasm.js\" defer></script>"));
    }

    // 5 -----------------------------------------------------------------------
    @Test
    void loadingOverlay_isAlwaysPresent() {
        IndexHtmlConfig config = IndexHtmlConfig.builder().build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("id=\"springui-loading\""));
        assertTrue(html.contains("springui-spinner"));
        assertTrue(html.contains("Loading SpringUI..."));
    }

    // 6 -----------------------------------------------------------------------
    @Test
    void errorOverlay_isAlwaysPresent() {
        IndexHtmlConfig config = IndexHtmlConfig.builder().build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("id=\"springui-error\""));
        assertTrue(html.contains("id=\"springui-error-message\""));
        assertTrue(html.contains("SpringUI failed to load"));
    }

    // 7 -----------------------------------------------------------------------
    @Test
    void devMode_injectsDevToolsBadgeAndHotReloadScript() {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .devMode(true)
                .devServerPort(8080)
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("id=\"springui-devtools-badge\""));
        assertTrue(html.contains("devMode: true"));
        assertTrue(html.contains("springui-hot-reload"));
        assertTrue(html.contains("ws://localhost:8080/springui-hot-reload"));
        assertTrue(html.contains("Hot reload triggered"));
    }

    // 8 -----------------------------------------------------------------------
    @Test
    void productionMode_doesNotContainDevToolsOrHotReload() {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .devMode(false)
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertFalse(html.contains("springui-devtools-badge"));
        assertFalse(html.contains("hot-reload"));
        assertFalse(html.contains("WebSocket"));
        assertTrue(html.contains("devMode: false"));
    }

    // 9 -----------------------------------------------------------------------
    @Test
    void bootstrapScript_injectsCorrectWindowConfig() {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .mountTargetId("root")
                .devMode(false)
                .version("0.1.0-alpha")
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("window.__SPRINGUI_CONFIG__"));
        assertTrue(html.contains("mountTarget: 'root'"));
        assertTrue(html.contains("version: '0.1.0-alpha'"));
        assertTrue(html.contains("springui:ready"));
        assertTrue(html.contains("springui:error"));
    }

    // 10 ----------------------------------------------------------------------
    @Test
    void writeToDisk_writesIndexHtmlToOutputDirectory(@TempDir Path tempDir) {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .title("Disk Write Test")
                .outputDir(tempDir.toString())
                .writeToDisk(true)
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        generator.generate();

        Path indexFile = tempDir.resolve("index.html");
        assertTrue(Files.exists(indexFile));
        assertDoesNotThrow(() -> {
            String content = Files.readString(indexFile);
            assertTrue(content.contains("<title>Disk Write Test</title>"));
        });
    }

    // 11 ----------------------------------------------------------------------
    @Test
    void nullConfig_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new IndexHtmlGenerator(null));
    }

    // 12 — bonus: HTML-special chars in title are escaped ----------------------
    @Test
    void specialCharsInTitle_areEscaped() {
        IndexHtmlConfig config = IndexHtmlConfig.builder()
                .title("<My & App>")
                .build();
        IndexHtmlGenerator generator = new IndexHtmlGenerator(config);

        String html = generator.generate().getHtml();

        assertTrue(html.contains("&lt;My &amp; App&gt;"));
        assertFalse(html.contains("<My & App>"));
    }
}