package com.github.mehrdadfalahati.pgdistinctjpa;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistinctOnUnitTest {

    private DistinctOn distinctOn;

    @BeforeEach
    void setUp() {
        distinctOn = new DistinctOn();
    }

    @Test
    @DisplayName("Test hasArguments returns true")
    void testHasArguments() {
        assertTrue(distinctOn.hasArguments(), "DISTINCT_ON should have arguments");
    }

    @Test
    @DisplayName("Test hasParenthesesIfNoArguments returns true")
    void testHasParenthesesIfNoArguments() {
        assertTrue(distinctOn.hasParenthesesIfNoArguments());
    }

    @Test
    @DisplayName("Test render with single argument throws exception")
    void testRenderWithSingleArgumentThrowsException() {
        List<String> arguments = Collections.singletonList("e");

        // DISTINCT ON needs at least one column and one entity (minimum 2 arguments)
        // After fix: we explicitly check for minimum 2 arguments
        assertThrows(QueryException.class, () -> {
            distinctOn.render(null, arguments, null);
        }, "Should throw QueryException when less than 2 arguments provided");
    }

    @Test
    @DisplayName("Test render with two arguments - FIXED: produces correct SQL")
    void testRenderWithTwoArguments() {
        // HQL usage: DISTINCT_ON(e.department, e)
        // Arguments: [e.department, e]
        // Expected SQL: DISTINCT ON(e.department) e
        List<String> arguments = Arrays.asList("e.department", "e");

        String result = distinctOn.render(null, arguments, null);

        System.out.println("Generated SQL with args " + arguments + ": " + result);

        // After fix:
        // commaSeparatedArgs = String.join(",", arguments.subList(0, 1))
        //                    = String.join(",", ["e.department"])
        //                    = "e.department"
        // entity = arguments.get(1) = "e"
        // return "DISTINCT ON(e.department) e "  <- CORRECT!

        assertEquals("DISTINCT ON(e.department) e ", result, "CORRECT BEHAVIOR");
    }

    @Test
    @DisplayName("Test render with three arguments - FIXED: produces correct SQL")
    void testRenderWithThreeArguments() {
        // HQL usage: DISTINCT_ON(e.department, e.name, e)
        // Arguments: [e.department, e.name, e]
        // Expected SQL: DISTINCT ON(e.department, e.name) e
        List<String> arguments = Arrays.asList("e.department", "e.name", "e");

        String result = distinctOn.render(null, arguments, null);

        System.out.println("Generated SQL with args " + arguments + ": " + result);

        // After fix:
        // commaSeparatedArgs = String.join(",", arguments.subList(0, 2))
        //                    = String.join(",", ["e.department", "e.name"])
        //                    = "e.department,e.name"  <- CORRECT!
        // entity = arguments.get(2) = "e"
        // return "DISTINCT ON(e.department,e.name) e "  <- CORRECT!

        assertEquals("DISTINCT ON(e.department,e.name) e ", result, "CORRECT BEHAVIOR");
    }

    @Test
    @DisplayName("Test render with empty arguments throws exception")
    void testRenderWithEmptyArgumentsThrowsException() {
        List<String> arguments = Collections.emptyList();

        assertThrows(QueryException.class, () -> {
            distinctOn.render(null, arguments, null);
        }, "Should throw QueryException when no arguments provided");
    }

    @Test
    @DisplayName("Analyze what the correct SQL should look like")
    void analyzeCorrectSqlSyntax() {
        // PostgreSQL DISTINCT ON syntax:
        // SELECT DISTINCT ON (column1, column2) * FROM table ORDER BY column1, column2

        // In HQL: SELECT DISTINCT_ON(e.department, e) FROM Employee e ORDER BY e.department
        // Hibernate will pass arguments in some order

        // The question is: what order does Hibernate pass arguments?
        // We need to understand if:
        // - First argument is the entity and rest are columns
        // - OR first arguments are columns and last is the entity
        // - OR some other pattern

        System.out.println("\n=== PostgreSQL DISTINCT ON Syntax Analysis ===");
        System.out.println("Correct PostgreSQL SQL:");
        System.out.println("  SELECT DISTINCT ON (department) * FROM employees ORDER BY department, salary DESC");
        System.out.println("");
        System.out.println("Expected HQL usage:");
        System.out.println("  SELECT DISTINCT_ON(e.department, e) FROM Employee e ORDER BY e.department, e.salary DESC");
        System.out.println("");
        System.out.println("Current implementation assumes:");
        System.out.println("  arguments[0] = entity to select");
        System.out.println("  arguments[1..n] = columns for DISTINCT ON");
        System.out.println("");
        System.out.println("Generated SQL: DISTINCT ON(columns) entity");
        System.out.println("===========================================\n");
    }
}
