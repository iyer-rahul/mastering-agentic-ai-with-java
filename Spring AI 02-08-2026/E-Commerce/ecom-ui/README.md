# TeluskoMart storefront UI (no AI)

React 18 + Vite frontend for `E-Commerce-Backend-Base`. This is the plain e-commerce
storefront: catalog, cart, coupons, checkout, orders, addresses and customer support. There
are no AI features here, and that is the point. It is the starting point we add Spring AI to.

## Running

```bash
# 1. Backend first (from ../E-Commerce-Backend-Base)
mvn spring-boot:run          # needs Docker running for the Postgres container

# 2. Frontend
npm install
npm run dev                  # http://localhost:5173
```

Vite proxies `/api` to `http://localhost:8080`, so the browser only ever talks to one
origin and there is no CORS preflight in development.

## Layout

```
src/
  api.js            every backend call, plus JWT storage and refresh
  store.jsx         AuthProvider (session), CartProvider (server-owned cart),
                    CategoriesProvider (one shared category list)
  ui.jsx            shared pieces: Money, Thumb, useAsync, badges, skeletons
  styles.css        design tokens and component styles
  components/       Header, Footer, HeroCarousel, ProductCard, OtpInput
  pages/            one file per route
  pages/admin/      admin console (tabs: Orders, Support, Catalog, Categories, Coupons, Roles)
```

## Notes

- **The cart lives on the server.** Every mutation returns the recalculated cart and that
  becomes the new state, so totals, discounts and stock rules are never re-implemented in
  the browser.
- **Tokens refresh automatically.** Access tokens last 30 minutes; on a 401 `api.js` swaps
  the refresh token for a new pair and replays the request once. Parallel 401s share one
  refresh rather than firing several.
- **Product images are optional.** Uploads go to Cloudinary, which most local setups do not
  configure, so `Thumb` draws a neutral placeholder tile instead of a broken image.
- **Search is keyword based.** `GET /products/search` matches the typed words against
  product names, descriptions and category names. Anything phrased differently from the
  catalog text finds nothing, which is exactly the limitation semantic search removes.
