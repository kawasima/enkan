type=page
status=published
title=Getting started | Enkan
~~~~~~

# Getting started

## Prerequisite

- Java 1.8 or higher
- Maven3

## Maven archetype

You can create an Enkan's blank project from maven archetype.  

```language bash
% mvn archetype:generate -DarchetypeGroupId=net.unit8.enkan -DarchetypeArtifactId=kotowari-archetype -DarchetypeVersion=0.1.0-beta3
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
enkan> /start /
```

## Scaffolding

`enkan-devel` is a useful tool in development. it contains the command to generate an application scaffold.

### Create table

```language-bash
enkan> /generate table PRODUCT (id identity primary key, name varchar(255))
```

If automatic building is unavailable, the `/compile` command is useful. 

```language-bash
enkan> /compile
```

When you reset your application, Flyway component will execute the generated migration. 

```language-bash
enkan> /reset
```

### Generate a CRUD controller and templates 

When you execute `/generate` command, a CRUD controller and templates will be generated.

```language-bash
enkan> /generate crud PRODUCT
```

If necessary, compile it.

```language-bash
enkan> /compile
```

When you reset your application, the generated routes are reloaded and become available. 

```language-bash
enkan> /reset
```

Try to access [`http://localhost:3000/product`](http://localhost:3000/product) in your browser.
