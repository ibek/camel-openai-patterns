## Compliance Gap Analysis

**The Goal:** Feed the system a contract and a checklist, and have it tell you exactly what is missing.

Asking an LLM to review a contract is usually a trap. The model wants to be helpful, so it tends to hallucinate compliance where none exists. To fix this, we use a pattern that forces the model to **"prove a negative."**

The mechanism here is strict: we demand an `evidence` field for every single check. If the model marks a section as `COMPLIANT`, it must copy-paste the exact sentence from the source text that proves it.

The logic is simple: **If you cannot quote the text, you cannot mark it as compliant.** This effectively kills hallucinations, because the model cannot invent a quote that doesn't exist without getting caught by the validator.

## Run examples

```
curl -s https://privacy.apache.org/policies/privacy-policy-public.html | camel run --source-dir=./
```
