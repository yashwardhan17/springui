package io.springui.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SpringUICli — main entry point for the SpringUI command-line tool.
 *
 * Parses {@code args[]}, routes to the appropriate command handler,
 * and exits with a standard POSIX exit code (0 = success, 1 = failure).
 *
 * <pre>
 * Usage:
 *   springui new &lt;n&gt;                       Scaffold a new project
 *   springui new &lt;n&gt; --template &lt;tpl&gt;     Scaffold from template (blank|todo|counter)
 *   springui build                              Compile Java → WASM → index.html
 *   springui build --entry &lt;class&gt;         Specify entry class
 *   springui build --output &lt;dir&gt;          Specify output directory
 *   springui dev                                Start hot-reload dev server
 *   springui dev --port &lt;n&gt;               Specify WebSocket port
 *   springui dev --no-open                  Don't open browser automatically
 * </pre>
 */
public class SpringUICli {

    // Visible for testing
    final CliPrinter printer;
    final NewCommand  newCommand;
    final BuildCommand buildCommand;
    final DevCommand   devCommand;

    // Production constructor
    public SpringUICli() {
        this.printer      = new CliPrinter();
        this.newCommand   = new NewCommand(new TemplateEngine(), printer);
        this.buildCommand = new BuildCommand(printer);
        this.devCommand   = new DevCommand(printer);
    }

    // Test constructor — allows injecting mocks / simulation modes
    SpringUICli(CliPrinter printer,
                NewCommand newCommand,
                BuildCommand buildCommand,
                DevCommand devCommand) {
        this.printer      = printer;
        this.newCommand   = newCommand;
        this.buildCommand = buildCommand;
        this.devCommand   = devCommand;
    }

    // ─── Entry point ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        int exitCode = new SpringUICli().run(args);
        System.exit(exitCode);
    }

    /**
     * Runs the CLI with the given args.
     *
     * @return POSIX exit code — 0 success, 1 failure
     */
    public int run(String[] args) {
        if (args == null || args.length == 0) {
            printer.banner();
            printer.usage();
            return 0;
        }

        String command = args[0];

        return switch (command.toLowerCase()) {
            case "new"     -> runNew(args);
            case "build"   -> runBuild(args);
            case "dev"     -> runDev(args);
            case "--help", "-h", "help" -> {
                printer.banner();
                printer.usage();
                yield 0;
            }
            case "--version", "-v", "version" -> {
                System.out.println("springui-cli 0.1.0-alpha");
                yield 0;
            }
            default -> {
                printer.error("Unknown command: '" + command + "'");
                printer.usage();
                yield 1;
            }
        };
    }

    // ─── Command: new ────────────────────────────────────────────────────────

    private int runNew(String[] args) {
        if (args.length < 2) {
            printer.error("Missing project name. Usage: springui new <n>");
            return 1;
        }

        String projectName = args[1];
        String template    = NewCommand.DEFAULT_TEMPLATE;

        // Parse --template <name>
        for (int i = 2; i < args.length - 1; i++) {
            if ("--template".equals(args[i]) || "-t".equals(args[i])) {
                template = args[i + 1];
                break;
            }
        }

        Path baseDir = Paths.get(System.getProperty("user.dir"));
        NewCommand.NewResult result = newCommand.execute(projectName, template, baseDir);

        if (!result.success()) {
            printer.error(result.message());
            return 1;
        }
        return 0;
    }

    // ─── Command: build ──────────────────────────────────────────────────────

    private int runBuild(String[] args) {
        String entryClass = null;
        String outputDir  = BuildCommand.DEFAULT_OUTPUT_DIR;

        for (int i = 1; i < args.length - 1; i++) {
            if ("--entry".equals(args[i]) || "-e".equals(args[i])) {
                entryClass = args[i + 1];
            }
            if ("--output".equals(args[i]) || "-o".equals(args[i])) {
                outputDir = args[i + 1];
            }
        }

        if (entryClass == null) {
            // Attempt auto-detection from pom.xml mainClass (future enhancement).
            // For now, require explicit --entry.
            printer.error("Missing --entry. Usage: springui build --entry io.myapp.AppComponent");
            return 1;
        }

        Path projectDir = Paths.get(System.getProperty("user.dir"));
        BuildCommand.BuildResult result = buildCommand.execute(projectDir, entryClass, outputDir);

        if (!result.success()) {
            printer.error(result.message());
            return 1;
        }
        return 0;
    }

    // ─── Command: dev ────────────────────────────────────────────────────────

    private int runDev(String[] args) {
        int     port       = DevCommand.DEFAULT_PORT;
        boolean openBrowser = true;

        for (int i = 1; i < args.length; i++) {
            if (("--port".equals(args[i]) || "-p".equals(args[i])) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    printer.error("Invalid port: '" + args[i + 1] + "'. Must be a number.");
                    return 1;
                }
            }
            if ("--no-open".equals(args[i])) {
                openBrowser = false;
            }
        }

        Path projectDir = Paths.get(System.getProperty("user.dir"));
        DevCommand.DevResult result = devCommand.execute(projectDir, port, openBrowser);

        if (!result.success()) {
            printer.error(result.message());
            return 1;
        }
        return 0;
    }
}