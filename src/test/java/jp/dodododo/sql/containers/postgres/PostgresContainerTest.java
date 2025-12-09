package jp.dodododo.sql.containers.postgres;

import jp.dodododo.sql.Dao;
import jp.dodododo.sql.impl.RdbDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

@Testcontainers
class PostgresContainerTest {

    @SuppressWarnings({"resource", "deprecation"})
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true)
                    .waitingFor(
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30))
                    );

    private static DataSource dataSource;

    @BeforeAll
    static void initSchema() throws Exception {
        System.out.println("=== PostgresContainer (manual) ===");
        System.out.println("Running        : " + postgres.isRunning());
        System.out.println("Container id   : " + postgres.getContainerId());
        System.out.println("Image          : " + postgres.getDockerImageName());
        System.out.println("JDBC URL       : " + postgres.getJdbcUrl());
        System.out.println("Mapped port(5432) : " + postgres.getMappedPort(5432));
        System.out.println("DOCKER_HOST=" + System.getenv("DOCKER_HOST"));
        System.out.println("TC docker.host property=" + System.getProperty("docker.host"));
        System.out.println("===================================");

        org.postgresql.Driver driver = new org.postgresql.Driver();

        final String jdbcUrl = postgres.getJdbcUrl();
        final Properties baseProps = new Properties();
        baseProps.setProperty("user", postgres.getUsername());
        baseProps.setProperty("password", postgres.getPassword());

        dataSource = new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                Connection conn = driver.connect(jdbcUrl, baseProps);
                if (conn == null) {
                    throw new SQLException("Driver.connect() returned null for url=" + jdbcUrl);
                }
                return conn;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                Properties p = new Properties();
                p.putAll(baseProps);
                p.setProperty("user", username);
                p.setProperty("password", password);
                Connection conn = driver.connect(jdbcUrl, p);
                if (conn == null) {
                    throw new SQLException("Driver.connect() returned null for url=" + jdbcUrl);
                }
                return conn;
            }

            @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
            @Override public java.io.PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(java.io.PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public java.util.logging.Logger getParentLogger() { throw new UnsupportedOperationException(); }
        };

        try (Connection conn = dataSource.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                        CREATE TABLE dept(
                            deptno BIGSERIAL PRIMARY KEY,
                            dname  VARCHAR(50)
                        )
                        """);
            }
        } catch (SQLException e) {
            System.out.println("=== POSTGRES CONTAINER LOGS (on init error) ===");
            System.out.println(postgres.getLogs());
            System.out.println("===============================================");
            throw e;
        }
    }

    @AfterAll
    static void cleanup() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DROP TABLE dept");
            }
        } catch (SQLException e) {
            System.out.println("=== POSTGRES CONTAINER LOGS (on cleanup error) ===");
            System.out.println(postgres.getLogs());
            System.out.println("==================================================");
            throw e;
        }
    }

    @Test
    void selectDept() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Dao dao = new RdbDao(connection);
            PostgresDept dept = new PostgresDept();
            dept.setDName("test1");

            dao.insert(dept);

            Optional<BigDecimal> deptno = dao.selectOneNumber("select deptno from dept");
            deptno.ifPresentOrElse(
                    num -> Assertions.assertEquals(num.longValue(), dept.getDeptNo())
                    , () -> {
                        throw new IllegalStateException();
                    });
        } catch (SQLException e) {
            System.out.println("=== POSTGRES CONTAINER LOGS ===");
            System.out.println(postgres.getLogs());
            System.out.println("================================");
            throw e;
        }
    }
}
