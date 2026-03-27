package io.springui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @BindAPI — auto-wires a SpringUI component to a Spring Boot REST endpoint.
 *
 * This is SpringUI's killer feature. Instead of manually writing
 * fetch() calls or configuring axios, just annotate your component
 * and SpringUI handles the wiring automatically.
 *
 * SpringUI will:
 * 1. Call the endpoint when the component mounts
 * 2. Deserialize the response into the @AutoFetched field
 * 3. Trigger a re-render with the fetched data
 * 4. Re-fetch automatically when @State fields used as params change
 *
 * Usage:
 *   @SpringUIComponent
 *   @BindAPI("/api/products")
 *   public class ProductList extends UIComponent {
 *
 *       @AutoFetched
 *       private List<Product> products;
 *
 *       @Override
 *       public VNode render() {
 *           return ul(
 *               products.stream()
 *                   .map(p -> li(p.getName()))
 *                   .toList()
 *           );
 *       }
 *   }
 *
 * With path variables:
 *   @BindAPI(value = "/api/products/{id}", pathVars = {"id"})
 *
 * With query params:
 *   @BindAPI(value = "/api/products", queryParams = {"page", "size"})
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindAPI {

    /**
     * The Spring Boot REST endpoint URL to bind to.
     * Supports path variables: "/api/products/{id}"
     */
    String value();

    /**
     * HTTP method to use when calling the endpoint.
     * Defaults to GET.
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * Path variable names — matched to @State fields by name.
     * Example: pathVars = {"id"} maps to a @State field named "id"
     */
    String[] pathVars() default {};

    /**
     * Query parameter names — matched to @State fields by name.
     * Example: queryParams = {"page", "size"}
     */
    String[] queryParams() default {};

    /**
     * Whether to fetch data automatically when the component mounts.
     * Defaults to true.
     */
    boolean fetchOnMount() default true;

    /**
     * Whether to re-fetch when dependent @State fields change.
     * Defaults to true.
     */
    boolean reactiveRefetch() default true;

    /**
     * HTTP methods supported by @BindAPI.
     */
    enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }
}