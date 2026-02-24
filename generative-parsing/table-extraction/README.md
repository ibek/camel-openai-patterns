## Invoice Extraction

**The Goal:** Extract structured invoice data (parties + line items + totals) from PDF invoices and return clean, schema-validated JSON.

Docling Serve converts PDFs into clean Markdown, and the LLM extracts invoice fields into a strict JSON schema (`invoice.schema.json`).

## Prerequisite

Start Docling Serve:
```bash
docker compose up -d

```

## Run examples

This example uses the **file adapter**. It scans `invoices/` for `*.pdf` and prints the extracted invoice JSON to stdout.

```bash
# from generative-parsing/table-extraction
camel run --source-dir=./
```

To test with your own PDF:

```bash
cp /path/to/invoice.pdf invoices/
camel run --source-dir=./
```

The route will pick up the PDF, send it to Docling Serve (`CONVERT_TO_MARKDOWN`), then call `openai:chat-completion` constrained by `invoice.schema.json`.
