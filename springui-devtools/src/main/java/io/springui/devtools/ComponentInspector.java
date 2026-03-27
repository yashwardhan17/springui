package io.springui.devtools;

import io.springui.core.ComponentRegistry;
import io.springui.core.UIComponent;
import io.springui.core.VNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ComponentInspector — runtime inspector for SpringUI components.
 * Lets developers inspect mounted components, their state,
 * and their current VNode tree during development.
 *
 * Think of this as the Java equivalent of React DevTools.
 *
 * Phase 1: logs component info to console
 * Phase 2: exposes data to browser DevTools extension via WebSocket
 */
public class ComponentInspector {

    // ===========================
    // Inspect all mounted components
    // ===========================

    /**
     * Prints a full inspection report of all mounted components.
     */
    public void inspectAll() {
        Collection<UIComponent> components =
                ComponentRegistry.getInstance().getAll();

        log("=== SpringUI Component Inspector ===");
        log("Mounted components: " + components.size());
        log("====================================");

        if (components.isEmpty()) {
            log("No components currently mounted.");
            return;
        }

        components.forEach(this::inspect);
    }

    /**
     * Inspects a single component and prints its details.
     */
    public InspectionReport inspect(UIComponent component) {
        log("\n--- Component: " + component.getClass().getSimpleName() + " ---");
        log("Class:   " + component.getClass().getName());
        log("Mounted: " + component.isMounted());

        VNode vnode = component.getPreviousVNode();
        if (vnode != null) {
            log("VNode tree:");
            printVNodeTree(vnode, 0);
        } else {
            log("VNode: null (not yet rendered)");
        }

        InspectionReport report = buildReport(component);
        log("State entries: " + report.getStateSize());
        return report;
    }

    /**
     * Inspects a component by ID from the registry.
     */
    public InspectionReport inspectById(String componentId) {
        UIComponent component = ComponentRegistry.getInstance().get(componentId);
        if (component == null) {
            throw new DevToolsException(
                    "No component found with id: " + componentId
            );
        }
        return inspect(component);
    }

    // ===========================
    // VNode tree printer
    // ===========================

    /**
     * Recursively prints the VNode tree with indentation.
     */
    private void printVNodeTree(VNode node, int depth) {
        String indent = "  ".repeat(depth);

        if (node.isTextNode()) {
            log(indent + "\"" + node.getTextContent() + "\"");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("<").append(node.getTag());

        if (!node.getAttrs().isEmpty()) {
            node.getAttrs().forEach((k, v) ->
                    sb.append(" ").append(k).append("=\"").append(v).append("\""));
        }

        if (node.getChildren().isEmpty()) {
            sb.append(" />");
            log(sb.toString());
        } else {
            sb.append(">");
            log(sb.toString());
            node.getChildren().forEach(child ->
                    printVNodeTree(child, depth + 1));
            log(indent + "</" + node.getTag() + ">");
        }
    }

    // ===========================
    // Snapshot
    // ===========================

    /**
     * Takes a snapshot of all currently mounted components.
     * Useful for debugging state at a specific point in time.
     */
    public List<InspectionReport> snapshot() {
        List<InspectionReport> reports = new ArrayList<>();
        ComponentRegistry.getInstance().getAll()
                .forEach(c -> reports.add(buildReport(c)));
        log("Snapshot taken — " + reports.size() + " component(s)");
        return reports;
    }

    // ===========================
    // Build report
    // ===========================

    private InspectionReport buildReport(UIComponent component) {
        return new InspectionReport(
                component.getClass().getSimpleName(),
                component.getClass().getName(),
                component.isMounted(),
                component.getPreviousVNode(),
                component.getState()
        );
    }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[ComponentInspector] " + message);
    }
}