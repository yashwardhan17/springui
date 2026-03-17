package io.springui.examples.todo;

/**
 * Todo — simple data model representing a single todo item.
 */
public class Todo {

    private final int id;
    private final String text;
    private final boolean completed;

    public Todo(int id, String text, boolean completed) {
        this.id = id;
        this.text = text;
        this.completed = completed;
    }

    public Todo complete() {
        return new Todo(id, text, true);
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public boolean isCompleted() { return completed; }

    @Override
    public String toString() {
        return "Todo[" + id + "] " + (completed ? "✓" : "○") + " " + text;
    }
}