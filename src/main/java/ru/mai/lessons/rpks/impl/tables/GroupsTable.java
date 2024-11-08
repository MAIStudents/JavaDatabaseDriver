package ru.mai.lessons.rpks.impl.tables;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Types.Group;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupsTable implements Table {
    private final List<Group> groups = new ArrayList<>();

    public GroupsTable(String file) throws CsvValidationException, IOException {
        CSVReader reader = new CSVReader(new FileReader(file));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            groups.add(new Group(Integer.parseInt(line[0]), line[1], Integer.parseInt(line[2])));
        }
    }

    @Override
    public List<String> getFieldNames() {
        return List.of("id", "group_name", "student_id");
    }

    @Override
    public String getTableName() {
        return "groups";
    }

    @Override
    public List<Object> getObjectsByField(String nameField, String value) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Group group : groups) {
            boolean found;
            switch (nameField) {
                case "id", "group_id" -> found = group.id() == Integer.parseInt(value);
                case "student_id" -> found = group.studentId() == Integer.parseInt(value);
                case "group_name" -> found = group.name().equals(value);
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(group);
            }
        }
        return result;
    }

    @Override
    public List<Object> getObjectsByField(String nameField, List<String> values) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Group group : groups) {
            boolean found;
            switch (nameField) {
                case "id", "group_id" -> {
                    List<Integer> intValues = new ArrayList<>();
                    values.forEach(value -> intValues.add(Integer.parseInt(value)));
                    found = intValues.contains(group.id());
                }
                case "student_id" -> {
                    List<Integer> intValues = new ArrayList<>();
                    values.forEach(value -> intValues.add(Integer.parseInt(value)));
                    found = intValues.contains(group.studentId());
                }
                case "group_name" -> found = values.contains(group.name());
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(group);
            }
        }
        return result;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Group group : groups) {
            switch (nameField) {
                case "group_name" -> result.add(group.name());
                case "student_id" -> result.add(group.studentId());
                case "id", "group_id" -> result.add(group.id());
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
        }
        return result;
    }
}
