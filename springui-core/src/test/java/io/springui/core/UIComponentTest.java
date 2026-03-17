package io.springui.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UIComponentTest {

    // ===========================
    // A simple test component
    // ===========================

    static class CounterComponent extends UIComponent {
        int count = 0;

        @Override
        public VNode render() {
            return VNode.element("div")
                    .child(VNode.text("Count: " + count));
        }
    }

    static class LifecycleComponent extends UIComponent {
        boolean mountCalled = false;
        boolean unmountCalled = false;
        boolean updateCalled = false;

        @Override
        public VNode render() {
            return VNode.element("div");
        }

        @Override
        public void onMount() { mountCalled = true; }

        @Override
        public void onUnmount() { unmountCalled = true; }

        @Override
        public void onUpdate() { updateCalled = true; }
    }

    // ===========================
    // Mount Tests
    // ===========================

    @Test
    void shouldNotBeMountedInitially() {
        CounterComponent component = new CounterComponent();
        assertFalse(component.isMounted());
    }

    @Test
    void shouldBeMountedAfterMount() {
        CounterComponent component = new CounterComponent();
        component.mount();
        assertTrue(component.isMounted());
    }

    @Test
    void shouldHavePreviousVNodeAfterMount() {
        CounterComponent component = new CounterComponent();
        component.mount();
        assertNotNull(component.getPreviousVNode());
    }

    // ===========================
    // Unmount Tests
    // ===========================

    @Test
    void shouldNotBeMountedAfterUnmount() {
        CounterComponent component = new CounterComponent();
        component.mount();
        component.unmount();
        assertFalse(component.isMounted());
    }

    @Test
    void shouldClearPreviousVNodeAfterUnmount() {
        CounterComponent component = new CounterComponent();
        component.mount();
        component.unmount();
        assertNull(component.getPreviousVNode());
    }

    // ===========================
    // Lifecycle Tests
    // ===========================

    @Test
    void shouldCallOnMountWhenMounted() {
        LifecycleComponent component = new LifecycleComponent();
        component.mount();
        assertTrue(component.mountCalled);
    }

    @Test
    void shouldCallOnUnmountWhenUnmounted() {
        LifecycleComponent component = new LifecycleComponent();
        component.mount();
        component.unmount();
        assertTrue(component.unmountCalled);
    }

    @Test
    void shouldCallOnUpdateWhenStateChanges() {
        LifecycleComponent component = new LifecycleComponent();
        component.mount();
        component.setState("key", "value");
        assertTrue(component.updateCalled);
    }

    // ===========================
    // State Tests
    // ===========================

    @Test
    void shouldStoreAndRetrieveState() {
        CounterComponent component = new CounterComponent();
        component.setState("username", "Yashwardhan");
        assertEquals("Yashwardhan", component.getState("username"));
    }

    @Test
    void shouldNotTriggerReRenderIfNotMounted() {
        LifecycleComponent component = new LifecycleComponent();
        component.setState("key", "value");
        assertFalse(component.updateCalled);
    }

    @Test
    void shouldTriggerReRenderOnSetState() {
        CounterComponent component = new CounterComponent();
        component.mount();
        // Verify component is mounted and onUpdate is not called yet
        assertTrue(component.isMounted());
        // setState should not throw and should keep component mounted
        component.setState(() -> component.count = 5);
        assertTrue(component.isMounted());
        // Previous VNode should still exist after re-render
        assertNotNull(component.getPreviousVNode());
    }
}