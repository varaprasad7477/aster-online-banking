# Portfolio talking points

## Honest resume bullet options

- Extended an existing Spring Boot banking API into a runnable full stack portfolio demo with an original responsive dashboard, JWT sign in, H2 persistence, and seeded accounts.
- Implemented transactional transfers with recipient and balance validation, ordered row locking, transaction history, and an integration test for rejected and successful transfers.
- Removed a hardcoded database password, added environment based configuration, and made the project run locally on Java 17 without a separate MySQL installation.

Use only bullets you can explain in an interview. Credit the original repository if you publish this work; do not present the inherited API as entirely authored from scratch.

## Two minute walkthrough

1. Open `http://localhost:8080` and choose **Explore the live demo**.
2. Show the account balance, account number, and activity view.
3. Send ₹125.50 to Sam Rivera (`4100200012`). Point out the sender and recipient entries and the updated balance.
4. Try an amount above the balance or an unknown account. Explain that the API rejects the operation before changing either balance.
5. Mention the Java 17 Spring Boot backend, JWT auth, H2 for the standalone demo, and the `@Transactional` transfer method with row locks.

## If asked about tradeoffs

The original project used MySQL and had no frontend files in this checkout. H2 makes the demo easy to run, while the MySQL driver is retained for optional configuration. The current `double` amount fields are a known limitation; a production upgrade would use `BigDecimal` or integer minor units and database migrations. Demo credentials and sample balances are for demonstration only. The password reset flow is disabled in demo mode until SMTP is configured. The project has no real payment integration or production compliance claims.
