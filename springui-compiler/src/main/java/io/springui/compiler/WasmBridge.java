package io.springui.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * WasmBridge — the runtime bridge between SpringUI's WebAssembly
 * module and the browser environment.
 *
 * Responsibilities:
 * - Manages the lifecycle of the WASM module instance
 * - Exposes WASM exports to the SpringUI runtime
 * - Routes DOM events from JS back into WASM handlers
 * - Tracks registered event callbacks
 *
 * Phase 1 (current): simulates WASM bridge with Java callbacks
 * Phase 2 (browser): replaced by TeaVM's actual JS interop layer
 */
public class WasmBridge {

    // ===========================
    // State
    // ===========================

    private boolean initialized = false;
    private String mountedComponentId = null;

    // Event callback registry — callbackId -> handler
    private final Map<Integer, Consumer<String>> eventCallbacks = new HashMap<>();
    private int nextCallbackId = 1;

    // WASM export simulation — in Phase 2 these call real WASM exports
    private final Map<String, WasmExport> exports = new HashMap<>();

    // ===========================
    // Initialization
    // ===========================

    /**
     * Initializes the WASM bridge.
     * Phase 2: loads and instantiates the .wasm binary.
     */
    public void initialize(String wasmFilePath) {
        if (initialized) {
            throw new BridgeException("WasmBridge is already initialized.");
        }

        log("Initializing WASM bridge...");
        log("Loading: " + wasmFilePath);

        // Phase 2: real WASM loading via TeaVM:
        // byte[] wasmBytes = Files.readAllBytes(Path.of(wasmFilePath));
        // WasmModule module = WasmModule.compile(wasmBytes);
        // this.instance = module.instantiate(buildImports());

        registerCoreExports();
        initialized = true;
        log("✓ WASM bridge initialized.");
    }

    // ===========================
    // Core WASM Exports
    // ===========================

    /**
     * Registers the core WASM exports SpringUI needs.
     * Phase 2: these map to real exported WASM functions.
     */
    private void registerCoreExports() {
        // springui_mount — mounts a component into a DOM element
        exports.put("springui_mount", args -> {
            String componentId = args[0];
            String targetElementId = args[1];
            mountedComponentId = componentId;
            log("WASM export: springui_mount('" + componentId +
                    "', '" + targetElementId + "')");
            return "mounted";
        });

        // springui_unmount — unmounts a component
        exports.put("springui_unmount", args -> {
            String componentId = args[0];
            log("WASM export: springui_unmount('" + componentId + "')");
            if (componentId.equals(mountedComponentId)) {
                mountedComponentId = null;
            }
            return "unmounted";
        });

        // springui_handle_event — routes a DOM event into WASM
        exports.put("springui_handle_event", args -> {
            int callbackId = Integer.parseInt(args[0]);
            String eventData = args.length > 1 ? args[1] : "{}";
            log("WASM export: springui_handle_event(" + callbackId + ")");
            routeEvent(callbackId, eventData);
            return "event_handled";
        });

        // springui_update — triggers a re-render cycle
        exports.put("springui_update", args -> {
            String componentId = args[0];
            log("WASM export: springui_update('" + componentId + "')");
            return "updated";
        });
    }

    // ===========================
    // Call WASM Exports
    // ===========================

    /**
     * Calls a WASM export by name with arguments.
     * Phase 2: calls real WASM function via JS interop.
     */
    public String call(String exportName, String... args) {
        checkInitialized();
        WasmExport export = exports.get(exportName);
        if (export == null) {
            throw new BridgeException("Unknown WASM export: " + exportName);
        }
        return export.call(args);
    }

    /**
     * Returns true if a WASM export with the given name is registered.
     */
    public boolean hasExport(String exportName) {
        return exports.containsKey(exportName);
    }

    // ===========================
    // Event System
    // ===========================

    /**
     * Registers an event callback and returns its ID.
     * The ID is passed to JS addEventListener, which calls
     * springui_handle_event(callbackId) when the event fires.
     */
    public int registerEventCallback(Consumer<String> callback) {
        int id = nextCallbackId++;
        eventCallbacks.put(id, callback);
        log("Registered event callback #" + id);
        return id;
    }

    /**
     * Routes an event to its registered callback.
     */
    private void routeEvent(int callbackId, String eventData) {
        Consumer<String> callback = eventCallbacks.get(callbackId);
        if (callback != null) {
            callback.accept(eventData);
        } else {
            log("Warning: no callback registered for id " + callbackId);
        }
    }

    /**
     * Removes an event callback by ID.
     */
    public void unregisterEventCallback(int callbackId) {
        eventCallbacks.remove(callbackId);
        log("Unregistered event callback #" + callbackId);
    }

    // ===========================
    // State Queries
    // ===========================

    public boolean isInitialized() { return initialized; }
    public String getMountedComponentId() { return mountedComponentId; }
    public int getRegisteredCallbackCount() { return eventCallbacks.size(); }
    public int getExportCount() { return exports.size(); }

    // ===========================
    // Reset — for testing
    // ===========================

    public void reset() {
        initialized = false;
        mountedComponentId = null;
        eventCallbacks.clear();
        exports.clear();
        nextCallbackId = 1;
        log("WasmBridge reset.");
    }

    // ===========================
    // Helpers
    // ===========================

    private void checkInitialized() {
        if (!initialized) {
            throw new BridgeException(
                    "WasmBridge is not initialized. Call initialize() first."
            );
        }
    }

    private void log(String message) {
        System.out.println("[WasmBridge] " + message);
    }

    // ===========================
    // Functional interface for WASM exports
    // ===========================

    @FunctionalInterface
    interface WasmExport {
        String call(String... args);
    }
}