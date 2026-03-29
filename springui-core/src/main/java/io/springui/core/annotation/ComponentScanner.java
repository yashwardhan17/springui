package io.springui.core.annotation;

import io.springui.core.ComponentRegistry;
import io.springui.core.UIComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ComponentScanner — auto-discovers and registers @SpringUIComponent classes.
 *
 * Inspired by Spring Boot's @ComponentScan, this scanner:
 * 1. Scans a given package for classes annotated with @SpringUIComponent
 * 2. Processes their annotations via AnnotationProcessor
 * 3. Auto-registers root components into the ComponentRegistry
 * 4. Builds a complete component map for the compiler
 *
 * Usage:
 *   ComponentScanner scanner = new ComponentScanner("io.myapp.components");
 *   ScanResult result = scanner.scan();
 *   result.getRootComponents().forEach(meta -> SpringUI.render(...));
 */
public class ComponentScanner {

    // ===========================
    // Configuration
    // ===========================

    private final String basePackage;
    private final AnnotationProcessor processor;
    private final List<Class<?>> registeredClasses = new ArrayList<>();

    // ===========================
    // Constructor
    // ===========================

    public ComponentScanner(String basePackage) {
        this.basePackage = basePackage;
        this.processor = new AnnotationProcessor();
    }

    // ===========================
    // Register classes manually
    // Phase 1: manual registration
    // Phase 2: real classpath scanning
    // ===========================

    /**
     * Manually registers a class for scanning.
     * Phase 1 approach — in Phase 2, classpath scanning
     * discovers these automatically.
     */
    public ComponentScanner register(Class<?> componentClass) {
        registeredClasses.add(componentClass);
        return this;
    }

    /**
     * Registers multiple classes at once.
     */
    public ComponentScanner registerAll(Class<?>... classes) {
        for (Class<?> c : classes) {
            registeredClasses.add(c);
        }
        return this;
    }

    // ===========================
    // Scan
    // ===========================

    /**
     * Scans all registered classes and returns a ScanResult.
     * Processes annotations, validates components, and
     * auto-mounts root components.
     */
    public ScanResult scan() {
        log("Scanning package: " + basePackage);
        log("Registered classes: " + registeredClasses.size());

        Map<String, ComponentMetadata> components = new HashMap<>();
        List<ComponentMetadata> rootComponents = new ArrayList<>();
        List<ComponentMetadata> bindAPIComponents = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Class<?> clazz : registeredClasses) {
            try {
                // Must be annotated with @SpringUIComponent
                if (!clazz.isAnnotationPresent(SpringUIComponent.class)) {
                    log("Skipping (no @SpringUIComponent): " + clazz.getSimpleName());
                    continue;
                }

                // Must extend UIComponent
                if (!UIComponent.class.isAssignableFrom(clazz)) {
                    errors.add(clazz.getName() +
                            " is annotated with @SpringUIComponent but " +
                            "does not extend UIComponent");
                    continue;
                }

                // Process annotations
                ComponentMetadata metadata = processor.process(clazz);
                components.put(metadata.getComponentId(), metadata);

                // Track root components
                if (metadata.isRootComponent()) {
                    rootComponents.add(metadata);
                    log("Root component found: " + metadata.getComponentId());
                }

                // Track @BindAPI components
                if (metadata.hasBindAPI()) {
                    bindAPIComponents.add(metadata);
                    log("@BindAPI component: " + metadata.getComponentId() +
                            " → " + metadata.getBindAPI().value());
                }

            } catch (Exception e) {
                errors.add("Failed to process " + clazz.getName() +
                        ": " + e.getMessage());
                log("Error processing: " + clazz.getSimpleName() +
                        " — " + e.getMessage());
            }
        }

        ScanResult result = new ScanResult(
                basePackage,
                components,
                rootComponents,
                bindAPIComponents,
                errors
        );

        log("Scan complete:");
        log("  Components found: " + components.size());
        log("  Root components: " + rootComponents.size());
        log("  @BindAPI components: " + bindAPIComponents.size());
        log("  Errors: " + errors.size());

        return result;
    }

    // ===========================
    // Getters
    // ===========================

    public String getBasePackage() { return basePackage; }
    public int getRegisteredClassCount() { return registeredClasses.size(); }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[ComponentScanner] " + message);
    }
}