## Grounded Pipelines (Contextual Integrity)

One of the most dangerous trends in 2025/2026 is the "Text-to-SQL" agent - giving a model direct access to your database schema and hoping it writes safe queries. It’s a security nightmare waiting to happen.

**Grounded Pipelines** take a stricter approach. We don't want the model to *execute* actions; we only want it to **define the intent and parameters**.

Instead of asking an agent to "figure it out," we decompose the task into three distinct phases. We treat the LLM not as a decision-maker, but as two separate components in a standard Camel route: a **Parser** at the start and a **Synthesizer** at the end. The AI never touches the database directly. It never executes code. It sits safely on the perimeter, leaving the core integration logic strictly deterministic.

- **The Parser (AI):** Converts unstructured user text into strict, validatable JSON. It identifies the intent and parameters but performs **no** actions.     
- **The Executor (System):** The "Air Gap." Camel runs the actual logic (SQL, APIs, File I/O) using the trusted JSON parameters. The AI is completely removed from this step and it cannot improvise here.
- **The Synthesizer (AI):** Receives the raw system data and transforms it into a clean natural language response. It reports only what the Executor provides.

**Use Cases:** Zero-Trust retrieval, "Air-Gapped" execution, and sanitized querying
