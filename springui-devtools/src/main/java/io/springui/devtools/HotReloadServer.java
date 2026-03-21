package io.springui.devtools;

import io.springui.compiler.CompilationResult;
import io.springui.compiler.CompilerConfig;
import io.springui.compiler.SpringUICompiler;
import io.springui.core.SpringUI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HotReloadServer — watches for component changes and triggers
 * automatic recompilation and browser refresh.
 *
 * In development mode, SpringUI watches your source files.
 * When a component changes:
 * 1. Recompiles the changed component to WASM
 * 2. Notifies connected browsers via WebSocket
 * 3. Browser reloads just the changed component — no full page refresh
 *
 * Phase 1 (current): simulates hot reload with file watching logic
 * Phase 2 (browser): real WebSocket server + browser client
 */
public class HotReloadServer {

    // ===========================
    // Configuration
    // ===========================

    private final HotReloadConfig config;
    private final SpringUICompiler compiler;

    // ===========================
    // State
    // ===========================

    private boolean running = false;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> watchTask;

    // Connected browser sessions (Phase 2: real WebSocket sessions)
    private final List<String> connectedClients = new ArrayList<>();

    // Reload listeners — called when a reload happens
    private final List<ReloadListener> reloadListeners = new ArrayList<>();

    // Reload history
    private final List<ReloadEvent> reloadHistory = new ArrayList<>();

    // ===========================
    // Constructor
    // ===========================

    public HotReloadServer(HotReloadConfig config) {
        this.config = config;
        this.compiler = new SpringUICompiler(
                new CompilerConfig.Builder()
                        .outputDir(config.getOutputDir())
                        .build()
        );
    }

    // ===========================
    // Start / Stop
    // ===========================

    /**
     * Starts the hot reload server.
     * Begins watching source files for changes.
     */
    public void start() {
        if (running) {
            throw new DevToolsException("HotReloadServer is already running.");
        }

        log("Starting SpringUI HotReload Server...");
        log("Watching: " + config.getWatchDir());
        log("Port: " + config.getPort());
        log("Poll interval: " + config.getPollIntervalMs() + "ms");

        scheduler = Executors.newSingleThreadScheduledExecutor();
        watchTask = scheduler.scheduleAtFixedRate(
                this::checkForChanges,
                0,
                config.getPollIntervalMs(),
                TimeUnit.MILLISECONDS
        );

        running = true;
        log("✓ HotReload Server started. Waiting for changes...");
    }

    /**
     * Stops the hot reload server.
     */
    public void stop() {
        if (!running) return;

        log("Stopping HotReload Server...");
        if (watchTask != null) watchTask.cancel(true);
        if (scheduler != null) scheduler.shutdown();

        running = false;
        connectedClients.clear();
        log("✓ HotReload Server stopped.");
    }

    // ===========================
    // File Watching
    // ===========================

    /**
     * Checks for changed source files.
     * Phase 2: uses Java WatchService for real file system events.
     */
    private void checkForChanges() {
        // Phase 2: real file watching via WatchService:
        // WatchService watcher = FileSystems.getDefault().newWatchService();
        // Path watchPath = Path.of(config.getWatchDir());
        // watchPath.register(watcher, ENTRY_MODIFY);
        // WatchKey key = watcher.poll();
        // key.pollEvents().forEach(event -> handleChange(event.context()));
    }

    /**
     * Triggers a hot reload for a specific component.
     * Called when a source file change is detected.
     */
    public ReloadEvent triggerReload(Class<?> componentClass) {
        if (!running) {
            throw new DevToolsException("HotReloadServer is not running.");
        }

        log("Change detected in: " + componentClass.getSimpleName());
        log("Recompiling...");

        long startTime = System.currentTimeMillis();

        try {
            // Step 1 — Recompile the component
            CompilationResult result = compiler.compile(componentClass);

            // Step 2 — Unmount old component instance
            String componentId = componentClass.getSimpleName().toLowerCase();
            if (SpringUI.isMounted(componentId)) {
                SpringUI.unmount(componentId);
                log("Unmounted old component: " + componentId);
            }

            // Step 3 — Notify connected browsers
            notifyClients(componentClass.getSimpleName());

            long duration = System.currentTimeMillis() - startTime;

            ReloadEvent event = new ReloadEvent(
                    componentClass.getName(),
                    true,
                    duration,
                    null
            );

            reloadHistory.add(event);
            reloadListeners.forEach(l -> l.onReload(event));

            log("✓ Hot reload complete in " + duration + "ms");
            log("  Notified " + connectedClients.size() + " client(s)");

            return event;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            ReloadEvent event = new ReloadEvent(
                    componentClass.getName(),
                    false,
                    duration,
                    e.getMessage()
            );
            reloadHistory.add(event);
            log("✗ Hot reload failed: " + e.getMessage());
            return event;
        }
    }

    // ===========================
    // Client Management
    // ===========================

    /**
     * Registers a connected browser client.
     * Phase 2: real WebSocket session ID.
     */
    public void connectClient(String clientId) {
        connectedClients.add(clientId);
        log("Client connected: " + clientId +
                " (" + connectedClients.size() + " total)");
    }

    /**
     * Removes a disconnected browser client.
     */
    public void disconnectClient(String clientId) {
        connectedClients.remove(clientId);
        log("Client disconnected: " + clientId +
                " (" + connectedClients.size() + " remaining)");
    }

    /**
     * Notifies all connected clients to reload a component.
     * Phase 2: sends WebSocket message to each client.
     */
    private void notifyClients(String componentName) {
        String message = "{\"type\":\"reload\",\"component\":\"" +
                componentName + "\"}";
        connectedClients.forEach(client ->
                log("  → Notifying client " + client + ": " + message));
    }

    // ===========================
    // Reload Listeners
    // ===========================

    public void addReloadListener(ReloadListener listener) {
        reloadListeners.add(listener);
    }

    public void removeReloadListener(ReloadListener listener) {
        reloadListeners.remove(listener);
    }

    // ===========================
    // State Queries
    // ===========================

    public boolean isRunning() { return running; }
    public int getConnectedClientCount() { return connectedClients.size(); }
    public List<ReloadEvent> getReloadHistory() { return reloadHistory; }
    public HotReloadConfig getConfig() { return config; }

    // ===========================
    // Logging
    // ===========================

    private void log(String message) {
        System.out.println("[HotReloadServer] " + message);
    }

    // ===========================
    // Listener interface
    // ===========================

    @FunctionalInterface
    public interface ReloadListener {
        void onReload(ReloadEvent event);
    }
}