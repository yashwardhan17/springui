package io.springui.core.annotation;

import io.springui.core.UIComponent;
import io.springui.core.VNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComponentScannerTest {

    // ===========================
    // Test components
    // ===========================

    @SpringUIComponent(id = "header", root = true, mountTarget = "header")
    static class HeaderComponent extends UIComponent {
        @State
        private String title = "SpringUI";

        @Override
        public VNode render() {
            return VNode.element("header")
                    .child(VNode.text(title));
        }
    }

    @SpringUIComponent(id = "footer")
    static class FooterComponent extends UIComponent {
        @Props
        private String copyright;

        @Override
        public VNode render() {
            return VNode.element("footer");
        }
    }

    @SpringUIComponent(id = "products", root = true)
    @BindAPI("/api/products")
    static class ProductComponent extends UIComponent {
        @AutoFetched
        private Object products;

        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    // Not annotated — should be skipped
    static class PlainComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    // Annotated but not UIComponent — should produce error
    @SpringUIComponent
    static class InvalidComponent {
        public void render() {}
    }

    private ComponentScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new ComponentScanner("io.springui.test");
    }

    // ===========================
    // Registration
    // ===========================

    @Test
    void shouldRegisterClass() {
        scanner.register(HeaderComponent.class);
        assertEquals(1, scanner.getRegisteredClassCount());
    }

    @Test
    void shouldRegisterMultipleClasses() {
        scanner.registerAll(HeaderComponent.class,
                FooterComponent.class,
                ProductComponent.class);
        assertEquals(3, scanner.getRegisteredClassCount());
    }

    @Test
    void shouldSupportFluentRegistration() {
        ComponentScanner result = scanner
                .register(HeaderComponent.class)
                .register(FooterComponent.class);
        assertSame(scanner, result);
        assertEquals(2, scanner.getRegisteredClassCount());
    }

    // ===========================
    // Scan results
    // ===========================

    @Test
    void shouldScanAndFindAnnotatedComponents() {
        scanner.registerAll(HeaderComponent.class,
                FooterComponent.class,
                ProductComponent.class);
        ScanResult result = scanner.scan();
        assertEquals(3, result.getTotalCount());
    }

    @Test
    void shouldSkipUnannotatedComponents() {
        scanner.registerAll(HeaderComponent.class, PlainComponent.class);
        ScanResult result = scanner.scan();
        assertEquals(1, result.getTotalCount());
    }

    @Test
    void shouldFindRootComponents() {
        scanner.registerAll(HeaderComponent.class,
                FooterComponent.class,
                ProductComponent.class);
        ScanResult result = scanner.scan();
        assertEquals(2, result.getRootComponents().size());
    }

    @Test
    void shouldFindBindAPIComponents() {
        scanner.registerAll(HeaderComponent.class,
                FooterComponent.class,
                ProductComponent.class);
        ScanResult result = scanner.scan();
        assertEquals(1, result.getBindAPIComponents().size());
        assertEquals("/api/products",
                result.getBindAPIComponents().get(0).getBindAPI().value());
    }

    @Test
    void shouldGetComponentById() {
        scanner.register(HeaderComponent.class);
        ScanResult result = scanner.scan();
        assertNotNull(result.getComponent("header"));
        assertEquals("header", result.getComponent("header").getComponentId());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        ScanResult result = scanner.scan();
        assertNull(result.getComponent("unknown"));
    }

    // ===========================
    // Error handling
    // ===========================

    @Test
    void shouldRecordErrorForInvalidComponent() {
        scanner.register(InvalidComponent.class);
        ScanResult result = scanner.scan();
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void shouldContinueScanningAfterError() {
        scanner.registerAll(InvalidComponent.class, HeaderComponent.class);
        ScanResult result = scanner.scan();
        assertTrue(result.hasErrors());
        assertEquals(1, result.getTotalCount());
    }

    @Test
    void shouldHaveNoErrorsForValidComponents() {
        scanner.registerAll(HeaderComponent.class, FooterComponent.class);
        ScanResult result = scanner.scan();
        assertFalse(result.hasErrors());
    }

    // ===========================
    // ScanResult
    // ===========================

    @Test
    void shouldReportBasePackage() {
        ScanResult result = scanner.scan();
        assertEquals("io.springui.test", result.getBasePackage());
    }

    @Test
    void shouldHaveNoRootComponentsWhenNoneRegistered() {
        ScanResult result = scanner.scan();
        assertFalse(result.hasRootComponents());
    }
}