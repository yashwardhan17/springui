package io.springui.core.annotation;

public class AutoFetchedFieldMetadata {
    private final String fieldName;
    private final Class<?> fieldType;
    private final String jsonPath;
    private final boolean showLoading;

    public AutoFetchedFieldMetadata(String fieldName, Class<?> fieldType,
                                    String jsonPath, boolean showLoading) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.jsonPath = jsonPath;
        this.showLoading = showLoading;
    }

    public String getFieldName() { return fieldName; }
    public Class<?> getFieldType() { return fieldType; }
    public String getJsonPath() { return jsonPath; }
    public boolean isShowLoading() { return showLoading; }
}