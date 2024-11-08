package ru.mai.lessons.rpks.impl.tables;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Types.Grade;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GradesTable implements Table {
    private final List<Grade> grades = new ArrayList<>();

    public GradesTable(String file) throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(file));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            grades.add(new Grade(Integer.parseInt(line[0]), Integer.parseInt(line[1]), Integer.parseInt(line[2]), line[3]));
        }
    }

    @Override
    public List<String> getFieldNames() {
        return List.of("subject_id", "student_id", "grade", "date");
    }

    @Override
    public String getTableName() {
        return "grades";
    }

    @Override
    public List<Object> getObjectsByField(String nameField, String value) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Grade grade : grades) {
            boolean found;
            switch (nameField) {
                case "subject_id" -> found = grade.subjectId() == Integer.parseInt(value);
                case "date" -> found = grade.date().equals(value);
                case "grade" -> found = grade.grade() == Integer.parseInt(value);
                case "student_id" -> found = grade.studentId() == Integer.parseInt(value);
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(grade);
            }
        }
        return result;
    }

    @Override
    public List<Object> getObjectsByField(String nameField, List<String> values) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Grade grade : grades) {
            boolean found;
            switch (nameField) {
                case "subject_id" -> {
                    List<Integer> intValues = new ArrayList<>();
                    values.forEach(value -> intValues.add(Integer.parseInt(value)));
                    found = intValues.contains(grade.subjectId());
                }
                case "date" -> found = values.contains(grade.date());
                case "grade" -> {
                    List<Integer> intValues = new ArrayList<>();
                    values.forEach(value -> intValues.add(Integer.parseInt(value)));
                    found = intValues.contains(grade.grade());
                }
                case "student_id" -> {
                    List<Integer> intValues = new ArrayList<>();
                    values.forEach(value -> intValues.add(Integer.parseInt(value)));
                    found = intValues.contains(grade.studentId());
                }
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(grade);
            }
        }
        return result;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Grade grade : grades) {
            switch (nameField) {
                case "grade" -> result.add(grade.grade());
                case "student_id" -> result.add(grade.studentId());
                case "subject_id" -> result.add(grade.subjectId());
                case "date" -> result.add(grade.date());
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
        }
        return result;
    }
}
