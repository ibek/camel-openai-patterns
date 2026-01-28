# Contributing to Camel OpenAI Patterns

Thank you for your interest in contributing! This project demonstrates architectural patterns for building reliable LLM applications using Apache Camel.

## Getting Started

### Prerequisites

1. **Java 17 or 21**
2. **Camel CLI** - [Installation Guide](https://camel.apache.org/manual/camel-jbang-launcher.html)
3. **An LLM inference server** - Any OpenAI-compatible endpoint:
   - Local: [Ollama](https://ollama.com), [vLLM](https://github.com/vllm-project/vllm), [llama.cpp](https://github.com/ggerganov/llama.cpp)
   - Cloud: OpenAI, Groq, Mistral

### Setup

1. Fork and clone the repository
2. Configure your environment:

```bash
export OPENAI_API_KEY=your-key-here
export OPENAI_BASE_URL=http://localhost:11434/v1  # Example for Ollama
export OPENAI_MODEL=ministral-3-8b
```

3. Test an example:

```bash
cd generative-parsing/classify-leaf-node
echo "I lost my credit card" | camel run --source-dir=./
```

## How to Contribute

### Reporting Bugs

Use the [bug report template](.github/ISSUE_TEMPLATE/bug_report.md) and include:
- Steps to reproduce
- Expected vs actual behavior
- Your environment (OS, Java version, model used)
- Relevant logs

### Suggesting New Patterns

1. Open a [feature request](.github/ISSUE_TEMPLATE/feature_request.md)
2. Describe the use case and which category it fits:
   - **Generative Parsing**: Structuring unstructured data
   - **Semantic Routing**: Intent-based traffic control
   - **Grounded Pipelines**: Air-gapped execution with context

### Submitting Changes

1. Create a feature branch: `git checkout -b feature/my-pattern`
2. Follow the existing project structure:

```
pattern-category/
└── your-pattern/
    ├── application.properties
    ├── your-pattern.camel.yaml
    ├── output.schema.json
    └── README.md
```

3. Ensure your pattern:
   - Works with the default console adapter
   - Has a README with example commands

4. Test with at least one LLM provider
5. Submit a pull request

## Code Style

- **YAML**: 2-space indentation
- **JSON Schemas**: Include `description` fields for all properties
- **Variable naming**: Use `camelCase` for Camel headers and variables

## Design Principles

This project follows these principles:

1. **Determinism over creativity** - Low temperature, structured outputs
2. **Transparency over magic** - No hidden prompts or runtime proxies
3. **Validation over trust** - JSON schema validation on all LLM outputs
4. **Separation of concerns** - AI parses/synthesizes, system executes

## Questions?

Open a [discussion](../../discussions) or reach out via issues.
