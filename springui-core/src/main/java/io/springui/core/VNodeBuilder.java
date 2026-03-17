package io.springui.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VNodeBuilder — a fluent DSL for building VNode trees cleanly.
 *
 * Instead of:
 *   VNode.element("div", Map.of("class", "card"), List.of(
 *       VNode.element("h1", Map.of(), List.of(VNode.text("Title")))
 *   ))
 *
 * You write:
 *   div(attrs("class", "card"),
 *       h1(text("Title"))
 *   )
 *
 * Extend this class in your component for clean, readable render() methods.
 */
public abstract class VNodeBuilder {

    // ===========================
    // Element helpers
    // ===========================

    protected VNode div(Object... children) {
        return buildElement("div", children);
    }

    protected VNode span(Object... children) {
        return buildElement("span", children);
    }

    protected VNode p(Object... children) {
        return buildElement("p", children);
    }

    protected VNode h1(Object... children) {
        return buildElement("h1", children);
    }

    protected VNode h2(Object... children) {
        return buildElement("h2", children);
    }

    protected VNode h3(Object... children) {
        return buildElement("h3", children);
    }

    protected VNode ul(Object... children) {
        return buildElement("ul", children);
    }

    protected VNode ol(Object... children) {
        return buildElement("ol", children);
    }

    protected VNode li(Object... children) {
        return buildElement("li", children);
    }

    protected VNode button(Object... children) {
        return buildElement("button", children);
    }

    protected VNode input(Object... children) {
        return buildElement("input", children);
    }

    protected VNode form(Object... children) {
        return buildElement("form", children);
    }

    protected VNode img(Object... children) {
        return buildElement("img", children);
    }

    protected VNode a(Object... children) {
        return buildElement("a", children);
    }

    protected VNode nav(Object... children) {
        return buildElement("nav", children);
    }

    protected VNode header(Object... children) {
        return buildElement("header", children);
    }

    protected VNode footer(Object... children) {
        return buildElement("footer", children);
    }

    protected VNode section(Object... children) {
        return buildElement("section", children);
    }

    protected VNode article(Object... children) {
        return buildElement("article", children);
    }

    protected VNode main(Object... children) {
        return buildElement("main", children);
    }

    // ===========================
    // Text helper
    // ===========================

    protected VNode text(String content) {
        return VNode.text(content);
    }

    // ===========================
    // Attrs helper
    // ===========================

    /**
     * Builds an attribute map from key-value pairs.
     * Usage: attrs("class", "btn", "id", "submit-btn")
     */
    protected Map<String, String> attrs(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "attrs() requires an even number of arguments (key-value pairs)."
            );
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // ===========================
    // Core builder logic
    // ===========================

    /**
     * Builds a VNode element from a tag and mixed children.
     * Children can be: VNode, Map<String,String> (attrs), or String (text).
     */
    private VNode buildElement(String tag, Object[] children) {
        Map<String, String> attrMap = new HashMap<>();
        List<VNode> childNodes = new ArrayList<>();

        for (Object child : children) {
            if (child instanceof Map<?, ?> map) {
                // It's an attrs map
                map.forEach((k, v) -> attrMap.put((String) k, (String) v));
            } else if (child instanceof VNode vnode) {
                // It's a child VNode
                childNodes.add(vnode);
            } else if (child instanceof String str) {
                // Shorthand — plain string becomes a text node
                childNodes.add(VNode.text(str));
            }
        }

        return VNode.element(tag, attrMap, childNodes);
    }
}