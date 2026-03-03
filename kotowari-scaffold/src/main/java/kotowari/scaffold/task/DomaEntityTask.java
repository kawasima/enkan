package kotowari.scaffold.task;

import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import org.apache.tools.ant.Project;
import org.seasar.doma.extension.gen.task.*;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author kawasima
 */
public class DomaEntityTask implements GenTask {
    private String destination;
    private DataSource datasource;
    private String tableName;
    private final String pkgName;

    public DomaEntityTask(String pkgName, String destination, String tableName, DataSource datasource) {
        this.pkgName = pkgName;
        this.destination = destination;
        this.tableName = tableName;
        this.datasource = datasource;
    }

    private String getURL() throws SQLException {
        try (Connection conn = datasource.getConnection()) {
            return conn.getMetaData().getURL();
        }
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        final Project project = new Project();
        project.setBaseDir(pathResolver.project());
        File destDir = pathResolver.destinationAsFile(destination);

        Gen genTask = new Gen() {
            @Override
            protected DataSource createDataSource() {
                return datasource;
            }

            @Override
            public EntityConfig createEntityConfig() {
                entityConfig = new EntityConfig();
                entityConfig.setProject(getProject());
                entityConfig.setUseListener(false);
                entityConfig.setDestDir(destDir);
                GenerationTypeAttribute generationType = new GenerationTypeAttribute();
                generationType.setValue("identity");
                entityConfig.setGenerationType(generationType);
                entityConfig.setPackageName(pkgName + "entity");
                return entityConfig;
            }

            @Override
            public DaoConfig createDaoConfig() {
                daoConfig = new DaoConfig();
                daoConfig.setProject(getProject());
                daoConfig.setDestDir(destDir);
                daoConfig.setPackageName(pkgName + "dao");
                return daoConfig;
            }

            @Override
            public SqlConfig createSqlConfig() {
                sqlConfig = new SqlConfig();
                sqlConfig.setProject(getProject());
                sqlConfig.setDestDir(new File(destination));
                return sqlConfig;
            }
        };
        genTask.setProject(project);
        genTask.setUrl(getURL());
        genTask.setUser("");
        genTask.setPassword("");
        genTask.setTableNamePattern(tableName);
        genTask.createEntityConfig();
        genTask.execute();
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }
}
