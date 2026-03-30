package io.springui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Route — maps a URL path to a UIComponent.
 * Used inside a @SpringUIRouter class.
 *
 * Supports:
 * - Static paths:  @Route("/about")
 * - Path params:   @Route("/product/:id")
 * - Wildcards:     @Route("/docs/*")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    /**
     * The URL path pattern for this route.
     * Supports path parameters via :paramName syntax.
     * Example: "/product/:id", "/user/:userId/posts/:postId"
     */
    String value();

    /**
     * Optional route title — used for browser tab title.
     */
    String title() default "";

    /**
     * Whether this route requires authentication.
     * If true and user is not authenticated, redirects to loginPath.
     */
    boolean requiresAuth() default false;

    /**
     * Path to redirect to if requiresAuth=true and user not authenticated.
     */
    String loginPath() default "/login";

    /**
     * Whether to keep this component alive when navigating away.
     * If true, component state is preserved when returning to this route.
     */
    boolean keepAlive() default false;
}