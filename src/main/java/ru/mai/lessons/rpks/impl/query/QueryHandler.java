package ru.mai.lessons.rpks.impl.query;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;

public class QueryHandler {

    private final JoinHandler joinHandler = new JoinHandler();

    public List<Map<String, String>> handler(QueryParser parser, Map<String, List<Map<String, String>>> tables)
            throws WrongCommandFormatException, FieldNotFoundInTableException {
        String select = parser.getQueryParts("SELECT");
        String from = parser.getQueryParts("FROM");
        String where = parser.getOptionalQueryParts("WHERE").orElse(null);
        String groupBy = parser.getOptionalQueryParts("GROUPBY").orElse(null);
        List<Map<String, String>> result = joinHandler.joinTables(from.split(","), tables);
        if (where != null) {
            result = processWhere(result, where);
        }
        if (groupBy != null) {
            result = processGroupBy(result, groupBy);
        }
        return processSelect(result, select);
    }

    private List<Map<String, String>> processWhere(List<Map<String, String>> rows, String queryPart) {
        List<Map<String, String>> resultRows = new ArrayList<>();
        String[] ConditionsOfOn = queryPart.trim()
                .replaceFirst("^\\(", "")
                .replaceFirst("\\)$", "")
                .split("\\s+OR\\s+");
        for (Map<String, String> row : rows) {
            boolean foundOr = false;
            for (String conditionOfOn : ConditionsOfOn) {
                boolean foundAnd = true;
                String[] conditionsOfAnd = conditionOfOn.split("\\s+AND\\s+");
                for (String conditionOfAnd : conditionsOfAnd) {
                    String[] parts = conditionOfAnd.split("=");
                    String columnName = parts[0].trim();
                    String expectedValue = parts[1].trim().replace("'", "");
                    if (!row.containsKey(columnName) || !row.get(columnName).equals(expectedValue)) {
                        foundAnd = false;
                    }
                }
                foundOr = foundOr || foundAnd;
            }
            if (foundOr) {
                resultRows.add(row);
            }
        }
        return resultRows;
    }

    private List<Map<String, String>> processGroupBy(List<Map<String, String>> rows, String queryPart) {
        Map<String, Map<String, String>> resultRows = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String column = row.get(queryPart);
            resultRows.putIfAbsent(column, row);
        }
        return new ArrayList<>(resultRows.values());
    }

    private List<Map<String, String>> processSelect(List<Map<String, String>> rows, String queryPart)
            throws FieldNotFoundInTableException {
        List<Map<String, String>> resultRows = new ArrayList<>();
        String[] сolumns = queryPart.split(",");
        for (Map<String, String> row : rows) {
            Map<String, String> selectedRow = new LinkedHashMap<>();
            for (String column : сolumns) {
                column = column.trim();
                if (!row.containsKey(column)) {
                    throw new FieldNotFoundInTableException("Column <" + column + "> not found in row.");
                }
                selectedRow.put(column, row.get(column));
            }
            resultRows.add(selectedRow);
        }
        return resultRows;
    }
}
