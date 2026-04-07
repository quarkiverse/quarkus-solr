# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Quarkus extension** that integrates Apache Solr (via SolrJ 10.0.0) into Quarkus applications. It follows the
standard Quarkus extension architecture with separate `deployment` and `runtime` modules, plus `integration-tests` and
`integration-tests-no-ext` modules.

## Build Commands

```bash
# Full build with tests
./mvnw clean install

# Build without formatting checks
./mvnw -B clean install -Dno-format

# Run a single test class
./mvnw test -Dtest=RunningDevServicesTest -pl deployment

# Native image build (requires Docker)
./mvnw -B install -Dnative -Dquarkus.native.container-build
```

## Architecture

### Module Structure

**`runtime/`** — Beans and config available at runtime:

- `SolrClientProducer` — CDI producer that creates `SolrClient` beans based on config
- `SolrClientProxy` — Wraps SolrClient to add metrics/observability
- `SolrRunTimeConfig` / `SolrBuildTimeConfig` / `SolrDevServicesConfig` — SmallRye Config mapping interfaces
- `observe/SolrHealthCheck` — MicroProfile Health readiness check
- `observe/SolrMetrics` — Micrometer metrics integration (timers/counters)
- `devui/SolrDevJsonRpcService` — Dev UI JSON-RPC service

**`deployment/`** — Build-time extension processors (not on runtime classpath):

- `SolrProcessor` — Main `@BuildStep` processor; registers beans, health, metrics
- `DevServiceProcessor` — Starts Solr via Docker (TestContainers) in dev/test mode
- `SolrDevContainer` — TestContainers wrapper for the Solr Docker image
- `DevUIProcessor` — Registers Dev UI cards and JSON-RPC service
- `ConfigurationFolderZipper` — Zips custom Solr config dirs for upload to dev container

**`integration-tests/`** — Example app + tests with health, metrics, and REST extensions:

- `SolrResource` — JAX-RS endpoints (`POST /solr`, `GET /solr`) demonstrating injection of `SolrClient`
- `SolrResourceTest` / `SolrResourceIT` — JVM and native image test variants using rest-assured

**`integration-tests-no-ext/`** — Minimal smoke test with only `quarkus-solr` (no REST, health, or metrics extensions):

- `SolrStartupTest` — Verifies the app starts and that no extra extensions (SmallRye Health, Micrometer, RESTEasy) are on the classpath

### Key Design Patterns

- **Extension split**: Build-time logic lives exclusively in `deployment/`; runtime beans in `runtime/`. Never add
  deployment dependencies to runtime.
- **CDI Producer**: `SolrClientProducer` uses `@Produces` to expose `SolrClient`. `SolrClientProxy` wraps it for
  observability.
- **Dev Services**: `DevServiceProcessor` uses TestContainers to spin up a Solr Docker container automatically when no
  `quarkus.solr.url` is configured and dev services are enabled.
- **Config Mapping**: All configuration uses SmallRye `@ConfigMapping` interfaces, not `@ConfigProperty` fields.

### Key Configuration Properties

| Property                                 | Description                                              |
|------------------------------------------|----------------------------------------------------------|
| `quarkus.solr.url`                       | Solr node URLs (required in production)                  |
| `quarkus.solr.defaultCollection`         | Default collection name                                  |
| `quarkus.solr.cloud`                     | Force SolrCloud/ZooKeeper mode                           |
| `quarkus.solr.devservices.enabled`       | Enable Docker-based dev service (default: true)          |
| `quarkus.solr.devservices.collection`    | Collection to create in dev service (default: `dummy`)   |
| `quarkus.solr.devservices.configuration` | Path to custom Solr config dir for dev service           |
| `quarkus.solr.healthEnabled`             | Enable health check endpoint (build-time, default: true) |

### Testing Approach

- Unit/extension tests in `deployment/src/test/` use `QuarkusUnitTest` with `@RegisterExtension`
- Integration tests in `integration-tests/src/test/` use `@QuarkusTest` (JVM) and `@QuarkusIntegrationTest` (native)
- Minimal smoke test in `integration-tests-no-ext/src/test/` uses `@QuarkusTest` with no extra extensions on the classpath
- Dev services start a real Solr Docker container during tests — no mocking of SolrClient
- The integration test app configures a custom Solr schema via
  `quarkus.solr.devservices.configuration=src/main/resources/solrconfig`
- The no-ext integration test uses default dev service config (no custom schema)
