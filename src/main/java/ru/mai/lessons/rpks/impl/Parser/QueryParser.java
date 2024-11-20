package ru.mai.lessons.rpks.impl.Parser;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;
import java.util.regex.*;

import ru.mai.lessons.rpks.impl.Parser.Query;
import ru.mai.lessons.rpks.impl.Parser.ConditionNode;


public class QueryParser {
    public static Query parse(String input) throws WrongCommandFormatException {
        Query query = new Query();
        query.originalInput = input;
        query.selectColumns = new ArrayList<>();
        query.fromFiles = new ArrayList<>();
        query.whereConditionNodes = new ArrayList<>();
        query.groupByColumn = null;

        // Регулярные выражения для извлечения частей запроса
        String selectPattern = "SELECT=([^\\s]+)";
        String fromPattern = "FROM=([^\\s]+)";
        String wherePattern = "WHERE=\\(([^\\)]+)\\)";
        String groupByPattern = "GROUPBY=([^\\s]+)";

        query.selectColumns = extractValues(input, selectPattern);
        query.fromFiles = extractValues(input, fromPattern);

        String whereMatch = extractSingleValue(input, wherePattern);
        if (whereMatch != null) {
            query.whereConditionNodes = parseConditions(whereMatch);
        }

        query.groupByColumn = extractSingleValue(input, groupByPattern);

        validateQuery(query);
        return query;
    }


    private static List<String> extractValues(String input, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        if (m.find()) {
            return Arrays.asList(m.group(1).split(","));
        }
        return new ArrayList<>();
    }

    private static String extractSingleValue(String input, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static List<ConditionNode> parseConditions(String conditions) {
        List<ConditionNode> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>(); // Стек операторов

        String regex = "(\\w+)=([^\\s]+)|\\b(AND|OR)\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(conditions);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Условие вида column=value
                output.add(new ConditionNode(matcher.group(1), matcher.group(2)));
            } else if (matcher.group(3) != null) {
                // Оператор AND/OR
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(matcher.group(3))) {
                    output.add(new ConditionNode(operators.pop()));
                }
                operators.push(matcher.group(3));
            }
        }

        // Добавляем оставшиеся операторы из стека
        while (!operators.isEmpty()) {
            output.add(new ConditionNode(operators.pop()));
        }

        return output;
    }

    private static int precedence(String operator) {
        return switch (operator) {
            case "AND" -> 2;
            case "OR" -> 1;
            default -> 0;
        };
    }



    private static void validateQuery(Query query) throws WrongCommandFormatException {
        if (query.selectColumns.isEmpty()) {
            throw new WrongCommandFormatException("Параметр SELECT обязателен!");
        }
        if (query.fromFiles.isEmpty()) {
            throw new WrongCommandFormatException("Параметр FROM обязателен!");
        }
        if (query.groupByColumn != null && query.groupByColumn.isEmpty()) {
            throw new WrongCommandFormatException("GROUPBY указан, но не содержит названия колонки!");
        }
        if (query.groupByColumn == null && inputContainsGroupBy(query)) {
            throw new WrongCommandFormatException("GROUPBY указан без значения!");
        }

    }
    private static boolean inputContainsGroupBy(Query query) {
        return query.originalInput.contains("GROUPBY");
    }
}
