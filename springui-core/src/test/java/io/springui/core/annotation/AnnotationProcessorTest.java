package io.springui.core.annotation;

import io.springui.core.UIComponent;
import io.springui.core.VNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotationProcessorTest {

    // ===========================
    // Test components
    // ===========================

    @SpringUIComponent(id = "counter", displayName = "Counter", root = true)
    static class FullyAnnotatedComponent extends UIComponent {
        @State
        private int count = 0;

        @State(persistent = true, name = "username")
        private String username = "";

        @Props(required = true)
        private String title;

        @Props(defaultValue = "default")
        private String subtitle;

        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    @SpringUIComponent
    @BindAPI(value = "/api/products",
            method = BindAPI.HttpMethod.GET,
            queryParams = {"page", "size"})
    static class ProductComponent extends UIComponent {
        @AutoFetched(showLoading = true)
        private Object products;

        @State
        private int page = 0;

        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    static class UnannotatedComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    static class NotAComponent {
        public void render() {}
    }

    private AnnotationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AnnotationProcessor();
    }

    // ===========================
    // @SpringUIComponent
    // ===========================

    @Test
    void shouldProcessComponentId() {
        ComponentMetadata meta = processor.process(FullyAnnotatedComponent.class);
        assertEquals("counter", meta.getComponentId());
    }

    @Test
    void shouldProcessDisplayName() {
        ComponentMetadata meta = processor.process(FullyAnnotatedComponent.class);
        assertEquals("Counter", meta.getDisplayName());
    }

    @Test
    void shouldDefaultIdToClassNameLowercase() {
        ComponentMetadata meta = processor.process(UnannotatedComponent.class);
        assertEquals("unannotatedcomponent", meta.getComponentId());
    }

    @Test
    void shouldDetectRootComponent() {
        ComponentMetadata meta = processor.process(FullyAnnotatedComponent.class);
        assertTrue(meta.isRootComponent());
    }

    @Test
    void shouldThrowForNonUIComponent() {
        assertThrows(AnnotationProcessingException.class, () ->
                processor.process(NotAComponent.class));
    }

    // ===========================
    // @State
    // ===========================

    @Test
    void shouldDetectStateFields() {
        ComponentMetadata meta = processor.process(FullyAnnotatedComponent.class);
        assertTrue(meta.hasStateFields());
        assertEquals(2, meta.getStateFields().size());
    }

    @Test
    void shouldDetectPersistentState() {
        ComponentMetadata meta = processor.process(FullyAnnotatedComponent.class);
        StateFieldMetadata persistent = meta.getStateFields().stream()
                .filter(StateFieldMetadata::isPersistent)
                .findFirst().orElse(null);
        assertNotNull(persistent);
        assertEquals("username", persistent.getDisplayName());
    }

    // ===========================
    // @Props
    // ===========================

    @Test
    void shouldDetectPropsFields() {
        ComponentMetadata meta = processor.process(FullyAnnotatedComponent.class);
        assertTrue(meta.hasPropsFields());
        assertEquals(2, meta.getPropsFields().size());
    }

    @Test
    void shouldDetectRequiredProps() {
        ComponentMetadata meta = processor.process(FullyAnnotatedComponent.class);
        PropsFieldMetadata required = meta.getPropsFields().stream()
                .filter(PropsFieldMetadata::isRequired)
                .findFirst().orElse(null);
        assertNotNull(required);
        assertEquals("title", required.getFieldName());
    }

    // ===========================
    // @BindAPI
    // ===========================

    @Test
    void shouldDetectBindAPI() {
        ComponentMetadata meta = processor.process(ProductComponent.class);
        assertTrue(meta.hasBindAPI());
        assertEquals("/api/products", meta.getBindAPI().value());
    }

    @Test
    void shouldDetectBindAPIMethod() {
        ComponentMetadata meta = processor.process(ProductComponent.class);
        assertEquals(BindAPI.HttpMethod.GET, meta.getBindAPI().method());
    }

    @Test
    void shouldDetectBindAPIQueryParams() {
        ComponentMetadata meta = processor.process(ProductComponent.class);
        assertArrayEquals(new String[]{"page", "size"},
                meta.getBindAPI().queryParams());
    }

    @Test
    void shouldDetectNoBindAPIWhenAbsent() {
        ComponentMetadata meta = processor.process(UnannotatedComponent.class);
        assertFalse(meta.hasBindAPI());
    }

    // ===========================
    // @AutoFetched
    // ===========================

    @Test
    void shouldDetectAutoFetchedFields() {
        ComponentMetadata meta = processor.process(ProductComponent.class);
        assertTrue(meta.hasAutoFetchedFields());
        assertEquals(1, meta.getAutoFetchedFields().size());
    }

    @Test
    void shouldDetectAutoFetchedFieldName() {
        ComponentMetadata meta = processor.process(ProductComponent.class);
        assertEquals("products",
                meta.getAutoFetchedFields().get(0).getFieldName());
    }
}