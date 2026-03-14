# ☕ SpringUI

> **Write your frontend in Java. Ship to the browser via WebAssembly.**
> No JavaScript. No context switching. Just Spring, all the way down.

---

## The Problem

You know Spring Boot. You know your way around `@RestController`, JPA, Spring Security.
But the moment you need a UI, the industry tells you to stop — learn React, learn TypeScript,
learn Webpack, learn npm, learn an entirely different mental model.

**SpringUI says no.**

---

## What is SpringUI?

SpringUI is a component-based frontend framework for Java developers, inspired by React and Vue,
built on top of WebAssembly. You write UI components in Java. SpringUI compiles them to `.wasm`
and runs them in the browser — with direct, annotation-driven binding to your Spring Boot backend.

One language. One team. One build system.

---

## Quick Look

```java
@SpringUIComponent
public class Counter extends UIComponent {

    @State
    private int count = 0;

    @Override
    public VNode render() {
        return div(
            h1("Count: " + count),
            button("Increment").onClick(e -> setState(() -> count++)),
            button("Reset").onClick(e -> setState(() -> count = 0))
        );
    }
}
```

That's it. No `useState`. No JSX. No `npm install`. Just Java.

---

## Architecture

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

## Core Features

### `@SpringUIComponent`
Define a component exactly like a Spring bean. Lifecycle methods, dependency injection, it all works.

```java
@SpringUIComponent
public class UserCard extends UIComponent {

    @Props
    private User user;

    @Override
    public VNode render() {
        return div(attrs("class", "card"),
            img(attrs("src", user.getAvatarUrl())),
            h2(user.getName()),
            p(user.getBio())
        );
    }
}
```

---

### `@State` and Reactivity
State changes trigger surgical DOM updates — no full re-renders.

```java
@State
private List<String> todos = new ArrayList<>();

public void addTodo(String text) {
    setState(() -> todos.add(text));
}
```

---

### `@BindAPI` — The killer feature
Auto-wire a component directly to a Spring Boot REST endpoint. No `fetch()`. No axios.
Spring Security, validation, and error handling all flow through naturally.

```java
@SpringUIComponent
@BindAPI("/api/products")
public class ProductList extends UIComponent {

    @AutoFetched
    private List<Product> products;   // populated automatically on mount

    @Override
    public VNode render() {
        return ul(
            products.stream()
                .map(p -> li(p.getName() + " — ₹" + p.getPrice()))
                .toList()
        );
    }
}
```

On the backend, nothing special — just a normal Spring controller:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }
}
```

---

### `@SpringUIRouter`
Client-side routing, Spring style.

```java
@SpringUIRouter
public class AppRouter extends UIRouter {

    @Route("/")
    public Class<? extends UIComponent> home() { return HomePage.class; }

    @Route("/products")
    public Class<? extends UIComponent> products() { return ProductList.class; }

    @Route("/product/:id")
    public Class<? extends UIComponent> productDetail() { return ProductDetail.class; }
}
```

---

### `@SpringUIStore`
Global state management. Think Redux — in Java.

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

---

## Getting Started

### Add to your Spring Boot project

**Maven:**
```xml
<dependency>
    <groupId>io.springui</groupId>
    <artifactId>springui-core</artifactId>
    <version>0.1.0-alpha</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'io.springui:springui-core:0.1.0-alpha'
```

---

### Configure

```java
@SpringBootApplication
@EnableSpringUI
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

```yaml
# application.yml
springui:
  output-dir: src/main/resources/static
  hot-reload: true
  wasm-target: browser
```

---

### Run

```bash
./mvnw spring-boot:run
```

SpringUI compiles your components to WASM on startup and serves them alongside your Spring Boot app.
Open `http://localhost:8080` — your Java UI is live in the browser.

---

## Comparison

| Feature | SpringUI | React + Spring Boot | Vaadin |
|---|---|---|---|
| Language | Java only | Java + JavaScript | Java only |
| Runtime | WASM in browser | JS in browser | Server-side + JS |
| Spring integration | Native (`@BindAPI`) | Manual (REST/axios) | Native |
| Component model | React-inspired | React | Vaadin Web Components |
| Build complexity | Single Maven/Gradle build | Separate frontend build | Single build |
| Learning curve for Java devs | Low | High | Medium |
| Ecosystem maturity | Early (alpha) | Massive | Mature |

---

## Why Not Just Use Vaadin?

Vaadin is great, but its UI model is server-side — every interaction round-trips to the server.
SpringUI renders entirely in the browser via WASM. Once loaded, it's as fast and offline-capable
as any React app. Think of it as the bridge between Vaadin's Java comfort and React's browser-native performance.

---

## Why Not Thymeleaf?

Thymeleaf is excellent for simple server-rendered pages. But it has 
a fundamental ceiling — every interaction requires a server round-trip, 
there's no component model, no reactive state, and no SPA capability.

| | Thymeleaf | SpringUI |
|---|---|---|
| Rendering | Server-side | Client-side (WASM) |
| Interactivity | Full page reloads | Reactive, like React |
| Offline capable | No | Yes |
| Component model | No | Yes |
| Real-time UI updates | Needs workarounds | Native |

Thymeleaf and SpringUI aren't competitors — they solve different problems. 
SpringUI is for when your UI needs to feel like a modern web app, not a 
server-rendered page.

---

## Roadmap

- [x] Core component model (`@SpringUIComponent`, `@State`, `@Props`)
- [x] Virtual DOM + diff engine
- [x] TeaVM-based WASM compiler integration
- [x] `@BindAPI` REST auto-wiring
- [ ] `@SpringUIRouter` — client-side routing
- [ ] `@SpringUIStore` — global state management
- [ ] Hot reload dev server
- [ ] Browser DevTools extension
- [ ] SpringUI component library (buttons, forms, tables, modals)
- [ ] GraphQL support via Spring GraphQL
- [ ] SSR (Server-Side Rendering) mode
- [ ] `springui-cli` scaffold tool

---

## Contributing

SpringUI is in early alpha and **we need contributors**.

If you're a Java developer who's ever been frustrated by having to context-switch to JavaScript
just to build a UI — this is your project.

See [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

---

## Philosophy

> The frontend/backend split shouldn't be a language split.
>
> Spring Boot proved that Java can be productive, elegant, and modern on the server.
> SpringUI is the bet that it can do the same in the browser.

---

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

---

<p align="center">
  Built by Java developers, for Java developers.<br/>
  <strong>Because you shouldn't have to learn JavaScript to build a UI.</strong>
</p>
