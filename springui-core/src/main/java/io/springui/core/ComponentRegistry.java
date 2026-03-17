package io.springui.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ComponentRegistry — the central hub of SpringUI.
 * Manages the lifecycle of all mounted components.
 *
 * Responsibilities:
 * - Register and mount components
 * - Unmount and deregister components
 * - Look up components by ID
 * - Track all active components
 *
 * Think of this as SpringUI's internal "React Root" —
 * the single source of truth for what's mounted in the UI.
 */
public class ComponentRegistry {

    // ===========================
    // Singleton
    // ===========================

    private static final ComponentRegistry INSTANCE = new ComponentRegistry();

    public static ComponentRegistry getInstance() {
        return INSTANCE;
    }

    private ComponentRegistry() {}

    // ===========================
    // Registry — componentId -> UIComponent
    // ===========================

    private final Map<String, UIComponent> registry = new ConcurrentHashMap<>();

    // ===========================
    // Register and Mount
    // ===========================

    /**
     * Registers a component with a given ID and mounts it.
     * Throws if a component with the same ID is already registered.
     */
    public void register(String componentId, UIComponent component) {
        if (registry.containsKey(componentId)) {
            throw new IllegalStateException(
                    "Component with id '" + componentId + "' is already registered. " +
                            "Use a unique component ID."
            );
        }
        registry.put(componentId, component);
        component.mount();
        log("Registered and mounted component: " + componentId);
    }

    // ===========================
    // Unregister and Unmount
    // ===========================

    /**
     * Unmounts and removes a component by ID.
     * Silently ignores if component not found.
     */
    public void unregister(String componentId) {
        UIComponent component = registry.remove(componentId);
        if (component != null) {
            component.unmount();
            log("Unregistered and unmounted component: " + componentId);
        } else {
            log("Attempted to unregister unknown component: " + componentId);
        }
    }

    // ===========================
    // Lookup
    // ===========================

    /**
     * Returns a component by ID, or null if not found.
     */
    public UIComponent get(String componentId) {
        return registry.get(componentId);
    }

    /**
     * Returns true if a component with the given ID is registered.
     */
    public boolean isRegistered(String componentId) {
        return registry.containsKey(componentId);
    }

    /**
     * Returns all currently registered components.
     */
    public Collection<UIComponent> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    /**
     * Returns the number of currently mounted components.
     */
    public int size() {
        return registry.size();
    }

    // ===========================
    // Clear — useful for testing
    // ===========================

    /**
     * Unmounts and removes all registered components.
     * Primarily used in tests to reset state between runs.
     */
    public void clear() {
        registry.forEach((id, component) -> component.unmount());
        registry.clear();
        log("Registry cleared.");
    }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[ComponentRegistry] " + message);
    }
}