package io.springui.core;

import io.springui.core.annotation.BindAPI;
import io.springui.core.annotation.Props;
import io.springui.core.annotation.SpringUIComponent;
import io.springui.core.annotation.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpringUIContextTest {

    @SpringUIComponent(id = "app", root = true, mountTarget = "root")
    static class AppComponent extends UIComponent {
        @State
        private String title = "SpringUI App";

        @Override
        public VNode render() {
            return VNode.element("div")
                    .attr("id", "app")
                    .child(VNode.text(title));
        }
    }

    @SpringUIComponent(id = "nav", root = true, mountTarget = "nav")
    static class NavComponent extends UIComponent {
        @Props
        private String brand;

        @Override
        public VNode render() {
            return VNode.element("nav");
        }
    }

    @SpringUIComponent(id = "products")
    @BindAPI("/api/products")
    static class ProductComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    // Not a root component — should not auto-mount
    @SpringUIComponent(id = "footer")
    static class FooterComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("footer");
        }
    }

    @BeforeEach
    void setUp() {
        SpringUI.reset();
    }

    @AfterEach
    void tearDown() {
        SpringUI.reset();
    }

    // ===========================
    // Start / Stop
    // ===========================

    @Test
    void shouldStartSuccessfully() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .start();
        assertTrue(context.isStarted());
        context.stop();
    }

    @Test
    void shouldThrowWhenStartedTwice() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .start();
        assertThrows(SpringUIContextException.class, context::start);
        context.stop();
    }

    @Test
    void shouldStopSuccessfully() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .start();
        context.stop();
        assertFalse(context.isStarted());
    }

    // ===========================
    // Component mounting
    // ===========================

    @Test
    void shouldMountRootComponents() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .register(NavComponent.class)
                .start();
        assertEquals(2, context.getMountedCount());
        context.stop();
    }

    @Test
    void shouldNotMountNonRootComponents() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .register(FooterComponent.class)
                .start();
        assertEquals(1, context.getMountedCount());
        context.stop();
    }

    @Test
    void shouldUnmountAllOnStop() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .register(NavComponent.class)
                .start();
        context.stop();
        assertEquals(0, context.getMountedCount());
    }

    @Test
    void shouldTrackMountedComponentIds() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .start();
        assertTrue(context.getMountedComponentIds().contains("app"));
        context.stop();
    }

    // ===========================
    // Scan result
    // ===========================

    @Test
    void shouldProduceScanResult() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .register(ProductComponent.class)
                .start();
        assertNotNull(context.getScanResult());
        assertEquals(2, context.getScanResult().getTotalCount());
        context.stop();
    }

    @Test
    void shouldDetectBindAPIComponents() {
        SpringUIContext context = SpringUIContext.create("io.springui.test")
                .register(AppComponent.class)
                .register(ProductComponent.class)
                .start();
        assertTrue(context.getScanResult().hasBindAPIComponents());
        context.stop();
    }

    // ===========================
    // Config
    // ===========================

    @Test
    void shouldApplyConfig() {
        SpringUIContext context = SpringUIContext.create("io.myapp")
                .register(AppComponent.class)
                .start();
        assertEquals("io.myapp", context.getConfig().getBasePackage());
        context.stop();
    }

    @Test
    void shouldDefaultDevModeToFalse() {
        SpringUIContext context = SpringUIContext.create("io.myapp")
                .register(AppComponent.class)
                .start();
        assertFalse(context.getConfig().isDevMode());
        context.stop();
    }

    @Test
    void shouldEnableDevMode() {
        SpringUIContext context = new SpringUIContext.Builder()
                .basePackage("io.myapp")
                .devMode(true)
                .register(AppComponent.class)
                .build()
                .start();
        assertTrue(context.getConfig().isDevMode());
        context.stop();
    }
}