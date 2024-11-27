package ru.mai.lessons.rpks.impl;


import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.IOException;
import java.util.*;

public class QueryHandler {
    private final Map<String, List<Map<String, String>>> loadedData = new HashMap<>();

    public void loadDataFromFile(String fileName) throws IOException {
        String[] parts = fileName.split("[\\\\/]");
        String[] fileNameAndCSV = parts[parts.length - 1].split("\\.");
        String alias = fileNameAndCSV[0];
        List<Map<String, String>> data = CsvReader.readCsv(fileName);
        loadedData.put(alias, data);
    }

    public List<Map<String, String>> handleQuery(String from, String select, String where, String orderBy) throws FieldNotFoundInTableException, WrongCommandFormatException {
//        List<Map<String, String>> result = validateTableName(from);
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
                    throw new FieldNotFoundInTableException("!!!!");
                }
            }
            result.add(selectedRows);
        }
        return result;
    }

    private List<Map<String, String>> groupData(List<Map<String, String>> data, String groupBy) {
        Map<String, List<Map<String, String>>> trueGroudedData = new HashMap<>();

        for (Map<String, String> row : data) {
            String key = row.get(groupBy);
            if (!trueGroudedData.containsKey(key)) {
                trueGroudedData.put(key, new ArrayList<>());
            }
            trueGroudedData.get(key).add(row);
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (String key : trueGroudedData.keySet()) {
            List<Map<String, String>> rowsByKey = trueGroudedData.get(key);
            if (!rowsByKey.isEmpty()) {
                result.add(rowsByKey.get(0));
            }
        }

        return result;
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

    private List<Map<String, String>> joinTables(String from) throws WrongCommandFormatException {
        String[] tableNames = from.split(",");
        List<Map<String, String>> result = new ArrayList<>();
        List<List<Map<String, String>>> allTables = new ArrayList<>();

        for (String tableName : tableNames) {
            tableName = tableName.trim();

            if (tableName.contains(" ")) {
                throw new WrongCommandFormatException(tableName);
            }

            String alias = tableName.split("\\.")[0].trim();

            if (loadedData.containsKey(alias)) {
                allTables.add(loadedData.get(alias));
            } else {
                throw new WrongCommandFormatException("!!!!");
            }
        }

        if (!allTables.isEmpty()) {
            result = allTables.get(0);
            for(int i = 1; i < allTables.size(); i++) {
                result = innerJoin(result, allTables.get(i));
            }
        }

        return result;
    }

    private List<Map<String, String>> innerJoin(List<Map<String, String>> leftist, List<Map<String, String>> rightist) {
        List<Map<String, String>> result = new ArrayList<>();

        for(Map<String, String> rowOfLeft : leftist) {
            for(Map<String, String> rowOfRight : rightist) {
                Map<String, String> joinedRow = new HashMap<>(rowOfLeft);
                joinedRow.putAll(rowOfRight);
                result.add(joinedRow);
            }
        }

        return result;
    }

    private List<Map<String, String>> validateTableName(String from) throws WrongCommandFormatException {
        String[] tableNames = from.split(",");
        List<Map<String, String>> result = new ArrayList<>();

        for (String tableName : tableNames) {
            tableName = tableName.trim();
            if (tableName.contains(" ")) {
                throw new WrongCommandFormatException(tableName);
            }

            String[] parts = tableName.split("\\.");
            tableName = parts[0].trim();
            if (loadedData.containsKey(tableName)) {
                result.addAll(loadedData.get(tableName));
            } else {
                throw new WrongCommandFormatException("You've want to get data from non existing table " + tableName + "!");
            }
        }
        return result;
    }
}
