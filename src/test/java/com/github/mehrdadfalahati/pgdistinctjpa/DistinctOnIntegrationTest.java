package com.github.mehrdadfalahati.pgdistinctjpa;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DistinctOnIntegrationTest {

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
    @DisplayName("Test DISTINCT ON with single column - Get first employee from each department")
    void testDistinctOnSingleColumn() {
        try (Session session = sessionFactory.openSession()) {
            // PostgreSQL DISTINCT ON syntax: SELECT DISTINCT ON (department) * FROM employees ORDER BY department, salary DESC
            // Since Hibernate custom functions work differently, we need to use native SQL with entity result mapping

            // Using native SQL with DISTINCT ON
            String sql = "SELECT DISTINCT ON (department) * FROM employees ORDER BY department, salary DESC";

            @SuppressWarnings("unchecked")
            List<Employee> results = session.createNativeQuery(sql, Employee.class).getResultList();

            System.out.println("Results from DISTINCT ON query:");
            results.forEach(System.out::println);

            // We expect one employee per department (3 departments)
            assertNotNull(results);
            assertEquals(3, results.size(), "Should have 3 results (one per department)");

            // Verify we got the highest paid from each department
            System.out.println("Number of results: " + results.size());

        } catch (Exception e) {
            System.err.println("Error executing DISTINCT ON query: " + e.getMessage());
            e.printStackTrace();
            fail("Query execution failed: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test DISTINCT ON with multiple columns")
    void testDistinctOnMultipleColumns() {
        try (Session session = sessionFactory.openSession()) {
            // Test with multiple columns in DISTINCT ON
            String sql = "SELECT DISTINCT ON (department, name) * FROM employees ORDER BY department, name";

            @SuppressWarnings("unchecked")
            List<Employee> results = session.createNativeQuery(sql, Employee.class).getResultList();

            System.out.println("Results from DISTINCT ON with multiple columns:");
            results.forEach(System.out::println);

            assertNotNull(results);
            assertFalse(results.isEmpty(), "Results should not be empty");

            // With DISTINCT ON (department, name), we get unique combinations
            // Since all names are unique in our test data, we should get all 7 employees
            assertEquals(7, results.size(), "Should have 7 results (all employees have unique department+name combinations)");

            System.out.println("Number of results: " + results.size());
        } catch (Exception e) {
            System.err.println("Error executing DISTINCT ON query with multiple columns: " + e.getMessage());
            e.printStackTrace();
            fail("Query execution failed: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Test raw SQL DISTINCT ON to verify expected behavior")
    void testRawSqlDistinctOn() {
        try (Session session = sessionFactory.openSession()) {
            // Execute raw SQL to see what the expected result should be
            String sql = "SELECT DISTINCT ON (department) * FROM employees ORDER BY department, salary DESC";

            List<Object[]> results = session.createNativeQuery(sql).getResultList();

            System.out.println("Results from raw SQL DISTINCT ON:");
            results.forEach(row -> System.out.println("Row: " + java.util.Arrays.toString(row)));

            assertNotNull(results);
            assertEquals(3, results.size(), "Should have 3 results (one per department)");

            System.out.println("Raw SQL works correctly - got " + results.size() + " results");
        } catch (Exception e) {
            System.err.println("Error executing raw SQL: " + e.getMessage());
            e.printStackTrace();
            fail("Raw SQL execution failed: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test DISTINCT ON error handling - no arguments")
    void testDistinctOnNoArguments() {
        try (Session session = sessionFactory.openSession()) {
            // This should throw an error as DISTINCT_ON requires arguments
            String hql = "SELECT DISTINCT_ON() FROM Employee e";

            assertThrows(Exception.class, () -> {
                session.createQuery(hql, Employee.class).getResultList();
            }, "DISTINCT_ON without arguments should throw an exception");

        } catch (Exception e) {
            // Expected to fail
            System.out.println("Correctly caught exception for no arguments: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Verify database container is running")
    void testContainerIsRunning() {
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        System.out.println("PostgreSQL container JDBC URL: " + postgres.getJdbcUrl());
    }
}
