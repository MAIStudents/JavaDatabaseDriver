package ru.mai.lessons.rpks.impl.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mai.lessons.rpks.impl.Entity;
import ru.mai.lessons.rpks.impl.parsers.FileParser;

import java.io.*;
import java.util.*;

@Data
@AllArgsConstructor
public class Subject implements Entity {
    String id;
    String subjectName;

    public Subject(List<String> tmp) {
        this(tmp.get(0), tmp.get(1));
    }

    public static List<Subject> loadEntityList(String fileName) throws FileNotFoundException {
        List<Subject> subjectList = new ArrayList<>();
        try (BufferedReader file = new BufferedReader(new FileReader(PATH + fileName))) {
            String currentLine;
            file.readLine();
            while ((currentLine = file.readLine()) != null) {
                List<String> tmp = new FileParser().parse(currentLine);
                subjectList.add(new Subject(tmp));
            }
            return subjectList;
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    @Override
    public String getField(String field) {
        return switch (field) {
            case "subject_id" -> getId();
            case "subject_name" -> getSubjectName();
            default -> "";
        };
    }

}
