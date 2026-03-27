package io.springui.core.annotation;

public class PropsFieldMetadata {
    private final String fieldName;
    private final String displayName;
    private final Class<?> fieldType;
    private final boolean required;
    private final String defaultValue;

    public PropsFieldMetadata(String fieldName, String displayName,
                              Class<?> fieldType, boolean required,
                              String defaultValue) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.fieldType = fieldType;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getFieldName() { return fieldName; }
    public String getDisplayName() { return displayName; }
    public Class<?> getFieldType() { return fieldType; }
    public boolean isRequired() { return required; }
    public String getDefaultValue() { return defaultValue; }
}