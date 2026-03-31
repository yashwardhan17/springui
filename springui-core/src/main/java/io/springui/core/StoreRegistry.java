package io.springui.core;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all @SpringUIStore singletons.
 * Thread-safe singleton — mirrors ComponentRegistry.
 *
 * Stores are registered by class type (or explicit name).
 * Components retrieve stores via get(Class<T>).
 */
public class StoreRegistry {

    private static final StoreRegistry INSTANCE = new StoreRegistry();
    private final Map<String, UIStore> stores = new ConcurrentHashMap<>();

    private StoreRegistry() {}

    public static StoreRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a store singleton. Idempotent — re-registering the same key is a no-op.
     */
    public <T extends UIStore> void register(String name, T store) {
        if (stores.containsKey(name)) {
            System.out.println("[SpringUI] StoreRegistry — already registered: " + name);
            return;
        }
        stores.put(name, store);
        System.out.println("[SpringUI] StoreRegistry — registered store: " + name);
    }

    /**
     * Retrieve a store by class type. Returns empty Optional if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends UIStore> Optional<T> get(Class<T> storeClass) {
        return stores.values().stream()
                .filter(s -> storeClass.isInstance(s))
                .map(s -> (T) s)
                .findFirst();
    }

    /**
     * Retrieve a store by name.
     */
    @SuppressWarnings("unchecked")
    public <T extends UIStore> Optional<T> get(String name) {
        return Optional.ofNullable((T) stores.get(name));
    }

    /**
     * Check if a store is registered.
     */
    public boolean isRegistered(String name) {
        return stores.containsKey(name);
    }

    /**
     * Number of registered stores.
     */
    public int size() {
        return stores.size();
    }

    /**
     * All registered stores — useful for DevTools.
     */
    public Collection<UIStore> all() {
        return stores.values();
    }

    /**
     * Destroy all stores and clear the registry. Used on SpringUI.reset().
     */
    public void reset() {
        stores.values().forEach(UIStore::destroy);
        stores.clear();
        System.out.println("[SpringUI] StoreRegistry — reset");
    }
}