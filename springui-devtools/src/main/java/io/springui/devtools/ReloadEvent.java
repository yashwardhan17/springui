package io.springui.devtools;

/**
 * ReloadEvent — represents a single hot reload occurrence.
 */
public class ReloadEvent {

    private final String componentClass;
    private final boolean success;
    private final long durationMs;
    private final String errorMessage;
    private final long timestamp;

    public ReloadEvent(String componentClass, boolean success,
                       long durationMs, String errorMessage) {
        this.componentClass = componentClass;
        this.success = success;
        this.durationMs = durationMs;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }

    public String getComponentClass() { return componentClass; }
    public boolean isSuccess() { return success; }
    public long getDurationMs() { return durationMs; }
    public String getErrorMessage() { return errorMessage; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "ReloadEvent{component='" + componentClass +
                "', success=" + success +
                ", duration=" + durationMs + "ms}";
    }
}