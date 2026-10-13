# Camel OpenAI Integration Patterns

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Apache Camel](https://img.shields.io/maven-central/v/org.apache.camel/camel-api?label=Apache%20Camel&color=orange)](https://camel.apache.org/)
[![Java](https://img.shields.io/badge/Java-17%2B-red.svg)](https://openjdk.org/)

> **From prompts to pipelines.**
>
> Turn an LLM into a dependable component in your data flow: predictable outputs, routing by intent, grounded answers, and tools it can't run away with.

This repository demonstrates architectural **patterns** for building reliable LLM applications using **Apache Camel**. Rather than relying on brittle prompt engineering alone, these examples show how to orchestrate interactions to ensure structured outputs, correct routing, and contextual integrity.

📖 **Read the companion articles**:

- [Making LLMs Boring: From Chatbots to Semantic Processors](https://developers.redhat.com/articles/2026/02/04/making-llms-boring-chatbots-semantic-processors)
- [Boring RAG: When Similarity is Just a SQL Query](https://developers.redhat.com/articles/2026/02/19/boring-rag-when-similarity-just-sql-query)

---

## Table of Contents

- [Architectural Patterns](#architectural-patterns)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Repository Structure](#repository-structure)
- [How to Run](#how-to-run)
- [Using Adapters](#using-adapters)
- [Recommended Models](#recommended-models)
- [Developer Tips](#developer-tips)
- [Learn More](#learn-more)
- [Contributing](#contributing)

---

## Architectural Patterns

1. **Generative Parsing**: Constraining LLM output to valid formats (JSON, XML, POJOs) for seamless integration with downstream systems.
2. **Semantic Routing**: Directing traffic flow based on the *intent* of the user's prompt rather than static headers.
3. **Grounded Pipelines**: Injecting context to ensure the LLM responds based on specific, retrieved data rather than hallucinations.
4. **Tool Calling**: Letting the LLM turn a request into a *deterministic operation* that Camel runs — a bounded, synchronous tool-call loop (not an autonomous agent) with hard limits on tools, iterations, and arguments.

## Prerequisites

Before running the examples, ensure you have the following:

* **Java 17** or **21**
* **Inference Server**: Any server exposing OpenAI-compatible endpoints.
  * *Local Runners:* [Ollama](https://ollama.com), [vLLM](https://github.com/vllm-project/vllm), [Llama.cpp](https://github.com/ggerganov/llama.cpp), [LocalAI](https://localai.io).
  * *Cloud Providers:* OpenAI Platform, Groq, Mistral, or others (Amazon Bedrock/Google Vertex if using an OpenAI-compatible gateway).
* **Docker or Podman** *(some examples only)*: a few examples spin up local infrastructure via `docker compose up -d` — PostgreSQL for `database-query`, PostgreSQL + pgvector for the embedding examples (`detect-duplicates`, `document-qa`, `similarity-search-query`), and Docling Serve for PDF extraction (`table-extraction`). Each such example's README lists its own setup step.

### Install Camel Launcher

You need the Camel CLI to run these examples. [Camel Launcher Documentation](https://camel.apache.org/manual/camel-jbang-launcher.html)

**Linux / macOS**

```bash
curl -fsSL https://camel.apache.org/install.sh -o install.sh
less install.sh
sh install.sh --version 4.22.1
```

**Windows**

```bash
irm https://camel.apache.org/install.ps1 -OutFile install.ps1
notepad install.ps1
./install.ps1 -Version 4.22.1
```

**Verify Installation**

```bash
camel --version
```

---

## Quick Start

**1. Configure your environment:**

```bash
export OPENAI_API_KEY=your-api-key
export OPENAI_BASE_URL=http://localhost:11434/v1  # Ollama example
export OPENAI_MODEL=granite4.2:8b
export OPENAI_EMBEDDING_MODEL=nomic-embed-text    # needed by embedding-based examples
```

> **Note:** If using the real OpenAI API, set `OPENAI_BASE_URL` to `https://api.openai.com/v1`.
>
> **Embeddings:** The `semantic-routing/detect-duplicates`, `grounded-pipelines/document-qa`, and `grounded-pipelines/similarity-search-query` examples use `openai:embeddings` and require `OPENAI_EMBEDDING_MODEL` to be set.

**2. Run your first example:**

```bash
cd generative-parsing/classify-leaf-node
echo "I lost my credit card and need to block it immediately" | camel run --source-dir=./
```

**3. See structured output:**

```json
{
    "rationale": "The user explicitly states they lost their credit card and requests immediate blocking, which is a critical security action to prevent unauthorized use. This falls under the highest priority category of 'Security_and_Access' due to the urgency and potential fraud risk associated with a lost card.",
    "path": "Security_and_Access > Fraud_and_Disputes > Report_Lost_or_Stolen_Card",
    "confidence": 1.0,
    "status": "ACCEPTED"
}
```

---

## Repository Structure

The project is organized by pattern. Each directory contains a standalone quickstart with its own README and runnable Camel YAML files.

```text
├── generative-parsing/          # Pattern 1: Structured Data Extraction
│   ├── classify-leaf-node/      # Deep taxonomy classification
│   ├── entity-resolution/       # Fuzzy matching to canonical IDs
│   ├── pii-redaction/           # Identify and mask PII
│   └── table-extraction/        # Extract invoice JSON from PDFs (tables/layout)
├── semantic-routing/            # Pattern 2: Intent-based Routing
│   ├── detect-duplicates/       # Embedding-based ticket deduplication
│   ├── detect-gaps/             # Compliance gap analysis
│   ├── moderation-policy/       # Content safety filtering
│   └── risk-scoring/            # Quantitative risk assessment
├── grounded-pipelines/          # Pattern 3: Context Injection
│   ├── database-query/          # Air-gapped SQL querying
│   ├── document-qa/             # RAG-based document Q&A
│   └── similarity-search-query/ # Embedding-based product search
├── tool-calling/                # Pattern 4: Bounded Tool Calling
│   ├── operate-services/        # In-process SRE tools: health API, metric query, restart, runbook
│   └── support-desk/            # Same loop split over MCP: a tool server + a remote assistant
└── adapters/                    # Pluggable Input/Output definitions
```

---

## How to Run

Navigate to a specific pattern directory and follow its `README.md` to use the `camel run` command.

**Example: Running the Leaf Node Classification example**

```bash
export OPENAI_LOG=debug

cd generative-parsing/classify-leaf-node
echo "I noticed a charge from a vendor in London that I never visited." | camel run --source-dir=./
```

**Quiet Mode (No Logging)**

If you want to focus on the output without Camel logs:

```bash
camel run --source-dir=./ --logging-level=OFF
```

---

## Using Adapters

By default, all examples use the **Console Adapter** (Standard Input/Output) for simple CLI interactivity.

You can switch the interface to HTTP, Kafka, or File by replacing the adapter route in the `*.camel.yaml` file. See the [adapters/README.md](adapters/README.md) for detailed instructions.

| Adapter | Use Case | Endpoint |
|---------|----------|----------|
| Console | CLI testing, piped input | `stream:in` / `stream:out` |
| HTTP | REST API integration | `platform-http:/api/...` |
| Kafka | Event-driven streaming | `kafka:topic-name` |
| File | Batch processing | `file:data/inbox` |

---

## Recommended Models

These patterns work with any OpenAI-compatible model. For cost-effective local processing, I recommend:

| Model | Size / Active | Notes |
|-------|------|-------|
| **granite4.2:8b** | 8B | IBM's enterprise-focused model, up to 512k context, text only, 4.8GB VRAM, full/low/no thinking |
| **Qwen3.5-4B** | 4B | Alibaba's open-weight model, 131k context, text&vision, 2.4GB VRAM, roughly 1.5-2x faster |
| **nomic-embed-text** | embeddings | Embedding model for the RAG / similarity examples, set via `OPENAI_EMBEDDING_MODEL` |

---

## Developer Tips

### Visual Route Design

Don't just write YAML by hand! Use [**Kaoto**](https://kaoto.io/) for designing your Camel routes visually, or leverage AI coding assistants (Claude, Cursor, Copilot) with prompts like:

```
Create a Camel 4.22 YAML route that monitors a folder for new text files,
sends small files (<5KB) to the camel-openai component for summarization,
and saves the response to an output folder.
```

### Export to Production

Convert the resources to Maven/Gradle project with Quarkus runtime. If the project uses Kaoto Forage dependencies (e.g. for database connection) install Kaoto Forage plugin to Camel Launcher `camel plugin add forage --gav=io.kaoto.forage:camel-jbang-plugin-forage:1.0`

```bash
camel export --runtime=quarkus --directory=./my-project <resources>
```

Convert the resources to Maven/Gradle project with SpringBoot runtime:

```bash
camel export --runtime=springboot --directory=./my-project  <resources>
```

---

## Learn More

* **Camel JBang Guide**: [Official Documentation](https://camel.apache.org/manual/camel-jbang.html#_using_camel_jbang)
* **Testing**: [How to write tests for Camel JBang](https://camel.apache.org/manual/camel-jbang-test.html)
* **Kubernetes**: [Deploying these routes to K8s](https://camel.apache.org/manual/camel-jbang-kubernetes.html)
* **Exporting**: [Convert these scripts to Maven](https://camel.apache.org/manual/camel-jbang.html#_creating_projects)

---

## Contributing

Contributions are welcome! Please read [Contributing Guide](CONTRIBUTING.md) for details on:

- Setting up your development environment
- Submitting bug reports and feature requests
- Creating new patterns

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
