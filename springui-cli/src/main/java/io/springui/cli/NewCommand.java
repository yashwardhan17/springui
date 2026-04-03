package io.springui.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * NewCommand — handles {@code springui new <name> [--template <tpl>]}.
 *
 * Scaffolds a fully-working SpringUI project in a new directory:
 * <pre>
 *   my-app/
 *   ├── pom.xml
 *   ├── src/
 *   │   └── main/
 *   │       ├── java/
 *   │       │   └── io/myapp/
 *   │       │       ├── MyAppApplication.java
 *   │       │       └── AppComponent.java         ← chosen template
 *   │       └── resources/
 *   │           └── application.yml
 *   └── .gitignore
 * </pre>
 */
public class NewCommand {

    static final String DEFAULT_TEMPLATE = "blank";

    private final TemplateEngine templateEngine;
    private final CliPrinter printer;

    public NewCommand(TemplateEngine templateEngine, CliPrinter printer) {
        this.templateEngine = templateEngine;
        this.printer        = printer;
    }

    // ─── Result ──────────────────────────────────────────────────────────────

    public record NewResult(
            boolean success,
            Path projectDir,
            String message
    ) {
        public static NewResult ok(Path dir) {
            return new NewResult(true, dir, "Project created at " + dir);
        }
        public static NewResult fail(String reason) {
            return new NewResult(false, null, reason);
        }
    }

    // ─── Execute ─────────────────────────────────────────────────────────────

    /**
     * Scaffolds a new project.
     *
     * @param projectName  the directory/artifact name (e.g. "my-app")
     * @param templateName template to use (blank | todo | counter)
     * @param baseDir      parent directory where the project folder is created
     * @return result describing success or failure
     */
    public NewResult execute(String projectName, String templateName, Path baseDir) {

        // ── Validation ────────────────────────────────────────────────────────
        if (projectName == null || projectName.isBlank()) {
            return NewResult.fail("Project name cannot be blank. Usage: springui new <name>");
        }
        if (!isValidProjectName(projectName)) {
            return NewResult.fail(
                    "Invalid project name '" + projectName +
                            "'. Use lowercase letters, digits, and hyphens only.");
        }
        if (!templateEngine.isValidTemplate(templateName)) {
            return NewResult.fail(
                    "Unknown template '" + templateName +
                            "'. Available templates: blank, todo, counter");
        }

        Path projectDir = baseDir.resolve(projectName);

        if (Files.exists(projectDir)) {
            return NewResult.fail(
                    "Directory '" + projectDir + "' already exists. " +
                            "Choose a different name or delete the existing directory.");
        }

        // ── Derive names ──────────────────────────────────────────────────────
        String artifactId      = projectName.toLowerCase();
        String groupId         = "io." + sanitize(projectName);
        String packageName     = groupId;
        String componentClass  = toClassName(projectName) + "Component";
        String appClass        = toClassName(projectName) + "Application";

        TemplateEngine.Template template = TemplateEngine.Template.fromString(templateName);

        // ── Scaffold ──────────────────────────────────────────────────────────
        try {
            printer.header("Scaffolding " + projectName);

            // Directory skeleton
            Path javaRoot = projectDir
                    .resolve("src/main/java")
                    .resolve(packageName.replace('.', '/'));
            Path resourcesRoot = projectDir.resolve("src/main/resources");

            Files.createDirectories(javaRoot);
            Files.createDirectories(resourcesRoot);
            Files.createDirectories(projectDir.resolve("src/test/java")
                    .resolve(packageName.replace('.', '/')));

            printer.step("create", "src/ directory tree");

            // pom.xml
            write(projectDir.resolve("pom.xml"),
                    templateEngine.pomXml(groupId, artifactId, projectName));
            printer.step("create", "pom.xml");

            // Main application class
            write(javaRoot.resolve(appClass + ".java"),
                    templateEngine.mainClass(packageName, appClass, componentClass));
            printer.step("create", appClass + ".java");

            // Component from template
            write(javaRoot.resolve(componentClass + ".java"),
                    templateEngine.componentSource(template, packageName, componentClass));
            printer.step("create", componentClass + ".java  [" + templateName + " template]");

            // application.yml
            write(resourcesRoot.resolve("application.yml"),
                    templateEngine.applicationYml(projectName));
            printer.step("create", "application.yml");

            // .gitignore
            write(projectDir.resolve(".gitignore"), GITIGNORE);
            printer.step("create", ".gitignore");

            printer.scaffoldDone(projectName);
            return NewResult.ok(projectDir);

        } catch (IOException e) {
            return NewResult.fail("I/O error during scaffolding: " + e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** "my-app" → "MyApp" */
    static String toClassName(String projectName) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : projectName.toCharArray()) {
            if (c == '-' || c == '_') {
                capitalizeNext = true;
            } else {
                sb.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        return sb.toString();
    }

    /** "my-app" → "myapp" (safe Java identifier for package suffix) */
    static String sanitize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    static boolean isValidProjectName(String name) {
        return name.matches("[a-z][a-z0-9\\-]*");
    }

    private void write(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    // ─── .gitignore ──────────────────────────────────────────────────────────

    private static final String GITIGNORE = """
            target/
            springui-out/
            *.class
            *.jar
            .idea/
            *.iml
            .DS_Store
            """;
}