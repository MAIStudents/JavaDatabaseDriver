package ru.mai.lessons.rpks.impl;

import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.command.Command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseDriver implements IDatabaseDriver {
    private static final String PREFIX = "src/test/resources/";
    Map<String, List<String>> cache = new HashMap<>();

    @Override
    public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                             String gradeCsvFile, String stringCommand) throws FieldNotFoundInTableException, WrongCommandFormatException {
        if (studentsCsvFile == null || studentsCsvFile.isEmpty()) {
            throw new IllegalArgumentException("String studentsCsvFile is empty");
        }
        if (groupsCsvFile == null || groupsCsvFile.isEmpty()) {
            throw new IllegalArgumentException("String groupsCsvFile is empty");
        }
        if (subjectsCsvFile == null || subjectsCsvFile.isEmpty()) {
            throw new IllegalArgumentException("String subjectsCsvFile is empty");
        }
        if (gradeCsvFile == null || gradeCsvFile.isEmpty()) {
            throw new IllegalArgumentException("String gradeCsvFile is empty");
        }
        if (stringCommand == null || stringCommand.isEmpty()) {
            throw new IllegalArgumentException("String command is empty");
        }

        if (cache.containsKey(stringCommand)) {
            return cache.get(stringCommand);
        }

        List<String> result = new ArrayList<>();
        try {
            DataBase dataBase = new DataBase(PREFIX + studentsCsvFile, PREFIX + groupsCsvFile, PREFIX + subjectsCsvFile, PREFIX + gradeCsvFile);
            Command command = new Command(stringCommand);
            Handler handler = new Handler(dataBase, command);
            result = handler.handle();
        } catch (IOException | CsvValidationException exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
        }

        cache.put(stringCommand, result);
        return result;
    }
}
