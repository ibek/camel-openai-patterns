# Operate Services (Tool Calling)

**The Goal:** Let the LLM *decide which deterministic operation to run* from a natural-language request — while Camel routes do the actual work, inside limits you set.

The LLM never runs anything itself. It reads the request, picks a tool and fills in its arguments; the built-in loop in `camel-openai` executes the matching Camel route, feeds the result back, and repeats until there is a final answer. The model stays a *boring semantic processor* — its only job is mapping language to a structured tool call.

> ### This is tool calling, not an autonomous agent
>
> `camel-openai` gives you a **synchronous, bounded tool-call loop**: one request in, a handful of tool calls, one answer out. That is genuinely useful — but it is deliberately **not** a long-running agent runtime. There is no durable execution or checkpointing, no planning/reflection beyond the retry loop, no cross-session memory, and no sub-agent orchestration. If you need those, reach for a dedicated agent framework. If you need *"map this request to a safe, deterministic operation and run it,"* this is exactly the right tool — and staying in these bounds is the point.

The tools mirror how a real SRE assistant works: a **status API** that reports every service at once, a **metrics query** the model shapes from fuzzy language, a recovery **action**, and a **runbook** lookup. They are plain Camel routes registered with the `ai-tool` component — **no MCP server, no subprocess, no HTTP**. The `openai:chat-completion` endpoint discovers them with a tag match:

```yaml
- to:
    uri: openai:chat-completion
    parameters:
      tags: devops          # only tools tagged "devops" are visible to the model

- route:
    from:
      uri: ai-tool:query_metric
      parameters:
        tags: devops        # this route becomes a tool the model can call
        description: "Query a single metric for one service over a time window."
        argSchema: "classpath:query-metric.args.schema.json"  # tool inputs as JSON Schema
```

### What keeps it bounded

| Knob | Where | What it limits |
|------|-------|----------------|
| `tags=devops` | `openai:chat-completion` | *Which* tools the model can ever see |
| `maxToolIterations=5` | `openai:chat-completion` | *How many* tool-call passes the loop can take before it stops |
| `argSchema` (classpath JSON Schema) | `ai-tool` route | *The shape and allowed values* of tool arguments (`enum`, `required`, `additionalProperties: false`) |
| `direct:` routes | the tool bodies | *What actually executes* — deterministic, testable Camel |
| `returnDirect=true` | `ai-tool:get_runbook` | *When the model is in the loop at all* — a terminal tool returns verbatim, skipping the final model round-trip |

### The four tools

Each tool covers a different reason to call a tool at all — which is what makes the model *choose* rather than loop:

- **`get_service_health`** — read-only, **no arguments**. Returns every service's status in **one call**, like a status API or Prometheus alert summary. This is why *"are all services healthy?"* is a single call — the efficiency is in the tool shape, not in clever prompting. The result flows back so the model can summarize it.
- **`query_metric`** — read-only, and the tool that makes tool calling worth it. The model turns a fuzzy request (*"why has payments been flaky this afternoon?"*) into a **structured query** — `service` + `metric` + `window` — that a metrics backend can answer. This is the same job as *Generative Parsing*, but the parsed result is a tool call.
- **`restart_service`** — mutating. Its result flows back so the model can confirm the outcome in plain language.
- **`get_runbook`** — read-only, `returnDirect=true`. The runbook text *is* the answer and needs no interpretation, so the loop short-circuits and returns it verbatim (`CamelOpenAIMcpReturnDirect=true`), no extra LLM turn. This is the right home for `returnDirect`: a single, terminal call. (Putting it on `get_service_health` or `query_metric` would be a bug — those results need the model to interpret and summarize them.)

Known services (canned data): `checkout-api` (HEALTHY), `payments-api` (DEGRADED, 7.2% errors), `inventory-db` (DOWN).

## Run examples

using default console-adapter:
```bash
export OPENAI_LOG=info

# Aggregate read — ONE call to get_service_health, then a summary
echo "Are all services healthy?" | camel run --source-dir=./ --logging-level=INFO

# Structured query — model shapes a query_metric call (service + metric + window)
echo "What's the error rate on payments-api over the last hour?" | camel run --source-dir=./

# Chained diagnosis — get_service_health finds the problem, query_metric explains it
echo "Is anything degraded right now, and if so what do the metrics say?" | camel run --source-dir=./

# Action path — model calls restart_service, then confirms in prose
echo "inventory-db is down, please restart it and let me know how it went." | camel run --source-dir=./

# returnDirect path — get_runbook short-circuits, you get the runbook verbatim
echo "Show me the runbook for payments-api." | camel run --source-dir=./
```

using http-adapter:
```bash
camel run --source-dir=./
curl -d "Are all services healthy?" http://localhost:8080/api/ops
```

Tip: run with `--logging-level=INFO` to watch the loop pick tools; `CamelOpenAIMcpToolCalls` lists the tools called and `CamelOpenAIToolIterations` shows how many loop passes it took.
