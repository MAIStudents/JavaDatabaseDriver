package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;

@Slf4j
public class DatabaseDriver implements IDatabaseDriver {
  private final Parser parser = new Parser();
  private final QueryParser queryParser = new QueryParser();
  private final CacheManager cacheManager = new CacheManager();

  @Override
  public List<String> find(String studentsFile, String groupsFile, String subjectsFile,
                           String gradeFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {

    Map<String, String> parsedQuery = queryParser.parseCommand(command);
    QueryParts constructedQueryParts = new QueryParts(parsedQuery);

    CacheManager.CacheEntry retrievedCache = cacheManager.getCache(constructedQueryParts);
    if (retrievedCache != null) {
      Map<String, FileTime> cachedTimestamps = retrievedCache.fileTimestamps;
      if (!haveFilesBeenUpdated(cachedTimestamps)) {
        log.info("Cache hit: returning precomputed results for query key {}", constructedQueryParts);
        return retrievedCache.result;
      }
    }

    Map<String, String> FileMappings = Map.of(
            studentsFile, "students",
            groupsFile, "groups",
            subjectsFile, "subjects",
            gradeFile, "grades"
    );

    Map<String, List<Map<String, String>>> structuredTables = new HashMap<>();
    for (Map.Entry<String, String> entry : FileMappings.entrySet()) {
      try {
        structuredTables.put(entry.getKey(), parser.parseRecords(entry.getKey()));
      } catch (Parser.parsingException | FieldNotFoundInTableException ex) {
        throw new RuntimeException("Error processing  file '" + entry.getKey() + "' for table '" + entry.getValue() + "'", ex);
      }
    }

    List<String> queryResults;
    try {
      queryResults = new DatabaseManager().executeQuery(parsedQuery, structuredTables);
    } catch (IllegalArgumentException invalidQueryException) {
      throw new WrongCommandFormatException("Error in query syntax: " + invalidQueryException.getMessage());
    }

    List<String> involvedFiles = List.of(studentsFile, groupsFile, subjectsFile, gradeFile);
    Map<String, FileTime> modificationTimestamps = new HashMap<>();
    for (String file : involvedFiles) {
      try {
        modificationTimestamps.put(file, Files.getLastModifiedTime(Paths.get(file)));
      } catch (IOException ioException) {
        log.warn("Could not retrieve file modification time for '{}'", file, ioException);
      }
    }

    CacheManager.CacheEntry newCacheEntry = new CacheManager.CacheEntry(queryResults, modificationTimestamps);
    cacheManager.putCache(constructedQueryParts, newCacheEntry);

    return queryResults;
  }

  private boolean haveFilesBeenUpdated(Map<String, FileTime> recordedTimestamps) {
    for (Map.Entry<String, FileTime> fileEntry : recordedTimestamps.entrySet()) {
      String relativePath = fileEntry.getKey();
      FileTime previousTimestamp = fileEntry.getValue();
      File currentFile = new File(Parser.DEFAULT_PATH + relativePath);
  
      try {
        FileTime latestTimestamp = Files.getLastModifiedTime(currentFile.toPath());
        if (!latestTimestamp.equals(previousTimestamp)) {
          return true;
        }
      } catch (Exception e) {
        log.warn("Unable to verify the timestamp for file '{}'. Considering it updated.", relativePath, e);
        return true;
      }
    }
    return false;
  }
}
