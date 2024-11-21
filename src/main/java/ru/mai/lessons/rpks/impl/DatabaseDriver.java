package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.CsvParser.ICsvParser;
import ru.mai.lessons.rpks.impl.CsvParser.impl.CsvParser;
import ru.mai.lessons.rpks.impl.DBService.IDBService;
import ru.mai.lessons.rpks.impl.DBService.impl.DBService;
import ru.mai.lessons.rpks.impl.QueryParser.IQueryParser;
import ru.mai.lessons.rpks.impl.QueryParser.impl.QueryParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;

import static ru.mai.lessons.rpks.impl.CsvParser.impl.CsvParser.START_PATH;

@Slf4j
public class DatabaseDriver implements IDatabaseDriver {
  private final ICsvParser csvParser = new CsvParser();
  private final IQueryParser queryParser = new QueryParser();

  private final Map<QueryKey, CacheEntry> cache = new HashMap<>();

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {

    Map<String, String> query = queryParser.parseCommand(command);
    QueryKey queryKey = new QueryKey(query);

    CacheEntry cachedResult = cache.get(queryKey);
    if (cachedResult != null && !filesChanged(cachedResult.fileTimestamps)) {
      log.info("Cache hit: returning cached result for query");
      return cachedResult.result;
    }

    List<Map<String, String>> students = csvParser.parseCsv(studentsCsvFile);
    List<Map<String, String>> groups = csvParser.parseCsv(groupsCsvFile);
    List<Map<String, String>> subjects = csvParser.parseCsv(subjectsCsvFile);
    List<Map<String, String>> grades = csvParser.parseCsv(gradeCsvFile);

    Map<String, List<Map<String, String>>> tables = Map.of(
            studentsCsvFile, students,
            groupsCsvFile, groups,
            subjectsCsvFile, subjects,
            gradeCsvFile, grades
    );

    IDBService service = new DBService();
    List<String> result;
    try {
      result = service.executeQuery(query, tables);
    } catch (IllegalArgumentException e) {
      throw new WrongCommandFormatException(e.getMessage());
    }


    List<String> filePaths = Arrays.asList(studentsCsvFile, groupsCsvFile, subjectsCsvFile, gradeCsvFile);
    Map<String, FileTime> fileTimestamps = getFileTimestamps(filePaths);
    cache.put(queryKey, new CacheEntry(result, fileTimestamps));

    return result;
  }

  private static class QueryKey {
    private final String select;
    private final String from;
    private final String where;
    private final String groupBy;

    public QueryKey(Map<String, String> query) {
      this.select = query.getOrDefault("SELECT", null);
      this.from = query.getOrDefault("FROM", null);
      this.where = query.getOrDefault("WHERE", null);
      this.groupBy = query.getOrDefault("GROUPBY", null);
    }

    public int hashCode() {
      return Objects.hash(select, from, where, groupBy);
    }

    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      QueryKey other = (QueryKey) obj;
      return Objects.equals(select, other.select) &&
              Objects.equals(from, other.from) &&
              Objects.equals(where, other.where) &&
              Objects.equals(groupBy, other.groupBy);
    }
  }

  private static class CacheEntry {
    private final List<String> result;
    private final Map<String, FileTime> fileTimestamps;

    public CacheEntry(List<String> result, Map<String, FileTime> fileTimestamps) {
      this.result = result;
      this.fileTimestamps = fileTimestamps;
    }
  }

  private boolean filesChanged(Map<String, FileTime> fileTimestamps) {
    for (Map.Entry<String, FileTime> entry : fileTimestamps.entrySet()) {
      File file = new File(START_PATH + entry.getKey());
      try {
        FileTime currentTimestamp = Files.getLastModifiedTime(file.toPath());
        if (!currentTimestamp.equals(entry.getValue())) {
          return true;
        }
      } catch (Exception e) {
        log.warn("Failed to check file timestamp for: {}", entry.getKey(), e);
        return true;
      }
    }
    return false;
  }

  private Map<String, FileTime> getFileTimestamps(List<String> filePaths) {
    Map<String, FileTime> fileTimestamps = new HashMap<>();
    for (String filePath : filePaths) {
      File file = new File(START_PATH + filePath);
      try {
        fileTimestamps.put(filePath, Files.getLastModifiedTime(file.toPath()));
      } catch (Exception e) {
        log.warn("Failed to get file timestamp for: {}", filePath, e);
      }
    }
    return fileTimestamps;
  }

}
