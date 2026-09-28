# Aster Banking

A runnable banking portfolio demo with a responsive web dashboard and a Spring Boot API. It demonstrates account registration, JWT sign in, balances, deposits, withdrawals, transfers between accounts, and transaction history. This is a learning project; it does not connect to real payment rails or hold real money.

## Run locally

Requirements: Java 17+ and Maven 3.9+. A separate MySQL server or Node build is not required.

From the `BankingApp` directory:

```powershell
cd BankingApp
mvn test
mvn spring-boot:run
```

Open **http://localhost:8080**. If `mvn` is not on PATH on this Windows machine, use `& 'C:\apache-maven\apache-maven-3.9.16\bin\mvn.cmd'` in place of `mvn`. The included `mvnw.cmd` can fail when the user directory contains a space, so the installed Maven command is more reliable here.

Click **Explore the live demo**, or sign in with `alex@aster.demo` / `AsterDemo2026!`. The demo recipient is Sam Rivera, account `4100200012`. Both accounts are created on first launch. The local H2 database persists in `BankingApp/data/asterbank.mv.db` and is ignored by Git; later launches keep your demo transactions.

## What this version adds

- Original Aster branding and a responsive browser dashboard, served directly by Spring Boot.
- File backed H2 data store with ready to use sample accounts.
- Java 17 compatibility and a Windows compatible Tomcat NIO2 connector.
- Positive amount and two decimal validation, insufficient balance checks, recipient validation, and transactional transfers with ordered row locks.
- An automated transfer test for successful movement and rejected invalid operations.
- Database and JWT settings from environment variables rather than hardcoded MySQL credentials.

## Configuration

Defaults are for local demonstration. Without `JWT_SECRET`, the app generates a fresh signing key on every start, so existing browser sessions need to sign in again after a restart. Set `DEMO_ENABLED=false` and provide a unique `JWT_SECRET` (at least 32 bytes) before using with your own accounts. `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` can point to MySQL when needed. The default H2 URL is `jdbc:h2:file:./data/asterbank;DB_CLOSE_ON_EXIT=FALSE`.

Password reset is disabled in the public demo because no email server is configured. For a separate deployment, configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`, then turn off demo mode. The health endpoint is `/actuator/health`.

## API at a glance

`POST /api/auth/register`, `POST /api/auth/login`, `GET /api/account/profile`, `GET /api/account/details`, `GET /api/account/transactions`, `POST /api/transaction/CREDIT?amount=100`, `POST /api/transaction/DEBIT?amount=100`, and `POST /api/transaction/transfer` with JSON `{ "amount": 100, "receiverAccount": 4100200012 }`. Account and transaction routes require `Authorization: Bearer <token>`.

## Project scope

This independent portfolio repository packages the Aster UI, local demo setup, validation, transfer handling, and tests as a clean project snapshot. It builds on an earlier Spring Boot banking API by Lakshay Tyagi; the current UI is plain HTML, CSS, and JavaScript served by Spring Boot. This remains a demo, not a production banking system. In particular, amounts are stored as `double`; production financial software should use decimal money types, stronger audit controls, rate limits, and an externally managed secret.

live link
https://aster-online-banking.onrender.com/
