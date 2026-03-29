package io.springui.examples.todo;

import io.springui.core.SpringUI;
import io.springui.core.SpringUIContext;
import io.springui.core.annotation.AnnotationProcessor;
import io.springui.core.annotation.ComponentMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TodoComponentTest {

    private TodoComponent todo;
    private SpringUIContext context;

    @BeforeEach
    void setUp() {
        SpringUI.reset();
        context = SpringUIContext.create("io.springui.examples.todo")
                .register(TodoComponent.class)
                .start();
        todo = (TodoComponent) SpringUI.getComponent("todo-app");
    }

    @AfterEach
    void tearDown() {
        if (context.isStarted()) context.stop();
        SpringUI.reset();
    }

    // ===========================
    // Annotation tests
    // ===========================

    @Test
    void shouldBeAnnotatedWithSpringUIComponent() {
        AnnotationProcessor processor = new AnnotationProcessor();
        ComponentMetadata meta = processor.process(TodoComponent.class);
        assertEquals("todo-app", meta.getComponentId());
        assertEquals("Todo App", meta.getDisplayName());
        assertTrue(meta.isRootComponent());
    }

    @Test
    void shouldHaveStateAnnotations() {
        AnnotationProcessor processor = new AnnotationProcessor();
        ComponentMetadata meta = processor.process(TodoComponent.class);
        assertTrue(meta.hasStateFields());
        assertEquals(3, meta.getStateFields().size());
    }

    @Test
    void shouldHavePersistentStateFields() {
        AnnotationProcessor processor = new AnnotationProcessor();
        ComponentMetadata meta = processor.process(TodoComponent.class);
        long persistentCount = meta.getStateFields().stream()
                .filter(f -> f.isPersistent())
                .count();
        assertEquals(2, persistentCount);
    }

    // ===========================
    // Context bootstrap tests
    // ===========================

    @Test
    void shouldBootstrapViaContext() {
        assertTrue(context.isStarted());
        assertEquals(1, context.getMountedCount());
    }

    @Test
    void shouldMountTodoAppViaContext() {
        assertTrue(SpringUI.isMounted("todo-app"));
    }

    // ===========================
    // Component behavior tests
    // ===========================

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
    void shouldClearCompletedTodos() {
        todo.completeTodo(1);
        todo.completeTodo(2);
        todo.clearCompleted();
        assertTrue(todo.getTodos().stream()
                .noneMatch(Todo::isCompleted));
    }

    @Test
    void shouldRenderCorrectVNodeStructure() {
        var vnode = todo.render();
        assertEquals("div", vnode.getTag());
        assertEquals("todo-app", vnode.getAttrs().get("id"));
    }
}