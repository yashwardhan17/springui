package io.springui.core;

import io.springui.core.annotation.SpringUIStore;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StoreRegistry")
class StoreRegistryTest {

    @SpringUIStore
    static class CartStore extends UIStore {}

    @SpringUIStore(name = "auth")
    static class AuthStore extends UIStore {}

    private StoreRegistry registry;

    @BeforeEach
    void setUp() {
        registry = StoreRegistry.getInstance();
        registry.reset();
    }

    // 1
    @Test
    @DisplayName("starts empty after reset")
    void startsEmpty() {
        assertEquals(0, registry.size());
    }

    // 2
    @Test
    @DisplayName("register adds a store")
    void registerAddsStore() {
        registry.register("cart", new CartStore());
        assertEquals(1, registry.size());
    }

    // 3
    @Test
    @DisplayName("get by name returns registered store")
    void getByNameReturnsStore() {
        CartStore cart = new CartStore();
        registry.register("cart", cart);
        Optional<CartStore> found = registry.get("cart");
        assertTrue(found.isPresent());
        assertSame(cart, found.get());
    }

    // 4
    @Test
    @DisplayName("get by class returns registered store")
    void getByClassReturnsStore() {
        CartStore cart = new CartStore();
        registry.register("cart", cart);
        Optional<CartStore> found = registry.get(CartStore.class);
        assertTrue(found.isPresent());
    }

    // 5
    @Test
    @DisplayName("get unregistered name returns empty")
    void getUnregisteredReturnsEmpty() {
        assertTrue(registry.get("nonexistent").isEmpty());
    }

    // 6
    @Test
    @DisplayName("register is idempotent — second call ignored")
    void registerIdempotent() {
        CartStore first = new CartStore();
        CartStore second = new CartStore();
        registry.register("cart", first);
        registry.register("cart", second);  // should be ignored
        assertSame(first, registry.<CartStore>get("cart").get());
        assertEquals(1, registry.size());
    }

    // 7
    @Test
    @DisplayName("isRegistered returns true for registered store")
    void isRegisteredTrue() {
        registry.register("cart", new CartStore());
        assertTrue(registry.isRegistered("cart"));
    }

    // 8
    @Test
    @DisplayName("isRegistered returns false for unknown store")
    void isRegisteredFalse() {
        assertFalse(registry.isRegistered("ghost"));
    }

    // 9
    @Test
    @DisplayName("multiple stores can be registered")
    void multipleStores() {
        registry.register("cart", new CartStore());
        registry.register("auth", new AuthStore());
        assertEquals(2, registry.size());
    }

    // 10
    @Test
    @DisplayName("reset clears all stores")
    void resetClearsAll() {
        registry.register("cart", new CartStore());
        registry.register("auth", new AuthStore());
        registry.reset();
        assertEquals(0, registry.size());
    }

    // 11
    @Test
    @DisplayName("reset destroys all stores")
    void resetDestroysAllStores() {
        CartStore cart = new CartStore();
        registry.register("cart", cart);
        registry.reset();
        assertTrue(cart.isDestroyed());
    }

    // 12
    @Test
    @DisplayName("all() returns all registered stores")
    void allReturnsAllStores() {
        registry.register("cart", new CartStore());
        registry.register("auth", new AuthStore());
        assertEquals(2, registry.all().size());
    }
}