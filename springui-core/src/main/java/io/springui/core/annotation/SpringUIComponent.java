package io.springui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @SpringUIComponent — marks a class as a SpringUI component.
 *
 * Every class annotated with @SpringUIComponent:
 * - Must extend UIComponent
 * - Will be picked up by the SpringUI component scanner
 * - Will be registered in the ComponentRegistry on startup
 * - Will be compiled to WASM by the SpringUI compiler
 *
 * Usage:
 *   @SpringUIComponent
 *   public class CounterComponent extends UIComponent {
 *       @Override
 *       public VNode render() {
 *           return div(h1("Counter"));
 *       }
 *   }
 *
 * Optionally provide an ID — defaults to class name lowercase:
 *   @SpringUIComponent(id = "counter")
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringUIComponent {

    /**
     * The component ID used in the ComponentRegistry.
     * Defaults to the class simple name in lowercase.
     * Example: CounterComponent -> "countercomponent"
     */
    String id() default "";

    /**
     * Human-readable display name for DevTools inspector.
     * Defaults to the class simple name.
     */
    String displayName() default "";

    /**
     * Whether this component is a root component.
     * Root components are mounted directly into the DOM.
     * Non-root components are used inside other components.
     */
    boolean root() default false;

    /**
     * The DOM element ID to mount this component into.
     * Only used when root = true.
     * Example: @SpringUIComponent(root = true, mountTarget = "app")
     */
    String mountTarget() default "root";
}