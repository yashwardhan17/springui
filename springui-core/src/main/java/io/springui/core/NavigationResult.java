package io.springui.core;

import java.util.Map;

/**
 * NavigationResult — result of a router.navigate() call.
 */
public class NavigationResult {

    private final String path;
    private final boolean success;
    private final Class<? extends UIComponent> componentClass;
    private final Map<String, String> pathParams;
    private final String errorMessage;

    public NavigationResult(String path, boolean success,
                            Class<? extends UIComponent> componentClass,
                            Map<String, String> pathParams,
                            String errorMessage) {
        this.path = path;
        this.success = success;
        this.componentClass = componentClass;
        this.pathParams = pathParams;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public String getPath() { return path; }
    public Class<? extends UIComponent> getComponentClass() { return componentClass; }
    public Map<String, String> getPathParams() { return pathParams; }
    public String getErrorMessage() { return errorMessage; }
}