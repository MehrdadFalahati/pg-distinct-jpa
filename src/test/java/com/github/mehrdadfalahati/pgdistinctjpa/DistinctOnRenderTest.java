package com.github.mehrdadfalahati.pgdistinctjpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Simple test to understand how DistinctOn.render() works
 * and what SQL it generates.
 */
public class DistinctOnRenderTest {

    @Test
    @DisplayName("Analyze current render behavior")
    void analyzeCurrentBehavior() {
        DistinctOn distinctOn = new DistinctOn();

        System.out.println("\n========== DISTINCT ON RENDER ANALYSIS ==========\n");

        // Test case 1: Two arguments (most common case)
        // HQL: SELECT DISTINCT_ON(e.department, e) FROM Employee e
        // Hibernate likely passes: [e.department, e] or [e, e.department]
        System.out.println("Test Case 1: Two arguments [e.department, e]");
        List<String> args1 = Arrays.asList("e.department", "e");
        String result1 = distinctOn.render(null, args1, null);
        System.out.println("  Input: " + args1);
        System.out.println("  Output: " + result1);
        System.out.println("  Expected PostgreSQL: DISTINCT ON(e.department) e");
        System.out.println();

        // Test case 2: Reversed arguments
        System.out.println("Test Case 2: Two arguments [e, e.department]");
        List<String> args2 = Arrays.asList("e", "e.department");
        String result2 = distinctOn.render(null, args2, null);
        System.out.println("  Input: " + args2);
        System.out.println("  Output: " + result2);
        System.out.println("  Expected PostgreSQL: DISTINCT ON(e.department) e");
        System.out.println();

        // Test case 3: Multiple columns
        // HQL: SELECT DISTINCT_ON(e.department, e.name, e) FROM Employee e
        System.out.println("Test Case 3: Three arguments [e.department, e.name, e]");
        List<String> args3 = Arrays.asList("e.department", "e.name", "e");
        String result3 = distinctOn.render(null, args3, null);
        System.out.println("  Input: " + args3);
        System.out.println("  Output: " + result3);
        System.out.println("  Expected PostgreSQL: DISTINCT ON(e.department, e.name) e");
        System.out.println();

        // Test case 4: Multiple columns reversed
        System.out.println("Test Case 4: Three arguments [e, e.department, e.name]");
        List<String> args4 = Arrays.asList("e", "e.department", "e.name");
        String result4 = distinctOn.render(null, args4, null);
        System.out.println("  Input: " + args4);
        System.out.println("  Output: " + result4);
        System.out.println("  Expected PostgreSQL: DISTINCT ON(e.department, e.name) e");
        System.out.println();

        System.out.println("========== ANALYSIS COMPLETE ==========\n");
        System.out.println("CONCLUSION:");
        System.out.println("Current implementation logic:");
        System.out.println("  - arguments.get(0) is placed AFTER 'DISTINCT ON(...)'");
        System.out.println("  - arguments.subList(1, size) are placed INSIDE 'DISTINCT ON(...)'");
        System.out.println();
        System.out.println("For correct PostgreSQL syntax 'DISTINCT ON(columns) entity',");
        System.out.println("the HQL call order should be: DISTINCT_ON(columns..., entity)");
        System.out.println("Example: DISTINCT_ON(e.department, e)");
        System.out.println();
    }
}
