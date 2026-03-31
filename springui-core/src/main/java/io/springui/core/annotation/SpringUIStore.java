package io.springui.core.annotation;

import java.lang.annotation.*;

/**
 * Marks a class as a SpringUI global store.
 * Subclass UIStore and annotate with @SpringUIStore.
 *
 * The store is registered as a singleton in StoreRegistry on startup.
 * Any UIComponent can inject it via @Autowired.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpringUIStore {

    /**
     * Optional name for this store. Defaults to the simple class name.
     */
    String name() default "";
}