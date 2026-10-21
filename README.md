<a href="https://fireflysoftware.io/openplatform/">
<picture>
  <source media="(max-width: 640px) and (prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-narrow-dark.svg">
  <source media="(max-width: 640px)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-narrow-light.svg">
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-dark.svg">
  <img src="https://raw.githubusercontent.com/fireflysoftwaresolutions/.github/main/profile/assets/banner-light.svg" alt="Firefly OpenPlatform - Domain Lending - Supply Chain Finance" width="100%">
</picture>
</a>

# Firefly OpenPlatform - Domain Lending - Supply Chain Finance

Developed by the Firefly Software Foundation. [dev@fireflysoftware.io](mailto:dev@fireflysoftware.io)

[Website](https://fireflysoftware.io/) · [OpenPlatform](https://fireflysoftware.io/openplatform/) · [Module catalogue](https://fireflysoftware.io/openplatform/modules/)

Domain layer for the trade finance family. This service owns the agreement saga in front of `core-lending-supply-chain-finance` and passes the product kind through on create and update.

Stored product kinds, accepted and forwarded as `financeType`:

| Kind | Meaning |
|------|---------|
| `FACTORING` | Purchase of receivables |
| `REVERSE_FACTORING` | Payables finance. Confirming is the Spanish market name for this kind. |

The saga orchestrates create, update, status change, close, and query.

Repository: [https://github.com/fireflysoftwaresolutions/domain-lending-supply-chain-finance](https://github.com/fireflysoftwaresolutions/domain-lending-supply-chain-finance)

---

## Overview

`domain-lending-supply-chain-finance` sits between experience services and `core-lending-supply-chain-finance`. Write operations run through the FireflyFramework saga engine. Queries use the CQRS query bus. The domain depends on `core-lending-supply-chain-finance-sdk` only. It does not import another core's internal packages. Identifiers that point at parties, contracts, or products stay UUIDs owned by those cores.

It creates, updates, changes status, and closes supply chain finance agreements through `core-lending-supply-chain-finance`. Invoices, advances, fees, settlements, and counterparties stay on the core. This saga covers the agreement, which is the product record with the lifecycle.

### Key features

- **Create** -- register a supply chain finance agreement through `core-lending-supply-chain-finance`, with compensation that deletes the core write
- **Update** -- read the current supply chain finance agreement and write the new field values through `update4` on `core-lending-supply-chain-finance`. Status is left unchanged. Compensation writes the previous record back.
- **Status change** -- read the current supply chain finance agreement and update its status through `core-lending-supply-chain-finance`. The supply chain finance core has no agreement status history resource, so the status saga does not append one.
- **Close** -- Close and cancel run the status saga and are refused when the agreement lifecycle does not allow that move. Remove deletes a draft only. A later status is kept. **Close** writes `CLOSED` when the core lifecycle allows that move. **Cancel** writes `TERMINATED` when the core lifecycle allows that move.
- **Query** -- read one supply chain finance agreement by identifier
- **Compensation** -- a failed later step asks the core to undo the work already accepted
- **CQRS** -- commands and queries travel on separate buses
- **Reactive I/O** -- Spring WebFlux with virtual threads enabled

## Architecture

### Module structure

| Module | Purpose |
|--------|---------|
| `domain-lending-supply-chain-finance-core` | Saga workflows, commands, handlers, and the application service |
| `domain-lending-supply-chain-finance-interfaces` | Request contracts for a status change and a closing reason |
| `domain-lending-supply-chain-finance-infra` | SDK client factory and downstream base-path configuration |
| `domain-lending-supply-chain-finance-web` | Spring Boot application, REST API, actuator, and OpenAPI |
| `domain-lending-supply-chain-finance-sdk` | Java client generated from the OpenAPI document |

### Dependency flow

```
web --> interfaces --> core --> infra --> core-lending-supply-chain-finance-sdk
sdk (generated from openapi.yml)
```

### Saga map

| Saga | Core calls | Compensation |
|------|------------|--------------|
| `RegisterSupplyChainFinanceAgreementSaga` | `create4` on `SupplyChainFinanceAgreementApi` | Deletes the record |
| `UpdateSupplyChainFinanceAgreementSaga` | Reads the current record, then writes field changes through `update4` on `SupplyChainFinanceAgreementApi`. Status is left unchanged. | Writes the previous record back through `update4` |
| `UpdateSupplyChainFinanceAgreementStatusSaga` | Reads the current record, updates its status through `update4` | Writes the previous status back through the core |
| Query | `getById4` on `SupplyChainFinanceAgreementApi` | None. A read has nothing to undo. |

Status values accepted by the core: `DRAFT`, `PENDING_APPROVAL`, `ACTIVE`, `SUSPENDED`, `TERMINATED`, `CLOSED`.

## Setup

### Prerequisites

- Java 25
- Apache Maven 3.9+
- `core-lending-supply-chain-finance-sdk` 26.10.3
- Apache Kafka on `localhost:9092` when event publishing is left at the default

### Environment

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_ADDRESS` | `localhost` | Bind address |
| `SERVER_PORT` | `8046` | HTTP port |
| `ENDPOINT_CORE_LENDING_SUPPLY_CHAIN_FINANCE` | `http://localhost:8080` | Base URL of `core-lending-supply-chain-finance` |

### Build and test

```bash
mvn clean verify
```

### Run

```bash
ENDPOINT_CORE_LENDING_SUPPLY_CHAIN_FINANCE=http://localhost:8080 mvn spring-boot:run -pl domain-lending-supply-chain-finance-web
```

Swagger UI (non-production): [http://localhost:8046/swagger-ui.html](http://localhost:8046/swagger-ui.html)

## API

| Method | Path | Behavior |
|--------|------|----------|
| `POST` | `/api/v1/supply-chain-finance-agreements` | Runs `RegisterSupplyChainFinanceAgreementSaga` |
| `GET` | `/api/v1/supply-chain-finance-agreements/{agreementId}` | Queries the core |
| `PUT` | `/api/v1/supply-chain-finance-agreements/{agreementId}/status` | Runs `UpdateSupplyChainFinanceAgreementStatusSaga` |
| `PUT` | `/api/v1/supply-chain-finance-agreements/{agreementId}` | Runs `UpdateSupplyChainFinanceAgreementSaga` |
| `POST` | `/api/v1/supply-chain-finance-agreements/{agreementId}/close` | Close supply chain finance agreement. Runs the status saga when `core-lending-supply-chain-finance` allows `CLOSED`. |
| `POST` | `/api/v1/supply-chain-finance-agreements/{agreementId}/cancel` | Cancel supply chain finance agreement. Runs the status saga when `core-lending-supply-chain-finance` allows `TERMINATED`. |
| `DELETE` | `/api/v1/supply-chain-finance-agreements/{agreementId}` | Deletes the record through `core-lending-supply-chain-finance` when that core allows removal |

The status request body is `status` plus an optional `statusReason`.

Close writes CLOSED when the lifecycle allows it. Cancel writes TERMINATED, which is the core cancellation status, when the lifecycle allows it. Remove deletes a draft only. A later status is kept.
A closing command body is an optional `statusReason`.
