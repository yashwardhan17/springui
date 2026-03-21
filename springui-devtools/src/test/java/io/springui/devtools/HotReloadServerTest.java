package io.springui.devtools;

import io.springui.core.SpringUI;
import io.springui.core.UIComponent;
import io.springui.core.VNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class HotReloadServerTest {

    static class TestComponent extends UIComponent {
        @Override
        public VNode render() {
            return VNode.element("div").child(VNode.text("Test"));
        }
    }

    private HotReloadServer server;

    @BeforeEach
    void setUp() {
        SpringUI.reset();
        HotReloadConfig config = new HotReloadConfig.Builder()
                .watchDir("./src/main/java")
                .outputDir("./target/springui-hot-out")
                .port(8081)
                .pollIntervalMs(500)
                .build();
        server = new HotReloadServer(config);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server.isRunning()) server.stop();
        SpringUI.reset();
    }

    // ===========================
    // Start / Stop
    // ===========================

    @Test
    void shouldBeRunningAfterStart() {
        assertTrue(server.isRunning());
    }

    @Test
    void shouldNotBeRunningAfterStop() {
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    void shouldThrowWhenStartedTwice() {
        assertThrows(DevToolsException.class, () -> server.start());
    }

    @Test
    void shouldThrowWhenReloadingWhileStopped() {
        server.stop();
        assertThrows(DevToolsException.class, () ->
                server.triggerReload(TestComponent.class));
    }

    // ===========================
    // Hot Reload
    // ===========================

    @Test
    void shouldTriggerReloadSuccessfully() {
        ReloadEvent event = server.triggerReload(TestComponent.class);
        assertTrue(event.isSuccess());
    }

    @Test
    void shouldRecordReloadInHistory() {
        server.triggerReload(TestComponent.class);
        assertEquals(1, server.getReloadHistory().size());
    }

    @Test
    void shouldRecordMultipleReloads() {
        server.triggerReload(TestComponent.class);
        server.triggerReload(TestComponent.class);
        assertEquals(2, server.getReloadHistory().size());
    }

    @Test
    void shouldNotifyReloadListener() {
        AtomicBoolean notified = new AtomicBoolean(false);
        server.addReloadListener(event -> notified.set(true));
        server.triggerReload(TestComponent.class);
        assertTrue(notified.get());
    }

    @Test
    void shouldPassReloadEventToListener() {
        server.addReloadListener(event -> {
            assertTrue(event.isSuccess());
            assertTrue(event.getComponentClass().contains("TestComponent"));
        });
        server.triggerReload(TestComponent.class);
    }

    // ===========================
    // Client Management
    // ===========================

    @Test
    void shouldStartWithNoClients() {
        assertEquals(0, server.getConnectedClientCount());
    }

    @Test
    void shouldTrackConnectedClients() {
        server.connectClient("browser-1");
        server.connectClient("browser-2");
        assertEquals(2, server.getConnectedClientCount());
    }

    @Test
    void shouldRemoveDisconnectedClients() {
        server.connectClient("browser-1");
        server.disconnectClient("browser-1");
        assertEquals(0, server.getConnectedClientCount());
    }

    @Test
    void shouldNotifyConnectedClientsOnReload() {
        server.connectClient("browser-1");
        server.connectClient("browser-2");
        ReloadEvent event = server.triggerReload(TestComponent.class);
        assertTrue(event.isSuccess());
        assertEquals(2, server.getConnectedClientCount());
    }

    // ===========================
    // Config
    // ===========================

    @Test
    void shouldHaveCorrectConfig() {
        HotReloadConfig config = server.getConfig();
        assertEquals(8081, config.getPort());
        assertEquals(500, config.getPollIntervalMs());
    }
}