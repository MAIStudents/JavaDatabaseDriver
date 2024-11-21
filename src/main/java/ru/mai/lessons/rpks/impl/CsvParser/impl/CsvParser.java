package ru.mai.lessons.rpks.impl.CsvParser.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.impl.CsvParser.ICsvParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CsvParser implements ICsvParser {
  public static final String START_PATH = "src/test/resources/";
  @Override
  public List<Map<String, String>> parseCsv(String filePath) {
    String validFilePath = START_PATH + filePath;
    validFile(validFilePath);
    try {
      List<String> lines = Files.readAllLines(Paths.get(validFilePath));
      String[] headers = lines.get(0).split(";");
      return getResult(validFilePath, lines, headers);
  } catch (IOException e) {
      throw new RuntimeException("Error reading the file: " + validFilePath, e);
    }
  }

  private static List<Map<String, String>> getResult(String filePath, List<String> lines, String[] headers) {
    List<Map<String, String>> result = new ArrayList<>();

    for (int i = 1; i < lines.size(); i++) {
      String[] values = lines.get(i).split(";");
      if (values.length != headers.length) {
        throw new IllegalArgumentException("Row " + (i + 1) + " in file " + filePath + " does not match the header length.");
      }

      Map<String, String> row = new HashMap<>();
      for (int j = 0; j < headers.length; j++) {
        row.put(headers[j].trim(), values[j].trim());
      }
      result.add(row);
  }
    return result;
  }

  private static void validFile(String filePath) {
    if (filePath == null || filePath.isEmpty()) {
      throw new IllegalArgumentException("File path is null or empty.");
    }
    try {
      if (!Files.exists(Paths.get(filePath))) {
        throw new IllegalArgumentException("File does not exist: " + filePath);
      }
      if (!Files.isRegularFile(Paths.get(filePath))) {
        throw new IllegalArgumentException("Path is not a file: " + filePath);
      }
    } catch (Exception e) {
      throw new RuntimeException("An error occurred while validating the file: " + filePath, e);
    }
  }
}
