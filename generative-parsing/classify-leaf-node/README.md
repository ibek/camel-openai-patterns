## Leaf-Node Classification

**The Goal:** Map content into a deep tree, not just loose keywords.

If you let them, LLMs are lazy. They will dump everything into the root category (e.g., "Banking"). This schema enforces a **"Leaf Node" selection**, forcing the model to be specific.

## Run examples

using default console-adapter:
```bash
echo "I noticed a charge on my account from a vendor in London that I never visited. I need this money back." | camel run --source-dir=./

echo "My card isn't working at the store." | camel run --source-dir=./
```

using http-adapter:
```bash
camel run --source-dir=./
curl -d "I noticed a charge on my account from a vendor in London that I never visited. I need this money back." http://localhost:8080/api/classify
```

using file-adapter:
```bash
camel run --source-dir=./

# in generative-parsing/classify-leaf-node folder
mkdir -p data/inbox
echo "I noticed a charge on my account from a vendor in London that I never visited. I need this money back." > data/inbox/request.txt
```

using kafka-adapter:
```
camel run --source-dir=./

# to test locally
# for PODMAN: export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock
run `camel infra run kafka redpanda`

echo "I noticed a charge on my account from a vendor in London that I never visited. I need this money back." | kcat -P -b localhost:9092 -t banking.inquiry.received

kcat -C -b localhost:9092 -t banking.inquiry.classified
```