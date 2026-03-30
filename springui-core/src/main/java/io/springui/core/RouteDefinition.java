package io.springui.core;

/**
 * RouteDefinition — metadata for a single registered route.
 */
public class RouteDefinition {

    private final String path;
    private final Class<? extends UIComponent> componentClass;
    private final String title;
    private final boolean requiresAuth;
    private final String loginPath;
    private final boolean keepAlive;

    public RouteDefinition(String path,
                           Class<? extends UIComponent> componentClass,
                           String title, boolean requiresAuth,
                           String loginPath, boolean keepAlive) {
        this.path = path;
        this.componentClass = componentClass;
        this.title = title;
        this.requiresAuth = requiresAuth;
        this.loginPath = loginPath;
        this.keepAlive = keepAlive;
    }

    public String getPath() { return path; }
    public Class<? extends UIComponent> getComponentClass() { return componentClass; }
    public String getTitle() { return title; }
    public boolean isRequiresAuth() { return requiresAuth; }
    public String getLoginPath() { return loginPath; }
    public boolean isKeepAlive() { return keepAlive; }
}