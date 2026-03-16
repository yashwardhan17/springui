package io.springui.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VNodeDiffer — the diff engine of SpringUI.
 * Compares two VNode trees (old vs new) and produces a minimal
 * list of Patch objects describing exactly what changed.
 * These patches will later be applied to the real browser DOM.
 */
public class VNodeDiffer {

    // ===========================
    // Patch Types
    // ===========================

    public enum PatchType {
        REPLACE,       // node type changed entirely — swap the whole node
        UPDATE_ATTRS,  // same tag, but attributes changed
        ADD_CHILD,     // a new child node was added
        REMOVE_CHILD,  // a child node was removed
        TEXT_CHANGE    // text content of a text node changed
    }

    // ===========================
    // Patch — represents a single change
    // ===========================

    public static class Patch {

        private final PatchType type;
        private final VNode oldNode;
        private final VNode newNode;
        private final String message;

        public Patch(PatchType type, VNode oldNode, VNode newNode, String message) {
            this.type = type;
            this.oldNode = oldNode;
            this.newNode = newNode;
            this.message = message;
        }

        public PatchType getType() { return type; }
        public VNode getOldNode() { return oldNode; }
        public VNode getNewNode() { return newNode; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "Patch[" + type + "] — " + message;
        }
    }

    // ===========================
    // Core Diff Method
    // ===========================

    /**
     * Compares oldNode and newNode trees.
     * Returns a list of patches representing the minimum changes needed.
     */
    public List<Patch> diff(VNode oldNode, VNode newNode) {
        List<Patch> patches = new ArrayList<>();
        diffNodes(oldNode, newNode, patches);
        return patches;
    }

    private void diffNodes(VNode oldNode, VNode newNode, List<Patch> patches) {

        // Case 1 — both are null, nothing to do
        if (oldNode == null && newNode == null) return;

        // Case 2 — old node didn't exist, new one appeared
        if (oldNode == null) {
            patches.add(new Patch(PatchType.ADD_CHILD, null, newNode,
                    "New node added: " + newNode));
            return;
        }

        // Case 3 — new node doesn't exist, old one was removed
        if (newNode == null) {
            patches.add(new Patch(PatchType.REMOVE_CHILD, oldNode, null,
                    "Node removed: " + oldNode));
            return;
        }

        // Case 4 — both are text nodes
        if (oldNode.isTextNode() && newNode.isTextNode()) {
            if (!oldNode.getTextContent().equals(newNode.getTextContent())) {
                patches.add(new Patch(PatchType.TEXT_CHANGE, oldNode, newNode,
                        "Text changed: '" + oldNode.getTextContent() +
                                "' -> '" + newNode.getTextContent() + "'"));
            }
            return;
        }

        // Case 5 — node type changed entirely (e.g. div -> span, or element -> text)
        if (!isSameType(oldNode, newNode)) {
            patches.add(new Patch(PatchType.REPLACE, oldNode, newNode,
                    "Node type changed: " + oldNode + " -> " + newNode));
            return;
        }

        // Case 6 — same tag, check if attributes changed
        if (oldNode.isElementNode() && newNode.isElementNode()) {
            if (!oldNode.getTag().equals(newNode.getTag())) {
                patches.add(new Patch(PatchType.REPLACE, oldNode, newNode,
                        "Tag changed: " + oldNode.getTag() + " -> " + newNode.getTag()));
                return;
            }

            // Check attribute differences
            if (!oldNode.getAttrs().equals(newNode.getAttrs())) {
                patches.add(new Patch(PatchType.UPDATE_ATTRS, oldNode, newNode,
                        "Attributes changed on <" + oldNode.getTag() + ">: " +
                                diffAttrs(oldNode.getAttrs(), newNode.getAttrs())));
            }

            // Recursively diff children
            diffChildren(oldNode.getChildren(), newNode.getChildren(), patches);
        }
    }

    // ===========================
    // Children Diffing
    // ===========================

    private void diffChildren(List<VNode> oldChildren, List<VNode> newChildren,
                              List<Patch> patches) {
        int maxLen = Math.max(oldChildren.size(), newChildren.size());

        for (int i = 0; i < maxLen; i++) {
            VNode oldChild = i < oldChildren.size() ? oldChildren.get(i) : null;
            VNode newChild = i < newChildren.size() ? newChildren.get(i) : null;
            diffNodes(oldChild, newChild, patches);
        }
    }

    // ===========================
    // Helpers
    // ===========================

    /**
     * Two nodes are the same type if both are text nodes or both are element nodes.
     */
    private boolean isSameType(VNode oldNode, VNode newNode) {
        return oldNode.isTextNode() == newNode.isTextNode();
    }

    /**
     * Produces a human-readable summary of what attributes changed.
     */
    private String diffAttrs(Map<String, String> oldAttrs, Map<String, String> newAttrs) {
        StringBuilder sb = new StringBuilder();

        // Find added or changed attrs
        for (Map.Entry<String, String> entry : newAttrs.entrySet()) {
            String key = entry.getKey();
            String newVal = entry.getValue();
            String oldVal = oldAttrs.get(key);
            if (oldVal == null) {
                sb.append("[added ").append(key).append("=").append(newVal).append("] ");
            } else if (!oldVal.equals(newVal)) {
                sb.append("[changed ").append(key).append(": ")
                        .append(oldVal).append(" -> ").append(newVal).append("] ");
            }
        }

        // Find removed attrs
        for (String key : oldAttrs.keySet()) {
            if (!newAttrs.containsKey(key)) {
                sb.append("[removed ").append(key).append("] ");
            }
        }

        return sb.toString().trim();
    }
}