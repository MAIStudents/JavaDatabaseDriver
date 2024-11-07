package ru.mai.lessons.rpks.impl.QueryBuilder;
import ru.mai.lessons.rpks.impl.DB.CsvFiles;
import ru.mai.lessons.rpks.impl.DB.Tables.Grade;
import ru.mai.lessons.rpks.impl.DB.Tables.Groups;
import ru.mai.lessons.rpks.impl.DB.Tables.Students;
import ru.mai.lessons.rpks.impl.DB.Tables.Subjects;
import ru.mai.lessons.rpks.impl.RPN.*;
import ru.mai.lessons.rpks.impl.ResultBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;

public class QueryOperators {
    public static Map<Object, Map<String, List<Object>>> orderedBy(Map<Object, Map<String, List<Object>>> valuesMap, String elRPN, List<String> currentlyTables, CsvFileData csvData) {
        Object sampleKey = valuesMap.keySet().stream().findFirst().orElse(null);
        if (sampleKey == null) {
            return null;
        }
        List<Object> sortedKeys = new ArrayList<>(valuesMap.keySet());
        if (sampleKey instanceof Students) {
            sortedKeys.sort(Comparator.comparing(key -> ((Students) key).getId()));
        } else if (sampleKey instanceof Grade) {
            sortedKeys.sort(Comparator.comparing(key -> ((Grade) key).getStudentId())
                                .thenComparing(key -> ((Grade) key).getSubjectId()));
        } else if (sampleKey instanceof Subjects) {
            sortedKeys.sort(Comparator.comparing(key -> ((Subjects) key).getId()));
        } else if (sampleKey instanceof Groups) {
            sortedKeys.sort(Comparator.comparing(key -> ((Groups) key).getId()));
        } else {
            return null;
        }

        Map<Object, Map<String, List<Object>>> sortedValuesMap = new LinkedHashMap<>();
        for (Object key : sortedKeys) {
            sortedValuesMap.put(key, valuesMap.get(key));
        }
        return sortedValuesMap;
    }
    public static void groupBy(List<List<Object>> columnsData) {
        List<List<Object>> uniqueInnerLists = new ArrayList<>();
        for (List<Object> innerList : columnsData) {
            Set<Object> uniqueSet = new LinkedHashSet<>(innerList);
            uniqueInnerLists.add(new ArrayList<>(uniqueSet));
        }
        Set<List<Object>> uniqueOuterSet = new LinkedHashSet<>(uniqueInnerLists);
        List<List<Object>> finalResult = new ArrayList<>(uniqueOuterSet);
        columnsData.clear();
        columnsData.addAll(finalResult);
    }
    public static List<String> from(Map<String, String> queryData, CsvFiles csvFiles, CsvFileData csvData) {
        String[] dataInFrom = queryData.get("FROM").split(",");
        List<String> filesInFrom = new ArrayList<>();
        for (String el : dataInFrom) {
            if (csvFiles.equalsAnyField(el)) {
                filesInFrom.add(el);
            }
        }
        csvData.loadCsvFiles(filesInFrom);
        return filesInFrom;
    }
    public static List<String> select(CsvFileData csvData, Map<String, String> queryData, 
                                List<String> currenlyTables, String groupByData) throws FieldNotFoundInTableException {
        String[] dataInSelect = queryData.get("SELECT").split(",");
        List<List<Object>> columnsData = new ArrayList<>();
        boolean flagErrorField = true;
        for (String dataName : dataInSelect) {
            for (String table : currenlyTables) {
                List<String> nameColumns = csvData.getcolumnNames(table);
                if (nameColumns.contains(dataName)) {
                    flagErrorField = false;
                    columnsData.add(csvData.getFieldValues(table, dataName, null, null));
                }
            }
        }
        if (flagErrorField) {
            throw new FieldNotFoundInTableException("Wrong field in query.");
        }
        if (groupByData != null && !groupByData.isEmpty()) {
            groupBy(columnsData);
        }
        return ResultBuilder.createResList(columnsData);
    }
    public static List<String> selectWithWhere(CsvFileData csvData, Map<String, String> queryData, 
        List<String> currenlyTables, String groupByData) throws FieldNotFoundInTableException {
        AssembleRPN objRPN = new AssembleRPN();
        List<String> dataWhereRPN = objRPN.toReversePolishNotation(queryData.get("WHERE"));
        List<List<Object>> columnsData = objRPN.assembleRPN(dataWhereRPN, csvData, currenlyTables, queryData.get("SELECT"), groupByData);
        return ResultBuilder.createResListWithWhere(columnsData);
    }
}
