package io.springui.core;

import java.util.List;

/**
 * PatchApplier — takes patches produced by VNodeDiffer and applies
 * them to the real browser DOM.
 *
 * In Phase 1 (current): simulates DOM operations with descriptive logs.
 * In Phase 2 (WASM): these methods will call real browser DOM APIs
 * via TeaVM's JavaScript interop layer.
 *
 * This class is the bridge between the Virtual DOM and the real DOM.
 */
public class PatchApplier {

    // ===========================
    // Apply all patches
    // ===========================

    /**
     * Takes a list of patches and applies each one.
     * Called by UIComponent after every re-render.
     */
    public void applyPatches(List<VNodeDiffer.Patch> patches) {
        if (patches.isEmpty()) {
            log("No patches to apply — DOM is up to date.");
            return;
        }

        log("Applying " + patches.size() + " patch(es)...");

        for (VNodeDiffer.Patch patch : patches) {
            applyPatch(patch);
        }

        log("DOM update complete.");
    }

    // ===========================
    // Apply a single patch
    // ===========================

    private void applyPatch(VNodeDiffer.Patch patch) {
        switch (patch.getType()) {

            case REPLACE -> {
                log("REPLACE: removing old node " + patch.getOldNode() +
                        " and inserting new node " + patch.getNewNode());
                domRemoveNode(patch.getOldNode());
                domInsertNode(patch.getNewNode());
            }

            case UPDATE_ATTRS -> {
                log("UPDATE_ATTRS: updating attributes on <" +
                        patch.getNewNode().getTag() + "> — " + patch.getMessage());
                domUpdateAttrs(patch.getNewNode());
            }

            case ADD_CHILD -> {
                log("ADD_CHILD: inserting new child node " + patch.getNewNode());
                domInsertNode(patch.getNewNode());
            }

            case REMOVE_CHILD -> {
                log("REMOVE_CHILD: removing child node " + patch.getOldNode());
                domRemoveNode(patch.getOldNode());
            }

            case TEXT_CHANGE -> {
                log("TEXT_CHANGE: updating text content to '" +
                        patch.getNewNode().getTextContent() + "'");
                domUpdateText(patch.getNewNode());
            }

            default -> log("UNKNOWN patch type: " + patch.getType());
        }
    }

    // ===========================
    // DOM Operations
    // Phase 1: simulated with logs
    // Phase 2: real browser DOM via TeaVM JS interop
    // ===========================

    /**
     * Inserts a new DOM node.
     * Phase 2: document.createElement(tag) + appendChild()
     */
    private void domInsertNode(VNode node) {
        if (node == null) return;
        if (node.isTextNode()) {
            log("  → DOM: createTextNode('" + node.getTextContent() + "')");
        } else {
            log("  → DOM: createElement('" + node.getTag() + "') with attrs " + node.getAttrs());
        }
    }

    /**
     * Removes an existing DOM node.
     * Phase 2: element.parentNode.removeChild(element)
     */
    private void domRemoveNode(VNode node) {
        if (node == null) return;
        log("  → DOM: removeChild(" + node + ")");
    }

    /**
     * Updates attributes on an existing DOM node.
     * Phase 2: element.setAttribute(key, value) / removeAttribute(key)
     */
    private void domUpdateAttrs(VNode node) {
        if (node == null) return;
        node.getAttrs().forEach((key, value) ->
                log("  → DOM: setAttribute('" + key + "', '" + value + "')"));
    }

    /**
     * Updates text content of a text node.
     * Phase 2: textNode.nodeValue = newText
     */
    private void domUpdateText(VNode node) {
        if (node == null) return;
        log("  → DOM: textNode.nodeValue = '" + node.getTextContent() + "'");
    }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[PatchApplier] " + message);
    }
}