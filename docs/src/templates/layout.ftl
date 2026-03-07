<#macro myLayout>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset=utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>ENKAN</#if></title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" type="text/css"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/themes/prism.min.css" type="text/css"/>
    <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.7.2/css/all.min.css" rel="stylesheet" type="text/css"/>
    <link rel="stylesheet" href="${content.rootpath}css/enkan.css" type="text/css"/>
  </head>
  <body id="page-top">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
      <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${content.rootpath}index.html">
          <img src="${content.rootpath}img/logo.png" alt="enkan logo" class="navbar-logo">enkan
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#bs-navbar-collapse-1" aria-controls="bs-navbar-collapse-1" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <a class="nav-link" href="${content.rootpath}getting-started.html">Docs</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="https://github.com/enkan/enkan">GitHub</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <#nested/>
    <footer>
      <div class="container">
        <div class="row">
          <div class="col-lg-4 offset-lg-8 text-end">
            © 2016–2026 kawasima<br/>
            Documentation licensed under <a href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a>
          </div>
        </div>
      </div>
    </footer>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/prism.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-java.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-bash.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-markup.min.js"></script>
  </body>
</html>
</#macro>
