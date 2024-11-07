package ru.mai.lessons.rpks.impl.RPN;
import ru.mai.lessons.rpks.impl.QueryBuilder.*;
import ru.mai.lessons.rpks.impl.ResultBuilder;

import java.util.*;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;

public class AssembleRPN {
    public int priority(String op) {
        if (op.equals("AND")){
          return 2;
        } else if (op.equals("OR")) {
          return 1;
        }
        return 0;
    }
    public List<String> toReversePolishNotation(String dataWhere) {
        ArrayDeque<String> stack = new ArrayDeque<>();
        List<String> result = new ArrayList<>();
        int currentPos = 0;
        while (currentPos < dataWhere.length()) {
        int andIndex = dataWhere.indexOf("AND", currentPos);
        int orIndex = dataWhere.indexOf("OR", currentPos);
        int nextOperatorIndex = -1;
        String operation = "";
        if (andIndex != -1 && orIndex != -1) {
            nextOperatorIndex = Math.min(andIndex, orIndex);
            operation = andIndex < orIndex
                        ? "AND"
                        : "OR";
        } else if (andIndex != -1) {
            nextOperatorIndex = andIndex;
            operation = "AND";
        } else if (orIndex != -1) {
            nextOperatorIndex = orIndex;
            operation = "OR";
        }
        String elInData = (nextOperatorIndex == -1) 
                            ? dataWhere.substring(currentPos) 
                            : dataWhere.substring(currentPos, nextOperatorIndex - 1);
        if (elInData.contains("(")) {
            int openCount = 0;
            while (elInData.charAt(openCount) == '(') {
                stack.addFirst("(");
                openCount++;
            }
        } else if (elInData.contains(")")) {
            result.add(elInData.substring(0, elInData.indexOf(")")));
            int countCloseBracket = elInData.length() - elInData.indexOf(")");
            while (countCloseBracket != 0) {
            if (stack.getFirst().equals("(")) {
                countCloseBracket--;
                stack.removeFirst();
            } else {
                result.add(stack.removeFirst());
            }
            }
        } else {
            result.add(elInData);
        }
        if (!operation.isEmpty() && operation != null) {
            while (!stack.isEmpty() && !stack.getFirst().equals("(") && (priority(operation) >= priority(stack.getFirst()))) {
                result.add(stack.removeFirst());
            }
            stack.add(operation);
        }
        currentPos = nextOperatorIndex == -1 
                    ? currentPos + elInData.length()
                    : nextOperatorIndex + operation.length() + 1;
        }
        while (!stack.isEmpty()) {
            result.add(stack.removeFirst());
        }
        return result;
    }
    public void orOperation(ArrayDeque<Map<Object, Map<String, List<Object>>>> stack, List<String> currentlyTables, String elRPN, CsvFileData csvData) {
        Map<Object, Map<String, List<Object>>> firstEl = stack.removeFirst();
        Map<Object, Map<String, List<Object>>> targetMap = stack.getFirst();
        Object key1 = firstEl.keySet().stream().findFirst().orElse(null);
        Object key2 = targetMap.keySet().stream().findFirst().orElse(null);
        for (Map.Entry<Object, Map<String, List<Object>>> entry : targetMap.entrySet()) {
            Object key = entry.getKey();
            Map<String, List<Object>> targetDataMap = entry.getValue();
            if (firstEl.containsKey(key)) {
                Map<String, List<Object>> firstElDataMap = firstEl.get(key);
                for (String table : currentlyTables) {
                    List<Object> firstElList = firstElDataMap.getOrDefault(table, new ArrayList<>());
                    List<Object> targetList = targetDataMap.getOrDefault(table, new ArrayList<>());
                    firstElList.addAll(targetList);
                    firstElDataMap.put(table, firstElList);
                }
            } else {
                firstEl.put(key, targetDataMap);
            }
        }
        if (key1 != null && key2 != null && key1.getClass().equals(key2.getClass())) {
            firstEl = QueryOperators.orderedBy(firstEl, elRPN, currentlyTables, csvData);
        }
        stack.removeFirst();
        stack.addFirst(firstEl);
    }
    public void andOperation(ArrayDeque<Map<Object, Map<String, List<Object>>>> stack, List<String> currentlyTables) {
        Map<Object, Map<String, List<Object>>> firstEl = stack.removeFirst();
        Map<Object, Map<String, List<Object>>> targetMap = stack.getFirst();
        Iterator<Map.Entry<Object, Map<String, List<Object>>>> iteratorFirstEl = firstEl.entrySet().iterator();
        Iterator<Map.Entry<Object, Map<String, List<Object>>>> iteratorTargetMap = targetMap.entrySet().iterator();
        while (iteratorFirstEl.hasNext() && iteratorTargetMap.hasNext()) {
        Map.Entry<Object, Map<String, List<Object>>> entryFL = iteratorFirstEl.next();
        Map.Entry<Object, Map<String, List<Object>>> entryTM = iteratorTargetMap.next();
        Map<String, List<Object>> firstInMap = entryFL.getValue();
        Map<String, List<Object>> targetInMap = entryTM.getValue();
        for (Map.Entry<String, List<Object>> entryIn : firstInMap.entrySet()) {
            targetInMap.get(entryIn.getKey()).retainAll(entryIn.getValue());
        }
        }
    }
    public List<List<Object>> assembleRPN(List<String> dataWhereRPN, CsvFileData csvData, 
                                        List<String> currentlyTables, String selectFormat, 
                                        String groupByData) throws FieldNotFoundInTableException {
        ArrayDeque<Map<Object, Map<String, List<Object>>>> stack = new ArrayDeque<>();
        String[] dataInSelect = selectFormat.split(",");
        QueryHandler queryHandler = new QueryHandler();
        for (String elRPN : dataWhereRPN) {
            String operandRPN = "";
            switch (priority(elRPN)) {
                case 0:
                    List<Object> currentData = queryHandler.getValuesFromTable(elRPN, csvData, currentlyTables);
                    Map<Object, Map<String, List<Object>>> valuesMap = queryHandler.initializingValuesMap(dataInSelect, csvData, currentData, currentlyTables);
                    for (String nameResColumn : dataInSelect) {
                        queryHandler.processingQuery(csvData, currentData, currentlyTables, nameResColumn, elRPN, valuesMap);
                    }
                    stack.addFirst(valuesMap);
                    operandRPN = elRPN;
                    break;
                case 1:
                    orOperation(stack, currentlyTables, operandRPN, csvData);
                    break;
                case 2:
                    andOperation(stack, currentlyTables);
                    break;
            }
        }
        List<List<Object>> resultRPN = ResultBuilder.createListsFromMap(stack.getFirst(), dataInSelect, csvData, currentlyTables);
        if (groupByData != null && !groupByData.isEmpty() && resultRPN != null) {
            QueryOperators.groupBy(resultRPN);
        }
        return resultRPN;
    }
}
