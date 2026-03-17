package io.springui.examples.todo;

import io.springui.core.SpringUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TodoComponentTest {

    private TodoComponent todo;

    @BeforeEach
    void setUp() {
        SpringUI.reset();
        todo = new TodoComponent();
        SpringUI.render("todo-app", todo);
    }

    @Test
    void shouldMountWithSampleTodos() {
        assertEquals(3, todo.getTodos().size());
    }

    @Test
    void shouldAddTodo() {
        int before = todo.getTodos().size();
        todo.addTodo("New task");
        assertEquals(before + 1, todo.getTodos().size());
    }

    @Test
    void shouldCompleteTodo() {
        todo.completeTodo(1);
        assertTrue(todo.getTodos().get(0).isCompleted());
    }

    @Test
    void shouldRemoveTodo() {
        int before = todo.getTodos().size();
        todo.removeTodo(1);
        assertEquals(before - 1, todo.getTodos().size());
    }

    @Test
    void shouldFilterActiveTodos() {
        todo.completeTodo(1);
        todo.setFilter("active");
        assertEquals("active", todo.getFilter());
    }

    @Test
    void shouldFilterCompletedTodos() {
        todo.completeTodo(1);
        todo.setFilter("completed");
        assertEquals("completed", todo.getFilter());
    }

    @Test
    void shouldClearCompletedTodos() {
        todo.completeTodo(1);
        todo.completeTodo(2);
        todo.clearCompleted();
        assertTrue(todo.getTodos().stream().noneMatch(Todo::isCompleted));
    }

    @Test
    void shouldRenderCorrectVNodeStructure() {
        var vnode = todo.render();
        assertEquals("div", vnode.getTag());
        assertEquals("todo-app", vnode.getAttrs().get("id"));
    }
}