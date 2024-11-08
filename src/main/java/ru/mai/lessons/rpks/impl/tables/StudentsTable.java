package ru.mai.lessons.rpks.impl.tables;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Types.Student;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentsTable implements Table {
    private final List<Student> students = new ArrayList<>();

    public StudentsTable(String file) throws CsvValidationException, IOException {
        CSVReader reader = new CSVReader(new FileReader(file));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            students.add(new Student(Integer.parseInt(line[0]), line[1]));
        }
    }

    @Override
    public List<String> getFieldNames() {
        return List.of("id", "full_name");
    }

    @Override
    public String getTableName() {
        return "students";
    }

    @Override
    public List<Object> getObjectsByField(String nameField, String value) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Student student : students) {
            boolean found;
            switch (nameField) {
                case "id", "student_id" -> found = student.id() == Integer.parseInt(value);
                case "full_name" -> found = student.name().equals(value);
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public List<Object> getObjectsByField(String nameField, List<String> values) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Student student : students) {
            boolean found;
            switch (nameField) {
                case "id", "student_id" -> {
                    List<Integer> intValues = new ArrayList<>();
                    values.forEach(value -> intValues.add(Integer.parseInt(value)));
                    found = intValues.contains(student.id());
                }
                case "full_name" -> found = values.contains(student.name());
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
            if (found) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Student student : students) {
            switch (nameField) {
                case "full_name" -> result.add(student.name());
                case "id", "student_id" -> result.add(student.id());
                default -> throw new FieldNotFoundInTableException("Field not found in students table");
            }
        }
        return result;
    }
}
