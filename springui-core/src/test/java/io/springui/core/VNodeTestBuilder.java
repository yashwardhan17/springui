package io.springui.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VNodeBuilderTest {

    // Concrete subclass for testing
    static class TestBuilder extends VNodeBuilder {
        public VNode buildDiv() {
            return div(attrs("class", "box"), text("Hello"));
        }

        public VNode buildNested() {
            return div(
                    h1("Title"),
                    p("Paragraph"),
                    ul(
                            li("Item 1"),
                            li("Item 2")
                    )
            );
        }

        public VNode buildButton() {
            return button(attrs("class", "btn", "type", "submit"), "Click me");
        }
    }

    private final TestBuilder builder = new TestBuilder();

    @Test
    void shouldBuildDivWithAttrsAndText() {
        VNode node = builder.buildDiv();
        assertEquals("div", node.getTag());
        assertEquals("box", node.getAttrs().get("class"));
        assertEquals(1, node.getChildren().size());
        assertTrue(node.getChildren().get(0).isTextNode());
        assertEquals("Hello", node.getChildren().get(0).getTextContent());
    }

    @Test
    void shouldBuildNestedStructure() {
        VNode node = builder.buildNested();
        assertEquals("div", node.getTag());
        assertEquals(3, node.getChildren().size());
        assertEquals("h1", node.getChildren().get(0).getTag());
        assertEquals("p", node.getChildren().get(1).getTag());
        assertEquals("ul", node.getChildren().get(2).getTag());
    }

    @Test
    void shouldBuildUlWithLiChildren() {
        VNode node = builder.buildNested();
        VNode ul = node.getChildren().get(2);
        assertEquals(2, ul.getChildren().size());
        assertEquals("li", ul.getChildren().get(0).getTag());
        assertEquals("li", ul.getChildren().get(1).getTag());
    }

    @Test
    void shouldBuildButtonWithAttrsAndText() {
        VNode node = builder.buildButton();
        assertEquals("button", node.getTag());
        assertEquals("btn", node.getAttrs().get("class"));
        assertEquals("submit", node.getAttrs().get("type"));
        assertEquals(1, node.getChildren().size());
        assertEquals("Click me", node.getChildren().get(0).getTextContent());
    }

    @Test
    void shouldBuildTextNode() {
        VNode node = builder.text("SpringUI rocks");
        assertTrue(node.isTextNode());
        assertEquals("SpringUI rocks", node.getTextContent());
    }

    @Test
    void shouldBuildAttrsMap() {
        var attrs = builder.attrs("class", "card", "id", "main");
        assertEquals("card", attrs.get("class"));
        assertEquals("main", attrs.get("id"));
    }

    @Test
    void shouldThrowForOddAttrsArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                builder.attrs("class"));
    }

    @Test
    void shouldBuildAllHtmlElements() {
        assertNotNull(builder.div());
        assertNotNull(builder.span());
        assertNotNull(builder.p());
        assertNotNull(builder.h1());
        assertNotNull(builder.h2());
        assertNotNull(builder.h3());
        assertNotNull(builder.ul());
        assertNotNull(builder.ol());
        assertNotNull(builder.li());
        assertNotNull(builder.button());
        assertNotNull(builder.input());
        assertNotNull(builder.form());
        assertNotNull(builder.nav());
        assertNotNull(builder.header());
        assertNotNull(builder.footer());
        assertNotNull(builder.section());
        assertNotNull(builder.article());
    }
}