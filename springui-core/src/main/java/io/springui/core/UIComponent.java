package io.springui.core;

import java.util.HashMap;
import java.util.Map;

/**
 * UIComponent — the base class for every SpringUI component.
 * Every component the developer writes will extend this class.
 *
 * Inspired by React's Component model:
 * - render()     → defines what the UI looks like (like React's render())
 * - setState()   → triggers a re-render (like React's setState())
 * - onMount()    → called when component is added to the DOM (like componentDidMount)
 * - onUnmount()  → called when component is removed (like componentWillUnmount)
 */
public abstract class UIComponent extends VNodeBuilder {

    // ===========================
    // Internal State
    // ===========================

    // The component's reactive state — any change triggers re-render
    private final Map<String, Object> state = new HashMap<>();

    // The last rendered VNode tree — used by differ to compute patches
    private VNode previousVNode;

    // Whether the component is currently mounted in the DOM
    private boolean mounted = false;

    // The differ — compares old and new VNode trees
    private final VNodeDiffer differ = new VNodeDiffer();

    // ===========================
    // Abstract Methods — developer must implement these
    // ===========================

    /**
     * Every component MUST implement render().
     * Returns a VNode tree representing the component's current UI.
     * Called automatically whenever state changes.
     */
    public abstract VNode render();

    // ===========================
    // Lifecycle Methods — developer can override these
    // ===========================

    /**
     * Called once when the component is first mounted.
     * Override to fetch data, set up subscriptions, etc.
     */
    public void onMount() {}

    /**
     * Called when the component is removed from the DOM.
     * Override to clean up resources, cancel subscriptions, etc.
     */
    public void onUnmount() {}

    /**
     * Called every time the component re-renders.
     * Override to react to render cycles.
     */
    public void onUpdate() {}

    // ===========================
    // State Management
    // ===========================

    /**
     * Updates state and triggers a re-render.
     * Usage: setState("count", 5);
     */
    public void setState(String key, Object value) {
        state.put(key, value);
        triggerReRender();
    }

    /**
     * Batch state update using a Runnable.
     * Usage: setState(() -> { count++; name = "Yash"; });
     * Triggers only one re-render for multiple changes.
     */
    public void setState(Runnable stateUpdate) {
        stateUpdate.run();
        triggerReRender();
    }

    /**
     * Returns a state value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T getState(String key) {
        return (T) state.get(key);
    }

    /**
     * Returns the entire state map.
     */
    public Map<String, Object> getState() {
        return state;
    }

    // ===========================
    // Mount / Unmount
    // ===========================

    /**
     * Called internally when the component is mounted.
     * Triggers first render and calls onMount().
     */
    public final void mount() {
        mounted = true;
        previousVNode = render();
        onMount();
    }

    /**
     * Called internally when the component is unmounted.
     * Calls onUnmount() and clears state.
     */
    public final void unmount() {
        mounted = false;
        onUnmount();
        state.clear();
        previousVNode = null;
    }

    // ===========================
    // Re-render
    // ===========================

    /**
     * Core re-render logic.
     * 1. Calls render() to get the new VNode tree
     * 2. Diffs it against the previous VNode tree
     * 3. Produces patches (later applied to real DOM)
     * 4. Calls onUpdate()
     */
    private void triggerReRender() {
        if (!mounted) return;

        VNode newVNode = render();
        var patches = differ.diff(previousVNode, newVNode);

        // TODO: in Phase 2, pass patches to DOMPatcher to update the real browser DOM
        // For now, log them so we can see the diff engine working
        patches.forEach(patch -> System.out.println("[SpringUI] " + patch));

        previousVNode = newVNode;
        onUpdate();
    }

    // ===========================
    // Getters
    // ===========================

    public boolean isMounted() { return mounted; }
    public VNode getPreviousVNode() { return previousVNode; }
}