# Техническая реализация и Архитектура

**Документ:** `docs/architecture/predictive-risk-service-tech-spec.md`
**Статус:** Approved for Development
**Сервис:** predictive-risk-service (Поддомен: Strategic Routing & AI Intelligence)

## 2. Графовая Архитектура и Модель Данных (Neo4j Graph Schema)

Логистическая сеть моделируется в Neo4j в виде направленного графа:

```text
 (Shipment:Order) ──[:ASSIGNED_TO]──► (Container) ──[:LOADED_ON]──► (Vessel)
                                                                       │
                                                                 [:BOUND_TO]
                                                                       ▼
 (Destination) ◄──[:DESTINED_FOR]─── (Truck) ◄───[:DISCHARGED_AT]── (PortNode)
```

Примеры Cypher-запросов:
Поиск каскадного поражения (Impact Query):

```cypher
MATCH (p:PortNode {id: $portId})<-[:BOUND_TO|DISCHARGED_AT*1..3]-(affected)
RETURN affected.id, labels(affected)
```
Запрос мгновенно возвращает все суда, контейнеры и заказы, зависящие от заблокированного порта.

## 3. Межсервисное Взаимодействие и Интеграции

### 3.1 Схема интеграционных связей

```text
┌─────────────────────────┐          ┌─────────────────────────┐          ┌─────────────────────────┐
│     telemetry-service   │          │   document-ocr-service  │          │ External Weather/Ports  │
└────────────┬────────────┘          └────────────┬────────────┘          └────────────┬────────────┘
             │                                    │                                    │
  Kafka: TelemetryEvent                Kafka: DocumentParsedEvent                 REST API (Polling)
             │                                    │                                    │
             └─────────────────┬──────────────────┘                                    │
                               │                                                       ▼
                               ▼                                          ┌─────────────────────────┐
                   ┌───────────────────────┐                              │  Redis Weather Cache    │
                   │predictive-risk-service│                              └────────────┬────────────┘
                   └───────────┬───────────┘                                           │
                               │                                                       │
                               │ Kafka: RiskLevelEscalatedEvent                        │
                               ▼                                                       │
                   ┌───────────────────────┐                                           │
                   │    routing-service    │◄──────────────────────────────────────────┘
                   └───────────────────────┘
```

### 3.2 Описание контрактов взаимодействия

**Входящие события (Kafka Consumers):**
*   `TelemetryPointReceivedEvent` (от `telemetry-service`): Обновляет текущие координаты транспорта в графе Neo4j и инициирует пересчет текущего Risk Score.
*   `DocumentParsedEvent` (от `document-ocr-service`): Вносит в граф новые узлы документов и проверяет временные рамки таможенного оформления.

**Исходящие события (Kafka Producers):**
*   `RiskLevelEscalatedEvent`: Генерируется, если Risk Score превышает порог 0.6. Содержит ID заказа, текущий уровень риска, причину ("Затор в порту Роттердама, задержка 140 мин") и варианты решения.
*   `CascadeImpactDetectedEvent`: Генерируется при массовых сбоях инфраструктурных узлов.

## 4. Архитектурные Паттерны и Оптимизация

### 4.1 Избежание шторма алертов (Debouncing & Rate Limiting)
Чтобы GPS-трекер, присылающий координаты каждую секунду, не выгружал CPU постоянными пересчетами графа:
Применяется паттерн Debouncing на базе Redis: пересчет Risk Score для конкретного рейса выполняется не чаще одного раза в 60 секунд, если координата не изменилась кардинально.

### 4.2 Алгоритм расчета Risk Score (Weighted Fusion)
Итоговый индекс риска вычисляется как взвешенная сумма факторов:
$$\text{RiskScore} = w_1 \cdot R_{\text{delay}} + w_2 \cdot R_{\text{weather}} + w_3 \cdot R_{\text{vendor\_history}} + w_4 \cdot R_{\text{document\_errors}}$$
где $w_n$ — весовые коэффициенты, настраиваемые через конфигурацию без пересборки сервиса.

### 4.3 Outbox Pattern для гарантии оповещений
Как и во всей системе, публикация критических алертов `RiskLevelEscalatedEvent` выполняется через Transactional Outbox Pattern с записью в PostgreSQL для исключения потери алертов при сбоях брокера.

## 5. Требования к Хранению Данных (Database Guidelines)

### 5.1 Разделение хранилищ
*   **Neo4j:** Хранит только активные (In-Transit / Planned) элементы цепочек поставок. После завершения рейса (COMPLETED) узлы архивного заказа удаляются из оперативного графа, чтобы не раздувать RAM инстанса Neo4j.
*   **PostgreSQL:** Хранит исторические логи всех проверок рисков, таблицы весовых коэффициентов и статистику точности прогнозов.

### 5.2 Оптимизация Neo4j
*   Обязательное создание уникальных индексов на ID узлов (`CREATE CONSTRAINT FOR (s:Shipment) REQUIRE s.id IS UNIQUE`).
*   Использование параметров запросов (Parameterized Cypher Queries) для предотвращения повторной компиляции планов выполнения.

## 6. Observability и Эксплуатация

*   **Metrics (Micrometer + Prometheus):**
    *   `risk_evaluation_duration_seconds` (histogram) — время расчета Risk Score.
    *   `risk_graph_nodes_total` (gauge) — общее количество активных узлов в Neo4j.
    *   `risk_escalations_total` (counter с тегами level=HIGH|CRITICAL) — количество сгенерированных алертов.
*   **Logging:** Логирование в формате JSON с обязательным пробросом `trace_id` и выводом ключевых факторов, повлиявших на повышение риска.
