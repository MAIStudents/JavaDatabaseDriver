package ru.mai.lessons.rpks.impl;


import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.IOException;
import java.util.*;

public class QueryHandler {
    private final Map<String, List<Map<String, String>>> loadedData = new HashMap<>();

    private final Map<String, Map<String, String>> joinRules = Map.of(
            "grade", Map.of("subject_id", "id"),
            "students", Map.of("id", "student_id"),
            "groups", Map.of("student_id", "student_id"),
            "subjects", Map.of("id", "subject_id")
    );

    public void loadDataFromFile(String fileName) throws IOException {
        String[] parts = fileName.split("[\\\\/]");
        String[] fileNameAndCSV = parts[parts.length - 1].split("\\.");
        String alias = fileNameAndCSV[0];
        List<Map<String, String>> data = CsvReader.readCsv(fileName);
        loadedData.put(alias, data);
    }

    public List<Map<String, String>> handleQuery(String from, String select, String where, String orderBy) throws FieldNotFoundInTableException, WrongCommandFormatException {

        List<Map<String, String>> result = joinTables(from);

        if (where != null) {
            result = filterDataByConditionWHERE(result, where);
        }

        if (orderBy != null) {
            result = groupData(result, orderBy);
        }

        result = selectColumns(result, select);


        return result;
    }

    private List<Map<String, String>> selectColumns(List<Map<String, String>> data, String select) throws FieldNotFoundInTableException {
        List<Map<String, String>> result = new ArrayList<>();
        String[] columns = select.split(",");

        for (Map<String, String> row : data) {
            Map<String, String> selectedRows = new LinkedHashMap<>();
            for (String column : columns) {
                column = column.trim();
                if (row.containsKey(column)) {
                    selectedRows.put(column, row.get(column));
                }
                else {
                    throw new FieldNotFoundInTableException("wrong field!");
                }
            }
            result.add(selectedRows);
        }
        return result;
    }

    private List<Map<String, String>> groupData(List<Map<String, String>> data, String groupBy) {
        Map<String, Map<String, String>> groupedData = new LinkedHashMap<>();

        for (Map<String, String> row : data) {
            String key = row.get(groupBy);
            groupedData.putIfAbsent(key, row);
        }

        return new ArrayList<>(groupedData.values());
    }

    private List<Map<String, String>> filterDataByConditionWHERE(List<Map<String, String>> data, String where) {
        List<Map<String, String>> result = new ArrayList<>();

        where = where.trim();

        String[] conditionGroups = where.split("\\s+OR\\s+");
        conditionGroups[0] = conditionGroups[0].replaceFirst("^\\(", "");
        conditionGroups[conditionGroups.length - 1] = conditionGroups[conditionGroups.length - 1].replaceFirst("\\)$", "");


        for (Map<String, String> row : data) {
            boolean matchesOR = false;

            for (String group : conditionGroups) {
                boolean matchesAND= true;

                String[] conditions = group.split("\\s+AND\\s+");
                for (String condition : conditions) {
                    String[] parts = condition.split("=");
                    String columnName = parts[0].trim();
                    String value = parts[1].trim().replace("'", "");

                    if (!row.containsKey(columnName) || !row.get(columnName).equals(value)) {
                        matchesAND = false;
                    }
                }

                matchesOR = matchesOR || matchesAND;
            }

            if (matchesOR) {
                result.add(row);
            }
        }

        return result;
    }

    public static void swapStringsByValue(String[] array, String value1, String value2) {

        int index1 = -1;
        int index2 = -1;
        boolean canContinue = true;

        for (int i = 0; i < array.length && canContinue; i++) {
            if (array[i].equals(value1)) {
                index1 = i;
            } else if (array[i].equals(value2)) {
                index2 = i;
            }

            if (index1 != -1 && index2 != -1) {
                canContinue = false;
            }
        }

        String temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
    }

    private List<Map<String, String>> joinTables(String from) throws WrongCommandFormatException {
        String[] tableNames = from.split(",");

        String isThereSubjects = null;
        String isThereGrades = null;

        for (String tableName : tableNames) {
            tableName = tableName.trim();
            if (tableName.equals("subjects.csv")) {
                isThereSubjects = tableName;
            }
            else if (tableName.equals("grade.csv")) {
                isThereGrades = tableName;
            }
        }

        if (isThereSubjects != null && isThereGrades != null) {
            swapStringsByValue(tableNames, isThereSubjects, isThereGrades);
        }

        List<Map<String, String>> result = new ArrayList<>();
        List<List<Map<String, String>>> allTables = new ArrayList<>();
        List<String> loadedTableNames = new ArrayList<>();

        for (String tableName : tableNames) {
            tableName = tableName.trim();

            if (tableName.contains(" ")) {
                throw new WrongCommandFormatException(tableName);
            }

            String alias = tableName.split("\\.")[0].trim();

            if (loadedData.containsKey(alias)) {
                allTables.add(loadedData.get(alias));
                loadedTableNames.add(alias);
            } else {
                throw new WrongCommandFormatException("Table not found: " + alias);
            }
        }

        if (!allTables.isEmpty()) {
            result = allTables.get(0);
            for (int i = 1; i < allTables.size(); i++) {
                result = innerJoin(result, allTables.get(i), loadedTableNames.get(i - 1), loadedTableNames.get(i));
            }
        }

        return result;
    }


    private List<Map<String, String>> innerJoin(List<Map<String, String>> leftist, List<Map<String, String>> rightist, String leftTable, String rightTable) {
        List<Map<String, String>> result = new ArrayList<>();


        Map<String, String> rules = joinRules.getOrDefault(leftTable, Map.of());

        for (Map<String, String> rowOfLeft : leftist) {
            for (Map<String, String> rowOfRight : rightist) {
                boolean matches = true;


                for (Map.Entry<String, String> rule : rules.entrySet()) {
                    String leftField = rule.getKey();
                    String rightField = rule.getValue();

                    if (rowOfLeft.containsKey(leftField) && rowOfRight.containsKey(rightField)) {

                        if (!rowOfLeft.get(leftField).equals(rowOfRight.get(rightField))) {
                            matches = false;
                        }
                    }
                }


                if (matches) {
                    Map<String, String> joinedRow = new HashMap<>(rowOfLeft);
                    for (Map.Entry<String, String> entry : rowOfRight.entrySet()) {
                        if (!joinedRow.containsKey(entry.getKey())) {
                            joinedRow.put(entry.getKey(), entry.getValue());
                        }
                    }
                    result.add(joinedRow);
                }
            }
        }

        return result;
    }
}
