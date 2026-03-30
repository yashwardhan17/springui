package io.springui.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @SpringUIRouter — marks a class as the client-side router
 * for a SpringUI application.
 *
 * Usage:
 *   @SpringUIRouter
 *   public class AppRouter extends UIRouter {
 *
 *       @Route("/")
 *       public Class<? extends UIComponent> home() {
 *           return HomeComponent.class;
 *       }
 *
 *       @Route("/products")
 *       public Class<? extends UIComponent> products() {
 *           return ProductListComponent.class;
 *       }
 *
 *       @Route("/product/:id")
 *       public Class<? extends UIComponent> productDetail() {
 *           return ProductDetailComponent.class;
 *       }
 *   }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringUIRouter {

    /**
     * The base path for all routes in this router.
     * Defaults to "/" (root).
     */
    String basePath() default "/";

    /**
     * Whether to use hash-based routing (#/path)
     * instead of history API (/path).
     * Hash routing works without server configuration.
     */
    boolean hashMode() default false;

    /**
     * The component to render when no route matches.
     * Defaults to empty (shows nothing).
     */
    String notFoundComponent() default "";
}