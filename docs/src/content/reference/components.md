type=page
status=published
title=Component Catalog | Enkan
~~~~~~

# Component Catalog

## Undertow

Undertow component provides the feature of a web server without depending on Servlet.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-undertow</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|port|int|Listen port|80|
|host|String|Host address|0.0.0.0|
|ssl|boolean|Enable SSL/TLS|false|
|sslPort|int|SSL listen port|443|
|keystorePath|String|Path to keystore file|-|
|keystorePassword|String|Password for keystore|-|

### Dependencies

- ApplicationComponent

## Jetty

Jetty component provides the feature of a web server based on Eclipse Jetty.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-jetty</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|port|int|Listen port|80|
|host|String|Host address|0.0.0.0|
|ssl|boolean|Enable SSL/TLS|false|
|sslPort|int|SSL listen port|443|
|keystorePath|String|Path to keystore file|-|
|keystorePassword|String|Password for keystore|-|

### Dependencies

- ApplicationComponent

## HikariCP

HikariCP component provides a high-performance JDBC connection pool.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-HikariCP</artifactId>
</dependency>
```

### Properties

Properties are passed via `OptionMap` in the constructor.

|Name|Key in OptionMap|Description|
|:---|:---------------|:---------|
|jdbcUrl|uri|JDBC connection URL|
|username|username|Database username|
|password|password|Database password|
|autoCommit|autoCommit?|Enable auto-commit|
|connectionTimeout|connTimeout|Connection timeout in ms|
|idleTimeout|idleTimeout|Idle timeout in ms|
|maxPoolSize|maxPoolSize|Maximum pool size|
|minimumIdle|minIdle|Minimum idle connections|

## Doma2

Doma2 component provides the DAO-oriented database mapping framework.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-doma2</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|dialect|Dialect|SQL dialect|StandardDialect|
|naming|Naming|Naming convention|Naming.DEFAULT|
|useLocalTransaction|boolean|Enable local transactions|true|
|maxRows|int|Maximum result rows|0|
|fetchSize|int|Fetch size for queries|0|
|queryTimeout|int|Query timeout in seconds|0|
|batchSize|int|Batch size for operations|0|

### Dependencies

- DataSourceComponent

## jOOQ

jOOQ component provides a DSL context for building type-safe SQL queries.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-jooq</artifactId>
</dependency>
```

### Properties

| Name | Type | Description | Default |
|:---|:---|:---------|:------|
| dialect | SQLDialect | SQL dialect | DEFAULT |

### Dependencies

- DataSourceComponent

## JPA

JPA component provides a base for JPA entity manager integration.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-jpa</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|
|:---|:---|:---------|
|name|String|JPA persistence unit name|
|jpaProperties|Map|JPA configuration properties|

## EclipseLink

EclipseLink component provides an EntityManagerProvider implementation using EclipseLink.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-eclipselink</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|sqlLogLevel|String|Logging level for SQL|FINE|
|managedClasses|List|Entity classes (via registerClass)|[]|

### Dependencies

- DataSourceComponent

## Flyway

Flyway component provides automatic database migration on startup.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-flyway</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|locations|String[]|Migration script locations|-|
|table|String|Migration metadata table name|schema_version|
|cleanBeforeMigration|boolean|Clean database before migration|false|

### Dependencies

- DataSourceComponent

## Freemarker

Freemarker component provides a template engine for rendering HTML.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-freemarker</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|prefix|String|Template directory prefix|templates|
|suffix|String|Template file suffix|.ftl|
|encoding|String|File encoding|UTF-8|

## Thymeleaf

Thymeleaf component provides a template engine for rendering HTML.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-thymeleaf</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|prefix|String|Template directory prefix|templates/|
|suffix|String|Template file suffix|.html|
|encoding|String|File encoding|UTF-8|

## Jackson

Jackson component provides a bean converter using Jackson ObjectMapper.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-jackson</artifactId>
</dependency>
```

## Metrics

Metrics component provides the feature of collecting application metrics using Dropwizard Metrics.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-metrics</artifactId>
</dependency>
```

### Properties

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|metricName|String|Base metric name prefix|enkan|

## OpenTelemetry

OpenTelemetry component wraps an `OpenTelemetry` instance and exposes a `Tracer` for use by `TracingMiddleware`.
Defaults to `OpenTelemetry.noop()` — no spans are emitted unless an SDK is configured at runtime
(e.g. via the OpenTelemetry Java Agent or an explicit SDK setup).

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-opentelemetry</artifactId>
</dependency>
```

```java
// No-op (default) — suitable for development
OpenTelemetryComponent otel = new OpenTelemetryComponent();

// With a configured SDK instance
OpenTelemetryComponent otel = new OpenTelemetryComponent(GlobalOpenTelemetry.get());
```

### Properties

| Name | Type | Description | Default |
|:---|:---|:---------|:------|
| openTelemetry | OpenTelemetry | OpenTelemetry instance | `OpenTelemetry.noop()` |
| instrumentationName | String | Tracer / instrumentation scope name | `"enkan"` |
