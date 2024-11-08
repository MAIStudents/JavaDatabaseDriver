package ru.mai.lessons.rpks.impl;

import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.tables.*;
import ru.mai.lessons.rpks.impl.Types.Grade;
import ru.mai.lessons.rpks.impl.Types.Group;
import ru.mai.lessons.rpks.impl.Types.Student;
import ru.mai.lessons.rpks.impl.Types.Subject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataBase {
    private final Table students, groups, subjects, grades;

    public DataBase(String studentsFile, String groupsFile, String subjectsFile, String gradesFile) throws CsvValidationException, IOException {
        students = new StudentsTable(studentsFile);
        groups = new GroupsTable(groupsFile);
        subjects = new SubjectsTable(subjectsFile);
        grades = new GradesTable(gradesFile);
    }

    Table getTableByName(String tableName) {
        return switch (tableName) {
            case "students.csv" -> students;
            case "grade.csv" -> grades;
            case "subjects.csv" -> subjects;
            case "groups.csv" -> groups;
            default -> null;
        };
    }

    public Table getTableWithField(String field, String[] tables) {
        for (String table : tables) {
            if (getTableByName(table).getFieldNames().contains(field)) {
                return getTableByName(table);
            }
        }
        return null;
    }

    public List<Object> getRelateValues(String tableName, List<Object> listObj, String nameField) throws FieldNotFoundInTableException {
        return switch (tableName) {
            case "grades" -> getGradesRelateValues(listObj, nameField);
            case "groups" -> getGroupsRelateValues(listObj, nameField);
            case "students" -> getStudentsRelateValues(listObj, nameField);
            case "subjects" -> getSubjectsRelateValues(listObj, nameField);
            default -> List.of();
        };
    }

    public List<Object> getGradesRelateValues(List<Object> objects, String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Object object : objects) {
            Grade grade = (Grade) object;
            switch (nameField) {
                case "full_name" -> {
                    long student_id = grade.studentId();
                    List<Object> list = students.getObjectsByField("id", String.valueOf(student_id));
                    for (Object obj : list) {
                        Student student = (Student) obj;
                        result.add(student.name());
                    }
                }
                case "group_name" -> {
                    long student_id = grade.studentId();
                    List<Object> list = students.getObjectsByField("id", String.valueOf(student_id));
                    for (Object obj : list) {
                        Student student = (Student) obj;
                        Group group = (Group) groups.getObjectsByField("student_id", String.valueOf(student.id())).get(0);
                        result.add(group.name());
                    }
                }
                case "subject_name" -> {
                    long subject_id = grade.subjectId();
                    List<Object> list = subjects.getObjectsByField("subject_id", String.valueOf(subject_id));
                    for (Object obj : list) {
                        Subject subject = (Subject) obj;
                        result.add(subject.name());
                    }
                }
            }
        }
        return result;
    }

    public List<Object> getGroupsRelateValues(List<Object> listObj, String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        if (nameField.equals("full_name")) {
            for (Object obj : listObj) {
                Group group = (Group) obj;
                long student_id = group.id();
                List<Object> list = students.getObjectsByField("id", String.valueOf(student_id));
                for (Object object : list) {
                    Student student = (Student) object;
                    result.add(student.name());
                }
            }
        }
        return result;
    }

    public List<Object> getStudentsRelateValues(List<Object> objects, String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Object object : objects) {
            Student student = (Student) object;
            long student_id = student.id();
            switch (nameField) {
                case "group_name" -> {
                    List<Object> list = groups.getObjectsByField("student_id", String.valueOf(student_id));
                    for (Object obj : list) {
                        Group group = (Group) obj;
                        result.add(group.name());
                    }
                }
                case "grade" -> {
                    List<Object> list = grades.getObjectsByField("student_id", String.valueOf(student_id));
                    for (Object obj : list) {
                        Grade grade = (Grade) obj;
                        result.add(grade.grade());
                    }
                }
                case "date" -> {
                    List<Object> list = grades.getObjectsByField("student_id", String.valueOf(student_id));
                    for (Object obj : list) {
                        Grade grade = (Grade) obj;
                        result.add(grade.date());
                    }
                }
                case "subject_name" -> {
                    List<Object> list = grades.getObjectsByField("student_id", String.valueOf(student_id));
                    for (Object obj : list) {
                        Grade grade = (Grade) obj;
                        Subject subject = (Subject) subjects.getObjectsByField("subject_id", String.valueOf(grade.subjectId())).get(0);
                        result.add(subject.name());
                    }
                }
            }
        }
        return result;
    }

    public List<Object> getSubjectsRelateValues(List<Object> objects, String nameField) throws FieldNotFoundInTableException {
        List<Object> result = new ArrayList<>();
        for (Object object : objects) {
            Subject subject = (Subject) object;
            long subject_id = subject.id();
            List<Object> list = grades.getObjectsByField("subject_id", String.valueOf(subject_id));
            for (Object obj : list) {
                Grade grade = (Grade) obj;
                long student_id = grade.studentId();
                switch (nameField) {
                    case "full_name" -> {
                        Student student = (Student) students.getObjectsByField("id", String.valueOf(student_id)).get(0);
                        result.add(student.name());
                    }
                    case "group_name" -> {
                        Group group = (Group) groups.getObjectsByField("student_id", String.valueOf(student_id)).get(0);
                        result.add(group.name());
                    }
                    case "grade" -> result.add(grade.grade());
                    case "date" -> result.add(grade.date());
                }
            }
        }
        return result;
    }
}
