package org.seasar.doma.jdbc.tx;


import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.jdbc.JdbcLogger;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public final class EnkanLocalTransactionDataSource implements DataSource  {
    private final ThreadLocal<LocalTransactionContext> localTxContextHolder = new ThreadLocal<>();
    private final DataSource dataSource;

    public EnkanLocalTransactionDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new DomaNullPointerException("dataSource");
        }
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnectionInternal();
    }

    @Override
    public Connection getConnection(String user, String password) throws SQLException {
        return getConnectionInternal();
    }

    private Connection getConnectionInternal() throws SQLException {
        LocalTransactionContext context = localTxContextHolder.get();
        if (context == null) {
            return dataSource.getConnection();
        }
        return context.getConnection();
    }

    @SuppressWarnings("unckecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == null) {
            throw new SQLException("iface must not be null");
        }
        if (iface.isAssignableFrom(getClass())) {
            return (T) this;
        }
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface == null) {
            return false;
        }
        return iface.isAssignableFrom(getClass()) || dataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter pw) throws SQLException {
        dataSource.setLogWriter(pw);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    public LocalTransaction getLocalTransaction(JdbcLogger jdbcLogger) {
        if (jdbcLogger == null) {
            throw new DomaNullPointerException("jdbcLogger");
        }
        return new LocalTransaction(dataSource, localTxContextHolder,
                jdbcLogger);
    }

    public LocalTransaction getLocalTransaction(JdbcLogger jdbcLogger,
                                                TransactionIsolationLevel transactionIsolationLevel) {
        if (jdbcLogger == null) {
            throw new DomaNullPointerException("jdbcLogger");
        }
        if (transactionIsolationLevel == null) {
            throw new DomaNullPointerException("transactionIsolationLevel");
        }
        return new LocalTransaction(dataSource, localTxContextHolder,
                jdbcLogger, transactionIsolationLevel);
    }

    public KeepAliveLocalTransaction getKeepAliveLocalTransaction(
            JdbcLogger jdbcLogger) {
        if (jdbcLogger == null) {
            throw new DomaNullPointerException("jdbcLogger");
        }
        return new KeepAliveLocalTransaction(dataSource, localTxContextHolder,
                jdbcLogger);
    }

    public KeepAliveLocalTransaction getKeepAliveLocalTransaction(
            JdbcLogger jdbcLogger,
            TransactionIsolationLevel transactionIsolationLevel) {
        if (jdbcLogger == null) {
            throw new DomaNullPointerException("jdbcLogger");
        }
        if (transactionIsolationLevel == null) {
            throw new DomaNullPointerException("transactionIsolationLevel");
        }
        return new KeepAliveLocalTransaction(dataSource, localTxContextHolder,
                jdbcLogger, transactionIsolationLevel);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }
}
