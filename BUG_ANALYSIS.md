# Bug Analysis: DISTINCT ON Implementation

## Current Implementation Analysis

### Code in DistinctOn.java (lines 33-34)
```java
String commaSeparatedArgs = String.join(",", arguments.subList(1, arguments.size()));
return "DISTINCT ON(" + commaSeparatedArgs + ") " + arguments.get(0) + " ";
```

### The Problem

**PostgreSQL DISTINCT ON syntax:**
```sql
SELECT DISTINCT ON (column1, column2) *
FROM table
ORDER BY column1, column2
```

**Expected HQL usage:**
```hql
SELECT DISTINCT_ON(e.department, e)
FROM Employee e
ORDER BY e.department, e.salary DESC
```

**When Hibernate calls `render()`, the arguments list contains:**
- `arguments = [e.department, e]`

**Current implementation produces:**
```sql
DISTINCT ON(e) e.department
```

This is **WRONG** because:
1. `arguments.get(0)` = `e.department` is placed AFTER `DISTINCT ON(...)`
2. `arguments.subList(1, size)` = `[e]` is placed INSIDE `DISTINCT ON(...)`

**Expected correct SQL:**
```sql
DISTINCT ON(e.department) e
```

## Root Cause

The argument ordering logic is inverted. The implementation assumes:
- First argument = what to SELECT (entity)
- Remaining arguments = columns for DISTINCT ON

But the actual HQL usage pattern is:
- First N-1 arguments = columns for DISTINCT ON
- Last argument = what to SELECT (entity)

## Test Case Examples

### Example 1: Single column DISTINCT ON
**HQL:** `SELECT DISTINCT_ON(e.department, e) FROM Employee e`

**Arguments:** `["e.department", "e"]`

**Current Output:** `DISTINCT ON(e) e.department` ❌

**Expected Output:** `DISTINCT ON(e.department) e` ✓

### Example 2: Multiple columns DISTINCT ON
**HQL:** `SELECT DISTINCT_ON(e.department, e.name, e) FROM Employee e`

**Arguments:** `["e.department", "e.name", "e"]`

**Current Output:** `DISTINCT ON(e.name,e) e.department` ❌

**Expected Output:** `DISTINCT ON(e.department, e.name) e` ✓

## The Fix

Change lines 33-34 in DistinctOn.java from:
```java
String commaSeparatedArgs = String.join(",", arguments.subList(1, arguments.size()));
return "DISTINCT ON(" + commaSeparatedArgs + ") " + arguments.get(0) + " ";
```

To:
```java
String commaSeparatedArgs = String.join(",", arguments.subList(0, arguments.size() - 1));
return "DISTINCT ON(" + commaSeparatedArgs + ") " + arguments.get(arguments.size() - 1) + " ";
```

This will:
- Put all arguments EXCEPT the last one inside `DISTINCT ON(...)`
- Put the last argument (the entity) after `DISTINCT ON(...)`

## Additional Issues to Fix

1. **Return Type (line 25):** Should return the type of the entity, not STRING
2. **Deprecated API:** Using old Hibernate SQLFunction interface
3. **Error handling:** Need better validation for minimum 2 arguments
