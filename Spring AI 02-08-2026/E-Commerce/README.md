# E-Commerce Application (without AI)

The plain version of the store. Everything a real e-commerce site needs, and no AI anywhere in it.

This is the starting point. Once it runs, the AI version adds features on top of exactly this code.

**Two folders make one app:**

| Folder | What it is |
|---|---|
| `E-Commerce-Backend-Base` | Spring Boot API, runs on port 8080 |
| `ecom-ui-base` | React storefront, runs on port 5173 |

Both have to be running at the same time.

---

## What the app can do

Sign up with email and a verification code, sign in, browse products by category, search, cart,
addresses, coupons, place an order, pay through Razorpay, track order status, and raise support
tickets. Admins get a separate panel for products, categories, coupons, orders, the support queue and
user roles.

---

## Before you start

Install these four things. Versions matter for the first two.

| Tool | Version | Check it with |
|---|---|---|
| Java | 17 | `java -version` |
| Maven | 3.9 or newer | `mvn -v` |
| Node.js | 18 or newer | `node -v` |
| PostgreSQL | 14 or newer | `psql --version` |

If `java -version` shows something other than 17, the project will still often compile, but keep 17
for the class so everyone sees the same behaviour.

---

## Step 1: Create the database

The app does not create the database for you. It creates the tables inside it.

Open psql or pgAdmin and run:

```sql
CREATE DATABASE telusko;
```

That is all. When the app starts, Hibernate reads the entity classes and creates every table itself,
because `spring.jpa.hibernate.ddl-auto=update` is set.

Now tell the app how to reach it. Open:

```
E-Commerce-Backend-Base/src/main/resources/application.properties
```

and check these three lines match your local Postgres:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/telusko}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:0076}
```

The password is almost certainly the one thing you need to change. Put your own Postgres password in
place of `0076`.

---

## Step 2: Get the API keys

The app talks to three outside services. Two of them are needed before anything useful works, and one
is optional.

| Service | What it does | Needed? |
|---|---|---|
| Mailtrap | Delivers the signup verification code | Yes, you cannot create an account without it |
| Cloudinary | Stores product images | Yes, if you want to add products |
| Razorpay | Online payment at checkout | Only for the payment step |
| GitHub OAuth | The "Sign in with GitHub" button | Optional |

Each one is free and takes a couple of minutes.

### 2.1 Mailtrap, for the verification code

When someone signs up, the app emails them a six digit code. Mailtrap catches that email so it never
reaches a real inbox, which is exactly what you want while testing.

1. Go to **mailtrap.io** and create a free account.
2. In the left menu open **Email Testing**, then **Inboxes**.
3. Click your inbox, then the **SMTP Settings** tab.
4. In the **Integrations** dropdown choose **Java**, or just read the credentials off the page.
5. Copy the **Username** and **Password**. They look like random strings, not like an email address.

Paste them here:

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=587
spring.mail.username=YOUR_MAILTRAP_USERNAME
spring.mail.password=YOUR_MAILTRAP_PASSWORD
```

**Where the codes arrive.** Not in your email. Go back to Mailtrap, open the same inbox, and the
newest message at the top is the verification email. Open it and read the code from there.

The code is six digits, valid for ten minutes, usable once, and you get five attempts. There is a one
minute wait before you can ask for a new one.

### 2.2 Cloudinary, for product images

Product photos are uploaded to Cloudinary rather than stored on your machine.

1. Go to **cloudinary.com** and sign up for the free plan.
2. You land on the **Dashboard**. Everything you need is in the box at the top.
3. Copy the **Cloud name**, **API Key** and **API Secret**. Click the eye icon to reveal the secret.

Paste them here:

```properties
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
cloudinary.folder=ecommerce
```

Leave `cloudinary.folder` as it is. It just keeps the uploads tidy inside your account.

### 2.3 Razorpay, for payments

Only needed if you want to walk through the online payment flow. Cash on delivery works without it.

1. Go to **razorpay.com** and create an account.
2. Switch to **Test Mode** using the toggle at the top. Do not use live mode.
3. Open **Settings**, then **API Keys**, then **Generate Test Key**.
4. Copy the **Key Id** and the **Key Secret**. The secret is shown once, so save it immediately.

Paste them here:

```properties
razorpay.api.key=YOUR_RAZORPAY_KEY_ID
razorpay.api.secret=YOUR_RAZORPAY_KEY_SECRET
```

The Key Id always starts with `rzp_test_` in test mode. If yours starts with `rzp_live_`, you are in
the wrong mode.

### 2.4 GitHub OAuth, optional

This is only for the "Sign in with GitHub" button. Email and password login works without it.

1. On GitHub open **Settings**, then **Developer settings**, then **OAuth Apps**, then
   **New OAuth App**.
2. Fill in:
   - Application name: anything
   - Homepage URL: `http://localhost:8080`
   - Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
3. Create it, then press **Generate a new client secret** and copy both values.

Paste them here:

```properties
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID:YOUR_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET:YOUR_CLIENT_SECRET}
```

If you skip this, leave the values as `not-configured`. Do not leave them blank, because Spring Boot
refuses to start with an empty client id.

---

## Step 3: Run the backend

From the `E-Commerce-Backend-Base` folder:

```bash
mvn spring-boot:run
```

Or open the project in IntelliJ and run `ECommerceBackendApplication`.

The first start takes longer because Hibernate is creating every table. You will see a lot of
`create table` statements go past. That is normal and only happens once.

You know it worked when the last line says:

```
Started ECommerceBackendApplication in 11.062 seconds
```

Check it in a browser:

```
http://localhost:8080/api/v1/ecommerce/products
```

An empty list `[]` is the correct answer. There are no products yet.

---

## Step 4: Run the frontend

Open a **second terminal**. Leave the backend running in the first one.

From the `ecom-ui-base` folder:

```bash
npm i
```

This downloads the dependencies. It takes a minute or two the first time and prints a folder called
`node_modules`. You only run this again if `package.json` changes.

Then:

```bash
npm run dev
```

You will see:

```
VITE ready in 400 ms
Local: http://localhost:5173/
```

Open **http://localhost:5173** in the browser. The storefront loads.

**There is nothing to configure in the frontend.** It calls `/api` and Vite forwards that to the
backend on 8080, which is already set up in `vite.config.js`. Because the browser only ever talks to
one address, there is no CORS problem to solve.

Leave both terminals running while you work. Vite reloads the page whenever you save a file.

---

## Step 5: Create your first admin

Everything in the admin panel is locked to admins, and the endpoint that grants the admin role is
itself admin only. So the very first admin has to be made directly in the database.

1. Open **http://localhost:5173** and register normally with any email.
2. Go to Mailtrap, open the newest message, and copy the six digit code.
3. Enter the code in the app. Your account is now verified.
4. Run this in psql or pgAdmin, using the email you signed up with:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

5. Sign out and sign back in. The token is issued at login, so the new role only takes effect on a
   fresh login. An **Admin** link now appears in the header.

From here on you can grant roles to other people from the admin panel, so this is a one time step.

---

## Step 6: Add some data

In the admin panel, in this order:

1. **Categories**, create two or three.
2. **Catalog**, add a few products. Each one needs a name, category, price, stock and an image.
3. **Coupons**, optional, if you want to demonstrate discounts.

Now go back to the shop. Products appear on the home page, categories work, and you can add to the
cart and place an order.

---

## Everyday commands

Once everything is set up, starting the app is two commands in two terminals.

```bash
# Terminal 1, in E-Commerce-Backend-Base
mvn spring-boot:run
```

```bash
# Terminal 2, in ecom-ui-base
npm run dev
```

`npm i` is only needed the first time, or after someone changes `package.json`.

---

## When something does not work

| What you see | What it usually is |
|---|---|
| `Connection refused` on startup | Postgres is not running, or the port is not 5432 |
| `FATAL: database "telusko" does not exist` | Step 1 was skipped. Create the database |
| `password authentication failed` | The password in `application.properties` is not your Postgres password |
| App starts, but no verification email | Mailtrap credentials are wrong. Check the inbox on mailtrap.io, not your own email |
| Product image upload fails | Cloudinary credentials are wrong, or the secret was copied with a space at the end |
| `Client id of registration 'github' must not be empty` | The GitHub values were left blank. Put `not-configured` back |
| Frontend loads but every request fails | The backend is not running. Check terminal 1 |
| `Port 8080 is already in use` | An older run is still going. Stop it, or change `server.port` |
| Admin link does not appear | Sign out and sign in again. The role is read from the token |

---

## Project layout

Two folders, one app.

```
Without AI/
  E-Commerce-Backend-Base/          the API
    src/main/java/com/telusko/
      controller/                   REST endpoints
      service/                      business logic
      repository/                   database queries
      model/                        JPA entities
      dto/                          request and response shapes
      security/                     JWT, OAuth2, SecurityConfig
      config/                       Cloudinary, mail, CORS
    src/main/resources/
      application.properties        every setting and key lives here

  ecom-ui-base/                     the storefront
    src/
      api.js                        every backend call, plus JWT storage and refresh
      store.jsx                     session, cart and shared category state
      ui.jsx                        shared pieces: Money, Thumb, badges, skeletons
      styles.css                    design tokens and component styles
      components/                   Header, Footer, HeroCarousel, ProductCard, OtpInput
      pages/                        one file per route
      pages/admin/                  admin console tabs
```

---

## Worth knowing about how it works

**The cart lives on the server.** Every change returns the recalculated cart and that becomes the new
state in the browser. Totals, discounts and stock rules are never worked out twice.

**Tokens refresh by themselves.** An access token lasts 30 minutes. When a call comes back 401,
`api.js` swaps the refresh token for a new pair and replays the request once. If several calls fail at
the same moment they share one refresh rather than firing several.

**Product images are optional.** If Cloudinary is not configured, the app draws a coloured tile from
the product name instead of showing a broken image, so you can work through the whole app without
setting it up.

**Search is keyword based.** It matches the words you typed against product names, descriptions and
category names. Type something phrased differently from the catalog text and you get nothing back.
That limitation is the reason the AI version exists.

---

## A note on the credentials in this file

The `application.properties` in this repository ships with working keys so the project runs out of the
box during the course. They are shared, rate limited, and can stop working at any time.

Create your own before you rely on the app for anything. It takes ten minutes for all four services,
and it means your uploads, your emails and your payments are yours.

If you push this project anywhere public, replace the keys first. Credentials in a properties file are
credentials in your git history, and deleting them later does not remove them from the history.
