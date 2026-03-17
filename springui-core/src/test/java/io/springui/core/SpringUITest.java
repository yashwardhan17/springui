package io.springui.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpringUITest {

    static class AppComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div")
                    .attr("id", "app")
                    .child(VNode.text("Hello from SpringUI!"));
        }
    }

    @BeforeEach
    void setUp() {
        SpringUI.reset();
    }

    @Test
    void shouldReturnCorrectVersion() {
        assertEquals("0.1.0-alpha", SpringUI.version());
    }

    @Test
    void shouldRenderComponent() {
        SpringUI.render("app", new AppComponent());
        assertTrue(SpringUI.isMounted("app"));
    }

    @Test
    void shouldTrackMountedCount() {
        assertEquals(0, SpringUI.mountedCount());
        SpringUI.render("app", new AppComponent());
        assertEquals(1, SpringUI.mountedCount());
        SpringUI.render("header", new AppComponent());
        assertEquals(2, SpringUI.mountedCount());
    }

    @Test
    void shouldUnmountComponent() {
        SpringUI.render("app", new AppComponent());
        SpringUI.unmount("app");
        assertFalse(SpringUI.isMounted("app"));
    }

    @Test
    void shouldReturnFalseForUnmountedComponent() {
        assertFalse(SpringUI.isMounted("nonexistent"));
    }

    @Test
    void shouldResetAllComponents() {
        SpringUI.render("app", new AppComponent());
        SpringUI.render("header", new AppComponent());
        SpringUI.reset();
        assertEquals(0, SpringUI.mountedCount());
    }

    @Test
    void shouldThrowWhenRenderingDuplicateId() {
        SpringUI.render("app", new AppComponent());
        assertThrows(IllegalStateException.class, () ->
                SpringUI.render("app", new AppComponent()));
    }
}