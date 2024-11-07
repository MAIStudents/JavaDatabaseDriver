package ru.mai.lessons.rpks.impl;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResultBuilder {
    static public List<List<Object>> createListsFromMap(Map<Object, Map<String, List<Object>>> data, 
                                                String[] keys, CsvFileData csvData, 
                                                List<String> currentlyTables) {
        List<List<Object>> result = new ArrayList<>();
        List<String> orderedTable = new ArrayList<>();
        for (String key : keys) {
            for (String table : currentlyTables) {
                List<String> nameColumns = csvData.getcolumnNames(table);
                if (nameColumns.contains(key)) {
                    orderedTable.add(table);
                }
            }
        }
        for (Map.Entry<Object, Map<String, List<Object>>> entryData : data.entrySet()) {
            Map<String, List<Object>> innerMap = entryData.getValue();
            int listSize = innerMap.containsKey(orderedTable.get(0)) 
                            ? innerMap.get(orderedTable.get(0)).size() 
                            : 0;
            for (int i = 0; i < listSize; i++) {
                int index = 0;
                List<Object> row = new ArrayList<>();
                for (String table : orderedTable) {
                    List<Object> values = innerMap.get(table);
                    if (values != null && i < values.size()) {
                        Object fieldValue = csvData.getFieldValues(List.of(values.get(i)), keys[index], null, null, null).get(0);
                        row.add(fieldValue);
                    } else {
                    row.add(null);
                    }
                    index++;
                }
                result.add(row);
            }
        }
        return result;
    }
    static public List<String> createResListWithWhere(List<List<Object>> columnsData) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> result = new ArrayList<>();
        if (columnsData == null || columnsData.isEmpty()) {
            result.add("");
            return result;
        }
        for (List<Object> list : columnsData) {
            for (Object obj : list) {
                stringBuilder.append(obj.toString()).append(";");
            }
            stringBuilder.setLength(stringBuilder.length() - 1);
            result.add(stringBuilder.toString());
            stringBuilder.setLength(0);
        }
        return result;
    }
    static public List<String> createResList(List<List<Object>> columnsData) {
        if (columnsData == null || columnsData.isEmpty()) {
            return List.of("");
        }
        StringBuilder stringBuilder = new StringBuilder();
        int maxSize = columnsData.stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(0);
        List<String> result = new ArrayList<>(Collections.nCopies(maxSize, ""));
        List<Object> firstColumn = columnsData.get(0);
        for (int i = 0; i < maxSize; i++) {
            int elIndex = i % firstColumn.size();
            result.set(i, firstColumn.get(elIndex).toString());
        }
        for (int colIndex = 1; colIndex < columnsData.size(); colIndex++) {
            List<Object> columnData = columnsData.get(colIndex);
            int sizeList = columnData.size();
            for (int i = 0; i < maxSize; i++) {
                int elIndex = i % sizeList;
                stringBuilder.append(result.get(i)).append(";").append(columnData.get(elIndex).toString());
                result.set(i, stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
        return result;
    }
}
