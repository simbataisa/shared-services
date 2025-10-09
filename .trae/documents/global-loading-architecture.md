# Global Loading Architecture

This document explains the loading system used across the frontend, including the global loader overlay, the Zustand-based loading store, and the reusable `LoadingSpinner` component. It provides guidance on when to rely on the global overlay and when to render a local page skeleton.

## Overview

- The global loader shows an overlay whenever there are active HTTP requests triggered through the shared Axios instance.
- A centralized loading store tracks the number of in-flight requests to decide when to display the overlay.
- Components can opt into a local page-level skeleton via `LoadingSpinner` for initial route loads.

## Architecture

- Store: `frontend/src/store/loading.ts`
  - `loadingCount`: integer counter of active requests
  - `isGlobalLoading`: derived boolean (`loadingCount > 0`)
  - `increment()`, `decrement()`, `reset()` for lifecycle management

- Axios instance: `frontend/src/lib/api.ts`
  - Request interceptor calls `increment()` before sending
  - Response and error interceptors call `decrement()`
  - Also injects `Authorization` header when a token is present

- Global overlay component: `frontend/src/components/common/GlobalLoader.tsx`
  - Subscribes to `isGlobalLoading` and renders `LoadingSpinner` with `variant="overlay"`
  - Included at the top of `App` so it overlays the entire UI

- Spinner component: `frontend/src/components/common/LoadingSpinner.tsx`
  - `variant="overlay"`: semi-transparent fullscreen overlay with a spinner and optional message
  - `variant="page"`: detail-page skeleton layout for route-level initial loads

## Data Flow

1. Component or utility calls through the shared Axios instance (`api` or `httpClient`).
2. Request interceptor increments `loadingCount`; `isGlobalLoading` becomes `true` if `loadingCount > 0`.
3. `GlobalLoader` renders the overlay spinner.
4. When the response/error returns, interceptor decrements `loadingCount`.
5. When `loadingCount` returns to `0`, `isGlobalLoading` becomes `false` and the overlay is hidden.

## Usage Guidelines

- Prefer global overlay for background HTTP activity
  - Route components can return `null` during fetch to allow the global overlay to show.
  - Example: replace ad-hoc local spinners with global overlay via Axios interceptors.

- Use `LoadingSpinner` with `variant="page"` for initial route content loads
  - When a page’s layout is empty without data, a skeleton improves perceived performance.
  - Return `\<LoadingSpinner variant="page" />` until the initial fetch completes.

- Avoid mixing multiple spinners on the same screen
  - Overlay + local skeleton at the same time is distracting.
  - Choose one pattern per state: overlay for background activity, skeleton for first-page render.

- Ensure requests go through the shared Axios instance
  - Global overlay only tracks requests made via `frontend/src/lib/api.ts`.
  - If using `fetch`, manually integrate with the loading store or refactor to use `api`/`httpClient`.

## Examples

### Global overlay on initial fetch

```tsx
// Route-level component
import LoadingSpinner from "@/components/common/LoadingSpinner";
import { httpClient } from "@/lib/httpClient";

export default function ExampleDetail() {
  const [item, setItem] = useState(null);
  const [hasFetched, setHasFetched] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const data = await httpClient.get(/* ... */);
        setItem(data);
      } finally {
        setHasFetched(true);
      }
    })();
  }, []);

  // Allow the global overlay to show during HTTP; render page skeleton for first load
  if (!hasFetched) {
    return <LoadingSpinner variant="page" />;
  }

  return <div>{/* render detail */}</div>;
}
```

### Manually tracking non-Axios work with the store

```ts
import { useLoadingStore } from "@/store/loading";

async function doWork() {
  const { increment, decrement } = useLoadingStore.getState();
  try {
    increment();
    // perform work with fetch or other APIs
    const res = await fetch("/some/endpoint");
    // handle result
  } finally {
    decrement();
  }
}
```

### Using the shared Axios instance directly

```ts
import api from "@/lib/api";

export async function loadUsers() {
  const res = await api.get("/v1/users");
  return res.data; // interceptors automatically manage the overlay
}
```

## Best Practices

- Keep interceptor calls symmetrical
  - Do not swallow errors without letting the error interceptor run; it’s responsible for decrementing.

- Debounce multi-request screens via batching where possible
  - Multiple concurrent calls will stack the overlay; consider combining requests or sequencing where UX benefits.

- Reset the store cautiously
  - `reset()` is provided for edge cases (e.g., navigation aborts) but use sparingly.

## Common Pitfalls

- Using `fetch` directly without increment/decrement
  - The overlay will not appear; wrap with the store or refactor to `api`/`httpClient`.

- Rendering both overlay and local skeleton simultaneously
  - Choose one for clarity; avoid visual clutter.

- Forgetting to decrement on error paths
  - The overlay can get stuck; rely on the Axios error interceptor or use `finally` blocks.

## Where It’s Used

- `GlobalLoader` is mounted in `frontend/src/App.tsx`, so the overlay applies across authenticated routes.
- Most data flows use `httpClient` (which wraps `api`) so the overlay is consistent.
- Some legacy flows still use `fetch` (e.g., certain role/group actions) — consider migrating to `api` or manually tracking with the store.

## FAQ

- How do I show a custom overlay message?
  - `LoadingSpinner` accepts a `message` prop, but `GlobalLoader` currently renders without a message. You can extend `GlobalLoader` to pass a message based on context if needed.

- Should I return `null` or a page skeleton during initial load?
  - Prefer a page skeleton for route-level initial loads; return `null` when the content structure remains visible and only background activity is ongoing.

- Does this handle non-HTTP work (e.g., CPU-heavy tasks)?
  - Not automatically. Use the store’s `increment/decrement` around non-HTTP work if you want the overlay to reflect it.

## References

- Store: `frontend/src/store/loading.ts`
- Axios: `frontend/src/lib/api.ts`
- Global overlay: `frontend/src/components/common/GlobalLoader.tsx`
- Spinner: `frontend/src/components/common/LoadingSpinner.tsx`

