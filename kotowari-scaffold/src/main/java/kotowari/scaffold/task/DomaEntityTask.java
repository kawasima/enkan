package kotowari.scaffold.task;

import kotowari.scaffold.util.BasePackageDetector;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import org.apache.tools.ant.Project;
import org.seasar.doma.extension.gen.task.*;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * @author kawasima
 */
public class DomaEntityTask implements GenTask {
    private String destination;
    private DataSource datasource;
    private String tableName;

    public DomaEntityTask(String destination, String tableName, DataSource datasource) {
        this.destination = destination;
        this.tableName = tableName;
        this.datasource = datasource;
    }

    private String getURL() throws SQLException {
        return datasource.getConnection().getMetaData().getURL();
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        final Project project = new Project();
        project.setBaseDir(pathResolver.project());
        String basePackage = BasePackageDetector.detect();
        File destDir = pathResolver.destinationAsFile(destination);

        Gen genTask = new Gen() {
            @Override
            protected DataSource createDataSource() {
                return datasource;
            }

            @Override
            public EntityConfig createEntityConfig() {
                entityConfig = new EntityConfig();
                entityConfig.setProject(project);
                entityConfig.setUseListener(false);
                entityConfig.setDestDir(destDir);
                GenerationTypeAttribute generationType = new GenerationTypeAttribute();
                generationType.setValue("identity");
                entityConfig.setGenerationType(generationType);
                entityConfig.setPackageName(basePackage + "entity");
                return entityConfig;
            }

            @Override
            public DaoConfig createDaoConfig() {
                daoConfig = new DaoConfig();
                daoConfig.setProject(project);
                daoConfig.setDestDir(destDir);
                daoConfig.setPackageName(basePackage + "dao");
                return daoConfig;
            }

            @Override
            public SqlConfig createSqlConfig() {
                sqlConfig = new SqlConfig();
                sqlConfig.setProject(project);
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
        //genTask.createDaoConfig();
        //genTask.createSqlConfig();
        genTask.execute();
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }
}
