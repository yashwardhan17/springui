package io.springui.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * TemplateEngine — renders scaffold templates for the {@code new} command.
 *
 * Templates are simple strings with {@code {{key}}} placeholders.
 * No external dependency — keeps the CLI self-contained.
 *
 * Built-in templates:
 *   blank   — minimal project with one empty component
 *   todo    — full todo-app demo (mirrors examples/todo-app)
 *   counter — counter component showcasing @State
 */
public class TemplateEngine {

    public enum Template {
        BLANK("blank"),
        TODO("todo"),
        COUNTER("counter");

        private final String id;

        Template(String id) { this.id = id; }

        public String getId() { return id; }

        public static Template fromString(String s) {
            for (Template t : values()) {
                if (t.id.equalsIgnoreCase(s)) return t;
            }
            throw new IllegalArgumentException("Unknown template: '" + s +
                    "'. Available: blank, todo, counter");
        }
    }

    // ─── Render ──────────────────────────────────────────────────────────────

    /**
     * Renders a named template string with the given variables.
     *
     * @param templateContent  raw template with {{key}} placeholders
     * @param variables        map of key → value substitutions
     * @return rendered string
     */
    public String render(String templateContent, Map<String, String> variables) {
        String result = templateContent;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    // ─── pom.xml template ────────────────────────────────────────────────────

    public String pomXml(String groupId, String artifactId, String projectName) {
        Map<String, String> vars = new HashMap<>();
        vars.put("groupId",     groupId);
        vars.put("artifactId",  artifactId);
        vars.put("projectName", projectName);
        return render(POM_TEMPLATE, vars);
    }

    // ─── Component templates ─────────────────────────────────────────────────

    public String componentSource(Template template, String packageName, String className) {
        Map<String, String> vars = new HashMap<>();
        vars.put("package",   packageName);
        vars.put("className", className);
        String raw = switch (template) {
            case TODO    -> TODO_COMPONENT_TEMPLATE;
            case COUNTER -> COUNTER_COMPONENT_TEMPLATE;
            default      -> BLANK_COMPONENT_TEMPLATE;
        };
        return render(raw, vars);
    }

    public String mainClass(String packageName, String appClassName, String componentClassName) {
        Map<String, String> vars = new HashMap<>();
        vars.put("package",             packageName);
        vars.put("appClassName",        appClassName);
        vars.put("componentClassName",  componentClassName);
        return render(MAIN_CLASS_TEMPLATE, vars);
    }

    public String applicationYml(String projectName) {
        Map<String, String> vars = new HashMap<>();
        vars.put("projectName", projectName);
        return render(APPLICATION_YML_TEMPLATE, vars);
    }

    // ─── Validate template name ───────────────────────────────────────────────

    public boolean isValidTemplate(String name) {
        try {
            Template.fromString(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ─── Raw templates ────────────────────────────────────────────────────────

    private static final String POM_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">

                <modelVersion>4.0.0</modelVersion>

                <groupId>{{groupId}}</groupId>
                <artifactId>{{artifactId}}</artifactId>
                <version>0.1.0-SNAPSHOT</version>
                <packaging>jar</packaging>

                <name>{{projectName}}</name>

                <properties>
                    <java.version>21</java.version>
                    <maven.compiler.source>21</maven.compiler.source>
                    <maven.compiler.target>21</maven.compiler.target>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <springui.version>0.1.0-alpha</springui.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>io.springui</groupId>
                        <artifactId>springui-core</artifactId>
                        <version>${springui.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>io.springui</groupId>
                        <artifactId>springui-compiler</artifactId>
                        <version>${springui.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter</artifactId>
                        <version>5.10.2</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.12.1</version>
                        </plugin>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.2.5</version>
                        </plugin>
                    </plugins>
                </build>

            </project>
            """;

    private static final String BLANK_COMPONENT_TEMPLATE = """
            package {{package}};

            import io.springui.core.UIComponent;
            import io.springui.core.VNode;
            import io.springui.core.VNodeBuilder;
            import io.springui.core.annotation.SpringUIComponent;
            import io.springui.core.annotation.State;

            @SpringUIComponent(id = "app", root = true, mountTarget = "root")
            public class {{className}} extends UIComponent {

                @Override
                public VNode render() {
                    return div(
                        h1("Hello from SpringUI!"),
                        p("Edit {{className}}.java and run springui dev to get started.")
                    );
                }
            }
            """;

    private static final String COUNTER_COMPONENT_TEMPLATE = """
            package {{package}};

            import io.springui.core.UIComponent;
            import io.springui.core.VNode;
            import io.springui.core.annotation.SpringUIComponent;
            import io.springui.core.annotation.State;

            @SpringUIComponent(id = "counter", root = true, mountTarget = "root")
            public class {{className}} extends UIComponent {

                @State
                private int count = 0;

                @Override
                public VNode render() {
                    return div(
                        h1("Count: " + count),
                        button("Increment").onClick(e -> setState(() -> count++)),
                        button("Decrement").onClick(e -> setState(() -> count--)),
                        button("Reset").onClick(e -> setState(() -> count = 0))
                    );
                }
            }
            """;

    private static final String TODO_COMPONENT_TEMPLATE = """
            package {{package}};

            import io.springui.core.UIComponent;
            import io.springui.core.VNode;
            import io.springui.core.annotation.SpringUIComponent;
            import io.springui.core.annotation.State;
            import java.util.ArrayList;
            import java.util.List;

            @SpringUIComponent(id = "todo", root = true, mountTarget = "root")
            public class {{className}} extends UIComponent {

                @State
                private List<String> todos = new ArrayList<>();

                @State
                private String input = "";

                @Override
                public VNode render() {
                    return div(
                        h1("Todo App"),
                        div(
                            input(attrs("placeholder", "What needs doing?", "value", input))
                                .onInput(e -> setState(() -> input = e.getValue())),
                            button("Add").onClick(e -> {
                                if (!input.isBlank()) {
                                    setState(() -> {
                                        todos.add(input);
                                        input = "";
                                    });
                                }
                            })
                        ),
                        ul(todos.stream().map(todo -> li(todo)).toList()),
                        p(todos.size() + " item(s)")
                    );
                }
            }
            """;

    private static final String MAIN_CLASS_TEMPLATE = """
            package {{package}};

            import io.springui.core.SpringUIContext;

            public class {{appClassName}} {

                public static void main(String[] args) {
                    SpringUIContext.create("{{package}}")
                        .register({{componentClassName}}.class)
                        .devMode(true)
                        .start();
                }
            }
            """;

    private static final String APPLICATION_YML_TEMPLATE = """
            springui:
              output-dir: src/main/resources/static
              hot-reload: true
              wasm-target: browser

            # {{projectName}} — powered by SpringUI
            """;
}