package io.springui.core;

import java.util.Map;

/**
 * RouteMatch — result of matching a URL path against a route pattern.
 */
public class RouteMatch {

    private final RouteDefinition definition;
    private final Map<String, String> params;

    public RouteMatch(RouteDefinition definition, Map<String, String> params) {
        this.definition = definition;
        this.params = params;
    }

    public RouteDefinition getDefinition() { return definition; }
    public Map<String, String> getParams() { return params; }
    public boolean hasParams() { return !params.isEmpty(); }
    public String getParam(String name) { return params.get(name); }
}