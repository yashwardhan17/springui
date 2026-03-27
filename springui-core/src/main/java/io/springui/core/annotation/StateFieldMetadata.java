package io.springui.core.annotation;

public class StateFieldMetadata {
    private final String fieldName;
    private final String displayName;
    private final Class<?> fieldType;
    private final boolean persistent;

    public StateFieldMetadata(String fieldName, String displayName,
                              Class<?> fieldType, boolean persistent) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.fieldType = fieldType;
        this.persistent = persistent;
    }

    public String getFieldName() { return fieldName; }
    public String getDisplayName() { return displayName; }
    public Class<?> getFieldType() { return fieldType; }
    public boolean isPersistent() { return persistent; }
}