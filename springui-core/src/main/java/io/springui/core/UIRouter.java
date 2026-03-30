package io.springui.core;

import io.springui.core.annotation.Route;
import io.springui.core.annotation.SpringUIRouter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UIRouter — the base class for all SpringUI routers.
 * Extend this class and annotate methods with @Route
 * to define your application's navigation structure.
 *
 * The router:
 * 1. Parses URL paths and matches them to routes
 * 2. Extracts path parameters (:id, :userId etc)
 * 3. Mounts the matched component
 * 4. Handles browser history (pushState / hashchange)
 */
public abstract class UIRouter {

    // ===========================
    // Route registry
    // ===========================

    private final Map<String, RouteDefinition> routes = new LinkedHashMap<>();
    private String currentPath = "/";
    private final List<NavigationListener> listeners = new ArrayList<>();

    // ===========================
    // Initialize
    // ===========================

    /**
     * Initializes the router by scanning @Route methods.
     * Called automatically by SpringUIContext.
     */
    public final void initialize() {
        log("Initializing router: " + getClass().getSimpleName());
        scanRoutes();
        log("Routes registered: " + routes.size());
        routes.forEach((path, def) ->
                log("  " + path + " → " + def.getComponentClass().getSimpleName()));
    }

    // ===========================
    // Route scanning
    // ===========================

    private void scanRoutes() {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Route.class)) {
                Route route = method.getAnnotation(Route.class);
                try {
                    method.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Class<? extends UIComponent> componentClass =
                            (Class<? extends UIComponent>) method.invoke(this);

                    RouteDefinition def = new RouteDefinition(
                            route.value(),
                            componentClass,
                            route.title(),
                            route.requiresAuth(),
                            route.loginPath(),
                            route.keepAlive()
                    );

                    routes.put(route.value(), def);

                } catch (Exception e) {
                    log("Failed to register route: " + route.value() +
                            " — " + e.getMessage());
                }
            }
        }
    }

    // ===========================
    // Navigation
    // ===========================

    /**
     * Navigates to the given path.
     * Matches route, extracts params, mounts component.
     */
    public NavigationResult navigate(String path) {
        log("Navigating to: " + path);

        RouteMatch match = matchRoute(path);

        if (match == null) {
            log("No route matched for: " + path);
            return new NavigationResult(path, false, null, null, "No route matched");
        }

        // Check auth
        if (match.getDefinition().isRequiresAuth() && !isAuthenticated()) {
            String loginPath = match.getDefinition().getLoginPath();
            log("Auth required, redirecting to: " + loginPath);
            return navigate(loginPath);
        }

        String previousPath = currentPath;
        currentPath = path;

        // Notify listeners
        NavigationResult result = new NavigationResult(
                path, true,
                match.getDefinition().getComponentClass(),
                match.getParams(),
                null
        );

        listeners.forEach(l -> l.onNavigate(previousPath, path, result));

        log("✓ Navigated to: " + path +
                " → " + match.getDefinition().getComponentClass().getSimpleName());

        if (!match.getParams().isEmpty()) {
            log("  Path params: " + match.getParams());
        }

        return result;
    }

    /**
     * Goes back to the previous route.
     * Phase 2: uses browser history API.
     */
    public void back() {
        log("Navigating back (browser history)");
    }

    /**
     * Goes forward in route history.
     * Phase 2: uses browser history API.
     */
    public void forward() {
        log("Navigating forward (browser history)");
    }

    // ===========================
    // Route matching
    // ===========================

    /**
     * Matches a URL path against registered routes.
     * Supports path parameters via :paramName syntax.
     */
    public RouteMatch matchRoute(String path) {
        for (Map.Entry<String, RouteDefinition> entry : routes.entrySet()) {
            String pattern = entry.getKey();
            RouteMatch match = tryMatch(pattern, path, entry.getValue());
            if (match != null) return match;
        }
        return null;
    }

    private RouteMatch tryMatch(String pattern, String path,
                                RouteDefinition definition) {
        // Convert route pattern to regex
        // :paramName → named capture group
        String regex = pattern
                .replaceAll(":([a-zA-Z]+)", "(?<$1>[^/]+)")
                .replace("*", ".*");

        Pattern p = Pattern.compile("^" + regex + "$");
        Matcher m = p.matcher(path);

        if (!m.matches()) return null;

        // Extract path parameters
        Map<String, String> params = new HashMap<>();
        List<String> paramNames = extractParamNames(pattern);
        paramNames.forEach(name -> {
            try {
                params.put(name, m.group(name));
            } catch (Exception ignored) {}
        });

        return new RouteMatch(definition, params);
    }

    private List<String> extractParamNames(String pattern) {
        List<String> names = new ArrayList<>();
        Matcher m = Pattern.compile(":([a-zA-Z]+)").matcher(pattern);
        while (m.find()) names.add(m.group(1));
        return names;
    }

    // ===========================
    // Auth hook — override to implement
    // ===========================

    /**
     * Override to implement authentication check.
     * Return true if user is authenticated.
     */
    protected boolean isAuthenticated() {
        return true;
    }

    // ===========================
    // Listeners
    // ===========================

    public void addNavigationListener(NavigationListener listener) {
        listeners.add(listener);
    }

    public void removeNavigationListener(NavigationListener listener) {
        listeners.remove(listener);
    }

    // ===========================
    // Queries
    // ===========================

    public String getCurrentPath() { return currentPath; }
    public Map<String, RouteDefinition> getRoutes() { return routes; }
    public int getRouteCount() { return routes.size(); }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[UIRouter] " + message);
    }

    // ===========================
    // Listener interface
    // ===========================

    @FunctionalInterface
    public interface NavigationListener {
        void onNavigate(String from, String to, NavigationResult result);
    }
}