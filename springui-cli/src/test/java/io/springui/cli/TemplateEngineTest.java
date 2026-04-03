package io.springui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateEngineTest {

    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TemplateEngine();
    }

    // ─── render() ────────────────────────────────────────────────────────────

    @Test
    void render_replacesAllPlaceholders() {
        String result = engine.render(
                "Hello {{name}}, welcome to {{place}}!",
                Map.of("name", "Yash", "place", "SpringUI")
        );
        assertEquals("Hello Yash, welcome to SpringUI!", result);
    }

    @Test
    void render_leavesUnknownPlaceholdersIntact() {
        String result = engine.render("Hello {{name}}!", Map.of());
        assertEquals("Hello {{name}}!", result);
    }

    @Test
    void render_handlesEmptyTemplate() {
        assertEquals("", engine.render("", Map.of("key", "val")));
    }

    // ─── pomXml() ────────────────────────────────────────────────────────────

    @Test
    void pomXml_containsGroupId() {
        String pom = engine.pomXml("io.myapp", "my-app", "My App");
        assertTrue(pom.contains("<groupId>io.myapp</groupId>"));
    }

    @Test
    void pomXml_containsArtifactId() {
        String pom = engine.pomXml("io.myapp", "my-app", "My App");
        assertTrue(pom.contains("<artifactId>my-app</artifactId>"));
    }

    @Test
    void pomXml_containsSpringUIDependency() {
        String pom = engine.pomXml("io.myapp", "my-app", "My App");
        assertTrue(pom.contains("springui-core"));
    }

    // ─── componentSource() ───────────────────────────────────────────────────

    @Test
    void componentSource_blank_containsPackageAndClass() {
        String src = engine.componentSource(
                TemplateEngine.Template.BLANK, "io.myapp", "AppComponent");
        assertTrue(src.contains("package io.myapp;"));
        assertTrue(src.contains("class AppComponent"));
    }

    @Test
    void componentSource_todo_containsTodoState() {
        String src = engine.componentSource(
                TemplateEngine.Template.TODO, "io.myapp", "TodoComponent");
        assertTrue(src.contains("todos"));
    }

    @Test
    void componentSource_counter_containsCountState() {
        String src = engine.componentSource(
                TemplateEngine.Template.COUNTER, "io.myapp", "CounterComponent");
        assertTrue(src.contains("count"));
    }

    // ─── isValidTemplate() ───────────────────────────────────────────────────

    @Test
    void isValidTemplate_acceptsKnownTemplates() {
        assertTrue(engine.isValidTemplate("blank"));
        assertTrue(engine.isValidTemplate("todo"));
        assertTrue(engine.isValidTemplate("counter"));
    }

    @Test
    void isValidTemplate_rejectsUnknown() {
        assertFalse(engine.isValidTemplate("react"));
        assertFalse(engine.isValidTemplate(""));
    }

    @Test
    void template_fromString_caseInsensitive() {
        assertDoesNotThrow(() -> TemplateEngine.Template.fromString("TODO"));
        assertDoesNotThrow(() -> TemplateEngine.Template.fromString("Blank"));
    }

    @Test
    void template_fromString_throwsOnUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> TemplateEngine.Template.fromString("unknown-tpl"));
    }
}