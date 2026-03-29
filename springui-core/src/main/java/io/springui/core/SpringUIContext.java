package io.springui.core;

import io.springui.core.annotation.BindAPI;
import io.springui.core.annotation.ComponentMetadata;
import io.springui.core.annotation.ComponentScanner;
import io.springui.core.annotation.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringUIContext — the application context for SpringUI.
 * The single entry point that bootstraps the entire framework.
 *
 * Ties together:
 * - ComponentScanner — discovers @SpringUIComponent classes
 * - ComponentRegistry — manages mounted components
 * - SpringUI entry point — renders root components
 *
 * Usage — bootstrap an entire SpringUI app in 3 lines:
 *
 *   SpringUIContext context = SpringUIContext.create("io.myapp")
 *       .register(CounterComponent.class)
 *       .register(ProductList.class)
 *       .start();
 *
 * Or with full config:
 *
 *   SpringUIContext context = new SpringUIContext.Builder()
 *       .basePackage("io.myapp")
 *       .devMode(true)
 *       .register(CounterComponent.class)
 *       .build()
 *       .start();
 */
public class SpringUIContext {

    // ===========================
    // State
    // ===========================

    private final ContextConfig config;
    private final ComponentScanner scanner;
    private ScanResult scanResult;
    private boolean started = false;
    private final List<String> mountedComponentIds = new ArrayList<>();

    // ===========================
    // Constructor
    // ===========================

    private SpringUIContext(Builder builder) {
        this.config = builder.buildConfig();
        this.scanner = new ComponentScanner(config.getBasePackage());
        builder.registeredClasses.forEach(scanner::register);
    }

    // ===========================
    // Static factory
    // ===========================

    /**
     * Creates a SpringUIContext for the given base package.
     * Returns a Builder for fluent configuration.
     */
    public static Builder create(String basePackage) {
        return new Builder().basePackage(basePackage);
    }

    // ===========================
    // Start
    // ===========================

    /**
     * Starts the SpringUI application context.
     * 1. Scans for @SpringUIComponent classes
     * 2. Validates all components
     * 3. Mounts all root components
     * 4. Logs startup summary
     */
    public SpringUIContext start() {
        if (started) {
            throw new SpringUIContextException(
                    "SpringUIContext is already started.");
        }

        log("===========================================");
        log("  Starting SpringUI v" + SpringUI.version());
        log("  Base package: " + config.getBasePackage());
        log("  Dev mode: " + config.isDevMode());
        log("===========================================");

        // Step 1 — Scan
        log("Scanning for components...");
        scanResult = scanner.scan();

        // Step 2 — Check for errors
        if (scanResult.hasErrors()) {
            scanResult.getErrors().forEach(e -> log("  ERROR: " + e));
            if (config.isFailOnError()) {
                throw new SpringUIContextException(
                        "Component scan failed with " +
                                scanResult.getErrors().size() + " error(s). " +
                                "Set failOnError=false to ignore.");
            }
        }

        // Step 3 — Mount root components
        log("Mounting root components...");
        for (ComponentMetadata meta : scanResult.getRootComponents()) {
            try {
                UIComponent instance =
                        (UIComponent) meta.getComponentClass()
                                .getDeclaredConstructor()
                                .newInstance();

                SpringUI.render(meta.getComponentId(), instance);
                mountedComponentIds.add(meta.getComponentId());
                log("  ✓ Mounted: " + meta.getComponentId() +
                        " → #" + meta.getComponentAnnotation().mountTarget());

            } catch (Exception e) {
                log("  ✗ Failed to mount: " + meta.getComponentId() +
                        " — " + e.getMessage());
            }
        }

        // Step 4 — Log @BindAPI components
        if (scanResult.hasBindAPIComponents()) {
            log("@BindAPI endpoints:");
            for (ComponentMetadata meta : scanResult.getBindAPIComponents()) {
                BindAPI bindAPI = meta.getBindAPI();
                log("  " + meta.getComponentId() +
                        " → " + bindAPI.method() + " " + bindAPI.value());
            }
        }

        started = true;

        log("===========================================");
        log("  SpringUI started successfully!");
        log("  Components mounted: " + mountedComponentIds.size());
        log("  Total components: " + scanResult.getTotalCount());
        log("===========================================");

        return this;
    }

    // ===========================
    // Stop
    // ===========================

    /**
     * Stops the SpringUI context and unmounts all components.
     */
    public void stop() {
        if (!started) return;
        log("Stopping SpringUI context...");
        mountedComponentIds.forEach(SpringUI::unmount);
        mountedComponentIds.clear();
        started = false;
        log("SpringUI context stopped.");
    }

    // ===========================
    // Queries
    // ===========================

    public boolean isStarted() { return started; }
    public ScanResult getScanResult() { return scanResult; }
    public List<String> getMountedComponentIds() { return mountedComponentIds; }
    public ContextConfig getConfig() { return config; }
    public int getMountedCount() { return mountedComponentIds.size(); }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[SpringUIContext] " + message);
    }

    // ===========================
    // Builder
    // ===========================

    public static class Builder {
        private final ContextConfig.Builder configBuilder =
                new ContextConfig.Builder();
        private final List<Class<?>> registeredClasses = new ArrayList<>();

        public Builder basePackage(String basePackage) {
            configBuilder.basePackage(basePackage);
            return this;
        }

        public Builder devMode(boolean devMode) {
            configBuilder.devMode(devMode);
            return this;
        }

        public Builder failOnError(boolean failOnError) {
            configBuilder.failOnError(failOnError);
            return this;
        }

        public Builder register(Class<?> componentClass) {
            registeredClasses.add(componentClass);
            return this;
        }

        public SpringUIContext build() {
            return new SpringUIContext(this);
        }

        public SpringUIContext start() {
            return build().start();
        }

        ContextConfig buildConfig() {
            return configBuilder.build();
        }
    }
}