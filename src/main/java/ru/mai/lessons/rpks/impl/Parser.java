package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class Parser {
    public static final String DEFAULT_PATH = "src/test/resources/";
    private static final String DEFAULT_DELIMITER = ";";

    public List<Map<String, String>> parseRecords(String filePath) throws parsingException, FieldNotFoundInTableException {
        String validFilePath = DEFAULT_PATH + filePath;
        checkFile(validFilePath);

        try {
            log.info("Reading file: {}", validFilePath);
            List<String> lines = Files.readAllLines(Paths.get(validFilePath));
            if (lines.isEmpty()) {
                log.error("CSV file is empty: {}", validFilePath);
                throw new parsingException("CSV file is empty.");
            }
            String[] headers = lines.get(0).split(DEFAULT_DELIMITER);
            return parseLines(lines, headers);
        } catch (IOException e) {
            log.error("Error reading the file: {}", validFilePath, e);
            throw new parsingException("Error reading the file: " + validFilePath, e);
        }
    }

    private List<Map<String, String>> parseLines(List<String> lines, String[] headers) throws parsingException, FieldNotFoundInTableException {
        List<Map<String, String>> result = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(DEFAULT_DELIMITER);
            if (values.length != headers.length) {
                log.error("Row {} does not match the header length: expected {}, found {}", i + 1, headers.length, values.length);
                throw new parsingException("Row " + (i + 1) + " does not match the header length.");
            }
            result.add(createRow(headers, values));
        }
        return result;
    }

    private Map<String, String> createRow(String[] headers, String[] values) throws FieldNotFoundInTableException {
        Map<String, String> row = new HashMap<>();
        for (int j = 0; j < headers.length; j++) {
            if (values[j].isEmpty()) {
                log.error("Field '{}' is missing in row: {}", headers[j], String.join(",", values));
                throw new FieldNotFoundInTableException("Field '" + headers[j] + "' is missing in the row.");
            }
            row.put(headers[j].trim(), values[j].trim());
        }
        return row;
    }

    public static class parsingException extends Exception {
        public parsingException(String message) {
            super(message);
        }

        public parsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private void checkFile(String filePath) throws parsingException {
        if (filePath == null || filePath.isEmpty()) {
            log.error("File path is null or empty.");
            throw new parsingException("File path is null or empty.");
        }
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("File does not exist: {}", filePath);
                throw new parsingException("File does not exist: " + filePath);
            }
            if (!Files.isRegularFile(path)) {
                log.error("Path is not a valid file: {}", filePath);
                throw new parsingException("Path is not a valid file: " + filePath);
            }
            log.info("File validated successfully: {}", filePath);
        } catch (Exception e) {
            log.error("An error occurred while validating the file: {}", filePath, e);
            throw new parsingException("An error occurred while validating the file: " + filePath, e);
        }
    }
}