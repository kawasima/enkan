<#import "layout.ftl" as layout>
<@layout.myLayout>
  <header>
    <div class="container">
      <div class="col-lg-12">

        <div class="intro-text">
          <span class="name"><ruby>enkan<rt>円環</rt></ruby></span>
          <hr class="star-light">
          <span class="skills">An explicit and simple Java web framework</span>
        </div>
        <div class="hero-code">
<pre><code class="language-bash">enkan&gt; /start
System started.

enkan&gt; /middleware app list
ANY   defaultCharset   (enkan.middleware.DefaultCharsetMiddleware@4929dbc3)
ANY   trace            (enkan.middleware.TraceMiddleware@1c985ffd)
ANY   session          (enkan.middleware.SessionMiddleware@32424a32)
ANY   routing          (kotowari.middleware.RoutingMiddleware@226c7147)

enkan&gt; /reset
System reset in 847ms.</code></pre>
        </div>
        <div class="buttons-unit">
          <a href="getting-started.html" class="btn btn-lg btn-primary">Read the Docs</a>
          <a href="https://github.com/enkan/enkan" class="btn btn-lg btn-outline-light">GitHub</a>
        </div>
      </div>
    </div>
  </header>
  <section class="top">
    <div class="container">
      <div class="row">
        <div class="col-lg-12 text-center">
          <h2>Features</h2>
          <hr class="star-primary">
        </div>
      </div>
      <div class="row">
        <div class="col-lg-3 offset-lg-1">
          <h3>Explicitness</h3>
          No configuration files. No annotation scanning. No auto-wiring surprises.
          Add middleware with a single line of Java and read the entire request pipeline top to bottom:
          <pre><code class="language-java">app.use(new SessionMiddleware());
app.use(new RoutingMiddleware(routes));</code></pre>
        </div>
        <div class="col-lg-3 offset-lg-1">
          <h3>Ease of Development</h3>
          Hot reload without restarting the JVM. The REPL resets only the application layer in ~1 second,
          keeping database connections alive. Misconfiguration throws a clear error at startup,
          not a <code>NullPointerException</code> at request time:
          <pre><code class="language-bash">enkan&gt; /reset   # ~1 second</code></pre>
        </div>
        <div class="col-lg-3 offset-lg-1">
          <h3>Ease of Operation</h3>
          Inspect and modify a live application from the REPL — no redeploy needed:
          <pre><code class="language-bash">enkan&gt; /start
enkan&gt; /middleware app list
enkan&gt; /routes app
enkan&gt; /reset</code></pre>
          Startup is fast (1–2 s) because there is no classpath scanning.
        </div>
      </div>
    </div>
  </section>

</@layout.myLayout>
