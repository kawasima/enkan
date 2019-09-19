package enkan.system.command;

import enkan.component.DataSourceComponent;
import enkan.system.EnkanSystem;
import enkan.system.ReplResponse;
import enkan.system.SystemCommand;
import enkan.system.Transport;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SqlCommand implements SystemCommand {
    private static final Set<String> DML_KEYWORDS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    static {
        DML_KEYWORDS.addAll(Arrays.asList(
                "INSERT", "UPDATE", "DELETE", "MERGE",
                "CREATE", "DROP"));
    }

    @Override
    public boolean execute(EnkanSystem system, Transport transport, String... args) {
        String sql = String.join(" ", args).trim();
        if (sql.isEmpty()) {
            transport.sendErr("/sql [SQL statement]");
            return true;
        }

        boolean isDML = Arrays.stream(args).anyMatch(DML_KEYWORDS::contains);
        List<DataSourceComponent> components = system.getComponents(DataSourceComponent.class);
        if (components.isEmpty()) {
            transport.sendErr("Not found DataSource");
            return true;
        }
        DataSourceComponent dataSourceComponent = components.get(0);
        DataSource ds = dataSourceComponent.getDataSource();
        if (ds == null) {
            transport.sendErr("DataSourceComponent is not started");
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

                    List<ColumnMeta> colMetas = new ArrayList<>();
                    for (int i = 1; i <= cols; i++) {
                        int size = rsMeta.getPrecision(i);
                        if (size == 0) size = 10;
                        if (size >= 20) size = 20;
                        colMetas.add(new ColumnMeta(rsMeta.getColumnName(i), size));
                    }

                    String header = colMetas.stream().map(meta ->
                            String.format(Locale.US, "%-" + meta.getDispSize() + "s", meta.name)
                    ).collect(Collectors.joining("|"));
                    transport.send(ReplResponse.withOut(header));
                    transport.send(ReplResponse.withOut(new String(new char[header.length()]).replace("\0", "-")));

                    int cnt = 0;
                    while (rs.next()) {
                        List<String> values = new ArrayList<>();
                        for (int i = 0; i < cols; i++) {
                            values.add(String.format(Locale.US,
                                    "%-" + colMetas.get(i).getDispSize() + "s",
                                    rs.getString(i+1)));
                        }
                        transport.send(ReplResponse.withOut(String.join("|", values)));
                        cnt++;
                    }
                    transport.sendOut(cnt + " rows selected");
                }
            }
        } catch (SQLException e) {
            transport.sendErr(e.getLocalizedMessage());
        }
        return true;
    }

    private static class ColumnMeta implements Serializable {
        private String name;
        private int dispSize;

        @java.beans.ConstructorProperties({"name", "dispSize"})
        public ColumnMeta(String name, int dispSize) {
            this.name = name;
            this.dispSize = dispSize;
        }

        public String getName() {
            return this.name;
        }

        public int getDispSize() {
            return this.dispSize;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof ColumnMeta)) return false;
            final ColumnMeta other = (ColumnMeta) o;
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (!Objects.equals(this$name, other$name)) return false;
            return this.getDispSize() == other.getDispSize();
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            result = result * PRIME + this.getDispSize();
            return result;
        }

        public String toString() {
            return "SqlCommand.ColumnMeta(name=" + this.getName() + ", dispSize=" + this.getDispSize() + ")";
        }
    }
}
