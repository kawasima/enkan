type=page
status=published
title=Getting started | Enkan
~~~~~~

# Getting started

## Prerequisite

- Java 21 or higher
- Maven 3.6.3 or higher

## Maven archetype

You can create an Enkan's blank project from maven archetype.  

```language-bash
% mvn archetype:generate -DarchetypeGroupId=net.unit8.enkan -DarchetypeArtifactId=kotowari-archetype -DarchetypeVersion=0.12.0
```

By default, following components is enabled.

- undertow
- flyway
- doma2
- HikariCP
- freemarker
- jackson

## Start REPL

```language-bash
% mvn -e compile exec:java
```

If you execute the `/start` command, application will start.

```language-bash
enkan> /start
```

Try to access [`http://localhost:3000/`](http://localhost:3000/) in your browser.
