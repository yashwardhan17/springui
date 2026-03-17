package io.springui.core;

/**
 * SpringUI — the main entry point of the framework.
 * This is the equivalent of ReactDOM.render() in React.
 *
 * Usage:
 *   SpringUI.render("app", new CounterComponent());
 *   SpringUI.unmount("app");
 *   SpringUI.version();
 */
public final class SpringUI {

    // ===========================
    // Version
    // ===========================

    public static final String VERSION = "0.1.0-alpha";

    // ===========================
    // Private constructor — static only
    // ===========================

    private SpringUI() {
        throw new UnsupportedOperationException("SpringUI is a static utility class.");
    }

    // ===========================
    // Core API
    // ===========================

    /**
     * Mounts a component into the virtual DOM at the given ID.
     * Equivalent to ReactDOM.render(<App />, document.getElementById('root'))
     *
     * Usage: SpringUI.render("app", new CounterComponent());
     */
    public static void render(String componentId, UIComponent component) {
        log("Rendering component '" + componentId + "'...");
        ComponentRegistry.getInstance().register(componentId, component);
        log("Component '" + componentId + "' is live.");
    }

    /**
     * Unmounts a component by ID and removes it from the registry.
     * Equivalent to ReactDOM.unmountComponentAtNode()
     *
     * Usage: SpringUI.unmount("app");
     */
    public static void unmount(String componentId) {
        log("Unmounting component '" + componentId + "'...");
        ComponentRegistry.getInstance().unregister(componentId);
        log("Component '" + componentId + "' unmounted.");
    }

    /**
     * Returns true if a component with the given ID is currently mounted.
     */
    public static boolean isMounted(String componentId) {
        return ComponentRegistry.getInstance().isRegistered(componentId);
    }

    /**
     * Returns the number of currently mounted components.
     */
    public static int mountedCount() {
        return ComponentRegistry.getInstance().size();
    }

    /**
     * Returns the SpringUI version string.
     */
    public static String version() {
        return VERSION;
    }

    /**
     * Unmounts all components and resets the framework.
     * Useful for testing and hot reload.
     */
    public static void reset() {
        log("Resetting SpringUI...");
        ComponentRegistry.getInstance().clear();
        log("SpringUI reset complete.");
    }

    // ===========================
    // Logging
    // ===========================

    private static void log(String message) {
        System.out.println("[SpringUI] " + message);
    }
}