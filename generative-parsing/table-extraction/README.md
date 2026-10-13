# Invoice Extraction

**The Goal:** Extract structured invoice data (parties + line items + totals) from PDF invoices and return clean, schema-validated JSON.

Docling Serve converts PDFs into clean Markdown, and the LLM extracts invoice fields into a strict JSON schema (`invoice.schema.json`).

## Prerequisites

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

**Example output** — the adapter logs `Processing invoice: ...pdf`, then prints one JSON object per invoice on stdout (abridged below; the full `invoice.schema.json` also carries tax IDs, postal addresses, and every line item):

```json
{
  "invoiceId": "BPXINV-00550",
  "issueDate": "2024-08-14",
  "currencyCode": "USD",
  "supplier": { "partyName": "BPX Industrial Supply Co." },
  "customer": { "partyName": "Northwind Traders" },
  "lineItems": [
    { "lineId": 1, "item": { "name": "Hex Bolt M8 (box of 100)" }, "invoicedQuantity": 10, "price": { "priceAmount": 12.50 } }
  ],
  "legalMonetaryTotal": { "taxInclusiveAmount": 170.30, "payableAmount": 170.30 }
}
```

To test with your own PDF:

```bash
cp /path/to/invoice.pdf invoices/
camel run --source-dir=./
```

The route will pick up the PDF, send it to Docling Serve (`CONVERT_TO_MARKDOWN`), then call `openai:chat-completion` constrained by `invoice.schema.json`.
