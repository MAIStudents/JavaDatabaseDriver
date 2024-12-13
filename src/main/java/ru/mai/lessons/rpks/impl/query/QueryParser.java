package ru.mai.lessons.rpks.impl.query;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QueryParser {
    private final Map<String, String> queryParts = new HashMap<>();

    public QueryParser(String query) throws WrongCommandFormatException {
        if (query == null || query.trim().isEmpty()) {
            throw new WrongCommandFormatException("Query cannot be empty.");
        }
        String[] parts = query.split("(?=\\b(SELECT|FROM|WHERE=\\(|GROUPBY))");
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                if (!part.contains("=") || part.contains("==")) {
                    throw new WrongCommandFormatException("the keyword must be followed by a single <=> character");
                }
                String[] keyValue = part.split("=", 2);
                if (keyValue.length != 2) {
                    throw new WrongCommandFormatException("Missing value for " + part);
                }
                if (keyValue[1].trim().isEmpty()) {
                    throw new WrongCommandFormatException("Empty value after <=> in " + part);
                }
                String key = keyValue[0].trim().toUpperCase();
                String value = keyValue[1].trim();
                queryParts.put(key, value);
            }
        }
    }

    public String getQueryParts(String commandName) throws WrongCommandFormatException {
        String value = queryParts.get(commandName.toUpperCase());
        if (value == null) {
            throw new WrongCommandFormatException("Missing value for " + commandName);
        }
        return value;
    }

    public Optional<String> getOptionalQueryParts(String commandName) {
        return Optional.ofNullable(queryParts.get(commandName.toUpperCase()));
    }
}