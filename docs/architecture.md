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
| TodoComponent | 8 |
| **Total** | **75** |

---

## Phase 2 — What's Next

Phase 1 (core framework) is complete. Phase 2 is the compiler pipeline:

- **TeaVM integration** — compile SpringUI components to WebAssembly
- **JS interop layer** — bridge between WASM and real browser DOM APIs
- **`@BindAPI`** — auto-wire components to Spring Boot REST endpoints
- **`@SpringUIRouter`** — client-side routing
- **`@SpringUIStore`** — global state management
- **Hot reload** — dev server with live component updates
- **Browser DevTools extension** — inspect SpringUI component trees

See Issue #3 for TeaVM research progress.

---

## Example — Todo App

A complete todo app built with SpringUI is available in `examples/todo-app/`.
It demonstrates the full stack working end to end — state management,
re-renders, filtering, and lifecycle hooks.
```java
TodoComponent todo = new TodoComponent();
SpringUI.render("todo-app", todo);

todo.addTodo("Learn SpringUI");
todo.completeTodo(1);
todo.setFilter("active");
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