# Environments

Enkan is following up on the Twelve-factor app. Config is provided from the following:

1. `env.properties` file on the classpath
2. Environment variables
3. Java system properties

The variables are read in this order.

`enkan.Env` class can access the variables. `Env` recognizes the same key in `ENV_PROP` and `env.prop`.
