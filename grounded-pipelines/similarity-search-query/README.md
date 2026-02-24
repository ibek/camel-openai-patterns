## Similarity Search Query

**The Goal:** Find products by meaning, not just exact filters.

This extends the Grounded Pipeline pattern with **embeddings**. Instead of the LLM parsing intent into exact filters (e.g., `category="MONITOR"`, `brand="Dell"`), we use vector similarity to find products matching the *meaning* of what the user wants. The AI never accesses the database directly — embeddings are pre-computed, and search is done by pgvector.

## Prerequisite

Start and initialize PostgreSQL with pgvector:
```
docker compose up -d

```

Index products:
```
camel run index-products.camel.yaml application.properties
```

## Run examples

```
echo "I need a large screen for design work" | camel run query-inventory.camel.yaml application.properties
```

```
echo "Something for blocking out noise" | camel run query-inventory.camel.yaml application.properties
```
