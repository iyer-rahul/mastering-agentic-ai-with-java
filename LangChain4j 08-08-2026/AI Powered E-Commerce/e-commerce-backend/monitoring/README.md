# Monitoring

Prometheus scrapes the app's metrics; Grafana displays them. Both are provisioned from files in
this folder, so the dashboard lives in git rather than only inside somebody's Grafana.

## Running it

```bash
# 1. Start the app first (it must be listening on 8080)
mvn spring-boot:run

# 2. Start the monitoring stack from this folder
cd monitoring
docker compose up -d
```

| What       | Where                                    | Login         |
|------------|------------------------------------------|---------------|
| Grafana    | http://localhost:3000                    | admin / admin |
| Dashboard  | Dashboards → E-Commerce → E-Commerce AI Monitoring | |
| Prometheus | http://localhost:9090                    | none          |
| Raw metrics| http://localhost:8080/actuator/prometheus | none          |

Check `http://localhost:9090/targets` first — if the target is not **UP**, no panel will have data.

## What is measured

**From Spring AI, automatically** — `gen_ai_client_operation_seconds` (model latency, tagged with
model and operation) and `gen_ai_client_token_usage_total` (input/output tokens). These answer
"is OpenAI slow?" and "what are we spending?".

**From the app, in `AiMetrics`** — these answer the questions the generic metrics cannot:

| Metric | Why it exists |
|---|---|
| `ecommerce_ai_feature_seconds` | Latency and failures per feature (`product-search`, `product-qa`, `recommendations`, `cart-suggestions`, `customer-assistant`, `admin-analytics`, `ticket-triage`, `return-eligibility`). Tells you *which* feature broke. |
| `ecommerce_ai_retrieval_documents` | How many documents each retrieval found. Falling towards zero means RAG quality is degrading. |
| `ecommerce_ai_retrieval_empty_total` | Retrievals that found nothing. The assistant then answers from general knowledge instead of your data — the earliest signal that indexing broke or the similarity threshold is too strict. |
| `ecommerce_ai_degraded_total` | AI call failed and a fallback was served. These return HTTP 200, so they are invisible everywhere else. |

## Notes

- **Prompts and completions are deliberately excluded** from metrics and traces
  (`spring.ai.chat.observations.include-*=false`). They contain customer names, addresses and
  order details, and observability backends are not access controlled.
- **`/actuator/prometheus` is open** so Prometheus can scrape it. In a real deployment do not
  leave it reachable from the internet: move Actuator to its own port with
  `management.server.port` and firewall it, or put the scrape behind basic auth.
- **Tracing is sampled at 100%** for local development. Lower
  `management.tracing.sampling.probability` in production.
- Panels use `rate(...[5m])`, so they go blank when there has been no traffic for five minutes.
  That is expected, not a broken dashboard.
- Embedding latency dominates vector search: the vector-store timer includes the embedding call,
  which is why those two p95 panels tend to track each other.
