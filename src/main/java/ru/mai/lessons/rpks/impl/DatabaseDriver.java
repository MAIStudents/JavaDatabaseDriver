package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    List<Path> involvedFiles = List.of(
            Paths.get(studentsFile), Paths.get(groupsFile),
            Paths.get(subjectsFile), Paths.get(gradeFile)
    );

    CacheManager.CacheEntry retrievedCache = cacheManager.getCache(constructedQueryParts, involvedFiles);

    if (retrievedCache != null) {
      log.info("Returning precomputed results for query: '{}'", constructedQueryParts);
      return retrievedCache.result;
    }

    Map<String, String> fileMappings = Map.of(
            studentsFile, "students",
            groupsFile, "groups",
            subjectsFile, "subjects",
            gradeFile, "grades"
    );

    Map<String, List<Map<String, String>>> structuredTables = new HashMap<>();
    for (Map.Entry<String, String> entry : fileMappings.entrySet()) {
      try {
        structuredTables.put(entry.getKey(), parser.parseRecords(entry.getKey()));
      } catch (Parser.parsingException | FieldNotFoundInTableException ex) {
        throw new RuntimeException("Error processing file '" + entry.getKey() + "' for table '" + entry.getValue() + "'", ex);
      }
    }

    List<String> queryResults;
    try {
      queryResults = new DatabaseManager().executeQuery(parsedQuery, structuredTables);
    } catch (IllegalArgumentException invalidQueryException) {
      throw new WrongCommandFormatException("Error in query syntax: " + invalidQueryException.getMessage());
    }

    Map<String, FileTime> modificationTimestamps = new HashMap<>();
    for (Path file : involvedFiles) {
      try {
        modificationTimestamps.put(file.toString(), Files.getLastModifiedTime(file));
      } catch (IOException ioException) {
        log.warn("Could not retrieve file modification time for '{}'", file, ioException);
      }
    }

    outputQueryResults(queryResults);

    CacheManager.CacheEntry newCacheEntry = new CacheManager.CacheEntry(queryResults, modificationTimestamps);
    cacheManager.putCache(constructedQueryParts, newCacheEntry);

    return queryResults;
  }

  private void outputQueryResults(List<String> queryResults) {
    String resetColor = "\u001B[0m";
    String blue = "\u001B[36m";
    String red = "\u001B[31m";
    String purple = "\u001B[35m";

    if (queryResults.isEmpty()) {
      System.out.println(red + "No results found." + resetColor);
    } else {
      System.out.println(purple + "Query Results:" + resetColor);
      for (String result : queryResults) {
        System.out.println(blue + result + resetColor);
      }
    }
  }
}
