# Invoice SaaS Backend

Spring Boot + PostgreSQL + Paystack backend for an invoicing SaaS app.

## Stack
- Java 17, Spring Boot 3.3
- PostgreSQL
- Spring Security + JWT auth
- iText7 + Thymeleaf for PDF generation
- Paystack for invoice payments and Premium subscription billing
- WhatsApp / Telegram sharing via generated share links (see note below)

## 1. Setup

### Prerequisites
- Java 17+
- Maven
- PostgreSQL running locally (or a hosted instance)

### Database
```sql
CREATE DATABASE invoice_saas_db;
```
Tables are auto-created on first run (`ddl-auto: update` in `application.yml`). Switch this to `validate` and use a migration tool like Flyway once you go to production.

### Environment variables
Set these before running (or edit `src/main/resources/application.yml` directly):

| Variable | Purpose |
|---|---|
| `JWT_SECRET` | Long random string, min 256 bits, used to sign JWTs |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP creds for sending invoice emails |
| `PAYSTACK_SECRET_KEY` | From Paystack dashboard |
| `PAYSTACK_PUBLIC_KEY` | From Paystack dashboard (used by frontend) |
| `PAYSTACK_PREMIUM_PLAN` | Plan code for your Premium subscription tier (create it in Paystack dashboard → Plans) |
| `TELEGRAM_BOT_TOKEN` | Optional, only if you wire up direct-send via Telegram bot |

### Run
```bash
mvn spring-boot:run
```
Backend starts on `http://localhost:8080`.

## 2. Key things this backend does

- **Auth**: register/login with JWT, one business per user account
- **Company profile**: business details + **optional** logo upload (`POST /api/company/logo`) — invoices render fine with just the business name if no logo is uploaded
- **Clients**: CRUD
- **Invoices**: line items, tax/discount calc, statuses, PDF generation, public shareable link (no login needed to view/pay)
- **The "Book" (ledger)**: `/api/ledger` — records sales, debts owed to you, debts you owe, expenses, and payments in one running record. Invoices auto-log a receivable entry; payments auto-log a payment entry.
- **Payments**: Paystack one-time checkout for clients paying invoices, manual "mark as paid," and a webhook endpoint for Paystack to confirm payment
- **Premium subscription**: Paystack recurring billing gates a free-tier invoice limit (5/month) vs unlimited for Premium

## 3. PDF sharing via WhatsApp / Telegram / social — how it actually works

There's no way for a backend to silently push a PDF into someone's WhatsApp or Telegram chat without extra approvals:
- **WhatsApp** requires Meta's WhatsApp Business Cloud API (application + approval process)
- **Telegram** requires a registered bot AND the recipient having already messaged that bot first (to get their `chat_id`)

So this backend uses the **zero-approval-needed approach** that's standard for this kind of feature:

1. `POST /api/invoices/{id}/pdf` generates the PDF and hosts it at a public URL (e.g. `http://yourhost/files/pdfs/invoice-INV-0001-xxxx.pdf`)
2. `GET /api/invoices/{id}/share` returns:
   - `pdfDownloadUrl` — direct link to the PDF
   - `whatsappShareUrl` — a `wa.me/?text=...` link pre-filled with a message + the PDF link
   - `telegramShareUrl` — a `t.me/share/url?...` link, same idea
   - `genericShareText` — plain text for any other platform (X, email, SMS, copy-paste)
3. Your React frontend just does `window.open(shareLinks.whatsappShareUrl)` etc. — WhatsApp/Telegram opens with the message ready to send. Works on both desktop and mobile, no API keys, no approval process.

If you later get WhatsApp Business API access or set up a Telegram bot properly, `TelegramBotService.java` has a ready-to-wire method (`sendDocumentToChat`) for pushing the file directly instead of just deep-linking.

## 3b. Deploying to Railway

Railway auto-detects this as a Maven/Spring Boot project, runs `mvn clean install`, and starts the jar — no Dockerfile needed for a standard deploy.

1. **Push this project to GitHub** (its own repo, e.g. `invoicepro-backend`).
2. In Railway: **New Project → Deploy from GitHub repo** → select it.
3. **Add Postgres**: in the same project, click **New → Database → Add PostgreSQL**. Railway provisions it and exposes connection details as `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` on the Postgres service.
4. **Set variables on the backend service** (Variables tab). Railway lets you reference another service's variables with `${{ServiceName.VAR}}` — if your Postgres service is named `Postgres`:

   | Variable | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
   | `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
   | `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
   | `JWT_SECRET` | a long random string |
   | `PAYSTACK_SECRET_KEY` / `PAYSTACK_PUBLIC_KEY` / `PAYSTACK_PREMIUM_PLAN` | from your Paystack dashboard |
   | `MAIL_USERNAME` / `MAIL_PASSWORD` | your SMTP creds |
   | `APP_BACKEND_BASE_URL` | your Railway public domain, e.g. `https://invoicepro-backend.up.railway.app` |
   | `APP_FRONTEND_BASE_URL` | your Vercel domain, e.g. `https://invoicepro.vercel.app` |
   | `APP_CORS_ALLOWED_ORIGINS` | same as above — your Vercel domain (comma-separate if you have more than one, e.g. a custom domain too) |

   Railway does **not** read `application.yml` and auto-populate anything — every variable above has to be set by hand.

5. Click **Deploy**. Once it's live, go to **Settings → Networking** and click **Generate Domain** to get your public `*.up.railway.app` URL (or attach a custom domain there).
6. Point your Paystack webhook at `https://<your-railway-domain>/api/payments/webhook`.

**One important caveat**: Railway's filesystem is ephemeral — anything written to `./uploads` or `./generated-pdfs` (your logos and invoice PDFs) is wiped on every redeploy. For a real deployment, either:
- Attach a [Railway Volume](https://docs.railway.com/reference/volumes) mounted at those paths, or
- Swap `FileStorageService` and `PdfService` to write to S3 / Cloudflare R2 instead of local disk (the cleaner long-term fix, and necessary anyway if you ever run more than one backend instance).

This isn't wired up in the code as-is — flagging it now so logos/PDFs don't mysteriously disappear after your first redeploy.

## 4. Paystack webhook

Point your Paystack dashboard webhook URL to:
```
https://yourdomain.com/api/payments/webhook
```
**Important**: the webhook handler currently has a `TODO` where you must verify the `x-paystack-signature` header (HMAC-SHA512 of the raw request body using your secret key) before trusting any payload. This is left as a clearly marked TODO in `PaymentController.java` — don't skip it in production, or anyone can forge "payment successful" events.

## 5. Main API endpoints

```
POST   /api/auth/register
POST   /api/auth/login

GET    /api/company
PUT    /api/company
POST   /api/company/logo          (multipart, optional)
DELETE /api/company/logo

POST   /api/clients
GET    /api/clients
GET    /api/clients/{id}
PUT    /api/clients/{id}
DELETE /api/clients/{id}

POST   /api/invoices
GET    /api/invoices
GET    /api/invoices/{id}
POST   /api/invoices/{id}/send
POST   /api/invoices/{id}/cancel
POST   /api/invoices/{id}/pdf     (generate/regenerate PDF)
GET    /api/invoices/{id}/share   (WhatsApp / Telegram / generic share links)

GET    /api/public/invoices/{token}   (no auth - public invoice view)

POST   /api/payments/init             (client pays via Paystack)
POST   /api/payments/manual           (owner marks paid manually)
POST   /api/payments/subscribe        (upgrade to Premium)
POST   /api/payments/webhook          (Paystack callback)

POST   /api/ledger        (log a sale / debt / expense / payment - "the book")
GET    /api/ledger
GET    /api/ledger/range?start=YYYY-MM-DD&end=YYYY-MM-DD
PATCH  /api/ledger/{id}/status?status=CLEARED
DELETE /api/ledger/{id}
```

## 6. Not included yet (natural next steps)
- Recurring invoice auto-generation job (scheduled task that clones a recurring invoice on its interval)
- Payment reminder emails before due date
- Multi-user/team roles per company (Role enum exists on User, just needs endpoint-level enforcement)
- Refresh tokens (currently a single JWT with 24h expiry)
- Rate limiting
- Flyway migrations instead of `ddl-auto: update`
