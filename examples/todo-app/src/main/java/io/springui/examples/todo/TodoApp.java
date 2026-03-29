package io.springui.examples.todo;

import io.springui.core.SpringUIContext;

/**
 * TodoApp — updated to use SpringUIContext for bootstrap.
 *
 * Before (manual):
 *   SpringUI.render("todo-app", new TodoComponent());
 *
 * After (annotation-driven):
 *   SpringUIContext.create("io.springui.examples.todo")
 *       .register(TodoComponent.class)
 *       .start();
 *
 * SpringUIContext auto-discovers @SpringUIComponent classes,
 * mounts root components, and wires @BindAPI endpoints.
 */
public class TodoApp {

    public static void main(String[] args) {

        System.out.println("===========================================");
        System.out.println("  SpringUI Todo App — Annotation-Driven");
        System.out.println("===========================================\n");

        // Bootstrap SpringUI using the context
        SpringUIContext context = SpringUIContext
                .create("io.springui.examples.todo")
                .register(TodoComponent.class)
                .devMode(true)
                .start();

        // Get the mounted component and interact with it
        TodoComponent todo = (TodoComponent) context
                .getScanResult()
                .getComponent("todo-app")
                .getComponentClass()
                .cast(
                        io.springui.core.ComponentRegistry
                                .getInstance()
                                .get("todo-app")
                );

        System.out.println("\n--- Adding a new todo ---");
        todo.addTodo("Write unit tests");

        System.out.println("\n--- Completing a todo ---");
        todo.completeTodo(1);

        System.out.println("\n--- Filtering by active ---");
        todo.setFilter("active");

        System.out.println("\n--- Clearing completed ---");
        todo.clearCompleted();

        System.out.println("\n--- Final state ---");
        todo.getTodos().forEach(System.out::println);

        System.out.println("\n===========================================");
        System.out.println("  Mounted: " + context.getMountedCount() +
                " component(s)");
        System.out.println("  Dev mode: " + context.getConfig().isDevMode());
        System.out.println("===========================================");

        context.stop();
    }
}