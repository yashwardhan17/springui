package io.springui.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VNode (Virtual Node) — the core building block of SpringUI's Virtual DOM.
 * Represents a single element in the UI tree, mirroring a real browser DOM node.
 */
public class VNode {

    // The HTML tag — "div", "h1", "button", etc. Null if this is a text node.
    private final String tag;

    // HTML attributes — class, id, style, href, etc.
    private final Map<String, String> attrs;

    // Child nodes — the tree structure
    private final List<VNode> children;

    // Text content — only used when this is a text node (no tag, no children)
    private final String textContent;

    // Optional key — used by the diff engine to identify nodes efficiently
    private final String key;

    // ===========================
    // Private constructor — use static factory methods below
    // ===========================
    private VNode(String tag, Map<String, String> attrs,
                  List<VNode> children, String textContent, String key) {
        this.tag = tag;
        this.attrs = attrs != null ? attrs : new HashMap<>();
        this.children = children != null ? children : new ArrayList<>();
        this.textContent = textContent;
        this.key = key;
    }

    // ===========================
    // Static Factory Methods
    // ===========================

    /**
     * Creates an element node — e.g. div(), h1(), button()
     */
    public static VNode element(String tag) {
        return new VNode(tag, new HashMap<>(), new ArrayList<>(), null, null);
    }

    /**
     * Creates an element node with attributes
     */
    public static VNode element(String tag, Map<String, String> attrs) {
        return new VNode(tag, attrs, new ArrayList<>(), null, null);
    }

    /**
     * Creates an element node with attributes and children
     */
    public static VNode element(String tag, Map<String, String> attrs, List<VNode> children) {
        return new VNode(tag, attrs, children, null, null);
    }

    /**
     * Creates a text node — no tag, just raw text content
     */
    public static VNode text(String content) {
        return new VNode(null, null, null, content, null);
    }

    // ===========================
    // Builder — for fluent construction
    // ===========================

    public VNode attr(String key, String value) {
        this.attrs.put(key, value);
        return this;
    }

    public VNode child(VNode child) {
        this.children.add(child);
        return this;
    }

    public VNode key(String key) {
        return new VNode(this.tag, this.attrs, this.children, this.textContent, key);
    }

    // ===========================
    // Utility
    // ===========================

    public boolean isTextNode() {
        return this.tag == null && this.textContent != null;
    }

    public boolean isElementNode() {
        return this.tag != null;
    }

    // ===========================
    // Getters
    // ===========================

    public String getTag() { return tag; }
    public Map<String, String> getAttrs() { return attrs; }
    public List<VNode> getChildren() { return children; }
    public String getTextContent() { return textContent; }
    public String getKey() { return key; }

    // ===========================
    // toString — useful for debugging
    // ===========================

    @Override
    public String toString() {
        if (isTextNode()) return "VNode[text='" + textContent + "']";
        return "VNode[tag=" + tag + ", attrs=" + attrs +
                ", children=" + children.size() + "]";
    }
}