## Content Moderation

**The Goal:** Filter content based on *your* rules, not generic OpenAI defaults.

Generic filters fail against adversarial users who use "l33t speak" to bypass blocks. This schema requires a `decodedFragment` field, forcing the model to normalize the text *before* judging it. This "Chain of Thought" makes the moderation decision explainable and robust.

**Tip:** Instead of using OpenAI chat-completion, take a look at Moderation APIs and specialized guardian models

## Run examples

```
echo 'Yo guys, dm me on d!sc0rd (user: k_ai#99). I found a glitch for f r e e m o n e y, 100% g u a r a n t e e d.' | camel run --source-dir=./
```