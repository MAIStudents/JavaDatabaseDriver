package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.QueryBuilder.QueryHandler;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;
import ru.mai.lessons.rpks.impl.DB.CsvFiles;
import ru.mai.lessons.rpks.impl.Parsing.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class DatabaseDriver implements IDatabaseDriver {
  public static final String PATH_CACHE_FILE = "src/test/resources/cache_file.txt";
  ConcurrentHashMap<String, List<String>> cacheMap = null;
  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {
    if (cacheMap == null) {
      cacheMap = new ConcurrentHashMap<>();
    } else if (cacheMap.containsKey(command)) {
      return cacheMap.get(command);
    }
    List<String> resultData = new ArrayList<>();
    CsvFileData csvData = new CsvFileData();
    csvData = ParseFiles.getCsvData(studentsCsvFile, groupsCsvFile, subjectsCsvFile, gradeCsvFile);
    if (csvData == null) {
      return null;
    }
    if (!ParseCommand.checkCommand(command)) {
      throw new WrongCommandFormatException("Command is incorrect.\n");
    }
    CsvFiles csvFiles = new CsvFiles(studentsCsvFile, groupsCsvFile, subjectsCsvFile, gradeCsvFile);
    Map<String, String> queryData = ParseCommand.parseCommand(command);
    QueryHandler queryHandler = new QueryHandler();
    try {
      resultData = queryHandler.queryExecution(csvData, queryData, command, csvFiles);
    } catch (FieldNotFoundInTableException e) {
      e.printStackTrace();
      throw new FieldNotFoundInTableException("Wrong field in query.");
    }
    if (resultData != null && !resultData.isEmpty()) {
      cacheMap.put(command, resultData);
    }
    return resultData;
  }
}
