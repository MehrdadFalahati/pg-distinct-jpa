package com.github.mehrdadfalahati.pgdistinctjpa;

import org.hibernate.dialect.PostgreSQL95Dialect;

public class PostgreSqlDistinctOnDialect extends PostgreSQL95Dialect {
    public PostgreSqlDistinctOnDialect() {
        super();
        registerFunction("DISTINCT_ON", new DistinctOn());
    }
}