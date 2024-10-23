package ru.mai.lessons.rpks.impl.Tables;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Group;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Grade;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.NameType;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Student;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Subject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadTables {
    private static final String PREFIX = "src/test/resources/";
    private StudentTable students;
    private GradeTable grades;
    private SubjectTable subjects;
    private GroupTable groups;

    private String studentsCsvFile;
    private String groupsCsvFile;
    private String subjectsCsvFile;
    private String gradeCsvFile;

    public LoadTables(String gradeCsvFile, String subjectsCsvFile, String groupsCsvFile, String studentsCsvFile) {
        this.gradeCsvFile = gradeCsvFile;
        this.subjectsCsvFile = subjectsCsvFile;
        this.groupsCsvFile = groupsCsvFile;
        this.studentsCsvFile = studentsCsvFile;
    }

    public void loadTables() {
        try {
            students = new StudentTable(readStudentsCSV(PREFIX + studentsCsvFile));
            grades = new GradeTable(readGradeCSV(PREFIX + gradeCsvFile));
            subjects = new SubjectTable(readSubjectsCSV(PREFIX + subjectsCsvFile));
            groups = new GroupTable(readGroupsCSV(PREFIX + groupsCsvFile));

            students.setGrades(grades).setSubjects(subjects).setGroups(groups);
            subjects.setGrades(grades).setStudents(students).setGroups(groups);
            grades.setStudents(students).setSubjects(subjects).setGroups(groups);
            groups.setGrades(grades).setSubjects(subjects).setStudents(students);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StudentTable getStudents() {
        return students;
    }

    public GradeTable getGrades() {
        return grades;
    }

    public SubjectTable getSubjects() {
        return subjects;
    }

    public GroupTable getGroups() {
        return groups;
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



    private List<Student> readStudentsCSV(String filename) throws IOException {
        List<Student> students = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] headers = reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                for (int i = 0; i < headers.length; i++) {
                    String[] values = line[i].split(";");
                    Student student = new Student(Integer.parseInt(values[0]), values[1]);
                    students.add(student);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return students;
    }

    private List<Subject> readSubjectsCSV(String filename) throws IOException {
        List<Subject> subjects = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] headers = reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                for (int i = 0; i < headers.length; i++) {
                    String[] values = line[i].split(";");
                    Subject subject = new Subject(Integer.parseInt(values[0]), values[1]);
                    subjects.add(subject);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return subjects;
    }

    private List<Group> readGroupsCSV(String filename) throws IOException {
        List<Group> groups = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] headers = reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                for (int i = 0; i < headers.length; i++) {
                    String[] values = line[i].split(";");
                    Group group = new Group(Integer.parseInt(values[0]), values[1], Integer.parseInt(values[2]));
                    groups.add(group);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return groups;
    }

    private List<Grade> readGradeCSV(String filename) throws IOException {
        List<Grade> grades = new ArrayList<>(); //subject_id;student_id;grade;date
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] headers = reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                for (int i = 0; i < headers.length; i++) {
                    String[] values = line[i].split(";");
                    Grade grade = new Grade(Integer.parseInt(values[0]), Integer.parseInt(values[2]),
                            Integer.parseInt(values[1]), values[3]);
                    grades.add(grade);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return grades;
    }
    public Table getTableWithField(String field, String[] tables) {
        for (var table : tables) {
            if (getTableByName(table).getFields().contains(field)) {
                return getTableByName(table);
            }
        }
        return null;
    }
    public Table getTableByClass(Class class_) {
        if (class_.equals(GradeTable.class)) {
            return grades;
        } else if (class_.equals(Group.class)) {
            return groups;
        }
        else {
            return null;
        }
    }
}
