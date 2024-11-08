package ru.mai.lessons.rpks.impl.tables;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Types.Subject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubjectsTable implements Table {
    private final List<Subject> subjects = new ArrayList<>();

    public SubjectsTable(String name) throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(name));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            subjects.add(new Subject(Integer.parseInt(line[0]), line[1]));
        }
    }

    @Override
    public List<String> getFieldNames() {
        return List.of("id", "subject_name");
    }

    @Override
    public String getTableName() {
        return "subjects";
    }

    @Override
    public List<Object> getObjectsByField(String nameField, String value) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Subject subject : subjects) {
            boolean found;
            switch (nameField) {
                case "id", "subject_id" -> found = subject.id() == Integer.parseInt(value);
                case "subject_name" -> found = subject.name().equals(value);
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(subject);
            }
        }
        return result;
    }

    @Override
    public List<Object> getObjectsByField(String nameField, List<String> values) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Subject subject : subjects) {
            boolean found;
            switch (nameField) {
                case "id", "subject_id" -> {
                    List<Integer> intValues = new ArrayList<>();
                    values.forEach(value -> intValues.add(Integer.parseInt(value)));
                    found = intValues.contains(subject.id());
                }
                case "subject_name" -> found = values.contains(subject.name());
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(subject);
            }
        }
        return result;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Subject subject : subjects) {
            switch (nameField) {
                case "subject_name" -> result.add(subject.name());
                case "id", "subject_id" -> result.add(subject.id());
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
        }
        return result;
    }
}
