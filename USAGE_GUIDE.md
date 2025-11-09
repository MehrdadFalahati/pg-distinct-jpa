# PostgreSQL DISTINCT ON - Usage Guide

## Overview

This library provides a custom Hibernate dialect that adds support for PostgreSQL's `DISTINCT ON` clause.

## Important Note About Usage

**DISTINCT ON is a PostgreSQL-specific clause that modifies the SELECT statement itself**, not a regular SQL function. Because of this architectural difference:

- ✅ **Recommended**: Use native SQL queries with DISTINCT ON
- ⚠️ **Limited**: HQL support is limited due to how Hibernate processes custom functions

## Configuration

### 1. Add Dependency (when published)

```xml
<dependency>
    <groupId>com.github.mehrdadfalahati</groupId>
    <artifactId>pg-distinct-jpa</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure Hibernate Dialect

**application.properties (Spring Boot):**
```properties
spring.jpa.properties.hibernate.dialect=com.github.mehrdadfalahati.pgdistinctjpa.PostgreSqlDistinctOnDialect
```

**persistence.xml:**
```xml
<property name="hibernate.dialect"
          value="com.github.mehrdadfalahati.pgdistinctjpa.PostgreSqlDistinctOnDialect"/>
```

**Hibernate Configuration:**
```java
Configuration config = new Configuration();
config.setProperty("hibernate.dialect",
    "com.github.mehrdadfalahati.pgdistinctjpa.PostgreSqlDistinctOnDialect");
```

## Usage Examples

### Example 1: Get Highest Paid Employee Per Department

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

### Example 2: Get Latest Record Per User

```java
public List<UserActivity> getLatestActivityPerUser() {
    String sql = """
        SELECT DISTINCT ON (user_id) *
        FROM user_activities
        ORDER BY user_id, created_at DESC
        """;

    return entityManager
        .createNativeQuery(sql, UserActivity.class)
        .getResultList();
}
```

### Example 3: Multiple Columns

```java
public List<Product> getFirstProductPerCategoryAndBrand() {
    String sql = """
        SELECT DISTINCT ON (category, brand) *
        FROM products
        ORDER BY category, brand, name
        """;

    return entityManager
        .createNativeQuery(sql, Product.class)
        .getResultList();
}
```

### Example 4: With Hibernate Session

```java
Session session = sessionFactory.openSession();

String sql = """
    SELECT DISTINCT ON (department) *
    FROM employees
    ORDER BY department, salary DESC
    """;

List<Employee> results = session
    .createNativeQuery(sql, Employee.class)
    .getResultList();
```

## PostgreSQL DISTINCT ON Syntax

```sql
SELECT DISTINCT ON (expression [, expression ...])
    select_list
FROM table_name
ORDER BY expression [ASC | DESC] [, ...]
```

### Key Rules:

1. **ORDER BY must include DISTINCT ON expressions first**
   ```sql
   -- ✅ CORRECT
   SELECT DISTINCT ON (dept) * FROM emp ORDER BY dept, salary DESC;

   -- ❌ WRONG - dept must come first in ORDER BY
   SELECT DISTINCT ON (dept) * FROM emp ORDER BY salary DESC, dept;
   ```

2. **Returns first row for each unique combination**
   - The ORDER BY determines which row is "first"
   - Only one row per unique DISTINCT ON expression combination

3. **Multiple columns in DISTINCT ON**
   ```sql
   SELECT DISTINCT ON (dept, position) *
   FROM employees
   ORDER BY dept, position, hire_date;
   ```

## Common Use Cases

### 1. Latest Record Per Group
```sql
SELECT DISTINCT ON (user_id) *
FROM login_logs
ORDER BY user_id, login_time DESC;
```

### 2. Top N Per Category
```sql
SELECT DISTINCT ON (category_id) *
FROM products
ORDER BY category_id, rating DESC, sales DESC;
```

### 3. Deduplication
```sql
SELECT DISTINCT ON (email) *
FROM user_registrations
ORDER BY email, created_at DESC;
```

### 4. First/Last in Sequence
```sql
SELECT DISTINCT ON (order_id) *
FROM order_status_changes
ORDER BY order_id, changed_at DESC;
```

## Why This Implementation Exists

The `DistinctOn` class provides:

1. **Proper SQL rendering** for the DISTINCT ON clause
2. **Validation** to ensure minimum argument requirements
3. **Documentation** of the argument ordering convention
4. **Foundation** for potential future HQL integration

The `render()` method follows this convention:
- **Arguments**: `[column1, column2, ..., entity]`
- **Generated SQL**: `DISTINCT ON(column1, column2, ...) entity`

## Testing

The test suite includes:

1. **Unit Tests** (`DistinctOnUnitTest.java`)
   - Validates SQL rendering logic
   - Tests argument handling
   - Verifies error cases

2. **Integration Tests** (`DistinctOnFunctionTest.java`)
   - Uses Testcontainers with real PostgreSQL
   - Tests actual query execution
   - Validates results match expected behavior

Run tests:
```bash
mvn test
```

## Limitations

1. **Native SQL Required**: Due to how Hibernate parses HQL, DISTINCT ON must be used via native SQL
2. **PostgreSQL Only**: This is a PostgreSQL-specific feature
3. **Hibernate 5.x**: Tested with Hibernate 5.6.15.Final

## Compatibility

- **PostgreSQL**: 9.5+ (DISTINCT ON has been available since 7.1)
- **Hibernate**: 5.6.x
- **Java**: 21 (can be adjusted in pom.xml)

## Future Enhancements

Potential improvements for future versions:

1. Upgrade to Hibernate 6.x APIs
2. Support for more PostgreSQL-specific features
3. HQL integration (if Hibernate adds support)
4. Additional PostgreSQL dialect enhancements

## Troubleshooting

### Issue: Query doesn't return expected results

**Check**: Ensure ORDER BY includes DISTINCT ON columns first
```sql
-- Wrong
SELECT DISTINCT ON (a) * FROM t ORDER BY b, a;

-- Correct
SELECT DISTINCT ON (a) * FROM t ORDER BY a, b;
```

### Issue: "column must appear in ORDER BY" error

**Solution**: Add DISTINCT ON columns to ORDER BY first
```sql
SELECT DISTINCT ON (category_id) *
FROM products
ORDER BY category_id, name;  -- category_id must be first
```

### Issue: Dialect not being used

**Check**: Verify dialect configuration in logs
```
Hibernate: HHH000400: Using dialect: com.github.mehrdadfalahati.pgdistinctjpa.PostgreSqlDistinctOnDialect
```

## Contributing

Found a bug or have a suggestion? Please open an issue on GitHub.

## License

[Add your license information here]
