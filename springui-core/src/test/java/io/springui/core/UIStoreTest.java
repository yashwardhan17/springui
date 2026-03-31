package io.springui.core;

import io.springui.core.annotation.SpringUIStore;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UIStore")
class UIStoreTest {

    // --- Concrete test store ---
    @SpringUIStore
    static class CartStore extends UIStore {
        private final List<String> items = new ArrayList<>();

        public void addItem(String item) {
            dispatch(() -> items.add(item));
        }

        public void removeItem(String item) {
            dispatch(() -> items.remove(item));
        }

        public List<String> getItems() {
            return List.copyOf(items);
        }

        public int getTotal() {
            return items.size();
        }
    }

    private CartStore store;

    @BeforeEach
    void setUp() {
        store = new CartStore();
    }

    // 1
    @Test
    @DisplayName("starts with zero subscribers")
    void startsWithZeroSubscribers() {
        assertEquals(0, store.subscriberCount());
    }

    // 2
    @Test
    @DisplayName("subscribe adds a listener")
    void subscribeAddsListener() {
        store.subscribe("comp-1", () -> {});
        assertEquals(1, store.subscriberCount());
    }

    // 3
    @Test
    @DisplayName("unsubscribe removes a listener")
    void unsubscribeRemovesListener() {
        store.subscribe("comp-1", () -> {});
        store.unsubscribe("comp-1");
        assertEquals(0, store.subscriberCount());
    }

    // 4
    @Test
    @DisplayName("dispatch mutates state")
    void dispatchMutatesState() {
        store.addItem("laptop");
        assertEquals(List.of("laptop"), store.getItems());
    }

    // 5
    @Test
    @DisplayName("dispatch notifies all subscribers")
    void dispatchNotifiesSubscribers() {
        List<String> called = new ArrayList<>();
        store.subscribe("comp-1", () -> called.add("comp-1"));
        store.subscribe("comp-2", () -> called.add("comp-2"));

        store.addItem("phone");

        assertEquals(2, called.size());
        assertTrue(called.contains("comp-1"));
        assertTrue(called.contains("comp-2"));
    }

    // 6
    @Test
    @DisplayName("multiple dispatches accumulate state")
    void multipleDispatchesAccumulateState() {
        store.addItem("a");
        store.addItem("b");
        store.addItem("c");
        assertEquals(3, store.getTotal());
    }

    // 7
    @Test
    @DisplayName("dispatch increments dispatchCount")
    void dispatchCountIncrements() {
        assertEquals(0, store.dispatchCount());
        store.addItem("x");
        store.addItem("y");
        assertEquals(2, store.dispatchCount());
    }

    // 8
    @Test
    @DisplayName("removeItem dispatches correctly")
    void removeItemDispatchesCorrectly() {
        store.addItem("laptop");
        store.removeItem("laptop");
        assertTrue(store.getItems().isEmpty());
    }

    // 9
    @Test
    @DisplayName("destroyed store throws on dispatch")
    void destroyedStoreThrowsOnDispatch() {
        store.destroy();
        assertThrows(IllegalStateException.class, () -> store.addItem("x"));
    }

    // 10
    @Test
    @DisplayName("destroy clears subscribers")
    void destroyClearsSubscribers() {
        store.subscribe("comp-1", () -> {});
        store.destroy();
        assertEquals(0, store.subscriberCount());
    }

    // 11
    @Test
    @DisplayName("isDestroyed returns true after destroy")
    void isDestroyedAfterDestroy() {
        assertFalse(store.isDestroyed());
        store.destroy();
        assertTrue(store.isDestroyed());
    }

    // 12
    @Test
    @DisplayName("multiple subscribers get independent callbacks")
    void multipleSubscribersGetIndependentCallbacks() {
        int[] countA = {0};
        int[] countB = {0};
        store.subscribe("a", () -> countA[0]++);
        store.subscribe("b", () -> countB[0]++);
        store.addItem("item");
        assertEquals(1, countA[0]);
        assertEquals(1, countB[0]);
    }
}