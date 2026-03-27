package io.springui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Props — marks a field as an input prop in a SpringUI component.
 *
 * Props are passed from parent components to child components.
 * Unlike @State, props are read-only — a component cannot modify
 * its own props, only its parent can change them.
 *
 * Usage:
 *   @Props
 *   private String title;
 *
 *   @Props(required = true)
 *   private User user;
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Props {

    /**
     * Whether this prop is required.
     * If true and not provided, SpringUI throws at mount time.
     */
    boolean required() default false;

    /**
     * Default value as a string — used for primitive props.
     * For complex types, use field initializer instead.
     */
    String defaultValue() default "";

    /**
     * Optional name for DevTools display.
     * Defaults to the field name.
     */
    String name() default "";
}