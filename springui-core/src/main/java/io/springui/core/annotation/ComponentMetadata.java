package io.springui.core.annotation;

import java.util.List;

/**
 * ComponentMetadata — all annotation-derived metadata for a component.
 */
public class ComponentMetadata {

    private final String componentId;
    private final String displayName;
    private final Class<?> componentClass;
    private final SpringUIComponent componentAnnotation;
    private final BindAPI bindAPI;
    private final List<StateFieldMetadata> stateFields;
    private final List<PropsFieldMetadata> propsFields;
    private final List<AutoFetchedFieldMetadata> autoFetchedFields;

    public ComponentMetadata(String componentId, String displayName,
                             Class<?> componentClass,
                             SpringUIComponent componentAnnotation,
                             BindAPI bindAPI,
                             List<StateFieldMetadata> stateFields,
                             List<PropsFieldMetadata> propsFields,
                             List<AutoFetchedFieldMetadata> autoFetchedFields) {
        this.componentId = componentId;
        this.displayName = displayName;
        this.componentClass = componentClass;
        this.componentAnnotation = componentAnnotation;
        this.bindAPI = bindAPI;
        this.stateFields = stateFields;
        this.propsFields = propsFields;
        this.autoFetchedFields = autoFetchedFields;
    }

    public boolean hasBindAPI() { return bindAPI != null; }
    public boolean hasStateFields() { return !stateFields.isEmpty(); }
    public boolean hasPropsFields() { return !propsFields.isEmpty(); }
    public boolean hasAutoFetchedFields() { return !autoFetchedFields.isEmpty(); }
    public boolean isRootComponent() {
        return componentAnnotation != null && componentAnnotation.root();
    }

    public String getComponentId() { return componentId; }
    public String getDisplayName() { return displayName; }
    public Class<?> getComponentClass() { return componentClass; }
    public SpringUIComponent getComponentAnnotation() { return componentAnnotation; }
    public BindAPI getBindAPI() { return bindAPI; }
    public List<StateFieldMetadata> getStateFields() { return stateFields; }
    public List<PropsFieldMetadata> getPropsFields() { return propsFields; }
    public List<AutoFetchedFieldMetadata> getAutoFetchedFields() {
        return autoFetchedFields;
    }

    @Override
    public String toString() {
        return "ComponentMetadata{id='" + componentId +
                "', bindAPI=" + (hasBindAPI() ? bindAPI.value() : "none") +
                ", stateFields=" + stateFields.size() +
                ", propsFields=" + propsFields.size() + "}";
    }
}