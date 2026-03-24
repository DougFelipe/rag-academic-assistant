# Distributed AI-Powered Question Answering System with RAG over Academic Profiles

---

## Abstract

This repository implements a distributed, cloud-native question-answering system designed to answer natural-language queries about academic staff. The system combines Retrieval-Augmented Generation (RAG) with LLM function calling over a structured repository of faculty profiles encoded as Markdown documents. The architecture follows the microservices pattern, with independent Spring Boot services coordinated through service discovery, centralized configuration, and an API gateway. Fault tolerance, observability, and horizontal scalability are treated as first-class concerns.

---

## System Architecture

The system is organized into six independently deployable services communicating over a named Docker bridge network (`distribuida3-network`). Service discovery is provided by Netflix Eureka, and all runtime configuration is externalized to a Spring Cloud Config Server backed by a Git repository.

```
Client
  └─► Gateway Service (8080)
        ├─► AI Service (8082)          ← RAG + LLM function calling
        ├─► Orchestrator Service (8081) ← Workflow coordination
        └─► Function Service (8083)    ← Stateless computation functions

Infrastructure
  ├─► Config Server (8888)             ← Centralized configuration
  ├─► Eureka Server (8761)             ← Service registry
  └─► MCP External Service (8084)      ← External API integrations

Observability
  ├─► Prometheus (9090)
  ├─► Grafana (3001)
  └─► Zipkin (9411)
```

### Data Flow — Question Answering

1. Client issues `POST /api/ai/ask` carrying a natural-language question and optional professor name hint.
2. The Gateway applies circuit-breaker and rate-limiting filters and routes the request to the AI Service via Eureka-based load balancing.
3. `AIProcessingService` performs keyword extraction and retrieves up to four matching professor records from an in-memory repository seeded from Markdown files at startup.
4. The enriched context is forwarded to a Spring AI `ChatClient` configured for the active LLM backend (Ollama by default).
5. The LLM may invoke registered `@Tool` functions — `listProfessors`, `professorInfo`, `searchByResearchArea` — to fetch additional data programmatically during generation.
6. A confidence score is computed from context quality and returned alongside the answer, source references, and processing metadata.

---

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17+ |
| Application framework | Spring Boot | 3.4.0+ |
| Microservices patterns | Spring Cloud (Eureka, Config, Gateway, Function) | 2024.x |
| LLM integration | Spring AI — Ollama starter | — |
| Resilience | Resilience4j (CircuitBreaker, Retry, TimeLimiter, RateLimiter) | — |
| Metrics | Micrometer + Prometheus registry | — |
| Distributed tracing | Micrometer Tracing + Brave + Zipkin (B3/W3C propagation) | — |
| Markdown parsing | flexmark-all | 0.64.8 |
| Build | Maven | 3.8+ |
| Containerization | Docker Compose | — |
| Load testing | Apache JMeter | — |

---

## Services

### AI Service
Core question-answering service. Implements the RAG pipeline: Markdown-based faculty profile ingestion at startup, keyword-driven retrieval, context assembly, and LLM invocation with function-calling tools. Resilience4j patterns wrap the LLM call path with a named circuit breaker (`aiClient`), exponential-backoff retry, and a time limiter. Responses include confidence scores, source attribution, and end-to-end processing time.

**Key classes:**
- `AIProcessingService` — RAG orchestration, LLM integration, resilience wrappers
- `AIController` — REST API (`/api/ai/**`)
- `ProfessorRepository` — In-memory `Map<String, Professor>` populated from classpath Markdown
- `MarkdownParser` — YAML-frontmatter + Markdown → `Professor` entity

**Exposed endpoints:**

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/ai/ask` | Submit a question; returns answer with confidence and sources |
| `GET` | `/api/ai/professors` | List all professor names |
| `GET` | `/api/ai/professors/{nameOrId}` | Retrieve a specific professor profile |
| `GET` | `/api/ai/professors/search?keyword=X` | Search by research area keyword |
| `GET` | `/api/ai/models` | List available AI models |
| `GET` | `/api/ai/health` | Service health check |

### Gateway Service
Single external entry point. Provides route definitions (managed in the Config Server repository), Eureka-based service discovery with `lb://` load-balanced URIs, and Resilience4j filters for circuit breaking and rate limiting at the edge. All client traffic — including JMeter load tests — enters through port 8080.

### Function Service
Hosts stateless Spring Cloud Function implementations used for question preprocessing and answer enrichment. Functions are exposed automatically via HTTP and can be invoked by the AI Service or Orchestrator. The service has no external dependencies and carries no shared state.

### Orchestrator Service
Coordinates multi-step workflows between the AI Service and Function Service. Routes complex request pipelines and is reachable through the Gateway at `/api/orchestrator/**`.

### MCP External Service
Handles integrations with external APIs. Currently active: OpenWeatherMap (weather data for Natal, BR, in metric/Portuguese locale). Additional integrations (GitHub, News API) are declared in environment configuration but not yet verified as active in the codebase.

### Infrastructure Services
- **Config Server** — Serves YAML configuration to all services at bootstrap time; supports live refresh via `POST /actuator/refresh`.
- **Eureka Server** — Maintains the service registry; all business services register on startup and use it for peer discovery.

---

## Observability

All services expose `/actuator/prometheus` for metric scraping and are instrumented with Micrometer Tracing. Traces are exported to Zipkin using HTTP transport with B3 propagation format. 100% trace sampling is enabled by default. Actuator endpoints exposed: `health`, `info`, `metrics`, `prometheus`, `env`, `loggers`. Circuit breaker and rate limiter state is included in health responses.

| Tool | URL | Purpose |
|---|---|---|
| Prometheus | `http://localhost:9090` | Metric storage and alerting |
| Grafana | `http://localhost:3001` | Metric visualization (data source: Prometheus) |
| Zipkin | `http://localhost:9411` | Distributed trace visualization |

---

## Resilience Configuration

| Pattern | Parameter | Value |
|---|---|---|
| Circuit Breaker | Sliding window | 10 calls |
| Circuit Breaker | Failure rate threshold | 50% |
| Circuit Breaker | Wait duration in OPEN state | 10 s |
| Retry | Max attempts | 3 |
| Retry | Backoff multiplier | 2× |
| Rate Limiter | Calls per second | 50 |
| Bulkhead | Max concurrent calls | 25 |

When the circuit opens, `AIProcessingService.fallbackProcessQuestion()` returns a structured fallback response with alternative guidance and logs circuit state for monitoring.

---

## Repository Structure

```
.
├── ai-service/               # RAG + LLM service
├── config-server/            # Spring Cloud Config Server
├── config-repo/              # Git-backed configuration source
├── eureka-server/            # Netflix Eureka registry
├── function-service/         # Spring Cloud Function implementations
├── gateway-service/          # API Gateway (Spring Cloud Gateway MVC)
├── mcp-external-service/     # External API integrations
├── jmeter/                   # Performance test scripts
├── docker-compose-master.yml # Top-level orchestration
├── docker-compose.config.yml
├── docker-compose.eureka.yml
├── docker-compose.services.yml
├── docker-compose.observability.yml
└── .env.example              # Environment variable template
```

Package convention: `br.ufrn.distribuida.{service}` (e.g., `br.ufrn.distribuida.ai`, `br.ufrn.distribuida.gateway`).

---

## Running the System

**Prerequisites:** Java 17+, Maven 3.8+, Docker, Ollama (local LLM runtime).

```bash
# 1. Copy and configure environment variables
cp .env.example .env

# 2. Set the Ollama endpoint (if not running on localhost)
# OLLAMA_BASE_URL=http://<host>:11434

# 3. Start all services
docker compose -f docker-compose-master.yml up --build
```

Services start in dependency order: Config Server → Eureka → Gateway + AI Service + Function Service + Orchestrator → MCP External Service. The observability stack starts independently.

**Primary API entry point:** `http://localhost:8080`

---

## Configuration

Configuration is externalized to `config-repo/` and served by the Config Server. Three Spring profiles are defined: `local`, `docker`, and `prod`. Active profile is set via `SPRING_PROFILES_ACTIVE`. Key environment variables:

| Variable | Default | Description |
|---|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama API endpoint |
| `EUREKA_URL` | `http://localhost:8761/eureka` | Eureka registry |
| `CONFIG_SERVER_URL` | `http://localhost:8888` | Config Server |
| `ZIPKIN_BASE_URL` | `http://localhost:9411` | Zipkin collector |
| `OPENWEATHER_API_KEY` | — | OpenWeatherMap credentials |

---

## Limitations and Scope

- **Data persistence:** Professor data is held entirely in memory and loaded from Markdown files at startup. There is no relational database integration active in the current implementation (PostgreSQL, Redis, and RabbitMQ are declared in the environment template but not wired into the application).
- **Authentication:** No authentication or authorization layer is active. The system assumes a trusted internal network. JWT-based auth is declared in environment configuration but has no corresponding Spring Security integration.
- **CI/CD:** No automated pipeline configuration is present in the repository.
- **LLM backend:** Ollama is the active provider. The OpenAI starter is explicitly excluded from the `ai-service` autoconfigure list; switching providers requires configuration changes only, with no code modifications needed.

---

## License

See `LICENSE` file in the repository root.
