package io.springui.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

class VNodeTest {

    // ===========================
    // Element Node Tests
    // ===========================

    @Test
    void shouldCreateElementNode() {
        VNode node = VNode.element("div");
        assertEquals("div", node.getTag());
        assertTrue(node.getAttrs().isEmpty());
        assertTrue(node.getChildren().isEmpty());
        assertNull(node.getTextContent());
    }

    @Test
    void shouldCreateElementNodeWithAttrs() {
        VNode node = VNode.element("div", Map.of("class", "container", "id", "main"));
        assertEquals("div", node.getTag());
        assertEquals("container", node.getAttrs().get("class"));
        assertEquals("main", node.getAttrs().get("id"));
    }

    @Test
    void shouldCreateElementNodeWithChildren() {
        VNode child = VNode.element("span");
        VNode parent = VNode.element("div", Map.of(), java.util.List.of(child));
        assertEquals(1, parent.getChildren().size());
        assertEquals("span", parent.getChildren().get(0).getTag());
    }

    @Test
    void shouldAddAttrViaBuilder() {
        VNode node = VNode.element("button")
                .attr("class", "btn")
                .attr("type", "submit");
        assertEquals("btn", node.getAttrs().get("class"));
        assertEquals("submit", node.getAttrs().get("type"));
    }

    @Test
    void shouldAddChildViaBuilder() {
        VNode parent = VNode.element("ul")
                .child(VNode.element("li"))
                .child(VNode.element("li"));
        assertEquals(2, parent.getChildren().size());
    }

    @Test
    void shouldIdentifyAsElementNode() {
        VNode node = VNode.element("div");
        assertTrue(node.isElementNode());
        assertFalse(node.isTextNode());
    }

    // ===========================
    // Text Node Tests
    // ===========================

    @Test
    void shouldCreateTextNode() {
        VNode node = VNode.text("Hello SpringUI");
        assertEquals("Hello SpringUI", node.getTextContent());
        assertNull(node.getTag());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    void shouldIdentifyAsTextNode() {
        VNode node = VNode.text("hello");
        assertTrue(node.isTextNode());
        assertFalse(node.isElementNode());
    }

    // ===========================
    // Key Tests
    // ===========================

    @Test
    void shouldAssignKey() {
        VNode node = VNode.element("li").key("item-1");
        assertEquals("item-1", node.getKey());
    }

    // ===========================
    // toString Tests
    // ===========================

    @Test
    void shouldReturnMeaningfulToStringForElementNode() {
        VNode node = VNode.element("div");
        assertTrue(node.toString().contains("div"));
    }

    @Test
    void shouldReturnMeaningfulToStringForTextNode() {
        VNode node = VNode.text("hello");
        assertTrue(node.toString().contains("hello"));
    }
}