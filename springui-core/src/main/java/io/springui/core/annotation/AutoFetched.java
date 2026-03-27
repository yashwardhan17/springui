package io.springui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @AutoFetched — marks a field to be automatically populated
 * by the @BindAPI endpoint response.
 *
 * Used alongside @BindAPI on the component class.
 * SpringUI deserializes the endpoint response into this field
 * and triggers a re-render automatically.
 *
 * Usage:
 *   @AutoFetched
 *   private List<Product> products;
 *
 *   @AutoFetched
 *   private User currentUser;
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFetched {

    /**
     * Optional JSON path within the response to extract.
     * Example: jsonPath = "$.data.items" extracts a nested array.
     * Defaults to root response.
     */
    String jsonPath() default "";

    /**
     * Whether to show a loading state while fetching.
     * Defaults to true.
     */
    boolean showLoading() default true;
}