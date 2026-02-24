## Grounded Pipelines (Contextual Integrity)

**Grounded Pipelines** treat the LLM as an *untrusted* component inside a trusted integration flow. The model can help interpret intent and write natural-language responses, but it must do so using **bounded inputs** (schemas, retrieved context, search results) rather than “whatever it remembers.”

This preserves **contextual integrity**: the answer is constrained to the *right* source of truth (your DB, your documents, your index) and to the *right* level of access (no direct SQL, no arbitrary tools, no hidden data paths).

**Use Cases:** RAG Q&A, product discovery, “safe” database querying, knowledge-base assistants, compliance-friendly summaries, and any workflow where “sounds plausible” is a failure mode.

## The Pattern

Most grounded pipelines follow the same shape:

- **Interpret (LLM → intent)**: Convert the user’s text into a safe intermediate form (e.g., JSON that matches a schema) or into an embedding for retrieval.
- **Retrieve/Execute (trusted code)**: Perform lookups with allowlisted queries, deterministic filters, and clear limits (SQL, MyBatis, vector similarity, etc.).
- **Compose (LLM → response)**: Ask the model to answer **using only** the retrieved evidence/results.
- **Refuse (no evidence)**: If nothing relevant is found, **say so** rather than hallucinating.

## What “Grounded” Means Here

- **No direct data access from the model**: the LLM never runs SQL or touches credentials.
- **Outputs are constrained**: schemas, low temperature, and few-shot examples reduce “creative” parsing.
- **Evidence is explicit**: the response is generated from retrieved chunks or query results you can log and audit.
- **Failure is a first-class path**: “I don’t have that information” is a valid (and desirable) outcome.

## How to Extend This Pattern

- **Tune retrieval**: adjust `topK`, similarity thresholds, and chunking rules.


