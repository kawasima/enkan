<#import "layout.ftl" as layout>
<@layout.myLayout>
  <header>
    <div class="container">
      <div class="col-lg-12">

        <div class="intro-text">
          <span class="name"><ruby>enkan<rt>円環</rt></ruby></span>
          <hr class="star-light">
          <span class="skills">An explicit and simple Java framework</span>
        </div>
        <div class="buttons-unit">
          <a href="getting-started.html" class="btn btn-lg btn-info">Get Started</a>
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
        <div class="col-lg-3 col-lg-offset-1">
          <h3>Explicitness</h3>
          We think there is a trade off between implicitness and understandability.
          Enkan's design emphasize the explicitness. For instance, Enkan has no configuration file and minimal DI and minimal annotations.
        </div>
        <div class="col-lg-3 col-lg-offset-1">
          <h3>Ease of Development</h3>
          Enkan is very developer friendly. Hot reloading brings continuous programming without waiting for compilation and deploy to you.
          And special exception for misconfiguration make you aware of your mistake in an understandable way.
        </div>
        <div class="col-lg-3 col-lg-offset-1">
          <h3>Ease of Operation</h3>

          In Enkan, You can do all in the REPL as follows:
          <ul>
            <li>Start/stop server</li>
            <li>View metrics</li>
            <li>Change the condition of applying the middleware</li>
            <li>Generate and compile sources</li>
          </ul>
          And enkan application starts very quickly (1-2sec).

        </div>
      </div>
    </div>
  </section>

</@layout.myLayout>
