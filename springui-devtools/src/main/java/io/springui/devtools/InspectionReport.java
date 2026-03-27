package io.springui.devtools;

import io.springui.core.VNode;
import java.util.Map;

/**
 * InspectionReport — snapshot of a component's state at inspection time.
 */
public class InspectionReport {

    private final String componentName;
    private final String componentClass;
    private final boolean mounted;
    private final VNode vnodeSnapshot;
    private final Map<String, Object> stateSnapshot;

    public InspectionReport(String componentName, String componentClass,
                            boolean mounted, VNode vnodeSnapshot,
                            Map<String, Object> stateSnapshot) {
        this.componentName = componentName;
        this.componentClass = componentClass;
        this.mounted = mounted;
        this.vnodeSnapshot = vnodeSnapshot;
        this.stateSnapshot = stateSnapshot;
    }

    public String getComponentName() { return componentName; }
    public String getComponentClass() { return componentClass; }
    public boolean isMounted() { return mounted; }
    public VNode getVnodeSnapshot() { return vnodeSnapshot; }
    public Map<String, Object> getStateSnapshot() { return stateSnapshot; }
    public int getStateSize() { return stateSnapshot != null ? stateSnapshot.size() : 0; }

    @Override
    public String toString() {
        return "InspectionReport{component='" + componentName +
                "', mounted=" + mounted +
                ", stateEntries=" + getStateSize() + "}";
    }
}