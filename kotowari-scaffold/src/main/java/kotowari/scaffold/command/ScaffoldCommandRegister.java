package kotowari.scaffold.command;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.ImportDeclaration;
import enkan.component.ApplicationComponent;
import enkan.component.DataSourceComponent;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.UnreachableException;
import enkan.system.EnkanSystem;
import enkan.system.Repl;
import enkan.system.repl.SystemCommandRegister;
import kotowari.scaffold.model.EntityField;
import kotowari.scaffold.task.*;
import kotowari.scaffold.util.BasePackageDetector;
import kotowari.scaffold.util.EntitySourceAnalyzer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.unit8.amagicman.Generator;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.PathResolverImpl;
import net.unit8.amagicman.helper.ClassReplaceVisitor;
import net.unit8.amagicman.task.ContentsReplaceTask;
import net.unit8.amagicman.task.JavaByTemplateTask;
import net.unit8.amagicman.task.RewriteJavaSourceTask;
import net.unit8.amagicman.util.CaseConverter;
import net.unit8.erebus.Erebus;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Registers commands to REPL for scaffold.
 *
 * @author kawasima
 */
public class ScaffoldCommandRegister implements SystemCommandRegister {
    private static final Logger LOG = LoggerFactory.getLogger(ScaffoldCommandRegister.class);
    PathResolver pathResolver = new PathResolverImpl(null, "META-INF/amagicman/templates", null);


    protected DataSource findDataSource(EnkanSystem system) {
        Optional<DataSource> datasource = system.getAllComponents().stream()
                .filter(DataSourceComponent.class::isInstance)
                .map(DataSourceComponent.class::cast)
                .map(DataSourceComponent::getDataSource)
                .filter(Objects::nonNull)
                .findFirst();
        return datasource.orElseThrow(() ->
                new IllegalStateException("Application must be started"));
    }

    protected String findApplicationFactoryPath(EnkanSystem system) {
        String factoryClassName = system.getAllComponents().stream()
                .filter(c -> c instanceof ApplicationComponent)
                .map(c -> ((ApplicationComponent) c).getFactoryClassName())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Application must be started"));

        return factoryClassName.replace('.', '/') + ".java";
    }


    private Generator tableGenerator(String sql, DataSource ds) {
        try {
            CreateTable stmt = (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE " + sql);
            return new Generator()
                    .writing("migration", g -> g.task(
                            new FlywayTask("src/main/java", stmt.getTable().getName(), "CREATE TABLE " + sql)));
        } catch (JSQLParserException e) {
            throw new IllegalArgumentException("Statement generating a table is wrong syntax.", e);
        }
    }

    private String convertToPathString(String packageName) {
        return Arrays.stream(packageName.split("\\."))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("/"));
    }

    private File getEntitySource(String pkgPath, String tableName) {
        try {
            return pathResolver.destinationAsFile("src/main/java/" + pkgPath + "/entity/" + CaseConverter.pascalCase(tableName) + ".java");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    private Generator crudGenerator(String tableName, DataSource ds) {
        String pkgName = BasePackageDetector.detect();
        String pkgPath = convertToPathString(pkgName);

        return new Generator()
                .writing("entity", g -> g.task(new DomaEntityTask("src/main/java", tableName, ds)))
                .writing("java", g -> {
                    String className = CaseConverter.pascalCase(tableName);
                    List<EntityField> fields = new ArrayList<>();
                    g.task(pathResolver -> {
                        File entitySource = getEntitySource(pkgPath, tableName);
                        fields.addAll(new EntitySourceAnalyzer().analyze(entitySource));
                    });
                    g.task(new JavaByTemplateTask(
                            "src/main/java/scaffold/crud/controller/UserController.java",
                            "src/main/java/" + pkgPath
                                    + "/controller/" + className + "Controller.java",
                            cu -> cu.accept(new ClassReplaceVisitor(
                                    "scaffold.crud.", pkgName,
                                    "user", tableName), null)));
                    g.task(new FormTask(pkgName, tableName, fields));
                    g.task(new JavaByTemplateTask(
                            "src/main/java/scaffold/crud/dao/UserDao.java",
                            "src/main/java/" + pkgPath
                                    + "/dao/" + className + "Dao.java",
                            cu -> cu.accept(new ClassReplaceVisitor(
                                    "scaffold.crud.", pkgName,
                                    "user", tableName), null)));
                    String domaConfig = "src/main/java/" + pkgPath + "/DomaConfig.java";
                    if (!Files.exists(Paths.get(domaConfig))) {
                        g.task(new DomaConfigTask(pkgName));
                    }
                    String formBase = "src/main/java/" + pkgPath + "/form/FormBase.java";
                    if (!Files.exists(Paths.get(formBase))) {
                        g.task(new JavaByTemplateTask(
                                "src/main/java/scaffold/crud/form/FormBase.java",
                                formBase,
                                cu -> cu.accept(new ClassReplaceVisitor(
                                        "scaffold.crud.", pkgName, "user", "tableName"), null)));
                    }
                })
                .writing("ftl", g -> {
                    Arrays.asList("edit.ftl", "list.ftl", "new.ftl", "show.ftl")
                            .stream().forEach(ftl ->
                            g.task(new ContentsReplaceTask(
                                    "src/main/resources/templates/user/" + ftl,
                                    "src/main/resources/templates/"
                                            + CaseConverter.camelCase(tableName)
                                            + "/" + ftl,
                                    line -> line.replaceAll("user", CaseConverter.camelCase(tableName))
                            )));
                    List<EntityField> fields = new ArrayList<>();
                    g.task(pathResolver -> {
                        File entitySource = getEntitySource(pkgPath, tableName);
                        fields.addAll(new EntitySourceAnalyzer().analyze(entitySource));
                    });

                    PatternTemplateTask formPartialTask = new PatternTemplateTask("src/main/resources/templates/" + CaseConverter.camelCase(tableName) + "/_form.ftl",
                            tableName, fields);
                    formPartialTask
                            .addMapping(String.class,  "src/main/resources/templates/parts/textfield.ftl")
                            .addMapping(Integer.class, "src/main/resources/templates/parts/textfield.ftl")
                            .addMapping(Long.class,    "src/main/resources/templates/parts/textfield.ftl");
                    g.task(formPartialTask);

                    PatternTemplateTask showPartialTask = new PatternTemplateTask("src/main/resources/templates/" + CaseConverter.camelCase(tableName) + "/_show.ftl",
                            tableName, fields);
                    showPartialTask
                            .addMapping(String.class,  "src/main/resources/templates/parts/text.ftl")
                            .addMapping(Integer.class, "src/main/resources/templates/parts/text.ftl")
                            .addMapping(Long.class,    "src/main/resources/templates/parts/text.ftl");
                    g.task(showPartialTask);

                    String defaultLayout = "src/main/resources/templates/layout/defaultLayout.ftl";
                    if (!Files.exists(Paths.get(defaultLayout))) {
                        g.task(new ContentsReplaceTask(
                                defaultLayout,
                                defaultLayout,
                                line -> line
                        ));
                    }
                })
                .writing("sql", g -> Arrays.asList("selectAll.sql", "selectById.sql")
                        .stream().forEach(sql ->
                                g.task(new ContentsReplaceTask(
                                        "src/main/resources/META-INF/scaffold/crud/dao/UserDao/" + sql,
                                        "src/main/resources/META-INF/" + convertToPathString(pkgName)
                                                + "/dao/" + CaseConverter.pascalCase(tableName) + "Dao/" + sql,
                                        line -> line.replaceAll("user", tableName)
                                ))));
    }

    private Generator configureApplicationFactory(Generator gen, String tableName, EnkanSystem system) {
        String path = findApplicationFactoryPath(system);
        return gen.writing("app", g -> g.task(new RewriteJavaSourceTask("src/main/java/" + path, cu -> {
            String controllerClassName = CaseConverter.pascalCase(tableName) + "Controller";
            String pkgName = BasePackageDetector.detect();

            cu.getImports().add(
                    new ImportDeclaration(
                            ASTHelper.createNameExpr(pkgName + "controller." + controllerClassName),
                            false, false));
            cu.accept(new AppendRoutingVisitor(controllerClassName),
                    new RoutingDefineContext());
        })));
    }

    private void withClassLoader(ClassLoader loader, Runnable runnable) {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    public void setPathResolver(PathResolver resolver) {
        this.pathResolver = resolver;
    }

    @Override
    public void register(Repl repl) {
        Erebus erebus = new Erebus.Builder().build();
        final ClassLoader scaffoldLoader;
        try {
            URL[] urls = erebus.resolveAsFiles("net.unit8.enkan:kotowari-crud-scaffold:0.1.0-beta1")
                    .stream()
                    .map(File::toURI)
                    .map(uri -> {
                        try {
                            return uri.toURL();
                        } catch (MalformedURLException e) {
                            throw new UnreachableException(e);
                        }})
                    .toArray(URL[]::new);
            scaffoldLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        } catch (DependencyCollectionException | DependencyResolutionException e) {
            throw new FalteringEnvironmentException(e);
        }

        repl.registerCommand("generate", (system, transport, args) -> {
            if (args.length == 0) {
                transport.sendOut("Usage: generate [target] [options]");
                return true;
            }

            switch(args[0]) {
                case "table":
                    if (args.length > 1) {
                        tableGenerator(
                                Arrays.stream(args).skip(1).collect(Collectors.joining(" ")),
                                findDataSource(system))
                                .setPathResolver(pathResolver)
                                .addTaskListener(new LoggingTaskListener(transport))
                                .invoke();
                        transport.sendOut("Generated table " + args[1]);
                    } else {
                        transport.sendOut("Usage: generate table [CREATE TABLE statement]");
                    }
                    break;
                case "crud":
                    if (args.length > 1) {
                        withClassLoader(scaffoldLoader, () -> {
                            Generator g = crudGenerator(args[1], findDataSource(system));
                            configureApplicationFactory(g, args[1], system);
                            g.setPathResolver(pathResolver)
                                    .addTaskListener(new LoggingTaskListener(transport))
                                    .invoke();
                        });
                        transport.sendOut("Generated CRUD " + args[0]);
                    } else {
                        transport.sendOut("Usage: generate crud [tableName]");
                    }
                    break;
                default:
                    transport.sendOut(String.format(Locale.US, "%s is not found.", args[0]));
            }

            return true;
        });
    }
}
