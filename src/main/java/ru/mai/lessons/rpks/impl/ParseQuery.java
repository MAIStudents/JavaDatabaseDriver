package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;

public class ParseQuery {
    private final Map<String, String> queryTypeValue =  new HashMap<>();

    public ParseQuery(String query) throws WrongCommandFormatException {
        if (query == null || query.trim().isEmpty()) {
            throw new WrongCommandFormatException("incorrect query");
        }

        String[] args = query.split("\\s+");

        for (String arg : args) {
            String[] queryValue = arg.split("=", 2);
            if (queryValue.length != 2) {
                throw new WrongCommandFormatException("Invalid query: " + arg);
            }
            queryTypeValue.put(queryValue[0].toUpperCase(), queryValue[1]);
        }
    }

    public String getColumnsToSELECT() throws WrongCommandFormatException {
        if (!queryTypeValue.containsKey("SELECT")) {
            throw new WrongCommandFormatException("Your query has no SELECT");
        }
        return queryTypeValue.get("SELECT");
    }

    public String getTablesForFROM() throws WrongCommandFormatException {
        if (!queryTypeValue.containsKey("FROM")) {
            throw new WrongCommandFormatException("Your query has no FROM");
        }
        return queryTypeValue.get("FROM");
    }

    public Optional<String> getWhereClause() {
        return Optional.ofNullable(queryTypeValue.get("WHERE"));
    }

    public Optional<String> getGroupByClause() {
        return Optional.ofNullable(queryTypeValue.get("GROUP BY"));
    }

}
