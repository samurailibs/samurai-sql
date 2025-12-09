package jp.dodododo.sql.unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.sql.DataSource;

import jp.dodododo.sql.empty_impl.DataSourceImpl;
import jp.dodododo.sql.error.SQLError;
import jp.dodododo.sql.util.ClassUtil;
import jp.dodododo.sql.wrapper.ConnectionWrapper;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DbTestExtension implements BeforeEachCallback, AfterEachCallback {

	protected DBConfig config;

	protected Connection connection;

	protected DataSource dataSource;

	protected Driver driver;

	public DbTestExtension() {
		Properties properties = new Properties();
		try (InputStream in = DbTestExtension.class.getResourceAsStream("/db.properties")) {
			properties.load(in);
			this.config = ClassUtil.newInstance(properties.getProperty("config"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public DbTestExtension(DBConfig config) {
		this.config = config;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            DriverManager.deregisterDriver(drivers.nextElement());
        }
        driver = ClassUtil.newInstance(config.driverClassName());
		DriverManager.registerDriver(driver);
		this.connection = newConnection();
		final Connection connection = this.connection;
		this.dataSource = new DataSourceImpl() {
			@Override
			public Connection getConnection() throws SQLException {
				return new ConnectionWrapper(connection) {
					@Override
					public void close() throws SQLException {
					}
				};
			}
		};
	}

	public Connection newConnection() {
		try {
			Connection connection = DriverManager.getConnection(config.URL(), config.properties());
			connection.setAutoCommit(false);
			return connection;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		try {
			getConnection().rollback();
			this.connection.close();
		} catch (SQLException e) {
			throw new SQLError(e);
		} finally {
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException e) {
				throw new SQLError(e);
			} finally {
				driver = null;
			}
		}
	}

	public Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new SQLError(e);
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}
}
