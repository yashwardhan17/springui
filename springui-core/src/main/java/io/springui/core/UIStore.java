package io.springui.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base class for all SpringUI global stores.
 * Annotate your subclass with @SpringUIStore.
 *
 * Usage:
 * <pre>
 *   @SpringUIStore
 *   public class CartStore extends UIStore {
 *       @State
 *       private List<CartItem> items = new ArrayList<>();
 *
 *       public void addItem(CartItem item) {
 *           dispatch(() -> items.add(item));
 *       }
 *   }
 * </pre>
 */
public abstract class UIStore {

    // Listeners keyed by subscriber ID
    private final Map<String, Runnable> listeners = new ConcurrentHashMap<>();
    private final List<Object> dispatchLog = new CopyOnWriteArrayList<>();
    private boolean destroyed = false;

    /**
     * Dispatch a state mutation. Notifies all subscribed components after mutation.
     * Mirrors UIComponent.setState() — same pattern, global scope.
     */
    protected void dispatch(Runnable mutation) {
        if (destroyed) {
            throw new IllegalStateException(
                    "[SpringUI] Attempted to dispatch on a destroyed store: " + getClass().getSimpleName()
            );
        }
        mutation.run();
        dispatchLog.add(mutation);
        notifyListeners();
    }

    /**
     * Subscribe a component to state changes in this store.
     * Returns a subscription ID that can be used to unsubscribe.
     */
    public String subscribe(String subscriberId, Runnable onStateChange) {
        listeners.put(subscriberId, onStateChange);
        System.out.println("[SpringUI] Store[" + getClass().getSimpleName()
                + "] — subscribed: " + subscriberId);
        return subscriberId;
    }

    /**
     * Unsubscribe a component from this store.
     */
    public void unsubscribe(String subscriberId) {
        listeners.remove(subscriberId);
        System.out.println("[SpringUI] Store[" + getClass().getSimpleName()
                + "] — unsubscribed: " + subscriberId);
    }

    /**
     * Returns the number of active subscribers.
     */
    public int subscriberCount() {
        return listeners.size();
    }

    /**
     * Returns how many dispatches have been made to this store.
     */
    public int dispatchCount() {
        return dispatchLog.size();
    }

    /**
     * Destroy the store — clears all listeners and marks it as dead.
     * Called by StoreRegistry on full reset.
     */
    public void destroy() {
        listeners.clear();
        dispatchLog.clear();
        destroyed = true;
        System.out.println("[SpringUI] Store[" + getClass().getSimpleName() + "] — destroyed");
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    private void notifyListeners() {
        listeners.forEach((id, listener) -> {
            System.out.println("[SpringUI] Store[" + getClass().getSimpleName()
                    + "] — notifying: " + id);
            listener.run();
        });
    }
}