## Generative Parsing (Structuring Data)

Regex is fragile. We all know it.

**Generative Parsing** is the fix. Instead of treating the LLM as a chatbot, we use it as a text-processing engine that converts unstructured input into rigid, type-safe JSON. This ensures that downstream processes receive clean, validated data. It forces the model to "fill in the blanks" of a predefined schema.

**Use Cases:** Classification into strict taxonomies, entity resolution, schema-constrained data extraction, and PII redaction.

## Examples

| Example | What It Does |
|---------|-------------|
| [classify-leaf-node](classify-leaf-node/) | Forces a **leaf-node** selection in a deep category tree |
| [entity-resolution](entity-resolution/) | Maps fuzzy mentions to a **canonical ID** from a whitelist |
| [pii-redaction](pii-redaction/) | Identifies + masks PII with **span verification** |
| [table-extraction](table-extraction/) | Extracts **invoice JSON** from PDFs using Docling + a JSON schema |
