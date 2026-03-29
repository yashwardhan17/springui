package io.springui.core.annotation;

import java.util.List;
import java.util.Map;

/**
 * ScanResult — the result of a ComponentScanner.scan() operation.
 * Contains all discovered components, root components,
 * @BindAPI components, and any errors encountered.
 */
public class ScanResult {

    private final String basePackage;
    private final Map<String, ComponentMetadata> components;
    private final List<ComponentMetadata> rootComponents;
    private final List<ComponentMetadata> bindAPIComponents;
    private final List<String> errors;

    public ScanResult(String basePackage,
                      Map<String, ComponentMetadata> components,
                      List<ComponentMetadata> rootComponents,
                      List<ComponentMetadata> bindAPIComponents,
                      List<String> errors) {
        this.basePackage = basePackage;
        this.components = components;
        this.rootComponents = rootComponents;
        this.bindAPIComponents = bindAPIComponents;
        this.errors = errors;
    }

    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasRootComponents() { return !rootComponents.isEmpty(); }
    public boolean hasBindAPIComponents() { return !bindAPIComponents.isEmpty(); }

    public ComponentMetadata getComponent(String id) {
        return components.get(id);
    }

    public int getTotalCount() { return components.size(); }

    public String getBasePackage() { return basePackage; }
    public Map<String, ComponentMetadata> getComponents() { return components; }
    public List<ComponentMetadata> getRootComponents() { return rootComponents; }
    public List<ComponentMetadata> getBindAPIComponents() { return bindAPIComponents; }
    public List<String> getErrors() { return errors; }

    @Override
    public String toString() {
        return "ScanResult{package='" + basePackage +
                "', total=" + getTotalCount() +
                ", root=" + rootComponents.size() +
                ", bindAPI=" + bindAPIComponents.size() +
                ", errors=" + errors.size() + "}";
    }
}