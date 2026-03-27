package io.springui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @State — marks a field as reactive state in a SpringUI component.
 *
 * When a @State field changes via setState(), SpringUI automatically:
 * 1. Calls render() to get the new VNode tree
 * 2. Diffs against the previous VNode tree
 * 3. Applies minimal patches to the DOM
 *
 * Usage:
 *   @State
 *   private int count = 0;
 *
 *   @State(persistent = true)
 *   private String username = "";
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface State {

    /**
     * Whether this state should persist across hot reloads.
     * If true, the value is preserved when the component reloads.
     */
    boolean persistent() default false;

    /**
     * Optional name for DevTools display.
     * Defaults to the field name.
     */
    String name() default "";
}