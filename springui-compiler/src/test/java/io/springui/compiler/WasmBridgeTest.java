package io.springui.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class WasmBridgeTest {

    private WasmBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new WasmBridge();
        bridge.initialize("test-component.wasm");
    }

    // ===========================
    // Initialization
    // ===========================

    @Test
    void shouldBeInitializedAfterInit() {
        assertTrue(bridge.isInitialized());
    }

    @Test
    void shouldThrowWhenInitializedTwice() {
        assertThrows(BridgeException.class, () ->
                bridge.initialize("test-component.wasm"));
    }

    @Test
    void shouldThrowWhenCallingExportBeforeInit() {
        WasmBridge uninitializedBridge = new WasmBridge();
        assertThrows(BridgeException.class, () ->
                uninitializedBridge.call("springui_mount", "app", "root"));
    }

    // ===========================
    // Core Exports
    // ===========================

    @Test
    void shouldHaveCoreExports() {
        assertTrue(bridge.hasExport("springui_mount"));
        assertTrue(bridge.hasExport("springui_unmount"));
        assertTrue(bridge.hasExport("springui_handle_event"));
        assertTrue(bridge.hasExport("springui_update"));
    }

    @Test
    void shouldHaveFourCoreExports() {
        assertEquals(4, bridge.getExportCount());
    }

    @Test
    void shouldMountComponent() {
        bridge.call("springui_mount", "app", "root");
        assertEquals("app", bridge.getMountedComponentId());
    }

    @Test
    void shouldUnmountComponent() {
        bridge.call("springui_mount", "app", "root");
        bridge.call("springui_unmount", "app");
        assertNull(bridge.getMountedComponentId());
    }

    @Test
    void shouldThrowForUnknownExport() {
        assertThrows(BridgeException.class, () ->
                bridge.call("unknown_export"));
    }

    // ===========================
    // Event System
    // ===========================

    @Test
    void shouldRegisterEventCallback() {
        int id = bridge.registerEventCallback(e -> {});
        assertEquals(1, id);
        assertEquals(1, bridge.getRegisteredCallbackCount());
    }

    @Test
    void shouldIncrementCallbackIds() {
        int id1 = bridge.registerEventCallback(e -> {});
        int id2 = bridge.registerEventCallback(e -> {});
        assertEquals(1, id1);
        assertEquals(2, id2);
    }

    @Test
    void shouldRouteEventToCallback() {
        AtomicBoolean called = new AtomicBoolean(false);
        int id = bridge.registerEventCallback(e -> called.set(true));
        bridge.call("springui_handle_event", String.valueOf(id));
        assertTrue(called.get());
    }

    @Test
    void shouldPassEventDataToCallback() {
        AtomicReference<String> received = new AtomicReference<>();
        int id = bridge.registerEventCallback(received::set);
        bridge.call("springui_handle_event", String.valueOf(id), "{type:'click'}");
        assertEquals("{type:'click'}", received.get());
    }

    @Test
    void shouldUnregisterEventCallback() {
        int id = bridge.registerEventCallback(e -> {});
        bridge.unregisterEventCallback(id);
        assertEquals(0, bridge.getRegisteredCallbackCount());
    }

    // ===========================
    // Reset
    // ===========================

    @Test
    void shouldResetCompletely() {
        bridge.call("springui_mount", "app", "root");
        bridge.registerEventCallback(e -> {});
        bridge.reset();
        assertFalse(bridge.isInitialized());
        assertNull(bridge.getMountedComponentId());
        assertEquals(0, bridge.getRegisteredCallbackCount());
        assertEquals(0, bridge.getExportCount());
    }
}