## Ticket Deduplication

**The Goal:** Route tickets based on similarity — duplicates get linked, new ones get created.

This extends Semantic Routing with **embeddings**. Instead of routing based on content classification, we route based on how similar the new content is to existing content. The route converts each ticket into a vector using `openai:embeddings`, searches for similar tickets using pgvector, and either links to an existing ticket or creates a new one.

## Prerequisite

Start and initialize PostgreSQL with pgvector:
```
docker compose up -d

```

## Run examples

First ticket (creates new):
```
echo "I cannot log into my account" | camel run --source-dir=./
```

Second ticket (detected as duplicate):
```
echo "My login is not working" | camel run --source-dir=./
```

More test pairs:
```
echo "Payment failed" | camel run --source-dir=./
echo "Credit card declined" | camel run --source-dir=./
```
