package enkan.system.command;

import enkan.component.DataSourceComponent;
import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.Transport;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static enkan.system.ReplResponse.ResponseStatus.DONE;

public class SqlCommand implements SystemCommand {
    private static final Set<String> DML_KEYWORDS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    static {
        DML_KEYWORDS.addAll(Arrays.asList(
                "INSERT", "UPDATE", "DELETE", "MERGE",
                "CREATE", "DROP"));
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        String sql = String.join(" ", args);
        boolean isDML = Arrays.stream(args).anyMatch(s -> DML_KEYWORDS.contains(s));
        List<DataSourceComponent> components = system.getComponents(DataSourceComponent.class);
        if (components.isEmpty()) {
            transport.sendErr("Not found DataSource", DONE);
            return true;
        }
        DataSourceComponent dataSourceComponent = components.get(0);
        DataSource ds = dataSourceComponent.getDataSource();
        if (ds == null) {
            transport.sendErr("DataSourceComponent is not started", DONE);
            return true;
        }

        try(Connection connection = ds.getConnection();
            Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            if (isDML) {
                int cnt = stmt.executeUpdate(sql);
                connection.commit();
                transport.sendOut(cnt + " updated.");
            } else {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int cols = rsMeta.getColumnCount();
                    List<String> colNames = new ArrayList<>();
                    for (int i = 1; i <= cols; i++) {
                        colNames.add(rsMeta.getColumnName(i));
                    }
                    transport.send(ReplResponse.withOut(String.join("|", colNames)));
                    transport.send(ReplResponse.withOut(""));

                    int cnt = 0;
                    while (rs.next()) {
                        List<String> values = new ArrayList<>();
                        for (int i = 1; i <= cols; i++) {
                            values.add(rs.getString(i));
                        }
                        transport.send(ReplResponse.withOut(String.join("|", values)));
                        cnt++;
                    }
                    transport.sendOut(cnt + " rows selected");
                }
            }
        } catch (SQLException e) {
            transport.sendErr(e.getLocalizedMessage(), DONE);
        }
        return true;
    }
}
