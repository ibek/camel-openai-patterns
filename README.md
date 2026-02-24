# Camel OpenAI Integration Patterns

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Apache Camel](https://img.shields.io/maven-central/v/org.apache.camel/camel-api?label=Apache%20Camel&color=orange)](https://camel.apache.org/)
[![Java](https://img.shields.io/badge/Java-17%2B-red.svg)](https://openjdk.org/)

> **Turn LLMs into boring, effective semantic processors.**

This repository demonstrates architectural **patterns** for building reliable LLM applications using **Apache Camel**. Rather than relying on brittle prompt engineering alone, these examples show how to orchestrate interactions to ensure structured outputs, correct routing, and contextual integrity.

📖 **Read the companion article**: [Making LLMs Boring: From Chatbots to Semantic Processors](https://developers.redhat.com/articles/2026/02/04/making-llms-boring-chatbots-semantic-processors)

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

## Prerequisites

Before running the examples, ensure you have the following:

* **Java 17** or **21**
* **Inference Server**: Any server exposing OpenAI-compatible endpoints.
  * *Local Runners:* [Ollama](https://ollama.com), [vLLM](https://github.com/vllm-project/vllm), [Llama.cpp](https://github.com/ggerganov/llama.cpp), [LocalAI](https://localai.io).
  * *Cloud Providers:* OpenAI Platform, Groq, Mistral, or others (Amazon Bedrock/Google Vertex if using an OpenAI-compatible gateway).

### Install Camel Launcher

You need the Camel CLI to run these examples. [Camel Launcher Documentation](https://camel.apache.org/manual/camel-jbang-launcher.html)

**Linux / macOS**

```bash
wget https://repo1.maven.org/maven2/org/apache/camel/camel-launcher/4.18.0/camel-launcher-4.18.0-bin.zip
unzip camel-launcher-*-bin.zip
cd camel-launcher-*/
chmod +x bin/camel.sh
mkdir -p $HOME/.local/bin
ln -sf "$PWD/bin/camel.sh" "$HOME/.local/bin/camel"
```

**Windows**

1. Download and unzip the package.
2. Add the `camel-launcher/bin` directory to your System `PATH`.

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
export OPENAI_MODEL=ministral-3:8b
```

> **Note:** If using the real OpenAI API, set `OPENAI_BASE_URL` to `https://api.openai.com/v1`.

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
└── adapters/                    # Pluggable Input/Output definitions
```

---

## How to Run

Navigate to a specific pattern directory and follow its `README.md` to use the `camel run` command.

**Example: Running the Leaf Node Classification example**

```bash
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

These patterns work with any OpenAI-compatible model. For cost-effective local processing, we recommend:

| Model | Size / Active | Notes |
|-------|------|-------|
| **Ministral-3-8B** | 8B | Excellent for structured output tasks |
| **Qwen3-VL-8B** | 8B | Strong reasoning, multilingual |
| **Granite-4.0-H-Small** | 32B/9B | IBM's enterprise-focused model |

---

## Developer Tips

### Visual Route Design

Don't just write YAML by hand! Use [**Kaoto**](https://kaoto.io/) for designing your Camel routes visually, or leverage AI coding assistants (Claude, Cursor, Copilot) with prompts like:

```
Create a Camel 4.18 YAML route that monitors a folder for new text files,
sends small files (<5KB) to the camel-openai component for summarization,
and saves the response to an output folder.
```

### Export to Production

Convert these examples to Maven/Gradle projects for Quarkus or Spring Boot:

```bash
camel export --runtime=quarkus --directory=./my-project
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
