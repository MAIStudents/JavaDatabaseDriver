package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QueryParser {
    public Map<String, String> parseCommand(String command) throws WrongCommandFormatException {
        if (command == null || command.isEmpty()) {
            log.error("Command is null or empty.");
            throw new WrongCommandFormatException("Command cannot be null or empty.");
        }

        if (!command.contains("SELECT") || !command.contains("FROM")) {
            log.error("Command must include both 'SELECT' and 'FROM' clauses. Command: {}", command);
            throw new WrongCommandFormatException("Command must include SELECT and FROM clauses.");
        }

        log.info("Parsing command: {}", command);

        Map<String, String> queryParts = new HashMap<>();

        int selectIndex = command.indexOf("SELECT");
        int fromIndex = command.indexOf("FROM");
        int whereIndex = command.indexOf("WHERE");
        int groupByIndex = command.indexOf("GROUPBY");

        String selectClause = extractSegment(command, selectIndex, "SELECT", fromIndex);
        queryParts.put("SELECT", selectClause);
        log.debug("SELECT clause extracted: {}", selectClause);

        int index = groupByIndex != -1 ? groupByIndex : command.length();
        int nextIndex = whereIndex != -1 ? whereIndex : index;
        String fromClause = extractSegment(command, fromIndex, "FROM", nextIndex);
        queryParts.put("FROM", fromClause);
        log.debug("FROM clause extracted: {}", fromClause);

        if (whereIndex != -1) {
            nextIndex = index;
            String whereClause = extractSegment(command, whereIndex, "WHERE", nextIndex);
            if (!whereClause.startsWith("(") || !whereClause.endsWith(")")) {
                log.error("WHERE clause is not enclosed in parentheses. Command: {}", command);
                throw new WrongCommandFormatException("WHERE clause must be enclosed in parentheses.");
            }
            queryParts.put("WHERE", whereClause.substring(1, whereClause.length() - 1).trim());
            log.debug("WHERE clause extracted: {}", queryParts.get("WHERE"));
        }

        if (groupByIndex != -1) {
            String groupByClause = extractSegment(command, groupByIndex, "GROUPBY", command.length());
            queryParts.put("GROUPBY", groupByClause);
            log.debug("GROUPBY clause extracted: {}", groupByClause);
        }

        log.info("Command parsed successfully: {}", queryParts);
        return queryParts;
    }

    private String extractSegment(String command, int startIndex, String keyword, int endIndex) throws WrongCommandFormatException {
        int equalIndex = command.indexOf("=", startIndex);
        if (equalIndex == -1 || equalIndex >= endIndex) {
            log.error("{} clause must have an '=' followed by a value. Command: {}", keyword, command);
            throw new WrongCommandFormatException(keyword + " clause must have an '=' followed by a value.");
        }
        String clause = command.substring(equalIndex + 1, endIndex).trim();
        if (clause.isEmpty()) {
            log.error("{} clause cannot be empty. Command: {}", keyword, command);
            throw new WrongCommandFormatException(keyword + " clause cannot be empty.");
        }
        return clause;
    }
}
