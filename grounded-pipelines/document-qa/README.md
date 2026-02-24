## Document Q&A

**The Goal:** Answer questions using only retrieved document context.

This is the core **RAG** (Retrieval-Augmented Generation) pattern implemented as a Grounded Pipeline. The user's question is converted to a vector, relevant document chunks are retrieved via pgvector similarity search, and the LLM produces an answer grounded only in the retrieved content. If no relevant chunks are found, the system explicitly says it doesn't have the information rather than hallucinating.

## Prerequisite

Start and initialize PostgreSQL with pgvector:
```
docker compose up -d
```

Index documents:
```
camel run index-documents.camel.yaml utils/* application.properties
```

## Run examples

```
echo "What is the return policy?" | camel run document-qa.camel.yaml application.properties
```

```
echo "How do I reset my password?" | camel run document-qa.camel.yaml application.properties
```

```
echo "Our users are able to log in, but they are complaining about extremely slow synchronization speeds and unusually high CPU usage. We don't see any error codes. What network configuration might be causing this?" | camel run document-qa.camel.yaml application.properties
```

To add your own documents, place `.md` files in the `documents/` folder and re-run the indexing step.
