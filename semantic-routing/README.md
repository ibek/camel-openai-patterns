# Semantic Routing (Directing Flow)

**Semantic Routing** is your intelligent traffic controller. Traditional routing relies on static headers or matching content. This pattern directs traffic based on *intent*. It allows the system to make dynamic decisions - blocking harmful content, prioritizing urgent requests, or flagging compliance risks based on a nuanced understanding of the text.

**Use Cases:** Content moderation, risk scoring, triage, and duplicate detection.

## Examples

| Example | What It Does |
|---------|-------------|
| [detect-duplicates](detect-duplicates/) | Routes tickets by **embedding similarity** — link duplicates or create new |
| [detect-gaps](detect-gaps/) | Compliance gap analysis that forces **quoted evidence** for every check |
| [moderation-policy](moderation-policy/) | Content safety filtering against **your** rules, decoding evasion first |
| [risk-scoring](risk-scoring/) | Turns subjective text into an integer **riskScore** for deterministic routing |
