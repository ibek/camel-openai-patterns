## Canonical Entity Resolution

**The Goal:** Raw text + a whitelist → ID.

Unlike a database lookup, this pattern excels at **fuzzy semantic matching**. It handles typos, abbreviations, and partial matches (e.g., mapping "Orion Logs" to "Orion Logistics") without you needing to write a complex fuzzy-search algorithm.

## Run examples

```
echo 'I am sending the payment to Orion Logs tomorrow.' | camel run --source-dir=./
```

```
echo 'We need to verify the latest invoice from Acme VAT regarding the Q3 tax filing.' | camel run --source-dir=./
```