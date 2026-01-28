# Adapters

Adapters define how data enters and exits your Camel routes. They decouple the **core processing logic** from the **transport layer**, allowing you to switch between CLI testing, HTTP APIs, message queues, or file-based processing without modifying your business logic.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Your Camel Route                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────┐     ┌──────────────────┐     ┌──────────────┐    │
│  │ ADAPTER  │────▶│  direct:process  │────▶│   ADAPTER    │    │
│  │  (input) │     │  (your logic)    │     │   (output)   │    │
│  └──────────┘     └──────────────────┘     └──────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

All quickstart examples use the `direct:` component as their entry point. The adapter routes simply bridge external inputs to this internal endpoint.

---

## Available Adapters

### Console Adapter (`console-adapter.camel.yaml`)

**Best for:** Local development, CLI testing, shell pipelines

Reads from standard input and writes to standard output.

```yaml
- route:
    from:
      uri: stream
      parameters:
        kind: in
      steps:
        - to: "direct:your-logic"
        - to: "stream:out"
```

**Usage:**

```bash
echo "Your input text" | camel run --source-dir=./
```

**Configuration:** Add to `application.properties`:

```properties
camel.main.durationMaxMessages=1
```

---

### HTTP Adapter (`http-adapter.camel.yaml`)

**Best for:** REST API integration, web services, microservices

Exposes an HTTP POST endpoint for receiving requests.

```yaml
- route:
    from:
      uri: "platform-http:/api/classify"
      parameters:
        httpMethodRestrict: "POST"
      steps:
        - to: "direct:your-logic"
        - removeHeaders: "*"
        - setHeader:
            name: "Content-Type"
            constant: "application/json"
```

**Usage:**

```bash
# Start the server
camel run --source-dir=./

# Send requests
curl -X POST -d "Your input text" http://localhost:8080/api/classify
```

**Configuration:** Add to `application.properties`:

```properties
camel.server.port=8080
# Remove: camel.main.durationMaxMessages=1
```

---

### Kafka Adapter (`kafka-adapter.camel.yaml`)

**Best for:** Event-driven architectures, streaming pipelines, async processing

Consumes from one Kafka topic and produces to another. Includes a dead-letter channel for error handling.

```yaml
- errorHandler:
    deadLetterChannel:
      deadLetterUri: "kafka:topic.manual-review"
      useOriginalMessage: true

- route:
    from:
      uri: "kafka:topic.received"
      parameters:
        groupId: "my-service"
        autoOffsetReset: "earliest"
      steps:
        - to: "direct:your-logic"
        - to: "kafka:topic.processed"
```

**Usage:**

```bash
# Start Kafka (using Camel's built-in infrastructure)
# **Podman users:** Set `export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock`
camel infra run kafka

# Start the consumer
camel run --source-dir=./

# Produce a message
echo "Your input" | kcat -P -b localhost:9092 -t topic.received

# Consume results
kcat -C -b localhost:9092 -t topic.processed
```

**Configuration:** Add to `application.properties`:

```properties
camel.component.kafka.brokers=localhost:9092
# Remove: camel.main.durationMaxMessages=1
```

---

### File Adapter (`file-adapter.camel.yaml`)

**Best for:** Batch processing, file-based integrations, legacy system bridges

Watches a directory for new files and writes results to an output folder.

```yaml
- route:
    from:
      uri: "file:data/inbox"
      parameters:
        move: data/processed/${date:now:yyyyMMdd-HHmmss}-${file:name}
      steps:
        - to: "direct:your-logic"
        - to:
            uri: "file:data/outbox"
            parameters:
              fileName: "result-${date:now:yyyyMMdd-HHmmss}.json"
```

**Usage:**

```bash
# Create directories
mkdir -p data/inbox data/outbox

# Start the watcher
camel run --source-dir=./

# Drop a file
echo "Your input text" > data/inbox/request.txt

# Check output
cat data/outbox/result-*.json
```

**Configuration:** Remove from `application.properties`:

```properties
# Remove: camel.main.durationMaxMessages=1
```

---

## How to Switch Adapters

1. **Copy the adapter content** from this folder into your quickstart's `.camel.yaml` file
2. **Replace the existing console route** (the one with `stream:in`)
3. **Update `application.properties`** with the required configuration
4. **Restart** with `camel run --source-dir=./`

### Example: Switching classify-leaf-node to HTTP

1. Open `generative-parsing/classify-leaf-node/classify-leaf-node.camel.yaml`
2. Replace the console adapter route at the bottom with the HTTP adapter content
3. Update the endpoint name (`direct:classify-leaf-node`)
4. Add `camel.server.port=8080` to `application.properties`
5. Remove `camel.main.durationMaxMessages=1` if present

---

## Creating Custom Adapters

You can create adapters for any Camel component. The pattern is simple:

```yaml
- route:
    id: my-custom-adapter
    from:
      uri: "component:source"
      steps:
        - to: "direct:your-logic"
        - to: "component:destination"
```

Popular options include:
- `amqp:` / `jms:` - Message queues
- `aws2-s3:` - AWS S3 buckets
- `ftp:` / `sftp:` - File transfers
- `telegram:` / `slack:` - Chat integrations
- `timer:` - Scheduled execution

See the [Camel Component Catalog](https://camel.apache.org/components/latest/) for 300+ available connectors.
