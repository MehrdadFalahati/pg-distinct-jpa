# Test Instructions for PostgreSQL DISTINCT ON

## Running Tests

### Option 1: Using Maven (Command Line)
```bash
mvn clean test
```

### Option 2: Using IntelliJ IDEA
1. Right-click on the test file or test class
2. Select "Run 'DistinctOnIntegrationTest'" or "Run 'DistinctOnUnitTest'"
3. IntelliJ will automatically download dependencies and run tests

### Option 3: Using VS Code
1. Install "Extension Pack for Java" if not already installed
2. Open the test file
3. Click the "Run Test" button that appears above each test method
4. Or right-click the test class and select "Run Tests"

## Prerequisites

### For Integration Tests (DistinctOnIntegrationTest)
- **Docker must be running** - Testcontainers will automatically pull and start a PostgreSQL container
- Internet connection for downloading the PostgreSQL Docker image (first run only)

### For Unit Tests (DistinctOnUnitTest)
- No Docker required
- Tests the rendering logic in isolation

## Test Files

1. **DistinctOnUnitTest.java** - Unit tests for the SQL rendering logic
   - Tests argument handling
   - Verifies SQL generation
   - No database required

2. **DistinctOnIntegrationTest.java** - Integration tests with real PostgreSQL
   - Uses Testcontainers to spin up PostgreSQL
   - Tests actual query execution
   - Verifies DISTINCT ON works with Hibernate

## What to Look For

The tests will reveal:

1. **Current Behavior**: How the DISTINCT_ON function currently renders SQL
2. **Bugs**: Any issues with argument ordering or SQL syntax
3. **Expected vs Actual**: Comparison between raw SQL and HQL-generated SQL

## Expected Issues

Based on code review, we expect to find:

1. **Argument Ordering Issue**: The current implementation may have the entity and columns in the wrong order
2. **SQL Syntax**: Generated SQL may not match PostgreSQL's DISTINCT ON syntax exactly
3. **Query Execution Failures**: HQL queries may fail if SQL is malformed

## After Running Tests

Review the test output to understand:
- Which tests pass/fail
- What SQL is being generated (check console output)
- Error messages that indicate what needs to be fixed
