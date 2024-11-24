package ru.mai.lessons.rpks.impl.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mai.lessons.rpks.impl.Entity;
import ru.mai.lessons.rpks.impl.parsers.FileParser;

import java.io.*;
import java.util.*;

@Data
@AllArgsConstructor
public class Group implements Entity {
    private String id;
    private String groupName;
    private String studentId;

    public Group(List<String> tmp) {
        this(tmp.get(0), tmp.get(1), tmp.get(2));
    }

    public static List<Group> loadEntityList(String fileName) throws FileNotFoundException {
        List<Group> groupList = new ArrayList<>();
        try (BufferedReader file = new BufferedReader(new FileReader(PATH + fileName))) {
            String currentLine;
            file.readLine();
            while ((currentLine = file.readLine()) != null) {
                List<String> tmp = new FileParser().parse(currentLine);
                groupList.add(new Group(tmp));
            }
            return groupList;
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    @Override
    public String getField(String field) {
        return switch (field) {
            case "id", "student_id" -> getStudentId();
            case "group_name" -> getGroupName();
            case "group_id" -> id;
            default -> "";
        };
    }
}
