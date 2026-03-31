# SpringUI Architecture

> A deep-dive into how SpringUI works internally.
> For contributors and developers who want to understand the framework.

---

## Overview

SpringUI is a React-inspired frontend framework for Java developers.
Write UI components in Java, compile to WebAssembly, run in the browser.
No JavaScript required.

The framework is built in 5 layers:
```
┌─────────────────────────────────────────────────┐
│              Developer writes Java               │
│         @SpringUIComponent, @State, @Props       │
└─────────────────────┬───────────────────────────┘
                      │ compile
                      ▼
┌─────────────────────────────────────────────────┐
│           TeaVM / GraalVM Toolchain              │
│     Java bytecode → WebAssembly + JS glue        │
└─────────────────────┬───────────────────────────┘
                      │ runs in browser
                      ▼
┌─────────────────────────────────────────────────┐
│              Browser Runtime (WASM)              │
│   Virtual DOM  →  Diff Engine  →  DOM Patcher    │
└─────────────────────┬───────────────────────────┘
                      │ HTTP / GraphQL
                      ▼
┌─────────────────────────────────────────────────┐
│            Spring Boot Backend                   │
│   @BindAPI auto-wires components to endpoints    │
└─────────────────────┬───────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│              Data Layer                          │
│     PostgreSQL · MongoDB · MySQL via Spring Data │
└─────────────────────────────────────────────────┘
```

---

## Core Classes (Phase 1 — Complete)

### `VNode.java`
The base building block of SpringUI's Virtual DOM.
Represents a single element in the UI tree — mirroring a real browser DOM node.

Fields:
- `tag` — the HTML element type (div, h1, button, etc.)
- `attrs` — HTML attributes (class, id, style, etc.)
- `children` — child VNodes forming the tree
- `textContent` — for text-only nodes
- `key` — optional, used by the diff engine for efficient updates

Usage:
```java
VNode node = VNode.element("div")
        .attr("class", "card")
        .child(VNode.element("h1").child(VNode.text("Hello")));
```

---

### `VNodeDiffer.java`
The diff engine. Compares two VNode trees (old vs new) and produces
a minimal list of `Patch` objects describing exactly what changed.

Patch types:
- `REPLACE` — node type changed entirely
- `UPDATE_ATTRS` — same node, attributes changed
- `ADD_CHILD` — new child added
- `REMOVE_CHILD` — child removed
- `TEXT_CHANGE` — text content changed

Usage:
```java
VNodeDiffer differ = new VNodeDiffer();
List<Patch> patches = differ.diff(oldTree, newTree);
```

---

### `UIComponent.java`
The base class every SpringUI component extends.
Inspired by React's Component model.

Key methods:
- `render()` — returns the VNode tree for the current state (must implement)
- `setState()` — updates state and triggers a re-render
- `onMount()` — lifecycle hook, called when component is added to DOM
- `onUnmount()` — lifecycle hook, called when component is removed
- `onUpdate()` — lifecycle hook, called after every re-render

Re-render flow:
```
setState() → render() → VNodeDiffer.diff() → PatchApplier.applyPatches()
```

---

### `PatchApplier.java`
Takes patches from `VNodeDiffer` and applies them to the real browser DOM.

Phase 1 (current): simulates DOM operations with descriptive logs.
Phase 2 (WASM): calls real browser DOM APIs via TeaVM JS interop.

DOM operations:
- `domInsertNode()` → `document.createElement()` + `appendChild()`
- `domRemoveNode()` → `element.parentNode.removeChild()`
- `domUpdateAttrs()` → `element.setAttribute()`
- `domUpdateText()` → `textNode.nodeValue = newText`

---

### `ComponentRegistry.java`
The central hub of SpringUI. Manages the lifecycle of all mounted components.
Implemented as a thread-safe Singleton using `ConcurrentHashMap`.

Responsibilities:
- Register and mount components
- Unmount and deregister components
- Look up components by ID
- Track all active components

---

### `SpringUI.java`
The main entry point of the framework.
Equivalent of `ReactDOM.render()` in React.
```java
// Mount a component
SpringUI.render("app", new AppComponent());

// Unmount a component
        SpringUI.unmount("app");

// Check if mounted
SpringUI.isMounted("app");

// Reset everything (useful for hot reload)
SpringUI.reset();
```

---

### `VNodeBuilder.java`
A fluent DSL for building VNode trees cleanly.
Every `UIComponent` extends this automatically.

Instead of:
```java
VNode.element("div", Map.of("class", "card"), List.of(
    VNode.element("h1", Map.of(), List.of(VNode.text("Title")))
))
```

You write:
```java
div(attrs("class", "card"),
    h1("Title")
)
```

Supported elements: `div, span, p, h1, h2, h3, ul, ol, li, button,
input, form, img, a, nav, header, footer, section, article, main`

---

### `SpringUIContext.java`
The application context — bootstraps the entire SpringUI framework.
Ties together ComponentScanner, ComponentRegistry, and SpringUI entry point.

```java
SpringUIContext.create("io.myapp")
    .register(AppComponent.class)
    .register(ProductList.class)
    .devMode(true)
    .start();
```

---

## Annotation System (Phase 3 — Complete)

### `@SpringUIComponent`
Marks a class as a SpringUI component. Picked up by ComponentScanner
and auto-registered in ComponentRegistry on startup.

```java
@SpringUIComponent(id = "counter", root = true, mountTarget = "root")
public class CounterComponent extends UIComponent {
    @Override
    public VNode render() {
        return div(h1("Counter"));
    }
}
```

---

### `@State`
Marks a field as reactive state. Any change via `setState()` triggers
a re-render and a diff against the previous VNode tree.

```java
@State
private int count = 0;

@State(persistent = true, name = "activeFilter")
private String filter = "all";
```

---

### `@Props`
Marks a field as a read-only input from a parent component.

```java
@Props(required = true)
private String title;

@Props(defaultValue = "default")
private String subtitle;
```

---

### `@BindAPI`
The killer feature. Auto-wires a component directly to a Spring Boot
REST endpoint. No `fetch()`. No axios. Spring Security flows through naturally.

```java
@SpringUIComponent
@BindAPI("/api/products")
public class ProductList extends UIComponent {

    @AutoFetched
    private List<Product> products;

    @Override
    public VNode render() {
        return ul(
            products.stream()
                .map(p -> li(p.getName()))
                .toList()
        );
    }
}
```

---

### `@SpringUIRouter` and `@Route`
Client-side routing, Spring style. Supports path parameters,
auth guards, and navigation listeners.

```java
@SpringUIRouter
public class AppRouter extends UIRouter {

    @Route("/")
    public Class<? extends UIComponent> home() { return HomeComponent.class; }

    @Route("/product/:id")
    public Class<? extends UIComponent> productDetail() {
        return ProductDetailComponent.class;
    }

    @Route(value = "/admin", requiresAuth = true, loginPath = "/login")
    public Class<? extends UIComponent> admin() { return AdminComponent.class; }
}
```

---

### `@SpringUIStore`
Global state management — think Redux in Java.
Any component can access shared store state without prop drilling.

```java
@SpringUIStore
public class CartStore extends UIStore {

    @State
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item) {
        dispatch(() -> items.add(item));
    }

    public int getTotal() {
        return items.stream().mapToInt(CartItem::getPrice).sum();
    }
}
```

Inject the store into any component:

```java
@Autowired
private CartStore cartStore;
```

Stores are registered as singletons in `StoreRegistry` on startup.
Components subscribe to store changes and re-render automatically
when `dispatch()` is called.

Store lifecycle:
```
dispatch() → state mutation → notifyListeners() → subscribed components re-render
```

---

## Compiler Pipeline (Phase 2 — Complete)

### `TeaVMCompiler.java`
Real TeaVM integration. Compiles SpringUI components (Java bytecode)
to WebAssembly using TeaVM's `TeaVMTool` API.

```java
TeaVMCompiler compiler = new TeaVMCompiler(
    new TeaVMCompilerConfig.Builder()
        .outputDir("./springui-out")
        .target(TeaVMCompilerConfig.Target.JAVASCRIPT)
        .sourceMaps(true)
        .build()
);
TeaVMCompilationResult result = compiler.compile("io.myapp.TodoApp");
```

---

### `JSInteropGenerator.java`
Generates the JavaScript glue layer that bridges the WASM module
with the browser DOM. Produces a `.js` file that:
- Loads the `.wasm` binary
- Exposes browser DOM APIs to WASM
- Initializes SpringUI in the browser
- Mounts the root component

---

### `WasmBridge.java`
Runtime bridge between SpringUI's WASM module and the browser.
Manages WASM exports, routes DOM events back into WASM handlers,
and tracks registered event callbacks.

---

## DevTools (Complete)

### `HotReloadServer.java`
Watches source files for changes and triggers automatic recompilation.
Notifies connected browsers via WebSocket for live component updates.

### `ComponentInspector.java`
Runtime inspector — inspect any mounted component, see its state,
props, and current VNode tree. The Java equivalent of React DevTools.

---

## Data Flow

Here's exactly what happens when a user interaction triggers a state change:
```
1. User clicks button
         │
         ▼
2. Component.setState(() -> count++)
         │
         ▼
3. render() called → produces new VNode tree
         │
         ▼
4. VNodeDiffer.diff(oldTree, newTree) → List<Patch>
         │
         ▼
5. PatchApplier.applyPatches(patches)
         │
         ▼
6. Real browser DOM updated surgically
   (only what changed, nothing more)
         │
         ▼
7. onUpdate() lifecycle hook called
```

This is exactly how React works internally —
SpringUI brings the same model to Java via WebAssembly.

---

## Test Coverage

| Class | Tests |
|---|---|
| VNode | 11 |
| VNodeDiffer | 11 |
| UIComponent | 11 |
| PatchApplier | 7 |
| ComponentRegistry | 12 |
| SpringUI | 7 |
| VNodeBuilder | 8 |
| SpringUIContext | 12 |
| AnnotationProcessor | 15 |
| ComponentScanner | 14 |
| UIRouter | 16 |
| UIStore          | 12 |
| StoreRegistry    | 12 |
| JSInteropGenerator | 11 |
| SpringUICompiler | 8 |
| TeaVMCompiler | 9 |
| WasmBridge | 14 |
| HotReloadServer | 14 |
| ComponentInspector | 8 |
| TodoComponent | 12 |
| **Total** | **224** |

---

## What's Next

- **`#25 index.html`** — browser entry point, first thing that runs in a real browser
- **`springui-cli`** — scaffold tool for new SpringUI projects
- **GraphQL support** — via Spring GraphQL
- **SSR mode** — server-side rendering
- **Browser DevTools extension** — visual component tree inspector

---

## Example — Todo App

A complete todo app built with SpringUI is in `examples/todo-app/`.
Fully annotation-driven using `@SpringUIComponent`, `@State`, and `SpringUIContext`.

```java
SpringUIContext.create("io.springui.examples.todo")
    .register(TodoComponent.class)
    .devMode(true)
    .start();
```

Output:
```
[SpringUI] Patch[TEXT_CHANGE] — Text changed: '0 active' -> '1 active'
[SpringUI] Patch[ADD_CHILD] — New node added: VNode[tag=li]
[SpringUI] Patch[UPDATE_ATTRS] — class: active -> completed
```

---

*SpringUI is built by Java developers, for Java developers.*
*Because you shouldn't have to learn JavaScript to build a UI.*