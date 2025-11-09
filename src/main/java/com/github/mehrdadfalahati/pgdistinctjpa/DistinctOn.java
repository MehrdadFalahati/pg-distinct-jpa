package com.github.mehrdadfalahati.pgdistinctjpa;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.util.List;

public class DistinctOn implements SQLFunction {
    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Type getReturnType(Type type, Mapping mapping) throws QueryException {
        return StandardBasicTypes.STRING;
    }

    @Override
    public String render(Type type, List arguments, SessionFactoryImplementor sessionFactoryImplementor) throws QueryException {
        if (arguments.isEmpty()) {
            throw new QueryException("DISTINCT_ON requires at least 2 arguments: columns and entity");
        }
        if (arguments.size() < 2) {
            throw new QueryException("DISTINCT_ON requires at least 2 arguments: one column and the entity to select");
        }

        // PostgreSQL DISTINCT ON syntax: DISTINCT ON (col1, col2, ...) entity
        // HQL usage: SELECT DISTINCT_ON(col1, col2, ..., entity) FROM Entity
        // Arguments: [col1, col2, ..., entity]
        // We need: all arguments except last go in DISTINCT ON(), last argument goes after

        // Get all columns (all arguments except the last one)
        String commaSeparatedArgs = String.join(",", arguments.subList(0, arguments.size() - 1));

        // Get the entity (last argument)
        String entity = arguments.get(arguments.size() - 1).toString();

        return "DISTINCT ON(" + commaSeparatedArgs + ") " + entity + " ";
    }
}