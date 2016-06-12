type=page
status=published
title=Sessions | Enkan
~~~~~~

# Sessions

You can use `session` same in Servlet. But Enkan's session doesn't depend on HttpSession.
There is a big difference.

In Enkan, both of the request and the response have the session properties.

```language-java
public HttpResponse wrongUsage(Session session) {
    session.put("newProp", "aaa");
    return HttpResponse.of("response");
}
```

In the above case, `newProp` property doesn't be saved.

Correct code as follows:

```language-java
public HttpResponse wrongUsage(Session session) {
    session.put("newProp", "aaa");
    return builder(HttpResponse.of("response"))
        .set(HttpResponse::setSession, session)
        .builder();
}
```

