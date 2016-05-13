type=page
status=published
~~~~~~

# Content negotiation

Enkan possible to serve different response at the same URI depending on the Accept header.
It enables to develop REST APIs.

`SerDesMiddleware` deserializes the request body and serializes the response body.
Available formats of the serialization are dependent on JAX-RS Entity providers.

If you add the dependency of `jackson-jaxrs-json-provider` into your Maven pom file, it will be enabled to serialize/deserialize JSON format.

```language-xml
<dependency>
    <groupId>com.fasterxml.jackson.jaxrs</groupId>
    <artifactId>jackson-jaxrs-json-provider</artifactId>
</dependency>
```

## Resource controller

If you want to return a response other than HTML format, simply return the Serializable object.

```language-java
public List<Customer> list() {
    CustomerDao customerDao = daoProvider.getDao(CustomerDao.class);
    return customerDao.selectAll();
}
```

You don't need any annotations! `SerDesMiddleware` converts the Java object to the appropriate format.