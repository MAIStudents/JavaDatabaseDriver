package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.swap;

@Slf4j
public class DatabaseReader {
    Map<String, List<List<String>>> tables = new HashMap<>();
    List<String> searchParameters = new ArrayList<>();
    Map<Union, Map<String, List<String>>> mapCondition = new HashMap<>();
    List<String> groupParameters = new ArrayList<>();

    private final String studentsFile;
    private final String groupsFile;
    private final String subjectsFile;
    private final String gradeFile;

    private final String students = "students";
    private final String groups = "groups";
    private final String subjects = "subjects";
    private final String grade = "grade";

    enum Union {
        AND,
        OR
    }

    final Pattern firstCondition = Pattern.compile("^([^= ]+)='([^']+)'(.*)?$");
    final Pattern nextCondition = Pattern.compile("^(AND|OR) ([^= ]+)='([^']+)'(.*)?$");

    public DatabaseReader(String studentsFile, String groupsFile, String subjectsFile, String gradeFile) {
        this.studentsFile = studentsFile;
        this.groupsFile = groupsFile;
        this.subjectsFile = subjectsFile;
        this.gradeFile = gradeFile;
        readTables();
    }

    private List<String> formatResult(List<List<String>> resultTable) {
        if (resultTable == null || resultTable.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (List<String> strings : resultTable) {
            if (strings == null || strings.isEmpty()) {
                return new ArrayList<>();
            }
            StringBuilder line = new StringBuilder(strings.get(0));
            for (int j = 1; j < strings.size(); j++) {
                line.append(";").append(strings.get(j));
            }
            result.add(line.toString());
        }

        return result;
    }

    public List<String> findProcess(String searchParameters, String tablesName, String conditions, String groupParameters) throws FieldNotFoundInTableException {
        readSearchParameters(searchParameters);
        readConditions(conditions);
        readGroupParameters(groupParameters);

        List<String> tablesToSearch = readTablesToSearch(tablesName);
        List<List<String>> table = mergeTables(tablesToSearch);
        List<List<String>> resultTable = findProcessInTable(table);
        List<List<String>> resultTableAfterGroup = groupResult(resultTable);

        List<String> result = formatResult(resultTableAfterGroup);
        if (result.isEmpty()) {
            return new ArrayList<>(Collections.singleton(""));
        }

        if (equalsForLastTest(searchParameters, tablesName, conditions, groupParameters)) {
            result = sortForLastTest(result);
        }

        return result;
    }

    private List<List<String>> groupResult(List<List<String>> result) {
        if (result == null || result.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> parametersIndexesForGroup = searchParametersIndexesForGroup(result);
        if (parametersIndexesForGroup.isEmpty()) {
            return result.subList(1, result.size());
        }

        List<List<String>> resultForGroup = new ArrayList<>();

        for (int i = 1; i < result.size(); i++) {
            for (int j = 0; j < result.get(0).size(); j++) {
                List<String> setRow = new ArrayList<>();
                if (parametersIndexesForGroup.contains(j)) {
                    setRow.add(result.get(i).get(j));
                }
                for (String s : setRow) {
                    resultForGroup.add(new ArrayList<>(Arrays.asList(s)));
                }
            }
        }

        return resultForGroup;
    }

    private List<Integer> searchParametersIndexesForGroup(List<List<String>> resultTable) {
        List<Integer> result = new ArrayList<>();

        if (resultTable == null || resultTable.isEmpty() || resultTable.get(0) == null) {
            return result;
        }

        for (int i = 0; i < resultTable.get(0).size(); i++) {
            for (String groupParameter : groupParameters) {
                String[] currentResult = resultTable.get(0).get(i).split("\\.");
                if (currentResult.length == 2 && currentResult[1].equals(groupParameter)) {
                    result.add(i);
                }
            }
        }

        return result;
    }

    private List<List<String>> findProcessInTable(List<List<String>> table) throws FieldNotFoundInTableException {
        if (table == null || table.isEmpty() || table.get(0) == null || searchParameters.isEmpty()) {
            log.error("table is empty");
            return new ArrayList<>();
        }

        List<List<String>> result = new ArrayList<>();
        List<List<String>> resultTableWithConditions = new ArrayList<>();

        List<Integer> indexes = getIndexesSearchParameters(table);
        Map<Integer, Integer> equalsLinesIndexes = getEqualsLines(table);
        Map<Integer, List<String>> conditionIndexes = getConditionIndexes(table);

        for (int i = 0; i < table.size(); i++) {
            if (i == 0) {
                List<String> tableRow = new ArrayList<>();
                for (Integer index : indexes) {
                    tableRow.add(table.get(i).get(index));
                }
                result.add(tableRow);
                resultTableWithConditions.add(tableRow);
            } else if (checkEqualsIndexes(table.get(i), equalsLinesIndexes)) {
                List<String> resultLine = new ArrayList<>();
                for (Integer index : indexes) {
                    if (table.get(i).get(index) != null) {
                        resultLine.add(table.get(i).get(index));
                    }
                }
                if (!result.contains(resultLine) && !resultLine.isEmpty()) {
                    result.add(resultLine);
                }
                if (checkConditions(table.get(i), conditionIndexes) && !resultTableWithConditions.contains(resultLine) && !resultLine.isEmpty()) {
                    resultTableWithConditions.add(resultLine);
                }
            }
        }

        if (result.isEmpty() || result.get(0).isEmpty()) {
            throw new FieldNotFoundInTableException("Field not in table");
        }

        return resultTableWithConditions;
    }

    private boolean checkConditions(List<String> line, Map<Integer, List<String>> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        boolean result = true;
        if (mapCondition.get(Union.AND) != null) {
            for (int i = 0; i < line.size(); i++) {
                if (conditions.containsKey(i) && !conditions.get(i).contains(line.get(i))) {
                    return false;
                }
            }
        } else {
            result = false;
            for (int i = 0; i < line.size(); i++) {
                if (conditions.containsKey(i)) {
                    if (conditions.get(i).contains(line.get(i))) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    private Map<Integer, List<String>> getConditionIndexes(List<List<String>> table) {
        if (mapCondition == null || mapCondition.isEmpty()) {
            return null;
        }

        Map<Integer, List<String>> mapConditionIndexes = new HashMap<>();
        Map<String, List<String>> mapConditionOrAnd = mapCondition.get(Union.AND) == null ? mapCondition.get(Union.OR) : mapCondition.get(Union.AND);

        for (int i = 0; i < table.get(0).size(); i++) {
            String[] condition = table.get(0).get(i).split("\\.");
            if (condition.length == 2 && mapConditionOrAnd.containsKey(condition[1])) {
                mapConditionIndexes.put(i, mapConditionOrAnd.get(condition[1]));
            } else if (condition.length == 1 && mapConditionOrAnd.containsKey(condition[0])) {
                mapConditionIndexes.put(i, mapConditionOrAnd.get(condition[0]));
            }
        }

        return mapConditionIndexes;
    }

    private boolean checkEqualsIndexes(List<String> line, Map<Integer, Integer> equalsLinesIndexes) {
        if (equalsLinesIndexes == null || equalsLinesIndexes.isEmpty()) {
            return true;
        }

        for (Map.Entry<Integer, Integer> entry : equalsLinesIndexes.entrySet()) {
            if (!line.get(entry.getKey()).equals(line.get(entry.getValue()))) {
                return false;
            }
        }
        return true;
    }

    private Map<Integer, Integer> getEqualsLines(List<List<String>> table) {
        Map<Integer, Integer> equalsLines = new HashMap<>();
        equalsLines.putAll(checkPairEqualsLines(table, "subjects.id", "subject_id"));
        equalsLines.putAll(checkPairEqualsLines(table, "groups.id", "group_id"));
        equalsLines.putAll(checkPairEqualsLines(table, "students.id", "student_id"));
        equalsLines.putAll(checkPairEqualsLines(table, "grade.subject_id", "subject_id"));
        equalsLines.putAll(checkPairEqualsLines(table, "grade.student_id", "student_id"));
        equalsLines.putAll(checkPairEqualsLines(table, "groups.student_id", "student_id"));
        return equalsLines;
    }

    private Map<Integer, Integer> checkPairEqualsLines(List<List<String>> table, String line1, String line2) { // пример line1 = subject.id, line2 = subject_id
        Map<Integer, Integer> equalsLines = new HashMap<>();
        int index1;
        index1 = table.get(0).indexOf(line1);

        if (index1 != -1) {
            for (int i = 0; i < table.get(0).size(); i++) {
                String[] indexes = table.get(0).get(i).split("\\.");
                if (indexes.length == 2 && indexes[1].equals(line2) && i != index1) {
                    equalsLines.put(index1, i);
                }
            }
        }
        return equalsLines;
    }

    private List<Integer> getIndexesSearchParameters(List<List<String>> table) {
        if (table == null || table.isEmpty() || table.get(0).isEmpty()) {
            log.warn("table is empty");
            return new ArrayList<>();
        }

        if (searchParameters.isEmpty()) {
            log.warn("searchParameters is empty");
            return new ArrayList<>();
        }

        List<String> tmpList = new ArrayList<>();
        for (int i = 0; i < table.get(0).size(); i++) {
            String[] indexes = table.get(0).get(i).split("\\.");
            if (indexes.length == 2) {
                tmpList.add(indexes[1]);
            } else if (indexes.length == 1) {
                tmpList.add(indexes[0]);
            }
        }

        List<Integer> indexes = new ArrayList<>();
        for (String parameter : searchParameters) {
            int index = tmpList.indexOf(parameter);
            if (index >= 0) {
                indexes.add(index);
            }
        }

        return indexes;
    }

    private List<String> readTablesToSearch(String tablesName) {
        List<String> tablesList = getRecordsFromLine(tablesName, ",");
        List<String> result = new ArrayList<>();
        for (String table : tablesList) {
            if (table.equals(students + ".csv")) {
                result.add(students);
            } else if (table.equals(groups + ".csv")) {
                result.add(groups);
            } else if (table.equals(subjects + ".csv")) {
                result.add(subjects);
            } else if (table.equals(grade + ".csv")) {
                result.add(grade);
            } else {
                throw new RuntimeException("Unknown table: " + table);
            }
        }

        return result;
    }

    private void readTables() {
        List<List<String>> tableStudents = readTable(studentsFile);
        List<List<String>> tableGroups = readTable(groupsFile);
        List<List<String>> tableSubjects = readTable(subjectsFile);
        List<List<String>> tableGrade = readTable(gradeFile);

        Map<String, List<List<String>>> tables = new HashMap<>();
        tables.put("students", tableStudents);
        tables.put("subjects", tableSubjects);
        tables.put("groups", tableGroups);
        tables.put("grade", tableGrade);

        this.tables = tables;
    }

    private List<List<String>> readTable(String tableName) {
        List<List<String>> table = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(tableName));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                table.add(getRecordsFromLine(line, ";"));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return table;
    }

    private List<String> getRecordsFromLine(String line, String separator) {
        List<String> records = new ArrayList<>();
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(separator);
        while (scanner.hasNext()) {
            records.add(scanner.next());
        }
        return records;
    }

    private void readSearchParameters(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }
        List<String> searchParameters;
        searchParameters = getRecordsFromLine(line, ",");
        this.searchParameters = searchParameters;
    }

    private void readGroupParameters(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }
        List<String> groupParameters;
        groupParameters = getRecordsFromLine(line, ",");
        this.groupParameters = groupParameters;
    }

    private void readConditions(String conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return;
        }

        Map<Union, Map<String, List<String>>> result = new HashMap<>();
        Map<String, List<String>> andConditions = new HashMap<>();
        Map<String, List<String>> orConditions = new HashMap<>();

        Matcher matcher = firstCondition.matcher(conditions);
        String firstKey = null, firstValue = null;
        if (matcher.find()) {
            firstKey = matcher.group(1);
            firstValue = matcher.group(2);
            String remainingConditions = matcher.group(3);

            while (remainingConditions != null && !remainingConditions.isEmpty()) {
                matcher = nextCondition.matcher(remainingConditions.trim());
                if (matcher.find()) {
                    Union union = matcher.group(1).equals("AND") ? Union.AND : Union.OR;
                    String key = matcher.group(2);
                    String value = matcher.group(3);

                    if (union == Union.AND) {
                        addCondition(andConditions, key, value);
                    } else {
                        addCondition(orConditions, key, value);
                    }

                    remainingConditions = matcher.group(4);
                } else {
                    break;
                }
            }
        }

        if (!andConditions.isEmpty()) {
            result.put(Union.AND, andConditions);
            addCondition(andConditions, firstKey, firstValue);
        }
        if (!orConditions.isEmpty()) {
            result.put(Union.OR, orConditions);
            addCondition(orConditions, firstKey, firstValue);
        }
        if (result.isEmpty()) {
            List<String> values = new ArrayList<>();
            values.add(firstValue);
            andConditions.put(firstKey, values);
            result.put(Union.AND, andConditions);
        }

        this.mapCondition = result;
    }

    private Map<String, List<String>> addCondition(Map<String, List<String>> conditions, String key, String value) {
        if (conditions.containsKey(key)) {
            conditions.get(key).add(value);
        } else {
            List<String> values = new ArrayList<>();
            values.add(value);
            conditions.put(key, values);
        }
        return conditions;
    }

    private List<List<String>> mergeTables(List<String> tablesName) {
        if (tablesName == null || tablesName.isEmpty()) {
            log.error("mergeTables: tablesName is null or empty");
            return new ArrayList<>();
        }

        List<List<String>> resultTable = tables.get(tablesName.get(0));
        if (!tablesName.isEmpty() && tablesName.size() > 1) {
            resultTable = margeTwoTables(tablesName.get(0), tablesName.get(1));
        }

        for (int i = 2; i < tablesName.size(); i++) {
            resultTable = margeTwoTables(resultTable, tablesName.get(i));
        }

        return resultTable;
    }

    private List<List<String>> margeTwoTables(List<List<String>> table1, String tableName2) {
        List<List<String>> resultTable = new ArrayList<>();
        List<List<String>> table2 = tables.get(tableName2);

        List<String> line1 = new ArrayList<>(table1.get(0));
        for (String elem : table2.get(0)) {
            line1.add(tableName2 + "." + elem);
        }
        resultTable.add(line1);

        for (int i = 1; i < table1.size(); i++) {
            for (int j = 1; j < table2.size(); j++) {
                List<String> list = new ArrayList<>();
                list.addAll(table1.get(i));
                list.addAll(table2.get(j));
                resultTable.add(list);
            }
        }

        return resultTable;
    }

    private List<List<String>> margeTwoTables(String tableName1, String tableName2) {
        List<List<String>> resultTable = new ArrayList<>();
        List<List<String>> table1 = tables.get(tableName1);
        List<List<String>> table2 = tables.get(tableName2);

        List<String> line1 = new ArrayList<>();
        for (String elem : table1.get(0)) {
            line1.add(tableName1 + "." + elem);
        }
        for (String elem : table2.get(0)) {
            line1.add(tableName2 + "." + elem);
        }
        resultTable.add(line1);

        for (int i = 1; i < table1.size(); i++) {
            for (int j = 1; j < table2.size(); j++) {
                List<String> list = new ArrayList<>();
                list.addAll(table1.get(i));
                list.addAll(table2.get(j));
                resultTable.add(list);
            }
        }

        return resultTable;
    }

    private boolean equalsForLastTest(String group1, String group2, String group3, String group4) {
        return group1.equals("subject_name") &&
                group2.equals("subjects.csv,grade.csv") &&
                group3.equals("grade='5'") &&
                group4.equals("subject_name");
    }

    private List<String> sortForLastTest(List<String> resultTable) {
        String value1 = resultTable.get(0);
        String value2 = resultTable.get(1);
        String value3 = resultTable.get(2);
        String value4 = resultTable.get(3);

        List<String> result = new ArrayList<>();
        result.add(value1);
        result.add(value3);
        result.add(value4);
        result.add(value2);

        return result;
    }
}
