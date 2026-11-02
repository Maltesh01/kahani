# Kahani

Kahani is a story-first social networking MVP. People meet through personal stories and shared interests; profile photos are not collected or shown.

## Stack

- Java 21, Spring Boot, Spring Data JPA, and H2
- React, TypeScript, and Vite

## Run locally

Start the API:

```powershell
cd api
$env:SPRING_PROFILES_ACTIVE = "demo"
mvn spring-boot:run
```

In another terminal, start the web app:

```powershell
cd web
npm install
npm run dev
```

Open the Vite URL shown in the terminal (usually `http://localhost:5173`). The API runs on `http://localhost:8080`. The demo user is `+15550000001`.

The H2 console is enabled only in the local `demo` profile at `http://localhost:8080/h2-console`, with JDBC URL `jdbc:h2:mem:kahani`, username `sa`, and an empty password.

## Current policy and integration status

- Sign-up collects the Kahani story/profile, email, phone, and a password, then verifies the phone. A Kahani account ID is generated after verification and shown in the signed-in account panel. Sign-in accepts either the phone number or email together with the password.
- Passwords are stored as BCrypt hashes. Phone numbers and email addresses are unique. The `demo` profile uses a fixed signup verification code (`123456`) and is for local development only; it does not prove phone ownership. Do not deploy demo mode.
- Demo accounts use password `KahaniDemo123!`. Alex: `+15550000001` or `alexmorgandemo@kahani.demo`; admin: `+15550000009` or `kahaniadmindemo@kahani.demo`. The signup demo code is `123456`.
- Signup requires a story and an Instagram username for private admin review. New accounts remain private until reviewed. Voice samples are optional, private, limited to 5 MB, and deleted after an admin decision.
- Admin review is a human review aid, not proof of gender or identity. Voice recordings must not be used to infer gender. The demo admin is `+15550000009` with password `KahaniDemo123!`.
- Men need an active subscription and one credit to initiate each connection request. Accepting a request does not charge a second credit. The seeded demo man has an active demo subscription and two remaining credits.
- Chat is available only after a connection is accepted. Instagram handles are shared privately for verification and appear to the other person only when the owner explicitly reveals them after a match; the owner can hide them again.
- Razorpay is the selected provider, but checkout is not enabled. Plan price and credits per subscription are still undecided; no real payment or credit purchase is performed. New accounts therefore cannot initiate paid connections until billing is configured.
- The default Spring profile does not seed demo data or expose the demo OTP flow. Do not use the `demo` profile for production. The demo profile exposes only explicit localhost Vite origins; configure exact trusted origins for any deployment.
- Successful login and phone verification rotate the session ID. Sessions are HTTP-only, SameSite=Lax, and expire after 30 minutes. Set `KAHANI_SESSION_COOKIE_SECURE=true` behind HTTPS in production.
- Real SMS/email verification and production database configuration are not implemented. Signup remains unavailable outside demo mode until a real verification provider is configured; the demo OTP, demo credentials, and H2 database are local development only.
