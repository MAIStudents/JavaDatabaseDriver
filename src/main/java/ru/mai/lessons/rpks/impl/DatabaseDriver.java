package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseDriver implements IDatabaseDriver {
  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException {

    ParseQuery parser = new ParseQuery(command);

    QueryHandler handler = new QueryHandler();

    try {
      handler.loadDataFromFile(studentsCsvFile);
      handler.loadDataFromFile(groupsCsvFile);
      handler.loadDataFromFile(subjectsCsvFile);
      handler.loadDataFromFile(gradeCsvFile);
    } catch (IOException e) {
      throw new IllegalArgumentException("You've entered wrong file!");
    }

    String from = parser.getTablesForFROM();
    String select = parser.getColumnsToSELECT();
    String where = null;
    if (parser.getWhereClause().isPresent()) {
      where = String.valueOf(parser.getWhereClause());
    }
    String groupBy = null;
    if (parser.getGroupByClause().isPresent()) {
      groupBy = String.valueOf(parser.getGroupByClause());
    }

    List<Map<String, String>> result = handler.handleQuery(from, select, where, groupBy);

    if (result.isEmpty()) {
      System.out.println("No results found.");
    } else {
      System.out.println("Query Results:");
      for (Map<String, String> row : result) {
        System.out.println(row.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ")));
      }
    }

    return null;









  }
}
