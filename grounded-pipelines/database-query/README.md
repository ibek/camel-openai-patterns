## "Air-Gapped" Database Querying

**The Goal:** Turn conversation into database constraints

Giving an LLM direct SQL access is a major security risk. This pattern separates **intent from execution**. The LLM defines *what* to search for (filters, limits, categories), but your trusted code handles the actual database connection, effectively **"air-gapping"** your sensitive data and logic from the model's unpredictability.

## Run examples

```
echo 'Show me only available Dell monitors' | camel run --source-dir=./
```

```
echo 'Price of a ThinkPad?' | camel run --source-dir=./
```