# Tool Calling (Acting on the World)

**Tool Calling** lets the LLM turn a natural-language request into a *deterministic operation* — and lets Camel run it. The model reads the request, picks a tool and fills in its arguments; the built-in loop in `camel-openai` executes the matching `ai-tool` route, feeds the result back, and repeats until there is a final answer. The model stays a boring semantic processor: its only job is mapping language to a structured, bounded action.

Crucially, this is **tool calling, not an autonomous agent**. `camel-openai` gives you a synchronous, bounded loop — one request in, a handful of tool calls, one answer out — with no durable execution, planning, cross-session memory, or sub-agent orchestration. That restraint is the feature: you get language-driven actions that stay deterministic, testable, and inside limits you set (`tags`, `maxToolIterations`, `argSchema`).

Tools are plain Camel routes registered with the `ai-tool` component and discovered by tag — **no MCP server, no subprocess, no HTTP**.

**Use Cases:** Operational runbooks, guarded lookups and mutations, form-filling, and any workflow where the model should *choose and parameterize a safe operation* rather than free-text an answer.

## Examples

| Example | What It Does |
|---------|-------------|
| [operate-services](operate-services/) | SRE assistant with **in-process** tools — loop and tools in one deployable, discovered by tag |
| [support-desk](support-desk/) | The same loop **split across two services** over MCP — ownership boundary + two-layer governance (`tags` + `toolNames`) |
