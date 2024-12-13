package ru.mai.lessons.rpks.impl.query;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.util.Pair;

import java.util.*;

public class JoinHandler {

    private final Map<Pair<String, String>, Pair<String, String>> joinConditions = Map.of(
            new Pair<>("students", "grade"), new Pair<>("id", "student_id"),
            new Pair<>("grade", "subjects"), new Pair<>("subject_id", "id"),
            new Pair<>("students", "groups"), new Pair<>("id", "student_id"),
            new Pair<>("groups", "grade"), new Pair<>("student_id", "student_id")
    );

    public List<Map<String, String>> joinTables(String[] tableNames, Map<String, List<Map<String, String>>> data)
            throws WrongCommandFormatException {
        String subjectsTable = locateTable(tableNames, "subjects.csv");
        String gradesTable = locateTable(tableNames, "grade.csv");
        if (subjectsTable != null && gradesTable != null) {
            switchTables(tableNames, subjectsTable, gradesTable);
        }
        List<List<Map<String, String>>> tables = loadDataTables(tableNames, data);
        List<Map<String, String>> result = tables.isEmpty() ? new ArrayList<>() : tables.get(0);
        for (int i = 1; i < tables.size(); i++) {
            String leftTableName = (tableNames[i - 1]).split("\\.")[0].trim();
            String rightTableName = (tableNames[i]).split("\\.")[0].trim();
            result = performJoin(result, tables.get(i), leftTableName, rightTableName);
        }
        return result;
    }

    private String locateTable(String[] tableNames, String targetTable) {
        return Arrays.stream(tableNames)
                .filter(tableName -> tableName.trim().equals(targetTable))
                .findFirst()
                .orElse(null);
    }

    private void switchTables(String[] tableNames, String table1, String table2) {
        int index1 = Arrays.asList(tableNames).indexOf(table1);
        int index2 = Arrays.asList(tableNames).indexOf(table2);

        if (index1 != -1 && index2 != -1) {
            String temp = tableNames[index1];
            tableNames[index1] = tableNames[index2];
            tableNames[index2] = temp;
        }
    }

    private List<List<Map<String, String>>> loadDataTables(
            String[] tableNames,
            Map<String, List<Map<String, String>>> data)
            throws WrongCommandFormatException {
        List<List<Map<String, String>>> tables = new ArrayList<>();
        for (String tableName : tableNames) {
            if (tableName.contains(" ")) {
                throw new WrongCommandFormatException("Table name incorrect: " + tableName);
            }
            String table = tableName.split("\\.")[0].trim();
            tables.add(getTableData(table, data));
        }
        return tables;
    }

    private List<Map<String, String>> getTableData(String tableName, Map<String, List<Map<String, String>>> data)
            throws WrongCommandFormatException {
        List<Map<String, String>> tableData = data.get(tableName);
        if (tableData == null) {
            throw new WrongCommandFormatException("Table not found: " + tableName);
        }
        return tableData;
    }

    private List<Map<String, String>> performJoin(
            List<Map<String, String>> leftTable,
            List<Map<String, String>> rightTable,
            String leftTableName,
            String rightTableName) throws WrongCommandFormatException {
        List<Map<String, String>> rows = new ArrayList<>();
        for (Map<String, String> leftRow : leftTable) {
            for (Map<String, String> rightRow : rightTable) {
                if (rowsMatch(leftRow, rightRow, leftTableName, rightTableName)) {
                    rows.add(mergeRows(leftRow, rightRow));
                }
            }
        }
        return rows;
    }

    private boolean rowsMatch(
            Map<String, String> leftRow,
            Map<String, String> rightRow,
            String leftTableName,
            String rightTableName) {
        Pair<String, String> tablePair = new Pair<>(leftTableName, rightTableName);
        Pair<String, String> columnPair = joinConditions.get(tablePair);
        if (columnPair == null) {
            return false;
        }
        return leftRow.get(columnPair.getLeft()).equals(rightRow.get(columnPair.getRight()));
    }

    private Map<String, String> mergeRows(Map<String, String> leftRow, Map<String, String> rightRow) {
        Map<String, String> Row = new HashMap<>(leftRow);
        rightRow.forEach((key, value) -> Row.putIfAbsent(key, value));
        return Row;
    }
}
