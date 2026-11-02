# Kahani

Kahani is a story-first social networking MVP. People meet through personal stories and shared interests; profile photos are not collected or shown.

## Stack

- Java 21 and Spring Boot, with H2 for local development and PostgreSQL for production
- Flyway-managed production schema, Redis-backed production sessions, and Actuator health checks
- React, TypeScript, and Vite

## Run locally

Recommended containerized demo:

```powershell
docker compose up --build
```

Open `http://127.0.0.1:5174`. The web container proxies `/api` to the API container.
The demo profile uses an in-memory database; its data is discarded when the API restarts.

Alternatively, run the API and frontend separately:

Start the API:

```powershell
cd api
$env:SPRING_PROFILES_ACTIVE = "demo"
mvn spring-boot:run
```

In another terminal, start the web app:

```powershell
cd web
npm ci
npm run dev
```

Open the Vite URL shown in the terminal (usually `http://localhost:5173`). The API runs on `http://localhost:8080`. The demo user is `+15550000001`.

The H2 console is enabled only in the local `demo` profile at `http://localhost:8080/h2-console`, with JDBC URL `jdbc:h2:mem:kahani`, username `sa`, and an empty password.

## Current policy and integration status

- Sign-up is a six-step onboarding: (1) personal details and verified email, (2) connection goals and a generated Kahani ID, (3) work, native city, and Indian-language selections, (4) interests with a note for each, (5) values note, and (6) a 250-word-minimum story, private Instagram link, and password. Phone verification then submits the account for human review. Users must be at least 18. Sign-in accepts the email address or Kahani ID with the password. Users who forget their ID can request it using a verified email.
- Relationship status options are single, dating/not sure yet, recent break up, awaiting divorce, committed relationship, and open relationship.
- Stories must contain at least 250 words. After admin approval, Discover publishes each verified profile's story, occupation, age, gender, relationship status, connection goals, native city, languages, interests and notes, and values note to other verified members. Phone, email, Kahani ID, and Instagram verification link are not shown in Discover; Instagram remains private for team review and optional sharing after a match.
- Passwords are stored as BCrypt hashes. Phone numbers and email addresses are unique. The `demo` profile uses a fixed verification code (`123456`) for both email and phone and is for local development only; it does not prove email ownership or phone ownership. Do not deploy demo mode.
- Demo accounts use password `KahaniDemo123!`. Alex: `+15550000001` or `alexmorgandemo@kahani.demo`; admin: `+15550000009` or `kahaniadmindemo@kahani.demo`. The signup demo code is `123456`.
- Signup requires a story and an Instagram username for private admin review. New accounts remain private until reviewed. Voice samples are private, limited to 5 MB, and deleted after an admin decision; they are required before approving women's accounts and optional for other accounts. Live face verification is not implemented yet.
- Admin review is a human review aid, not proof of gender or identity. Voice recordings must not be used to infer gender. The demo admin is `+15550000009` with password `KahaniDemo123!`.
- A man needs an active billing entitlement and one credit to request a connection with a woman; women can send connection requests without credits. Accepting a request does not charge a second credit. Credit packages are ₹249 for 5 credits, ₹899 for 20 credits, or custom credits at ₹80 each. Admin approval of a manually checked payment reference grants the credits and activates access with no expiry. The seeded demo man has an active demo entitlement and two remaining credits.
- Chat is available only after a connection is accepted. Instagram handles are shared privately for verification and appear to the other person only when the owner explicitly reveals them after a match; the owner can hide them again.
- Every Discover profile has Skip and Say hello actions. Skip hides the profile for the browser session. Saying hello requires a 10–500 character note; it is saved with a pending request, updates an existing pending hello, accepts an incoming request and starts a chat, or is sent as a message after a match.
- The More page links to accepted matches, browser-saved favourites, profiles skipped during the current session, and (for men) membership and credit purchases; it also provides logout.
- Discover presents one complete profile and story at a time, with Previous and Next story navigation; search and sorting controls are not shown. Payment packages and purchase history are kept exclusively in More → Membership and do not appear in Discover.
- The payment-reference workflow is manual: users pay outside Kahani and submit a UPI/payment reference; admins must verify it in the payment provider before approving it. No payment gateway, automatic payment verification, or refund flow is configured. Never approve a reference based only on the user's submission.
- The default Spring profile does not seed demo data or expose the demo OTP flow. Do not use the `demo` profile for production. The demo profile exposes only explicit localhost Vite origins; configure exact trusted origins for any deployment.
- Successful login and phone verification rotate the session ID. Sessions are HTTP-only, SameSite=Lax, and expire after 30 minutes. Set `KAHANI_SESSION_COOKIE_SECURE=true` behind HTTPS in production.
- Real email/WhatsApp verification and production database configuration are not implemented. Signup remains unavailable outside demo mode until real verification providers are configured; demo OTPs, demo credentials, and the H2 database are local development only.

## AWS production preparation

The `prod` Spring profile is intended for a managed AWS runtime. It requires PostgreSQL, Redis with TLS and authentication, a durable Flyway-managed schema, and secure session cookies. The production container defaults to this profile. Do not start a production deployment with the `demo` profile; startup fails if `demo` and `prod` are active together or demo authentication is enabled in production.

Inject the following values into the API task from AWS Secrets Manager or equivalent secret storage. Never commit real values:

- `KAHANI_DATABASE_URL`: JDBC URL for private RDS PostgreSQL, with certificate and hostname verification enabled.
- `KAHANI_DATABASE_USERNAME` and `KAHANI_DATABASE_PASSWORD`: least-privilege application database credentials.
- `KAHANI_REDIS_HOST`, `KAHANI_REDIS_PORT`, `KAHANI_REDIS_USERNAME`, and `KAHANI_REDIS_AUTH_TOKEN`: private ElastiCache Redis endpoint and TLS/auth credentials.
- `KAHANI_ALLOWED_ORIGINS`: optional exact browser origins only when the API is called cross-origin. A same-origin CloudFront route should leave this unset.
- `KAHANI_DB_POOL_SIZE` and `KAHANI_DB_MIN_IDLE`: size these for the selected RDS instance and total ECS task count.

The root `docker-compose.yml` is for the local demo only. It is not an AWS deployment stack. The GitHub Actions workflow verifies API tests and the frontend production build; it does not deploy to AWS.

### Remaining launch blockers

- Implement and configure a real SMS/phone-ownership verification provider. Demo OTP is not identity or phone verification.
- Integrate a payment provider with signed webhook verification, refunds/reconciliation, and robust idempotency before enabling automated payments.
- Move voice recordings to private, encrypted S3 storage with lifecycle deletion and narrowly scoped access; the current API stores them in PostgreSQL.
- Provision production admin access through a controlled bootstrap procedure; never seed public demo users or rely on profile fields as the only operational access control.
- Add rate limiting and abuse controls for sign-in, signup, messaging, and verification; threat-model CSRF/session behavior for the final domain and proxy topology.
- Configure AWS infrastructure (VPC, ECS/Fargate, ALB/TLS, RDS backups, Redis, S3/CloudFront, KMS, IAM least privilege, Secrets Manager, alarms, and deployment rollback) for the account and domain.
- Complete privacy/terms, age-eligibility and safety workflows, data deletion/retention, backup-restore drills, and production load/security testing before accepting real users.
