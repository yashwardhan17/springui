package io.springui.core.annotation;

import io.springui.core.UIComponent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * AnnotationProcessor — reads SpringUI annotations from component classes
 * and produces metadata used by the runtime and compiler.
 *
 * This is the bridge between annotations and actual framework behavior.
 */
public class AnnotationProcessor {

    // ===========================
    // Process a component class
    // ===========================

    /**
     * Reads all SpringUI annotations from a component class
     * and returns a ComponentMetadata object.
     */
    public ComponentMetadata process(Class<?> componentClass) {
        validateIsUIComponent(componentClass);

        SpringUIComponent componentAnnotation =
                componentClass.getAnnotation(SpringUIComponent.class);
        BindAPI bindAPI =
                componentClass.getAnnotation(BindAPI.class);

        String componentId = resolveComponentId(componentClass, componentAnnotation);
        String displayName = resolveDisplayName(componentClass, componentAnnotation);

        List<StateFieldMetadata> stateFields = processStateFields(componentClass);
        List<PropsFieldMetadata> propsFields = processPropsFields(componentClass);
        List<AutoFetchedFieldMetadata> autoFetchedFields =
                processAutoFetchedFields(componentClass);

        log("Processed component: " + componentClass.getSimpleName());
        log("  ID: " + componentId);
        log("  State fields: " + stateFields.size());
        log("  Props fields: " + propsFields.size());
        log("  BindAPI: " + (bindAPI != null ? bindAPI.value() : "none"));

        return new ComponentMetadata(
                componentId,
                displayName,
                componentClass,
                componentAnnotation,
                bindAPI,
                stateFields,
                propsFields,
                autoFetchedFields
        );
    }

    // ===========================
    // Field processors
    // ===========================

    private List<StateFieldMetadata> processStateFields(Class<?> componentClass) {
        List<StateFieldMetadata> fields = new ArrayList<>();
        for (Field field : componentClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(State.class)) {
                State annotation = field.getAnnotation(State.class);
                String name = annotation.name().isEmpty()
                        ? field.getName() : annotation.name();
                fields.add(new StateFieldMetadata(
                        field.getName(), name,
                        field.getType(), annotation.persistent()
                ));
                log("  @State: " + field.getName() +
                        " (" + field.getType().getSimpleName() + ")");
            }
        }
        return fields;
    }

    private List<PropsFieldMetadata> processPropsFields(Class<?> componentClass) {
        List<PropsFieldMetadata> fields = new ArrayList<>();
        for (Field field : componentClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Props.class)) {
                Props annotation = field.getAnnotation(Props.class);
                String name = annotation.name().isEmpty()
                        ? field.getName() : annotation.name();
                fields.add(new PropsFieldMetadata(
                        field.getName(), name,
                        field.getType(), annotation.required(),
                        annotation.defaultValue()
                ));
                log("  @Props: " + field.getName() +
                        " (required=" + annotation.required() + ")");
            }
        }
        return fields;
    }

    private List<AutoFetchedFieldMetadata> processAutoFetchedFields(
            Class<?> componentClass) {
        List<AutoFetchedFieldMetadata> fields = new ArrayList<>();
        for (Field field : componentClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoFetched.class)) {
                AutoFetched annotation = field.getAnnotation(AutoFetched.class);
                fields.add(new AutoFetchedFieldMetadata(
                        field.getName(), field.getType(),
                        annotation.jsonPath(), annotation.showLoading()
                ));
                log("  @AutoFetched: " + field.getName());
            }
        }
        return fields;
    }

    // ===========================
    // Helpers
    // ===========================

    private void validateIsUIComponent(Class<?> componentClass) {
        if (!UIComponent.class.isAssignableFrom(componentClass)) {
            throw new AnnotationProcessingException(
                    componentClass.getName() + " must extend UIComponent " +
                            "to use SpringUI annotations."
            );
        }
    }

    private String resolveComponentId(Class<?> componentClass,
                                      SpringUIComponent annotation) {
        if (annotation != null && !annotation.id().isEmpty()) {
            return annotation.id();
        }
        return componentClass.getSimpleName().toLowerCase();
    }

    private String resolveDisplayName(Class<?> componentClass,
                                      SpringUIComponent annotation) {
        if (annotation != null && !annotation.displayName().isEmpty()) {
            return annotation.displayName();
        }
        return componentClass.getSimpleName();
    }

    private void log(String message) {
        System.out.println("[AnnotationProcessor] " + message);
    }
}