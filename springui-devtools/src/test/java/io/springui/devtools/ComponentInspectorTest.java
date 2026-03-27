package io.springui.devtools;

import io.springui.core.SpringUI;
import io.springui.core.UIComponent;
import io.springui.core.VNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ComponentInspectorTest {

    static class SimpleComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div")
                    .attr("class", "simple")
                    .child(VNode.text("Hello SpringUI"));
        }
    }

    static class StatefulComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div");
        }
    }

    private ComponentInspector inspector;

    @BeforeEach
    void setUp() {
        SpringUI.reset();
        inspector = new ComponentInspector();
    }

    @AfterEach
    void tearDown() {
        SpringUI.reset();
    }

    @Test
    void shouldInspectMountedComponent() {
        SpringUI.render("simple", new SimpleComponent());
        UIComponent component = SpringUI.getComponent("simple");
        InspectionReport report = inspector.inspect(component);
        assertNotNull(report);
        assertEquals("SimpleComponent", report.getComponentName());
        assertTrue(report.isMounted());
    }

    @Test
    void shouldInspectById() {
        SpringUI.render("simple", new SimpleComponent());
        InspectionReport report = inspector.inspectById("simple");
        assertNotNull(report);
        assertEquals("SimpleComponent", report.getComponentName());
    }

    @Test
    void shouldThrowForUnknownId() {
        assertThrows(DevToolsException.class, () ->
                inspector.inspectById("nonexistent"));
    }

    @Test
    void shouldCaptureVNodeSnapshot() {
        SpringUI.render("simple", new SimpleComponent());
        UIComponent component = SpringUI.getComponent("simple");
        InspectionReport report = inspector.inspect(component);
        assertNotNull(report.getVnodeSnapshot());
        assertEquals("div", report.getVnodeSnapshot().getTag());
    }

    @Test
    void shouldTakeSnapshotOfAllComponents() {
        SpringUI.render("comp1", new SimpleComponent());
        SpringUI.render("comp2", new SimpleComponent());
        List<InspectionReport> snapshot = inspector.snapshot();
        assertEquals(2, snapshot.size());
    }

    @Test
    void shouldInspectAllMountedComponents() {
        SpringUI.render("comp1", new SimpleComponent());
        SpringUI.render("comp2", new SimpleComponent());
        assertDoesNotThrow(() -> inspector.inspectAll());
    }

    @Test
    void shouldHandleNoMountedComponents() {
        assertDoesNotThrow(() -> inspector.inspectAll());
    }

    @Test
    void shouldTrackStateSize() {
        StatefulComponent component = new StatefulComponent();
        SpringUI.render("stateful", component);
        component.setState("key1", "value1");
        component.setState("key2", "value2");
        InspectionReport report = inspector.inspect(component);
        assertEquals(2, report.getStateSize());
    }
}