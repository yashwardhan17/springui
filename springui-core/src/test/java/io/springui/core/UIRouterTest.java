package io.springui.core;

import io.springui.core.annotation.Route;
import io.springui.core.annotation.SpringUIRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class UIRouterTest {

    // ===========================
    // Test components
    // ===========================

    static class HomeComponent extends UIComponent {
        @Override public VNode render() {
            return VNode.element("div").child(VNode.text("Home"));
        }
    }

    static class AboutComponent extends UIComponent {
        @Override public VNode render() {
            return VNode.element("div").child(VNode.text("About"));
        }
    }

    static class ProductDetailComponent extends UIComponent {
        @Override public VNode render() {
            return VNode.element("div").child(VNode.text("Product"));
        }
    }

    static class LoginComponent extends UIComponent {
        @Override public VNode render() {
            return VNode.element("div").child(VNode.text("Login"));
        }
    }

    static class ProtectedComponent extends UIComponent {
        @Override public VNode render() {
            return VNode.element("div").child(VNode.text("Protected"));
        }
    }

    // ===========================
    // Test router
    // ===========================

    @SpringUIRouter(basePath = "/", hashMode = false)
    static class TestRouter extends UIRouter {

        @Route("/")
        public Class<? extends UIComponent> home() {
            return HomeComponent.class;
        }

        @Route(value = "/about", title = "About Us")
        public Class<? extends UIComponent> about() {
            return AboutComponent.class;
        }

        @Route("/product/:id")
        public Class<? extends UIComponent> productDetail() {
            return ProductDetailComponent.class;
        }

        @Route("/user/:userId/posts/:postId")
        public Class<? extends UIComponent> userPost() {
            return AboutComponent.class;
        }

        @Route(value = "/protected",
                requiresAuth = true,
                loginPath = "/login")
        public Class<? extends UIComponent> protectedRoute() {
            return ProtectedComponent.class;
        }

        @Route("/login")
        public Class<? extends UIComponent> login() {
            return LoginComponent.class;
        }
    }

    // Unauthenticated router
    @SpringUIRouter
    static class UnauthenticatedRouter extends UIRouter {

        @Route(value = "/protected", requiresAuth = true, loginPath = "/login")
        public Class<? extends UIComponent> protectedRoute() {
            return ProtectedComponent.class;
        }

        @Route("/login")
        public Class<? extends UIComponent> login() {
            return LoginComponent.class;
        }

        @Override
        protected boolean isAuthenticated() {
            return false;
        }
    }

    private TestRouter router;

    @BeforeEach
    void setUp() {
        router = new TestRouter();
        router.initialize();
    }

    // ===========================
    // Initialization
    // ===========================

    @Test
    void shouldRegisterAllRoutes() {
        assertEquals(6, router.getRouteCount());
    }

    @Test
    void shouldStartAtRootPath() {
        assertEquals("/", router.getCurrentPath());
    }

    // ===========================
    // Static routes
    // ===========================

    @Test
    void shouldNavigateToRoot() {
        NavigationResult result = router.navigate("/");
        assertTrue(result.isSuccess());
        assertEquals(HomeComponent.class, result.getComponentClass());
    }

    @Test
    void shouldNavigateToAbout() {
        NavigationResult result = router.navigate("/about");
        assertTrue(result.isSuccess());
        assertEquals(AboutComponent.class, result.getComponentClass());
    }

    @Test
    void shouldUpdateCurrentPath() {
        router.navigate("/about");
        assertEquals("/about", router.getCurrentPath());
    }

    @Test
    void shouldReturnFailureForUnknownPath() {
        NavigationResult result = router.navigate("/unknown");
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    // ===========================
    // Path parameters
    // ===========================

    @Test
    void shouldExtractSinglePathParam() {
        NavigationResult result = router.navigate("/product/42");
        assertTrue(result.isSuccess());
        assertEquals(ProductDetailComponent.class, result.getComponentClass());
        assertEquals("42", result.getPathParams().get("id"));
    }

    @Test
    void shouldExtractMultiplePathParams() {
        NavigationResult result = router.navigate("/user/123/posts/456");
        assertTrue(result.isSuccess());
        assertEquals("123", result.getPathParams().get("userId"));
        assertEquals("456", result.getPathParams().get("postId"));
    }

    @Test
    void shouldHaveEmptyParamsForStaticRoutes() {
        NavigationResult result = router.navigate("/about");
        assertTrue(result.isSuccess());
        assertTrue(result.getPathParams().isEmpty());
    }

    // ===========================
    // Auth guard
    // ===========================

    @Test
    void shouldAllowAccessWhenAuthenticated() {
        NavigationResult result = router.navigate("/protected");
        assertTrue(result.isSuccess());
        assertEquals(ProtectedComponent.class, result.getComponentClass());
    }

    @Test
    void shouldRedirectWhenNotAuthenticated() {
        UnauthenticatedRouter unauthRouter = new UnauthenticatedRouter();
        unauthRouter.initialize();
        NavigationResult result = unauthRouter.navigate("/protected");
        assertTrue(result.isSuccess());
        assertEquals(LoginComponent.class, result.getComponentClass());
    }

    // ===========================
    // Navigation listener
    // ===========================

    @Test
    void shouldNotifyNavigationListener() {
        AtomicBoolean notified = new AtomicBoolean(false);
        router.addNavigationListener((from, to, result) ->
                notified.set(true));
        router.navigate("/about");
        assertTrue(notified.get());
    }

    @Test
    void shouldPassCorrectPathsToListener() {
        AtomicReference<String> fromPath = new AtomicReference<>();
        AtomicReference<String> toPath = new AtomicReference<>();
        router.addNavigationListener((from, to, result) -> {
            fromPath.set(from);
            toPath.set(to);
        });
        router.navigate("/about");
        assertEquals("/", fromPath.get());
        assertEquals("/about", toPath.get());
    }

    // ===========================
    // Route matching
    // ===========================

    @Test
    void shouldMatchStaticRoute() {
        RouteMatch match = router.matchRoute("/about");
        assertNotNull(match);
        assertEquals(AboutComponent.class,
                match.getDefinition().getComponentClass());
    }

    @Test
    void shouldMatchParameterizedRoute() {
        RouteMatch match = router.matchRoute("/product/99");
        assertNotNull(match);
        assertEquals("99", match.getParam("id"));
    }

    @Test
    void shouldReturnNullForNoMatch() {
        RouteMatch match = router.matchRoute("/nonexistent");
        assertNull(match);
    }
}