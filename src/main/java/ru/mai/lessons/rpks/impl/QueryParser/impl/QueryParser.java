package ru.mai.lessons.rpks.impl.QueryParser.impl;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.QueryParser.IQueryParser;

import java.util.HashMap;
import java.util.Map;

public class QueryParser implements IQueryParser {
  @Override
  public Map<String, String> parseCommand(String command) throws WrongCommandFormatException {
    if (command == null || command.isEmpty()) {
      throw new WrongCommandFormatException("Command cannot be null or empty.");
    }

    if (!command.contains("SELECT") || !command.contains("FROM")) {
      throw new WrongCommandFormatException("Command must include SELECT and FROM clauses.");
    }

    Map<String, String> queryParts = new HashMap<>();

    int selectIndex = command.indexOf("SELECT");
    int fromIndex = command.indexOf("FROM");
    int whereIndex = command.indexOf("WHERE");
    int groupByIndex = command.indexOf("GROUPBY");

    queryParts.put("SELECT", extractClause(command, selectIndex, "SELECT", fromIndex));

    int nextIndex = whereIndex != -1 ? whereIndex : groupByIndex != -1 ? groupByIndex : command.length();
    queryParts.put("FROM", extractClause(command, fromIndex, "FROM", nextIndex));

    if (whereIndex != -1) {
      nextIndex = groupByIndex != -1 ? groupByIndex : command.length();
      String whereClause = extractClause(command, whereIndex, "WHERE", nextIndex);
      if (!whereClause.startsWith("(") || !whereClause.endsWith(")")) {
        throw new WrongCommandFormatException("WHERE clause must be enclosed in parentheses.");
      }
      queryParts.put("WHERE", whereClause.substring(1, whereClause.length() - 1).trim());
    }

    if (groupByIndex != -1) {
      queryParts.put("GROUPBY", extractClause(command, groupByIndex, "GROUPBY", command.length()));
    }

    return queryParts;
  }

  private String extractClause(String command, int startIndex, String keyword, int endIndex) throws WrongCommandFormatException {
    int equalIndex = command.indexOf("=", startIndex);
    if (equalIndex == -1 || equalIndex >= endIndex) {
      throw new WrongCommandFormatException(keyword + " clause must have an '=' followed by a value.");
    }
    String clause = command.substring(equalIndex + 1, endIndex).trim();
    if (clause.isEmpty()) {
      throw new WrongCommandFormatException(keyword + " clause cannot be empty.");
    }
    return clause;
  }
}
