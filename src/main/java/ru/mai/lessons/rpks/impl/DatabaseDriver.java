package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.ITable;
import ru.mai.lessons.rpks.enums.FieldType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DatabaseDriver implements IDatabaseDriver {

  Map<String, ITable> tables;
  Map<String, String> tableFiles;
  Map<String, Long> filesLastModified;

  public DatabaseDriver() {
    tables = new HashMap<>();
    tableFiles = new HashMap<>();
    filesLastModified = new HashMap<>();
  }

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) {
    boolean tableUpdateFlag = false;

    tableUpdateFlag |= initTable("students",
            Map.of("id", FieldType.INT, "full_name", FieldType.STRING), studentsCsvFile);
    tableUpdateFlag |= initTable("groups", Map.of("id", FieldType.INT,
            "group_name", FieldType.STRING, "student_id", FieldType.INT), groupsCsvFile);
    tableUpdateFlag |= initTable("subjects",
            Map.of("id", FieldType.INT, "subject_name", FieldType.STRING), subjectsCsvFile);
    tableUpdateFlag |= initTable("grade", Map.of("subject_id", FieldType.INT, "student_id",
            FieldType.INT, "grade", FieldType.INT, "date", FieldType.STRING), gradeCsvFile);

    if (tableUpdateFlag) {
      initReferences();
    }



    return null; // реализовать проверку
  }

  boolean initTable(String tableName, Map<String, FieldType> fields, String csvFile) {
    long lastModifiedTime = new File(csvFile).lastModified();

    if (tables.containsKey(tableName) && tableFiles.get(tableName).equals(csvFile) &&
            filesLastModified.get(tableName).equals(lastModifiedTime)) {
      return false;
    }

    tables.put(tableName, new DatabaseTable(tableName, fields, csvFile));
    tableFiles.put(tableName, csvFile);
    filesLastModified.put(tableName, lastModifiedTime);

    return true;
  }

  void initReferences() {
    tables.get("students").setReference("id", "groups");
    tables.get("students").setReference("id", "grade");
    tables.get("subjects").setReference("id", "grade");
    tables.get("groups").setReference("student_id", "students");
    tables.get("grade").setReference("subject_id", "subjects");
    tables.get("grade").setReference("student_id", "students");
  }
}
