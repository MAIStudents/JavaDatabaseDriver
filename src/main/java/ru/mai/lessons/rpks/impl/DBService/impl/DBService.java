package ru.mai.lessons.rpks.impl.DBService.impl;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.DBService.IDBService;

import java.util.*;

public class DBService implements IDBService {
  @Override
  public List<String> executeQuery(Map<String, String> query, Map<String, List<Map<String, String>>> tables) throws FieldNotFoundInTableException, WrongCommandFormatException {
    String selectClause = query.get("SELECT");
    String fromClause = query.get("FROM");
    String whereClause = query.get("WHERE");
    String groupByClause = query.get("GROUPBY");

    List<String> fromTables = Arrays.stream(fromClause.split(","))
            .map(String::trim)
            .toList();


    Map<String, List<Map<String, String>>> selectedTables = new HashMap<>();
    for (String table : fromTables) {
      if (tables.containsKey(table)) {
        selectedTables.put(table, tables.get(table));
      } else {
        throw new WrongCommandFormatException("Table " + table + " not found in provided data");
      }
    }

    List<Map<String, String>> joinedData = joinTables(selectedTables);
    if (joinedData.isEmpty()) {
      throw new FieldNotFoundInTableException("Field not found");
    }

    if (whereClause != null && !whereClause.isEmpty()) {
      joinedData = applyWhereCondition(joinedData, whereClause);
    }

    if (groupByClause != null && !groupByClause.isEmpty()) {
      joinedData = applyGroupBy(joinedData, groupByClause);
    }

    List<String> selectColumns = Arrays.stream(selectClause.split(","))
            .map(String::trim)
            .toList();

    return applySelect(joinedData, selectColumns);
  }

  private List<Map<String, String>> applyWhereCondition(List<Map<String, String>> joinedData, String whereClause) {
    String[] conditions = whereClause.split("AND|OR");
    boolean useOr = whereClause.contains("OR");

    return joinedData.stream()
            .filter(row -> {
              boolean conditionMatched = !useOr;

              for (String condition : conditions) {
                String[] keyValue = condition.replace("(", "").replace(")", "").trim().split("=");
                if (keyValue.length != 2) {
                  throw new IllegalArgumentException("Invalid condition format: " + condition);
                }

                String field = keyValue[0].trim();
                String value = keyValue[1].replace("'", "").trim();

                boolean match = row.containsKey(field) && row.get(field).equals(value);

                if (useOr) {
                  conditionMatched = conditionMatched || match;
                } else {
                  conditionMatched = conditionMatched && match;
                }
              }
              return conditionMatched;
            })
            .toList();
  }



  private List<Map<String, String>> joinTables(Map<String, List<Map<String, String>>> tables) {
    if (tables.size() == 1) {
      return tables.values().iterator().next();
    }

    List<Map<String, String>> joinedTable = new ArrayList<>();

    List<Map<String, String>> grades = tables.getOrDefault("grade.csv", new ArrayList<>());
    List<Map<String, String>> students = tables.getOrDefault("students.csv", new ArrayList<>());
    List<Map<String, String>> groups = tables.getOrDefault("groups.csv", new ArrayList<>());
    List<Map<String, String>> subjects = tables.getOrDefault("subjects.csv", new ArrayList<>());

    for (Map<String, String> grade : grades) {
      String studentId = grade.get("student_id");
      String subjectId = grade.get("subject_id");

      Map<String, String> student = students.stream()
              .filter(row -> row.get("id").equals(studentId))
              .findFirst()
              .orElse(new HashMap<>());

      Map<String, String> group = groups.stream()
              .filter(row -> row.get("student_id").equals(studentId))
              .findFirst()
              .orElse(new HashMap<>());

      Map<String, String> subject = subjects.stream()
              .filter(row -> row.get("id").equals(subjectId))
              .findFirst()
              .orElse(new HashMap<>());

      Map<String, String> joinedRow = new HashMap<>(grade);
      joinedRow.putAll(student);
      joinedRow.putAll(group);
      joinedRow.putAll(subject);
      joinedTable.add(joinedRow);
    }
    return joinedTable;
  }

  private List<Map<String, String>> applyGroupBy(List<Map<String, String>> joinedData, String groupByClause) {
    List<String> groupByColumns = Arrays.stream(groupByClause.split(","))
            .map(String::trim)
            .toList();

    Map<List<String>, Map<String, String>> groupedData = new LinkedHashMap<>();

    for (Map<String, String> row : joinedData) {
      List<String> groupKey = groupByColumns.stream()
              .map(column -> row.getOrDefault(column, ""))
              .toList();

      groupedData.putIfAbsent(groupKey, row);
    }
    return new ArrayList<>(groupedData.values());
  }

  private List<String> applySelect(List<Map<String, String>> joinedData, List<String> selectColumns) throws FieldNotFoundInTableException {
    List<String> result = new ArrayList<>();

    for (Map<String, String> row : joinedData) {
      StringBuilder rowBuilder = new StringBuilder();

      for (String column : selectColumns) {
        if (row.containsKey(column)) {
          rowBuilder.append(row.get(column)).append(";");
        } else {
          throw new FieldNotFoundInTableException("Field not found");
        }
      }

      if (!rowBuilder.isEmpty()) {
        rowBuilder.setLength(rowBuilder.length() - 1);
      }
      result.add(rowBuilder.toString());
    }

    return (result.isEmpty()) ? List.of("") : result;
  }
}
