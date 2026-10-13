# Grounded Pipelines (Contextual Integrity)

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
- **Swap the source of truth**: point the retrieve/execute step at a different DB, index, or search API without touching the interpret/compose steps.
- **Add an evidence check**: before composing, verify the retrieved rows/chunks actually support the question — and refuse when they don't.
- **Harden the query layer**: keep allowlisted filters and bind parameters so the model's output can never widen access.
- **Log the evidence**: persist the retrieved context alongside the answer so every response is auditable.

## Examples

| Example | What It Does |
|---------|-------------|
| [database-query](database-query/) | "Air-gaps" the DB — the LLM shapes **filters**, trusted code runs the **SQL** |
| [document-qa](document-qa/) | Core **RAG** Q&A over pgvector; **refuses** when no relevant chunks are found |
| [similarity-search-query](similarity-search-query/) | Finds products by **meaning** via vector similarity, not exact filters |
