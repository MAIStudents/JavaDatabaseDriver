package ru.mai.lessons.rpks.impl.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvFileHandler {

    private static final String PATH = "src/test/resources/";

    public Map<String, List<Map<String, String>>> loadCsvFiles(String... files) throws IOException {
        Map<String, List<Map<String, String>>> tables = new HashMap<>();
        for (String file : files) {
            String filename = file.split("\\.")[0];
            List<Map<String, String>> data = parseCsvFile(PATH + file);
            tables.put(filename, data);
        }
        return tables;
    }

    private List<Map<String, String>> parseCsvFile(String filePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            List<Map<String, String>> result = new ArrayList<>();
            String line;
            String[] column = null;
            while ((line = readLine(file)) != null) {
                String[] values = line.split(";");
                if (column == null) {
                    column = values;
                } else {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 0; i < column.length; i++) {
                        if (i < values.length) {
                            row.put(column[i].trim(), values[i].trim());
                        }
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (IOException e) {
            System.err.println("Error reading Csv file: " + filePath);
            e.printStackTrace();
            throw e;
        }
    }

    private String readLine(RandomAccessFile file) throws IOException {
        String line = file.readLine();
        if (line != null) {
            return new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
}
