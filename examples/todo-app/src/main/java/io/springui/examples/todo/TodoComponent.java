package io.springui.examples.todo;

import io.springui.core.UIComponent;
import io.springui.core.VNode;
import io.springui.core.annotation.SpringUIComponent;
import io.springui.core.annotation.State;

import java.util.ArrayList;
import java.util.List;

/**
 * TodoComponent — updated to use SpringUI annotation system.
 *
 * Demonstrates:
 * - @SpringUIComponent — marks this as a SpringUI component
 * - @State — reactive state fields
 * - SpringUIContext bootstrap in TodoApp.java
 */
@SpringUIComponent(
        id = "todo-app",
        displayName = "Todo App",
        root = true,
        mountTarget = "root"
)
public class TodoComponent extends UIComponent {

    // ===========================
    // @State fields
    // ===========================

    @State
    private List<Todo> todos = new ArrayList<>();

    @State(persistent = true)
    private int nextId = 1;

    @State(persistent = true, name = "activeFilter")
    private String filter = "all";

    // ===========================
    // Lifecycle
    // ===========================

    @Override
    public void onMount() {
        System.out.println("[TodoComponent] Mounted — adding sample todos...");
        addTodo("Learn SpringUI");
        addTodo("Build a WASM compiler");
        addTodo("Ship to production");
    }

    @Override
    public void onUpdate() {
        System.out.println("[TodoComponent] Re-rendered — " +
                todos.size() + " todos, filter: " + filter);
    }

    // ===========================
    // Actions
    // ===========================

    public void addTodo(String text) {
        setState(() -> todos.add(new Todo(nextId++, text, false)));
    }

    public void completeTodo(int id) {
        setState(() -> {
            todos = todos.stream()
                    .map(t -> t.getId() == id ? t.complete() : t)
                    .toList();
        });
    }

    public void removeTodo(int id) {
        setState(() -> {
            todos = todos.stream()
                    .filter(t -> t.getId() != id)
                    .toList();
        });
    }

    public void setFilter(String filter) {
        setState(() -> this.filter = filter);
    }

    public void clearCompleted() {
        setState(() -> {
            todos = todos.stream()
                    .filter(t -> !t.isCompleted())
                    .toList();
        });
    }

    // ===========================
    // Render
    // ===========================

    @Override
    public VNode render() {
        List<Todo> filtered = getFilteredTodos();

        VNode todoList = VNode.element("ul");
        for (Todo todo : filtered) {
            todoList.child(
                    VNode.element("li")
                            .attr("class", todo.isCompleted() ? "completed" : "active")
                            .attr("data-id", String.valueOf(todo.getId()))
                            .child(VNode.text(todo.toString()))
            );
        }

        long activeCount = todos.stream()
                .filter(t -> !t.isCompleted()).count();
        long completedCount = todos.stream()
                .filter(Todo::isCompleted).count();

        return VNode.element("div")
                .attr("id", "todo-app")
                .child(VNode.element("h1")
                        .child(VNode.text("SpringUI Todo App")))
                .child(VNode.element("p")
                        .child(VNode.text(activeCount + " active, " +
                                completedCount + " completed")))
                .child(todoList)
                .child(VNode.element("div")
                        .attr("class", "filters")
                        .child(VNode.text("Filter: " + filter)));
    }

    // ===========================
    // Helpers
    // ===========================

    private List<Todo> getFilteredTodos() {
        return switch (filter) {
            case "active" ->
                    todos.stream().filter(t -> !t.isCompleted()).toList();
            case "completed" ->
                    todos.stream().filter(Todo::isCompleted).toList();
            default -> new ArrayList<>(todos);
        };
    }

    public List<Todo> getTodos() { return todos; }
    public String getFilter() { return filter; }
}