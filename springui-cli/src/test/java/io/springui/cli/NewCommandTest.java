package io.springui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NewCommandTest {

    @TempDir
    Path tempDir;

    private NewCommand command;

    @BeforeEach
    void setUp() {
        CliPrinter printer = new CliPrinter(false);
        command = new NewCommand(new TemplateEngine(), printer);
    }

    // ─── Happy path ──────────────────────────────────────────────────────────

    @Test
    void execute_createsProjectDirectory() {
        NewCommand.NewResult result = command.execute("my-app", "blank", tempDir);
        assertTrue(result.success());
        assertTrue(Files.exists(tempDir.resolve("my-app")));
    }

    @Test
    void execute_createsPomXml() {
        command.execute("my-app", "blank", tempDir);
        assertTrue(Files.exists(tempDir.resolve("my-app/pom.xml")));
    }

    @Test
    void execute_createsApplicationYml() {
        command.execute("my-app", "blank", tempDir);
        assertTrue(Files.exists(tempDir.resolve("my-app/src/main/resources/application.yml")));
    }

    @Test
    void execute_createsGitignore() {
        command.execute("my-app", "blank", tempDir);
        assertTrue(Files.exists(tempDir.resolve("my-app/.gitignore")));
    }

    @Test
    void execute_createsComponentSourceFile() throws IOException {
        command.execute("my-app", "blank", tempDir);
        Path javaDir = tempDir.resolve("my-app/src/main/java/io/myapp");
        boolean anyJava = Files.walk(javaDir)
                .anyMatch(p -> p.toString().endsWith(".java"));
        assertTrue(anyJava);
    }

    @Test
    void execute_todoTemplate_createsTodoComponent() throws IOException {
        command.execute("todo-app", "todo", tempDir);
        Path base = tempDir.resolve("todo-app/src/main/java");
        boolean hasTodo = Files.walk(base)
                .anyMatch(p -> p.getFileName().toString().contains("TodoApp"));
        assertTrue(hasTodo);
    }

    @Test
    void execute_returnsProjectDirInResult() {
        NewCommand.NewResult result = command.execute("my-app", "blank", tempDir);
        assertNotNull(result.projectDir());
        assertTrue(result.projectDir().endsWith("my-app"));
    }

    // ─── Validation failures ─────────────────────────────────────────────────

    @Test
    void execute_nullName_fails() {
        NewCommand.NewResult result = command.execute(null, "blank", tempDir);
        assertFalse(result.success());
        assertTrue(result.message().contains("blank"));
    }

    @Test
    void execute_blankName_fails() {
        NewCommand.NewResult result = command.execute("  ", "blank", tempDir);
        assertFalse(result.success());
    }

    @Test
    void execute_invalidNameWithUppercase_fails() {
        NewCommand.NewResult result = command.execute("MyApp", "blank", tempDir);
        assertFalse(result.success());
    }

    @Test
    void execute_invalidTemplate_fails() {
        NewCommand.NewResult result = command.execute("my-app", "react", tempDir);
        assertFalse(result.success());
        assertTrue(result.message().contains("react"));
    }

    @Test
    void execute_directoryAlreadyExists_fails() {
        // First call succeeds
        command.execute("my-app", "blank", tempDir);
        // Second call with same name should fail
        NewCommand.NewResult result = command.execute("my-app", "blank", tempDir);
        assertFalse(result.success());
        assertTrue(result.message().contains("already exists"));
    }

    // ─── Helper methods ──────────────────────────────────────────────────────

    @Test
    void toClassName_hyphenated_becomesPascalCase() {
        assertEquals("MyApp",     NewCommand.toClassName("my-app"));
        assertEquals("TodoApp",   NewCommand.toClassName("todo-app"));
        assertEquals("Springui",  NewCommand.toClassName("springui"));
    }

    @Test
    void sanitize_removesNonAlphanumeric() {
        assertEquals("myapp",    NewCommand.sanitize("my-app"));
        assertEquals("todoapp",  NewCommand.sanitize("todo-app"));
    }

    @Test
    void isValidProjectName_acceptsValidNames() {
        assertTrue(NewCommand.isValidProjectName("my-app"));
        assertTrue(NewCommand.isValidProjectName("springui"));
        assertTrue(NewCommand.isValidProjectName("app123"));
    }

    @Test
    void isValidProjectName_rejectsInvalidNames() {
        assertFalse(NewCommand.isValidProjectName("MyApp"));  // uppercase
        assertFalse(NewCommand.isValidProjectName("-app"));   // starts with hyphen
        assertFalse(NewCommand.isValidProjectName(""));
    }
}