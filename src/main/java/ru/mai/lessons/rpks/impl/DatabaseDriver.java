package ru.mai.lessons.rpks.impl;


import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseDriver implements IDatabaseDriver {
  private final String pathToResources = "src/test/resources/";

  private final Map<Integer, List<String>> cacheForQuery = new HashMap<>();

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {

    if (command == null || command.isEmpty()) {
      throw new FieldNotFoundInTableException("You've entered empty command!");
    }

    ParseQuery parser = new ParseQuery(command);

    QueryHandler handler = new QueryHandler();

    try {
      handler.loadDataFromFile(pathToResources + studentsCsvFile);
      handler.loadDataFromFile(pathToResources + groupsCsvFile);
      handler.loadDataFromFile(pathToResources + subjectsCsvFile);
      handler.loadDataFromFile(pathToResources + gradeCsvFile);
    } catch (IOException e) {
      throw new IllegalArgumentException("You've entered wrong file!");
    }

    String from = parser.getTablesForFROM();
    String select = parser.getColumnsToSELECT();
    String where = null;
    if (parser.getWhereClause().isPresent()) {
      where = parser.getWhereClause().get();
    }
    String groupBy = null;
    if (parser.getGroupByClause().isPresent()) {
      groupBy = parser.getGroupByClause().get();
    }

    AmaCashMachine cache = new AmaCashMachine(from, select, where, groupBy);

    if (cacheForQuery.containsKey(cache.hashCode())) {
      return cacheForQuery.get(cache.hashCode());
    }

    List<Map<String, String>> result = handler.handleQuery(from, select, where, groupBy);

    List<String> resultStrings = new ArrayList<>();


    if (result.isEmpty()) {
      resultStrings.add("");
    } else {
      for (Map<String, String> row : result) {
        String rowString = String.join(";", row.values());
        resultStrings.add(rowString);
      }
    }

    cacheForQuery.put(cache.hashCode(), resultStrings);

    return resultStrings;

  }
}
