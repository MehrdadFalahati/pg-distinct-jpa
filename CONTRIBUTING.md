# Contributing to pg-distinct-jpa

Thank you for your interest in contributing! This guide will help you get started.

## Development Setup

### Prerequisites

- Java 21
- Maven 3.6+
- Docker (for integration tests)
- Git

### Clone and Build

```bash
git clone https://github.com/mehrdadfalahati/pg-distinct-jpa.git
cd pg-distinct-jpa
mvn clean install
```

## Running Tests

### All Tests
```bash
mvn test
```

### Unit Tests Only
```bash
mvn test -Dtest=DistinctOnUnitTest
```

### Integration Tests (requires Docker)
```bash
mvn test -Dtest=DistinctOnFunctionTest
```

## Project Structure

```
src/
├── main/java/                      # Source code
│   └── com/github/mehrdadfalahati/pgdistinctjpa/
│       ├── PostgreSqlDistinctOnDialect.java
│       └── DistinctOn.java
└── test/java/                      # Tests
    └── com/github/mehrdadfalahati/pgdistinctjpa/
        ├── DistinctOnUnitTest.java
        ├── DistinctOnFunctionTest.java
        └── Employee.java
```

## Making Changes

### 1. Create a Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Your Changes

- Write clear, documented code
- Add tests for new functionality
- Ensure all tests pass

### 3. Commit

```bash
git add .
git commit -m "Brief description of changes"
```

Follow commit message conventions:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `test:` Test changes
- `refactor:` Code refactoring

### 4. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a pull request on GitHub.

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and concise

## Testing Guidelines

- Write unit tests for new functionality
- Add integration tests for database interactions
- Ensure tests are independent and repeatable
- Use descriptive test names

## Reporting Issues

When reporting issues, please include:

- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Environment details (Java version, Hibernate version, PostgreSQL version)
- Code example (if applicable)

## Questions?

Open an issue for questions or discussions.

Thank you for contributing!
