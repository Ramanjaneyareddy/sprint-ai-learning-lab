# Spring AI Learning Lab

A hands-on Spring Boot project that walks through Spring AI step by step: prompts, output converters, tool calling, advisors, chat memory, streaming, RAG (simple, filtered, multi-hop and advanced), audio, images and a few small assignments. It is built to run against **HCL AI Cafe** (an Azure OpenAI style gateway), and can also be pointed at OpenAI.

## Tech stack

| Area | Choice |
|---|---|
| Language / build | Java 25, Maven |
| Framework | Spring Boot 4.0.2 (Web MVC, Data JPA, Actuator) |
| AI | Spring AI 2.0.0-M2 (OpenAI starter, chat-memory JDBC, RAG, advisors, Tika reader) |
| Vector store | OpenSearch (optional); a no-op store is used when it is disabled |
| Databases | H2 in-memory (default), SQLite or PostgreSQL (optional) |
| API docs | springdoc OpenAPI / Swagger UI |

## What is implemented

1. **Integration:** the same question through `ChatClient`, `ChatModel` and a plain `RestTemplate` call. See [Spring Boot revisited](./src/main/resources/notes/1-spring-boot-revisited.md) and [Spring AI intro](./src/main/resources/notes/2-spring-ai-intro.md).
2. **Messages and prompts:** `SystemMessage`, `UserMessage`, `AssistantMessage`, `PromptTemplate` and String Template (`.st`) files.
3. **Output converters:** `ListOutputConverter`, `MapOutputConverter`, `BeanOutputConverter`.
4. **Prompting styles:** zero-shot, few-shot, chain-of-thought. See [Prompts notes](./src/main/resources/notes/3-spring-ai-prompts.md).
5. **Data augmentation:** prompt stuffing and tool calling with `@Tool` (date/time, alarm, FX rate, weather, customer lookup). See [Data augmentation](./src/main/resources/notes/4-spring-ai-data-augmentation-techniques.md).
6. **Advisors and chat memory:** `MessageChatMemoryAdvisor`, `PromptChatMemoryAdvisor`, `SimpleLoggerAdvisor`, `SafeGuardAdvisor` and a custom `TokenCalculatorAdvisor`. See [Advisors](./src/main/resources/notes/5-spring-ai-advisors.md).
7. **Streaming and per-user memory:** `Flux<String>` responses, in-memory and JDBC chat memory keyed by a `user-id` header. See [Data streaming](./src/main/resources/notes/6-spring-ai-data-streaming.md).
8. **RAG:** manual retrieval, `QuestionAnswerAdvisor`, `RetrievalAugmentationAdvisor`, metadata filters (document id, tenant, topic). See [RAG notes](./src/main/resources/notes/7-spring-ai-rag.md).
9. **Multi-hop RAG:** follows document-to-document references stored as metadata. See [Multi-file RAG](./src/main/resources/notes/8-spring-ai-work-with-multiple-files-rag.md).
10. **Advanced RAG:** query rewrite, translation, multi-query expansion, retrieval, joining and augmentation. See [Advanced RAG](./src/main/resources/notes/9-advanced-rag.md).
11. **Audio and images:** speech-to-text and text-to-image. See [Audio and images](./src/main/resources/notes/10-audio-and-images.md).
12. **Agents and MCP:** theory notes only (no code yet). See [AI agents and MCP](./src/main/resources/notes/11-ai-agents-and-mcp.md).

### Assignments

1. **Travel itinerary:** `PromptTemplate` for dynamic values plus a system message for the persona.
2. **Personal banking advisor:** balance lookup from the `CUSTOMER` table through a tool.
3. **HR advisor:** remembers earlier questions, logs prompts and responses, and blocks restricted topics.
4. **Subtitle snap:** finds the time code in an SRT subtitle file that matches a described scene (RAG).

## Prerequisites

- JDK 25 and Maven (or the Maven wrapper)
- An AI Cafe (or OpenAI) key
- Optional: OpenSearch for the vector store ([install notes](./src/main/resources/notes/install-configure-opensearch-vector-db.md))

## Configuration

Set these environment variables **before** starting the app, then restart your terminal or IDE so they are picked up.

| Variable | Required | Purpose |
|---|---|---|
| `AICAFE_API_KEY` | Yes | AI Cafe subscription key (also used as `spring.ai.openai.api-key`) |
| `AICAFE_BASE_URL` | Yes | Gateway URL, for example `https://aicafe.hcl.com/AICafeService/api/v1/subscription` (no trailing slash) |
| `AICAFE_ENDPOINT` | Yes | Same gateway URL |
| `WEATHER_API_KEY` | For `/weather` | Key from [weatherapi.com](https://www.weatherapi.com/) |
| `OPENSEARCH_USERNAME`, `OPENSEARCH_PASSWORD` | Only with the vector store | OpenSearch credentials |

> The older `API_KEY_OPENAI` variable is no longer read. Do not commit keys to the repository.

### Turn AI Cafe on

AI Cafe support is **off by default** (`spring.ai.cafe.enabled: false`). With it off, requests go to the default OpenAI URL and your AI Cafe key is rejected as invalid. Enable it in `application.yaml`:

```yaml
spring:
  ai:
    cafe:
      enabled: true
```

or start with `SPRING_AI_CAFE_ENABLED=true`. On startup you should see the log line `Creating custom OpenAiApi bean`. When enabled, the app sends the key in an `api-key` header and calls paths such as `/openai/deployments/gpt-4.1/chat/completions?api-version=2024-12-01-preview`. Deployment names (`gpt-4.1`, `ada`) and the API version live under `spring.ai.cafe` in `application.yaml`.

### Optional: vector store (needed for RAG endpoints)

```yaml
spring:
  ai:
    custom:
      vectorstore:
        enabled: true
        opensearch:
          uris: http://localhost:9200
```

Start OpenSearch first. When this flag is `false`, a no-op vector store is used, so RAG endpoints return no context. Embeddings for the vector store go through the configured OpenAI client (AI Cafe when it is enabled).

### Optional: database

H2 in memory is the default. To use SQLite or PostgreSQL, set `spring.custom.sqlite.datasource.enabled` or `spring.custom.postgres.datasource.enabled` to `true`. The `/chat/persistent/user-aware` endpoint only survives restarts when a file-based or server database is enabled.

## Run

```bash
mvn spring-boot:run
```

Open Swagger UI at <http://localhost:8080/swagger-ui.html>. The H2 console is enabled by default.

### Load test data

Call these once (POST) from Swagger before using the related features:

| Endpoint | Loads |
|---|---|
| `/data-feed/save-java-test-data` | Java facts into the vector store |
| `/data-feed/save-employee-project-test-data` | `docs/file1-3.docx` with tenant `ORG` and document references |
| `/data-feed/save-last-signal-subtitle-data` | The SRT file in small chunks |
| `/data-feed/save-big-bunny-subtitle-test-data` | A tiny subtitle sample |
| `/data-feed/save-customer-test-data` | Two customers into the `CUSTOMER` table |

## Endpoints

| Lab | Endpoint | Notes |
|---|---|---|
| 1 | `GET /chat/ask-me`, `/chat/ask`, `/chat/joke` | ChatClient, ChatModel, RestTemplate |
| 2 | `GET /popular`, `/dest`, `/authors/{author}`, `/byauthor` | Templates and output converters |
| 3 | `GET /explain1`, `/explain2`, `/explain3` | Zero-shot, few-shot, chain-of-thought |
| 4 | `GET /sports?stuffit=true`, `/weather`, `/info` | Stuffing and tool calling |
| 5 | `GET /chat/?msg=`, `POST /chat/{conversationId}` | Advisors and memory |
| 6 | `GET /chat/stream`, `/chat/user-aware`, `/chat/persistent/user-aware` | Header `user-id` required for the last two |
| 7 | `GET /rag/answer`, `/rag/answer-query`, `/rag/answer-coding-doubt` | Manual, QuestionAnswerAdvisor, RetrievalAugmentationAdvisor |
| 7 | `GET /rag/answer-from-document` | Params `question`, `document-id`; header `X-TENANT` |
| 7 | `GET /rag/multi-hop-search` | Params `start-document-id`, `maxHops`, `topK`, `threshold`; header `X-TENANT` |
| 7 | `GET /rag/search` | Advanced RAG pipeline |
| 8 | `GET /audio/v1/transcribe`, `/audio/v2/transcribe` | Param `filename` (default `java.m4a`) |
| 9 | `GET /ai-image/v1/gen` | Params `context`, `image-width`, `image-height` |
| Assignments | `GET /travel-itinerary/`, `/banking-advisor/`, `/hr-advisor/`, `/subtitle/snap-to-position` | |

## Project layout

```
src/main/java/com/edu/spring/ai
  config/      AI Cafe client, vector store, data sources, chat options
  controller/  REST endpoints (one class per lab)
  service/     Business logic and Spring AI calls
  tools/       @Tool classes (date/time, FX, weather, banking)
  advisor/     Custom TokenCalculatorAdvisor
  weather/     WeatherAPI client
  entity, repository, dto, util, enums, exceptions
src/main/resources
  prompts/     String Template prompt files
  docs/        Sample .docx and .txt files for RAG
  notes/       Study notes for each lab
  db/          schema.sql and sample databases
```

## Troubleshooting

| Symptom | Likely cause and fix |
|---|---|
| "Invalid API key" | `spring.ai.cafe.enabled` is false, `AICAFE_API_KEY` is empty or stale, the key expired, or you are off the HCL network |
| 404 from the gateway | Wrong base URL (trailing slash) or deployment name |
| RAG answers say the query is not in the database | Vector store disabled, or sample data not loaded |
| `/audio` or `/ai-image` fail | Audio is hard-coded to `https://api.openai.com`, and the image model uses the default OpenAI endpoint, so an AI Cafe key will not work for them |
| `/weather` fails | `WEATHER_API_KEY` is missing |

## Known limitations

- Audio and image features call OpenAI directly, not AI Cafe.
- The OpenSearch host is hard-coded to `http://localhost:9200` in `VectorStoreAutoConfig` (the `uris` property is not used).
- Keyword blocking in `SafeGuardAdvisor` is a simple substring check and is easy to bypass.
- In-memory chat memory is lost on restart.
- MCP and agents are covered in notes only.

## Developer/Contributor

Author: Ramanjaneya Reddy 
([LinkedIn](https://www.linkedin.com/in/ramanjaneyareddys/), [GitHub](https://github.com/Ramanjaneyareddy?tab=repositories)).
