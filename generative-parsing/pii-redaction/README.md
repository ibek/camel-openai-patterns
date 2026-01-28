## PII Redaction

**The Goal:** Identify and mask personally identifiable information (PII)

This relies on **Span Verification**. We ask the model to return the original `span` of text it identified as PII. This allows us to programmatically verify that the text actually exists in the input before we let the code redact it. It adds a layer of deterministic safety to the probabilistic LLM output.

**Tip:** Instead of using OpenAI chat-completion, take a look at Moderation APIs and specialized guardian models

## Run examples

```
echo 'Customer John Doe (email: john.doe@example.com) requested a refund for order #998877.' | camel run --source-dir=./
```