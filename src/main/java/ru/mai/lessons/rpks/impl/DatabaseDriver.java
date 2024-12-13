package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.cache.QueryCache;
import ru.mai.lessons.rpks.impl.data.CsvFileHandler;
import ru.mai.lessons.rpks.impl.query.QueryHandler;
import ru.mai.lessons.rpks.impl.query.QueryParser;

import java.io.IOException;
import java.util.*;

public class DatabaseDriver implements IDatabaseDriver {

    private final CsvFileHandler csvFileHandler = new CsvFileHandler();
    private final QueryCache queryCache = new QueryCache();
    private final QueryHandler queryHandler = new QueryHandler();

    @Override
    public List<String> find(
            String studentsCsvFile,
            String groupsCsvFile,
            String subjectsCsvFile,
            String gradeCsvFile,
            String command) throws WrongCommandFormatException, FieldNotFoundInTableException {
        if (command == null || command.isEmpty()) {
            throw new FieldNotFoundInTableException("The command cannot be empty.");
        }
        String queryKey = String.join(
                "-",
                studentsCsvFile,
                groupsCsvFile,
                subjectsCsvFile,
                gradeCsvFile,
                command);
        List<String> result;
        if (queryCache.containsKey(queryKey)) {
            System.out.println("The result is taken from the cache.");
            result = queryCache.get(queryKey);
        } else {
            try {
                QueryParser parser = new QueryParser(command);
                Map<String, List<Map<String, String>>> tablesFromCsvFiles =
                        csvFileHandler.loadCsvFiles(studentsCsvFile, groupsCsvFile, subjectsCsvFile, gradeCsvFile);
                List<Map<String, String>> queryResult = queryHandler.handler(parser, tablesFromCsvFiles);
                result = outputFormat(queryResult);
                queryCache.put(queryKey, result);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error loading Csv files.");
            }
        }
        printResult(result);
        return result;

    }

    private void printResult(List<String> result) {
        if (result.size() == 1 && result.get(0).isEmpty()) {
            System.out.println("There is no data on your query.");
        } else {
            for (String line : result) {
                System.out.println(line);
            }
        }
    }

    private List<String> outputFormat(List<Map<String, String>> queryResult) {
        List<String> formattedResult = new ArrayList<>();
        if (queryResult.isEmpty()) {
            formattedResult.add("");
        } else {
            for (Map<String, String> row : queryResult) {
                String rowString = String.join(";", row.values());
                formattedResult.add(rowString);
            }
        }
        return formattedResult;
    }
}
