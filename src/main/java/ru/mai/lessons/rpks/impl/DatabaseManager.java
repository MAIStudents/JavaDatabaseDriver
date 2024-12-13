package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;

public class DatabaseManager {

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
            if (!tables.containsKey(table)) {
                throw new WrongCommandFormatException("Table '" + table + "' not found in the provided data.");
            }
            selectedTables.put(table, tables.get(table));
        }

        System.out.println("Selected tables for join: " + selectedTables.keySet());

        List<Map<String, String>> joinedData = joinTables(selectedTables);

        if (joinedData.isEmpty()) {
            throw new FieldNotFoundInTableException("No matching data found during table join.");
        }

        System.out.println("Joined data contains " + joinedData.size() + " rows.");

        if (whereClause != null && !whereClause.isEmpty()) {
            joinedData = joinedData.stream()
                    .filter(row -> manageCondition(row, whereClause))
                    .toList();
            System.out.println("Filtered data after WHERE clause: " + joinedData.size() + " rows.");
        }

        if (groupByClause != null && !groupByClause.isEmpty()) {
            joinedData = applyGroupby(joinedData, groupByClause);
            System.out.println("Grouped data after GROUPBY clause: " + joinedData.size() + " rows.");
        }

        List<String> selectColumns = Arrays.stream(selectClause.split(","))
                .map(String::trim)
                .toList();

        System.out.println("Columns to be selected: " + selectColumns);

        return applySelect(joinedData, selectColumns);
    }

    private boolean manageCondition(Map<String, String> row, String whereClause) {
        String[] conditions = whereClause.split("AND|OR");
        boolean useOr = whereClause.contains("OR");

        boolean conditionMatched = !useOr;
        for (String condition : conditions) {
            String[] keyValue = condition.replace("(", "").replace(")", "").trim().split("=");
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid condition format: " + condition);
            }

            String field = keyValue[0].trim();
            String value = keyValue[1].replace("'", "").trim();

            boolean match = row.containsKey(field) && row.get(field).equals(value);
            conditionMatched = useOr ? conditionMatched || match : conditionMatched && match;
        }
        return conditionMatched;
    }

    private List<Map<String, String>> joinTables(Map<String, List<Map<String, String>>> tables) {
        if (tables.size() == 1) {
            return new ArrayList<>(tables.values().iterator().next());
        }

        List<Map<String, String>> joinedTable = new ArrayList<>();

        Map<String, List<Map<String, String>>> defaultTables = new HashMap<>();
        defaultTables.put("grade.csv", tables.getOrDefault("grade.csv", Collections.emptyList()));
        defaultTables.put("students.csv", tables.getOrDefault("students.csv", Collections.emptyList()));
        defaultTables.put("groups.csv", tables.getOrDefault("groups.csv", Collections.emptyList()));
        defaultTables.put("subjects.csv", tables.getOrDefault("subjects.csv", Collections.emptyList()));

        for (Map<String, String> grade : defaultTables.get("grade.csv")) {
            String studentId = grade.get("student_id");
            String subjectId = grade.get("subject_id");

            Map<String, String> student = findMatchingRow(defaultTables.get("students.csv"), "id", studentId);
            Map<String, String> group = findMatchingRow(defaultTables.get("groups.csv"), "student_id", studentId);
            Map<String, String> subject = findMatchingRow(defaultTables.get("subjects.csv"), "id", subjectId);

            Map<String, String> joinedRow = new HashMap<>(grade);
            joinedRow.putAll(student);
            joinedRow.putAll(group);
            joinedRow.putAll(subject);

            joinedTable.add(joinedRow);
        }

        return joinedTable;
    }

    private Map<String, String> findMatchingRow(List<Map<String, String>> list, String key, String value) {
        return list.stream()
                .filter(row -> value.equals(row.get(key)))
                .findFirst()
                .orElse(Collections.emptyMap());
    }

    private List<Map<String, String>> applyGroupby(List<Map<String, String>> joinedData, String groupByClause) {
        List<String> groupByColumns = Arrays.asList(groupByClause.split(","));

        Map<Map<String, String>, List<Map<String, String>>> groupedData = new LinkedHashMap<>();

        for (Map<String, String> row : joinedData) {
            Map<String, String> groupKey = new HashMap<>();

            for (String column : groupByColumns) {
                groupKey.put(column, row.getOrDefault(column, ""));
            }

            groupedData.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(row);
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (List<Map<String, String>> group : groupedData.values()) {
            result.add(group.get(0)); // Take the first row of each group
        }

        return result;
    }

    private List<String> applySelect(List<Map<String, String>> joinedData, List<String> selectColumns) throws FieldNotFoundInTableException {
        List<String> result = new ArrayList<>();

        for (Map<String, String> row : joinedData) {
            List<String> selectedValues = new ArrayList<>();

            for (String column : selectColumns) {
                String value = row.get(column);
                if (value != null) {
                    selectedValues.add(value);
                } else {
                    throw new FieldNotFoundInTableException("Field '" + column + "' not found in the row.");
                }
            }

            result.add(String.join(";", selectedValues));
        }

        return result.isEmpty() ? List.of("") : result;
    }
}