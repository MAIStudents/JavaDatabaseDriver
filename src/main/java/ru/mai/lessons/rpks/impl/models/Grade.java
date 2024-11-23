package ru.mai.lessons.rpks.impl.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mai.lessons.rpks.impl.Entity;
import ru.mai.lessons.rpks.impl.parsers.FileParser;

import java.io.*;
import java.util.*;

@Data
@AllArgsConstructor
public final class Grade implements Entity {
    private String subjectId;
    private String studentId;
    private String grade;
    private String date;

    public Grade(List<String> tmp) {
        this(tmp.get(0), tmp.get(1), tmp.get(2), tmp.get(3));
    }

    static public List<Grade> loadEntityList(String fileName) throws FileNotFoundException {
        List<Grade> gradeList = new ArrayList<>();
        try (BufferedReader file = new BufferedReader(new FileReader(PATH + fileName))) {
            String currentLine;
            file.readLine();
            while ((currentLine = file.readLine()) != null) {
                List<String> tmp = new FileParser().parse(currentLine);
                gradeList.add(new Grade(tmp));
            }
            return gradeList;
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    @Override
    public String getField(String field) {
        return switch (field) {
            case "subject_id" -> getSubjectId();
            case "student_id", "id" -> getStudentId();
            case "grade" -> getGrade();
            case "date" -> getDate();
            default -> "";
        };
    }
}