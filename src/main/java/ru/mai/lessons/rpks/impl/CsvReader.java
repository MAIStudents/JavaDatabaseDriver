package ru.mai.lessons.rpks.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvReader {
    public static List<Map<String, String>> readCsv (String pathToFile) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(pathToFile));

        if (lines.isEmpty()) {
            throw new IOException("Your file is empty!");
        }

        String[] headers = lines.get(0).split(";");

        for (int i = 1; i < lines.size(); i++) {

            String[] values = lines.get(i).split(";");
            Map<String, String> row = new HashMap<>();

            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j].trim(), values[j].trim());
            }

            result.add(row);
        }

        return result;
    }
}
