type=page
status=published
~~~~~~

# Sessions

You can use `session` same in Servlet. But Enkan's session doesn't depend on HttpSession.
There is a big difference.

In Enkan, both of the request and the response have the session properties.

```language-java
public HttpResponse wrongUsage(Session session) {
    session.put("new", "aaa");
    return HttpResponse.of("response");
}
```

In the above case, `new` property doesn't be saved.