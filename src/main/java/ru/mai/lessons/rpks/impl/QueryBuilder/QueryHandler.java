package ru.mai.lessons.rpks.impl.QueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;
import ru.mai.lessons.rpks.impl.DB.CsvFiles;

public class QueryHandler {
    public List<Object> getValuesFromTable(String expression, CsvFileData csvData, 
                                         List<String> currentlyTables) throws FieldNotFoundInTableException {
        List<Object> dataFromTable = new ArrayList<>();
        int indexEqual = expression.indexOf("=");
        String nameColumn = expression.substring(0, indexEqual);
        List<Pair<List<String>, Object>> listCondition = new ArrayList<>();
        List<String> conditionEl = new ArrayList<>();
        conditionEl.add(expression.substring(indexEqual + 2, expression.length() - 1));
        listCondition.add(Pair.of(conditionEl, null));
        boolean flagErrorField = true;
        for (String table : currentlyTables) {
            List<String> nameColumns = csvData.getcolumnNames(table);
            if (nameColumns.contains(nameColumn)) {
                flagErrorField = false;
                dataFromTable = csvData.getFieldValues(table, nameColumn, null, listCondition);
            }
        }
        if (flagErrorField) {
            throw new FieldNotFoundInTableException("Wrong field in query.");
        }
        return dataFromTable;
    }
    public Map<Object, Map<String, List<Object>>> initializingValuesMap(String[] dataInSelect, CsvFileData csvData, List<Object> currentData, List<String> currentlyTables) {
        Map<Object, Map<String, List<Object>>> valuesMap = new LinkedHashMap<>();
        for (Object elData : currentData) {
            Map<String, List<Object>> dataWithRelation = new LinkedHashMap<>();
            for (String elSelect : dataInSelect) {
                for (String table : currentlyTables) {
                    List<String> nameColumns = csvData.getcolumnNames(table);
                    if (nameColumns.contains(elSelect)) {
                        dataWithRelation.putIfAbsent(table, new ArrayList<>());
                    }
                }
            }
            valuesMap.put(elData, dataWithRelation); 
        }
        return valuesMap;
    }
    public List<Pair<List<String>, Object>> getMapWithConditions(Map<Object, Map<String, List<Object>>> valuesMap, 
                                                  List<String> conditions, List<Object> currentData,
                                                  String nameStartTable, String nameEndTable) {
        List<Pair<List<String>, Object>> objWithReq = new ArrayList<>();
        int index = 0;
        List<String> listConditions = new ArrayList<>();
        boolean flagNotEqualTables = false;
        if (nameEndTable != nameStartTable) {
            flagNotEqualTables = true;
            objWithReq.add(Pair.of(listConditions, null));
        }
        for (String condition : conditions) {
            if (flagNotEqualTables) {
                listConditions.add(condition);
            } else {
                listConditions.add(condition);
                if ((currentData.size() != 1) && (conditions.size() % currentData.size() == (listConditions.size() - 1))) {
                    objWithReq.add(Pair.of(new ArrayList<>(listConditions), currentData.get(index)));
                    listConditions.clear();
                    index++;
                }
            }
        }
        if (currentData.size() == 1 && !flagNotEqualTables) {
            objWithReq.add(Pair.of(listConditions, currentData.get(index)));
        }
        return objWithReq;
    }
    public boolean findRelationAndJoin(CsvFileData csvData, List<String> currentlyTables, 
                                      String nameStartTable, String nameEndTable,
                                      List<Object> currentData, Set<String> visitedTables,
                                      Map<Object, Map<String, List<Object>>> valuesMap, 
                                      String staticNameEndTable, List<Object> startData) {
        if (visitedTables.contains(nameEndTable)) {
            return false;
        }
        visitedTables.add(nameEndTable);
    
        String foreignKey;
        if (nameEndTable.equals(nameStartTable)) {
            visitedTables.remove(nameEndTable);
            return true;
        }
        for (String table : currentlyTables) {
            if (csvData.getRelation(Pair.of(nameEndTable, table)) != null) {
                if (findRelationAndJoin(csvData, currentlyTables, nameStartTable, table, currentData, visitedTables, valuesMap, staticNameEndTable, startData)) {
                    foreignKey = csvData.getRelation(Pair.of(table, nameEndTable));
                    String reverseForeignKey = csvData.getRelation(Pair.of(nameEndTable, table));
                    List<?> objectConditions = csvData.getFieldValues(currentData, reverseForeignKey, null, null, null);
                    List<String> stringConditions = objectConditions.stream()
                                                .map(Object::toString)
                                                .collect(Collectors.toList());
                    List<Pair<List<String>, Object>> mapWithConditions= getMapWithConditions(valuesMap, stringConditions, startData, staticNameEndTable, nameEndTable);            
                    currentData.clear();
                    currentData.addAll(csvData.getFieldValues(nameEndTable, foreignKey, valuesMap, mapWithConditions));
                    visitedTables.remove(nameEndTable);
                    return true;
                }
            }
        }
        visitedTables.remove(nameEndTable);
        return false;
    }
    public void processingQuery(CsvFileData csvData, List<Object> currentData, 
                                    List<String> currentlyTables, String nameResColumn, 
                                    String nameCurrentColumn, Map<Object, Map<String, List<Object>>> valuesMap) {
        if (currentData == null || currentData.isEmpty()) {
            return;
        }
        String nameStartTable = "";
        String nameEndTable = "";
        int indexEqual = nameCurrentColumn.indexOf('=');
        nameCurrentColumn = nameCurrentColumn.substring(0, indexEqual);
        for (String table : currentlyTables) {
            List<String> nameColumns = csvData.getcolumnNames(table);
            if (nameColumns.contains(nameResColumn)) {
                nameEndTable = table;
            } else if (nameColumns.contains(nameCurrentColumn)) {
                nameStartTable = table;
            }
        }

        String foreignKey;
        if ((foreignKey = csvData.getRelation(Pair.of(nameStartTable, nameEndTable))) != null) {
            String reverseForeignKey = csvData.getRelation(Pair.of(nameEndTable, nameStartTable));
            List<Object> objectConditions = csvData.getFieldValues(currentData, reverseForeignKey, null, null, null);
            List<String> stringConditions = objectConditions.stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toList());
            List<Pair<List<String>, Object>> mapWithConditions = getMapWithConditions(valuesMap, stringConditions, currentData, null, null);
            currentData = csvData.getFieldValues(nameEndTable, foreignKey, valuesMap, mapWithConditions);
        } else {
            final List<Object> startData = new ArrayList<>(currentData);
            findRelationAndJoin(csvData, currentlyTables, nameStartTable, nameEndTable, new ArrayList<>(currentData), new TreeSet<>(), valuesMap, nameEndTable, startData);
        }
    }
    public List<String> queryExecution(CsvFileData csvData, Map<String, String> queryData, 
                                    String command, CsvFiles csvFiles) throws FieldNotFoundInTableException {
        List<String> currentlyTables = QueryOperators.from(queryData, csvFiles, csvData);
        if (!command.contains("WHERE")) {
            return QueryOperators.select(csvData, queryData, currentlyTables, queryData.get("GROUPBY"));
        } else {
            return QueryOperators.selectWithWhere(csvData, queryData, currentlyTables, queryData.get("GROUPBY"));
        }
    }
}
