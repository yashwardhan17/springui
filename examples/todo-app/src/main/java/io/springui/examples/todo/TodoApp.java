package io.springui.examples.todo;

import io.springui.core.SpringUI;

/**
 * TodoApp — entry point for the SpringUI todo example.
 *
 * Run this to see SpringUI working end to end:
 * - Component mounts
 * - State changes trigger re-renders
 * - VNodeDiffer produces patches
 * - PatchApplier logs DOM operations
 */
public class TodoApp {

    public static void main(String[] args) {

        System.out.println("===========================================");
        System.out.println("  SpringUI Todo App — v" + SpringUI.version());
        System.out.println("===========================================\n");

        // Mount the todo component
        TodoComponent todo = new TodoComponent();
        SpringUI.render("todo-app", todo);

        System.out.println("\n--- Adding a new todo ---");
        todo.addTodo("Write unit tests");

        System.out.println("\n--- Completing a todo ---");
        todo.completeTodo(1);

        System.out.println("\n--- Filtering by active ---");
        todo.setFilter("active");

        System.out.println("\n--- Filtering by completed ---");
        todo.setFilter("completed");

        System.out.println("\n--- Clearing completed ---");
        todo.clearCompleted();

        System.out.println("\n--- Removing a todo ---");
        todo.removeTodo(2);

        System.out.println("\n--- Final state ---");
        todo.getTodos().forEach(System.out::println);

        System.out.println("\n===========================================");
        System.out.println("  SpringUI running. " +
                SpringUI.mountedCount() + " component(s) mounted.");
        System.out.println("===========================================");
    }
}