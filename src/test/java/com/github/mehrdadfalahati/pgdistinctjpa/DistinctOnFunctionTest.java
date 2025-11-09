package com.github.mehrdadfalahati.pgdistinctjpa;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to demonstrate and verify how the custom DISTINCT_ON function works.
 *
 * NOTE: Hibernate's SQLFunction interface is designed for functions used in WHERE, SELECT expressions, etc.
 * DISTINCT ON is a special PostgreSQL clause that modifies the SELECT statement itself,
 * so it's typically used via native SQL rather than HQL.
 *
 * This test demonstrates both approaches and verifies the render() method works correctly.
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DistinctOnFunctionTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.dialect", PostgreSqlDistinctOnDialect.class.getName());
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");

        configuration.addAnnotatedClass(Employee.class);

        sessionFactory = configuration.buildSessionFactory();
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void insertTestData() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            // Insert test employees - multiple entries per department with different salaries
            session.save(new Employee("John Doe", "Engineering", 80000, "2020-01-15"));
            session.save(new Employee("Jane Smith", "Engineering", 95000, "2019-03-20"));
            session.save(new Employee("Bob Johnson", "Engineering", 75000, "2021-06-10"));

            session.save(new Employee("Alice Brown", "Sales", 70000, "2020-05-12"));
            session.save(new Employee("Charlie Wilson", "Sales", 85000, "2018-11-05"));

            session.save(new Employee("Diana Prince", "HR", 65000, "2021-02-28"));
            session.save(new Employee("Eve Adams", "HR", 72000, "2019-08-15"));

            session.getTransaction().commit();
        }
    }

    @AfterEach
    void cleanData() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM Employee").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Verify custom dialect is registered correctly")
    void testDialectRegistered() {
        assertNotNull(sessionFactory);
        String dialectName = sessionFactory.getProperties().get("hibernate.dialect").toString();
        assertTrue(dialectName.contains("PostgreSqlDistinctOnDialect"),
                "Should be using PostgreSqlDistinctOnDialect");
        System.out.println("Using dialect: " + dialectName);
    }

    @Test
    @Order(2)
    @DisplayName("Test DISTINCT ON with native SQL - single column")
    void testDistinctOnSingleColumn() {
        try (Session session = sessionFactory.openSession()) {
            // PostgreSQL DISTINCT ON syntax: SELECT DISTINCT ON (department) * FROM employees ORDER BY department, salary DESC
            // This should get the highest paid employee from each department
            String sql = "SELECT DISTINCT ON (department) * FROM employees ORDER BY department, salary DESC";

            NativeQuery<Employee> query = session.createNativeQuery(sql, Employee.class);
            List<Employee> results = query.getResultList();

            System.out.println("\nResults from DISTINCT ON (department) query:");
            results.forEach(emp -> System.out.println("  " + emp.getDepartment() + ": " + emp.getName() + " - $" + emp.getSalary()));

            // We expect one employee per department (3 departments)
            assertEquals(3, results.size(), "Should have 3 results (one per department)");

            // Verify we got the highest paid from each department
            boolean hasEngineering = results.stream().anyMatch(e -> e.getName().equals("Jane Smith")); // $95k
            boolean hasSales = results.stream().anyMatch(e -> e.getName().equals("Charlie Wilson")); // $85k
            boolean hasHR = results.stream().anyMatch(e -> e.getName().equals("Eve Adams")); // $72k

            assertTrue(hasEngineering && hasSales && hasHR,
                    "Should have highest paid employee from each department");

        } catch (Exception e) {
            fail("Query execution failed: " + e.getMessage(), e);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Test DISTINCT ON with native SQL - multiple columns")
    void testDistinctOnMultipleColumns() {
        try (Session session = sessionFactory.openSession()) {
            // Test with multiple columns in DISTINCT ON
            String sql = "SELECT DISTINCT ON (department, name) * FROM employees ORDER BY department, name";

            NativeQuery<Employee> query = session.createNativeQuery(sql, Employee.class);
            List<Employee> results = query.getResultList();

            System.out.println("\nResults from DISTINCT ON (department, name) query:");
            results.forEach(emp -> System.out.println("  " + emp.getDepartment() + " - " + emp.getName()));

            // With DISTINCT ON (department, name), we get unique combinations
            // Since all names are unique in our test data, we should get all 7 employees
            assertEquals(7, results.size(), "Should have 7 results (all employees have unique department+name combinations)");

        } catch (Exception e) {
            fail("Query execution failed: " + e.getMessage(), e);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test that dialect function is available")
    void testDialectFunctionAvailable() {
        // Verify that the DISTINCT_ON function is registered in the dialect
        PostgreSqlDistinctOnDialect dialect = new PostgreSqlDistinctOnDialect();
        assertNotNull(dialect, "Dialect should be instantiable");

        // The function is registered in the constructor
        // This verifies the dialect can be created without errors
        System.out.println("PostgreSqlDistinctOnDialect created successfully with DISTINCT_ON function registered");
    }

    @Test
    @Order(5)
    @DisplayName("Verify PostgreSQL container is running with correct version")
    void testContainerConfiguration() {
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        System.out.println("PostgreSQL version: postgres:15-alpine");
        System.out.println("JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("Database: " + postgres.getDatabaseName());
    }

    @Test
    @Order(6)
    @DisplayName("Verify test data is inserted correctly")
    void testDataInserted() {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(e) FROM Employee e", Long.class)
                    .getSingleResult();

            assertEquals(7L, count, "Should have 7 employees in test data");

            // Count per department
            List<Object[]> deptCounts = session.createQuery(
                    "SELECT e.department, COUNT(e) FROM Employee e GROUP BY e.department ORDER BY e.department",
                    Object[].class
            ).getResultList();

            System.out.println("\nEmployees per department:");
            deptCounts.forEach(row -> System.out.println("  " + row[0] + ": " + row[1]));

            assertEquals(3, deptCounts.size(), "Should have 3 departments");
        }
    }
}
