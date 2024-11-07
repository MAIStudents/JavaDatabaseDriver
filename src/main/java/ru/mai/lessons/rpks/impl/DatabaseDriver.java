package ru.mai.lessons.rpks.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.QueryBuilder.QueryHandler;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;
import ru.mai.lessons.rpks.impl.DB.CsvFiles;
import ru.mai.lessons.rpks.impl.Parsing.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DatabaseDriver implements IDatabaseDriver {
  public static final String PATH_CACHE_FILE = "src/test/resources/cache_file.txt";
  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {
    List<String> resultData = new ArrayList<>();
    if (!checkCacheFile(command, resultData)) {
      return null;
    } else if (!resultData.isEmpty()) {
      return resultData;
    }
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
      pushInCacheFile(command, resultData);
    }
    return resultData;
  }
  public boolean checkCacheFile(String command, List<String> resultData) {
    File cacheFile = new File(PATH_CACHE_FILE);
    try {
      if (!cacheFile.exists()) {
          cacheFile.createNewFile();
      }
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(cacheFile))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        if (Objects.equals(line, command)) {
          line = bufferedReader.readLine();
          while (line != null && !line.trim().isEmpty()) {
            resultData.add(line);
            line = bufferedReader.readLine();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  public void pushInCacheFile(String command, List<String> resultData) {
    File cacheFile = new File(PATH_CACHE_FILE);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile, true))) {
      writer.write(command);
      writer.newLine();
      for (String data : resultData) {
        writer.write(data);
        writer.newLine();
      }
      writer.newLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
