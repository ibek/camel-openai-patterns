# Support Desk (Tool Calling over MCP)

**The Goal:** the *same* bounded tool-calling loop as [operate-services](../operate-services/), but with the tools living in a **separate service** — published over MCP and consumed across a process boundary.

The sibling example is captioned *"no MCP server, no subprocess, no HTTP"*: its tools are plain Camel routes discovered in-process by tag. This example is the mirror image. Two services run at once:

- **`commerce-tools`** — the tool server (the "mcp-gateway"). It owns the data and the deterministic Camel logic and has **no LLM**. `camel-mcp-server` publishes every route tagged `commerce` as an MCP tool over streamable HTTP at `http://localhost:8080/mcp`. No route is needed for the server itself — tagging the `ai-tool` routes is the whole configuration.
- **`self-service-assistant`** — the client. It has **no tools and no data access**. It runs the bounded loop in `camel-openai`, pointed at the tool server over MCP.

The model's job is identical to the in-process example: read a request, pick a tool, fill in its arguments. Only the topology changed.

### Same loop, two topologies

| Concern | `operate-services` (in-process) | `support-desk` (MCP gateway) |
|---|---|---|
| Where tools live | same YAML file as the loop | a separate service/process |
| How the model sees them | `tags=devops` on the openai endpoint | `mcpServer.commerce.url` on the openai endpoint |
| How they're published | in-JVM `AiToolRegistry` | `ai-tool` → `McpServerBridge` → HTTP |
| Ownership boundary | none | tool team vs. assistant team |
| What the *client* can restrict | (nothing beyond tags) | `toolNames` allow-list, per server |
| Client's tool dependencies | `camel-ai-tool` | none — only `camel-openai` |

### Two-layer governance is the reason to run a gateway

There are two independent allow-lists, one on each side of the boundary:

```properties
# commerce-tools/application.properties — the server decides what is publishable at all
camel.server.mcp.tags=commerce
```
```yaml
# self-service-assistant/assistant.camel.yaml — the client decides what THIS consumer may see
mcpServer.commerce.transportType: streamableHttp
mcpServer.commerce.url: http://localhost:8080/mcp
mcpServer.commerce.toolNames: check_inventory,lookup_order,get_return_policy
```

`initiate_refund` is published by the server — a human-operated console could connect and use it — but the self-service bot's `toolNames` omits it. **The model cannot call refund no matter how it is prompted.** The tool exists, is reachable over the network, and is still invisible to this consumer by policy. That is the guarantee the in-process example structurally cannot make.

### The four tools

The tools mirror the four *reasons to call a tool* from the in-process example, so they map across one-to-one:

- **`check_inventory`** — read-only, **no arguments**. Returns every product's stock in **one call**, like a status API. This is why *"is anything out of stock?"* is a single call, not one lookup per SKU. The result flows back so the model can summarize it.
- **`lookup_order`** — read-only, and the tool that makes tool calling worth it. The model turns a fuzzy request (*"where's my headphones order?"*) into a **structured call** — a single `order_id` — that an order backend can answer.
- **`get_return_policy`** — read-only, `returnDirect=true`. The policy text *is* the answer, so the loop short-circuits and returns it verbatim — **even across MCP**. The `returnDirect` annotation travels `ai-tool` → MCP → the assistant's loop and is honoured there.
- **`initiate_refund`** — mutating, `destructiveHint=true`. Published by the server, **excluded by the assistant's `toolNames`** (see above).

Known orders (canned data): `ORD-1001` (Wireless Headphones, SHIPPED), `ORD-1002` (USB-C Cable, PROCESSING), `ORD-1003` (Standing Desk, DELAYED). Inventory: headphones IN_STOCK, USB-C cable OUT_OF_STOCK, standing desk LOW.

## Run examples

You need **two terminals** — one per service. Configure your OpenAI-compatible endpoint first (see the [top-level README](../../README.md)):

```bash
export OPENAI_API_KEY=your-api-key
export OPENAI_BASE_URL=http://localhost:11434/v1   # Ollama example
export OPENAI_MODEL=granite4.2:8b
```

**Terminal 1 — start the tool server:**

```bash
cd commerce-tools
camel run --source-dir=./
# serves MCP tools at http://localhost:8080/mcp
```

**Terminal 2 — talk to the assistant:**

```bash
cd self-service-assistant

# Aggregate read — ONE call to check_inventory, then a summary
echo "Are the wireless headphones in stock?" | camel run --source-dir=./

# Structured call — the model shapes a lookup_order call from the order id
echo "What's the status of order ORD-1003?" | camel run --source-dir=./

# returnDirect over the wire — get_return_policy short-circuits, returned verbatim
echo "What's your EU returns policy?" | camel run --source-dir=./

# Governance — the model has no refund tool, so it declines and offers the policy
echo "Please refund order ORD-1001, it arrived damaged." | camel run --source-dir=./
```

That last request is the point of the example: `initiate_refund` is live on the server, but the assistant was never given it, so the model cannot call it — it explains a human must process the refund and offers the returns policy instead.

> **Tip:** the client tolerates the server not being up yet — if you start the assistant first, it logs a WARN and connects on first use. Run the assistant with `--logging-level=INFO` to watch the loop pick tools: `CamelOpenAIMcpToolCalls` lists the tools called and `CamelOpenAIToolIterations` shows how many passes it took.

### When to reach for this vs. in-process

Stay in-process ([operate-services](../operate-services/)) when the tools and the loop are one deployable. Cross the boundary (this example) when a different team owns the tools, when several assistants share one tool set, or when you need a governance point — a single place to select publishable tools, and per-consumer allow-lists on top.
