package io.springui.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComponentRegistryTest {

    private ComponentRegistry registry;

    // Simple test component
    static class TestComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div").child(VNode.text("Hello SpringUI"));
        }
    }

    @BeforeEach
    void setUp() {
        registry = ComponentRegistry.getInstance();
        registry.clear();
    }

    // ===========================
    // Register Tests
    // ===========================

    @Test
    void shouldRegisterComponent() {
        registry.register("header", new TestComponent());
        assertTrue(registry.isRegistered("header"));
    }

    @Test
    void shouldMountComponentOnRegister() {
        TestComponent component = new TestComponent();
        registry.register("header", component);
        assertTrue(component.isMounted());
    }

    @Test
    void shouldThrowWhenRegisteringDuplicateId() {
        registry.register("header", new TestComponent());
        assertThrows(IllegalStateException.class, () ->
                registry.register("header", new TestComponent()));
    }

    @Test
    void shouldTrackSizeCorrectly() {
        assertEquals(0, registry.size());
        registry.register("comp1", new TestComponent());
        registry.register("comp2", new TestComponent());
        assertEquals(2, registry.size());
    }

    // ===========================
    // Unregister Tests
    // ===========================

    @Test
    void shouldUnregisterComponent() {
        registry.register("header", new TestComponent());
        registry.unregister("header");
        assertFalse(registry.isRegistered("header"));
    }

    @Test
    void shouldUnmountComponentOnUnregister() {
        TestComponent component = new TestComponent();
        registry.register("header", component);
        registry.unregister("header");
        assertFalse(component.isMounted());
    }

    @Test
    void shouldNotThrowWhenUnregisteringUnknownComponent() {
        assertDoesNotThrow(() -> registry.unregister("nonexistent"));
    }

    // ===========================
    // Lookup Tests
    // ===========================

    @Test
    void shouldReturnComponentById() {
        TestComponent component = new TestComponent();
        registry.register("header", component);
        assertSame(component, registry.get("header"));
    }

    @Test
    void shouldReturnNullForUnknownId() {
        assertNull(registry.get("unknown"));
    }

    @Test
    void shouldReturnAllComponents() {
        registry.register("comp1", new TestComponent());
        registry.register("comp2", new TestComponent());
        assertEquals(2, registry.getAll().size());
    }

    // ===========================
    // Clear Tests
    // ===========================

    @Test
    void shouldClearAllComponents() {
        registry.register("comp1", new TestComponent());
        registry.register("comp2", new TestComponent());
        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    void shouldUnmountAllComponentsOnClear() {
        TestComponent c1 = new TestComponent();
        TestComponent c2 = new TestComponent();
        registry.register("comp1", c1);
        registry.register("comp2", c2);
        registry.clear();
        assertFalse(c1.isMounted());
        assertFalse(c2.isMounted());
    }
}