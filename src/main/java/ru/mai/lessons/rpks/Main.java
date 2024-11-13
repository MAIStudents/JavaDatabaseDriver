package ru.mai.lessons.rpks;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.enums.FieldType;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.DatabaseTable;
import ru.mai.lessons.rpks.impl.TemporaryTable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Main {
  public static void main(String[] args)
      throws FieldNotFoundInTableException, WrongCommandFormatException, IOException {

    String studentPath = Objects.requireNonNull(
            Main.class.getClassLoader().getResource("students.csv")).getPath();
    String subjectPath = Objects.requireNonNull(
            Main.class.getClassLoader().getResource("subjects.csv")).getPath();
    String groupPath = Objects.requireNonNull(
            Main.class.getClassLoader().getResource("groups.csv")).getPath();
    String gradesPath = Objects.requireNonNull(
            Main.class.getClassLoader().getResource("grade.csv")).getPath();

    ITable studentTable = new DatabaseTable("student",
            Map.of("id", FieldType.INT, "full_name", FieldType.STRING), studentPath);

    ITable subjectTable = new DatabaseTable("subject",
            Map.of("id", FieldType.INT, "subject_name", FieldType.STRING), subjectPath);

    ITable groupTable = new DatabaseTable("group",
            Map.of("id", FieldType.INT, "group_name", FieldType.STRING,
                    "student_id", FieldType.INT), groupPath);

    ITable gradeTable = new DatabaseTable("grade",
            Map.of("subject_id", FieldType.INT, "student_id", FieldType.INT,
                    "grade", FieldType.INT, "date", FieldType.STRING), gradesPath);

    studentTable.setReference("id", "group");
    studentTable.setReference("id", "grade");
    subjectTable.setReference("id", "grade");
    groupTable.setReference("student_id", "student");
    gradeTable.setReference("subject_id", "subject");
    gradeTable.setReference("student_id", "student");

    IRequestTable table = new TemporaryTable();

    table.join(studentTable);
    table.join(groupTable);
    table.join(gradeTable);
    table.join(subjectTable);

    //List<List<?>> data = table.getData();
    List<List<?>> data = table.select(List.of("full_name", "subject_name", "grade"));

    for (List<?> record : data) {
      System.out.println(record);
    }

//    log.info("Start service DatabaseDriver");
//    IDatabaseDriver service = new DatabaseDriver(); // ваша реализация service
//    String studentsCsvFile = args[0];
//    String groupsCsvFile = args[1];
//    String subjectsCsvFile = args[2];
//    String gradeCsvFile = args[3];
//    String command = args[4];
//    List<String> results = service.find(studentsCsvFile, groupsCsvFile, subjectsCsvFile,
//                                        gradeCsvFile, command);
//    log.info("Found data: {}", results);
//    log.info("Terminate service DatabaseDriver");
  }
}