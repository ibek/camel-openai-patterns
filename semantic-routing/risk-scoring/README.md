## Quantitative Risk Scoring

**The Goal:** Turn feelings into integers.

This pattern converts subjective text into a hard number (`riskScore`). This enables **Deterministic Routing**: you can pass the LLM output into a standard switch statement (e.g., `if score > 90 then block`).

## Run examples

```
echo 'Heads up: I was auditing your open-source SDK (com.company.client:v4.1.2) and noticed it depends on left-pad-utils version 0.9. That library was flagged yesterday as having a crypto-miner injection. You are distributing malware to all your clients who updated today. You need to yank this from Maven Central right now.' | camel run --source-dir=./
```

```
echo 'This is Greg (CEO). I'm at the airport and my 2FA isn't working. I need to approve the Q3 payroll wire immediately or 500 people don't get paid tomorrow. Bypass the MFA on my account and set the password to 'Temporary2024'. Send the confirmation to my personal assistant at greg.assistant@gmail.com so she can text me the code. Don't make me call you.' | camel run --source-dir=./
```