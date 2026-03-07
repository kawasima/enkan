type=page
status=published
title=Environments | Enkan
~~~~~~

# Environments

Enkan is following up on the Twelve-factor app. Config is provided from the following sources, in priority order (highest to lowest):

1. Java system properties (`-Dkey=value`)
2. Environment variables
3. `env.properties` file on the classpath

Higher-priority sources override lower-priority ones.

`enkan.Env` class can access the variables. `Env` recognizes the same key in `ENV_PROP` and `env.prop`.
