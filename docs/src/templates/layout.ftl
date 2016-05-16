<#macro myLayout>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/bootstrap/3.3.6/css/bootstrap.min.css" type="text/css"/>
    <link rel="stylesheet" href="${content.rootpath}css/enkan.css" type="text/css"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/prism/1.5.0/themes/prism.css" type="text/css"/>

    <link href="http://sbootstrap-freelancer.startbootstrapc.netdna-cdn.com/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
  </head>
  <body id="page-top">
    <nav class="navbar navbar-default navbar-fixed-top <#if (content.uri??) && content.uri != "/">navbar-shrink</#if>">
      <div class="container">
        <div class="navbar-header page-scroll">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="${content.rootpath}index.html">enkan</a>
        </div>
        <div class="collapse navbar-collapse">
          <ul class="nav navbar-nav navbar-right">
            <li class="hidden active"><a href="#page-top"></a></li>
            <li class="page-scroll">
              <a href="getting-started.html">Docs</a>
            </li>
            <li>
              <a href="https://github.com/kawasima/enkan">GitHub</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <#nested/>
    <footer>
      <div class="container">
        <div class="col-lg-4">

        </div>
        <div class="col-lg-4 col-lg-offset-4">
          © 2016 kawasima<br/>
          Documentation licensed under <a href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a>
        </div>
      </div>
    </footer>
    <script src="https://cdn.jsdelivr.net/prism/1.5.0/prism.js"></script>
    <script src="https://cdn.jsdelivr.net/prism/1.5.0/components/prism-java.min.js"></script>
    <script src="https://cdn.jsdelivr.net/prism/1.5.0/components/prism-bash.min.js"></script>
    <script src="https://cdn.jsdelivr.net/prism/1.5.0/plugins/line-numbers/prism-line-numbers.min.js"></script>
  </body>
</html>
</#macro>
