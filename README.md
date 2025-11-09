# PostgreSQL DISTINCT ON for Hibernate/JPA

A custom Hibernate dialect that adds support for PostgreSQL's `DISTINCT ON` clause.

## Features

- ✅ Custom Hibernate dialect for PostgreSQL with DISTINCT ON support
- ✅ Proper SQL generation with correct argument ordering
- ✅ Comprehensive test coverage with Testcontainers
- ✅ Error handling and validation
- ✅ Works with Hibernate 5.6.x and Java 21

## Quick Start

### 1. Configure Hibernate Dialect

```properties
# application.properties (Spring Boot)
spring.jpa.properties.hibernate.dialect=com.github.mehrdadfalahati.pgdistinctjpa.PostgreSqlDistinctOnDialect
```

### 2. Use DISTINCT ON in Native SQL

```java
@Repository
public class EmployeeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Employee> getHighestPaidPerDepartment() {
        String sql = """
            SELECT DISTINCT ON (department) *
            FROM employees
            ORDER BY department, salary DESC
            """;

        return entityManager
            .createNativeQuery(sql, Employee.class)
            .getResultList();
    }
}
```

## What is DISTINCT ON?

PostgreSQL's `DISTINCT ON` clause allows you to select the first row from each group defined by the expressions. It's commonly used for:

- Getting the latest/first record per group
- Deduplication based on specific columns
- Top-N-per-group queries

### Example: Latest Login Per User

```sql
SELECT DISTINCT ON (user_id) *
FROM login_logs
ORDER BY user_id, login_time DESC;
```

This returns the most recent login for each user.

## Installation

### Maven

```xml
<dependency>
    <groupId>com.github.mehrdadfalahati</groupId>
    <artifactId>pg-distinct-jpa</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Build from Source

```bash
git clone https://github.com/mehrdadfalahati/pg-distinct-jpa.git
cd pg-distinct-jpa
mvn clean install
```

## Usage Examples

### Get Highest Paid Employee Per Department

```java
String sql = """
    SELECT DISTINCT ON (department) *
    FROM employees
    ORDER BY department, salary DESC
    """;

List<Employee> results = entityManager
    .createNativeQuery(sql, Employee.class)
    .getResultList();
```

### Get First Product Per Category and Brand

```java
String sql = """
    SELECT DISTINCT ON (category, brand) *
    FROM products
    ORDER BY category, brand, name
    """;

List<Product> results = entityManager
    .createNativeQuery(sql, Product.class)
    .getResultList();
```

### With Hibernate Session

```java
Session session = sessionFactory.openSession();

String sql = """
    SELECT DISTINCT ON (user_id) *
    FROM activities
    ORDER BY user_id, created_at DESC
    """;

List<Activity> results = session
    .createNativeQuery(sql, Activity.class)
    .getResultList();
```

## Important Notes

### Use Native SQL

Due to how Hibernate's HQL parser works, `DISTINCT ON` must be used via **native SQL** queries, not HQL.

```java
// ✅ CORRECT - Native SQL
String sql = "SELECT DISTINCT ON (department) * FROM employees ORDER BY department, salary DESC";
List<Employee> results = entityManager.createNativeQuery(sql, Employee.class).getResultList();

// ❌ NOT SUPPORTED - HQL
String hql = "SELECT DISTINCT_ON(e.department, e) FROM Employee e ORDER BY e.department";
// This won't work due to HQL parser limitations
```

### ORDER BY Requirements

PostgreSQL requires that DISTINCT ON expressions appear first in the ORDER BY clause:

```sql
-- ✅ CORRECT
SELECT DISTINCT ON (department) *
FROM employees
ORDER BY department, salary DESC;

-- ❌ WRONG - department must come first in ORDER BY
SELECT DISTINCT ON (department) *
FROM employees
ORDER BY salary DESC, department;
```

## Testing

The project includes comprehensive tests:

### Run All Tests

```bash
mvn test
```

### Test Coverage

1. **Unit Tests** (`DistinctOnUnitTest`) - Tests SQL rendering logic
2. **Integration Tests** (`DistinctOnFunctionTest`) - Tests with real PostgreSQL via Testcontainers

**Note**: Integration tests require Docker to be running.

### Test in IDE

- **IntelliJ IDEA**: Right-click test class → Run Tests
- **VS Code**: Click "Run Test" above test methods

## What Was Fixed

This version includes critical bug fixes:

### Before (Broken)

```java
// Arguments: [e.department, e]
// Generated: "DISTINCT ON(e) e.department" ❌ WRONG
```

### After (Fixed)

```java
// Arguments: [e.department, e]
// Generated: "DISTINCT ON(e.department) e" ✅ CORRECT
```

See [CHANGES.md](CHANGES.md) for full details.

## Documentation

- [USAGE_GUIDE.md](USAGE_GUIDE.md) - Comprehensive usage guide with examples
- [CHANGES.md](CHANGES.md) - List of changes and fixes
- [BUG_ANALYSIS.md](BUG_ANALYSIS.md) - Technical analysis of bugs fixed
- [TEST_RESULTS_SUMMARY.md](TEST_RESULTS_SUMMARY.md) - Test suite documentation

## Requirements

- **Java**: 21 (configurable in pom.xml)
- **Hibernate**: 5.6.15.Final
- **PostgreSQL**: 9.5+ (DISTINCT ON available since 7.1)
- **Docker**: Required for running integration tests

## Project Structure

```
pg-distinct-jpa/
├── src/main/java/
│   └── com/github/mehrdadfalahati/pgdistinctjpa/
│       ├── PostgreSqlDistinctOnDialect.java  # Custom Hibernate dialect
│       └── DistinctOn.java                   # DISTINCT ON SQL function
├── src/test/java/
│   └── com/github/mehrdadfalahati/pgdistinctjpa/
│       ├── Employee.java                     # Test entity
│       ├── DistinctOnUnitTest.java          # Unit tests
│       ├── DistinctOnIntegrationTest.java   # Integration tests
│       └── DistinctOnFunctionTest.java      # Comprehensive tests
├── pom.xml                                   # Maven configuration
└── *.md                                      # Documentation files
```

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

[Add your license here]

## Author

Mehrdad Falahati

## Acknowledgments

- Built with [Hibernate ORM](https://hibernate.org/)
- Tests use [Testcontainers](https://www.testcontainers.org/)
- Targets [PostgreSQL](https://www.postgresql.org/)
