package ru.mai.lessons.rpks.impl;


import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryHandler {
    private final Map<String, List<Map<String, String>>> loadedData = new HashMap<>();

    public void loadDataFromFile(String fileName) throws IOException {
        String[] parts = fileName.split("[\\\\.]");
        String alias = parts[parts.length - 2];
        List<Map<String, String>> data = CsvReader.readCsv(fileName);
        loadedData.put(alias, data);
    }

    public List<Map<String, String>> handleQuery(String from, String select, String where, String orderBy) throws WrongCommandFormatException {
        List<Map<String, String>> result = validateTableName(from);

        if (where != null) {
            result = filterDataByConditionWHERE(result, where);
        }

        if (orderBy != null) {
            result = groupData(result, orderBy);
        }

        result = selectColumns(result, select);


        return result;
    }

    private List<Map<String, String>> selectColumns(List<Map<String, String>> data, String select) {
        List<Map<String, String>> result = new ArrayList<>();
        String[] columns = select.split(",");

        for (Map<String, String> row : data) {
            Map<String, String> selectedRows = new HashMap<>();
            for (String column : columns) {
                if (row.containsKey(column)) {
                    selectedRows.put(column, row.get(column));
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

        String[] conditions = where.split("\\s+(AND|OR)\\s+");

        for(Map<String, String> row : data) {
            boolean needAdd = true;

            for (String condition : conditions) {
                String[] partsOfCondition = condition.split("=");
                String columnName = partsOfCondition[0].trim();
                String value = partsOfCondition[1].trim();

                if (!row.get(columnName).equals(value)) {
                    needAdd = false;
                }
            }

            if (needAdd) {
                result.add(row);
            }
        }

        return result;
    }

    private List<Map<String, String>> validateTableName(String from) throws WrongCommandFormatException {
        String[] tableNames = from.split(",");
        List<Map<String, String>> result = new ArrayList<>();

        for (String tableName : tableNames) {
            tableName = tableName.trim();
            if (loadedData.containsKey(tableName)) {
                result.addAll(loadedData.get(tableName));
            } else {
                throw new WrongCommandFormatException("You've want to get data from non existing table " + tableName + "!");
            }
        }
        return result;
    }
}
