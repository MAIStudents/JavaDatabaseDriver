package ru.mai.lessons.rpks.impl.Parser;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;
import java.util.regex.*;


public class QueryParser {
    public static Query parse(String input) throws WrongCommandFormatException {

        Query query = new Query();
        query.originalInput = input;
        query.selectColumns = new ArrayList<>();
        query.fromFiles = new ArrayList<>();
        query.whereConditionNodes = new ArrayList<>();
        query.groupByColumn = null;

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

        String regex = "(\\w+)=(['\"])(.*?)\\2|\\b(AND|OR)\\b|\\b(\\w+)=([^\\s]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(conditions);

        while (matcher.find()) {
            if (matcher.group(1) != null && matcher.group(3) != null) {
                output.add(new ConditionNode(matcher.group(1), matcher.group(3)));
            } else if (matcher.group(1) != null && matcher.group(6) != null) {
                output.add(new ConditionNode(matcher.group(1), matcher.group(6)));
            } else if (matcher.group(4) != null) {
                output.add(new ConditionNode(matcher.group(4)));
            }
        }

        return output;
    }

    public static ConditionNode buildConditionTree(List<ConditionNode> nodes) {
        if (nodes.size() == 1) {
            return nodes.get(0);
        }

        int index = findLowestPrecedenceOperator(nodes);

        ConditionNode root = nodes.get(index);
        root.left = buildConditionTree(nodes.subList(0, index));
        root.right = buildConditionTree(nodes.subList(index + 1, nodes.size()));

        return root;
    }

    private static int findLowestPrecedenceOperator(List<ConditionNode> nodes) {
        int minPrecedence = Integer.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < nodes.size(); i++) {
            ConditionNode node = nodes.get(i);
            if (node.type == ConditionNode.NodeType.OPERATOR) {
                int precedence = precedence(node.operator);
                if (precedence < minPrecedence) {
                    minPrecedence = precedence;
                    index = i;
                }
            }
        }
        return index;
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
            throw new WrongCommandFormatException("Параметр SELECT обязателен");
        }
        if (query.fromFiles.isEmpty()) {
            throw new WrongCommandFormatException("Параметр FROM обязателен");
        }
        if (query.groupByColumn != null && query.groupByColumn.isEmpty()) {
            throw new WrongCommandFormatException("GROUPBY указан, но не содержит названия колонки");
        }
        if (query.groupByColumn == null && inputContainsGroupBy(query)) {
            throw new WrongCommandFormatException("GROUPBY указан без значения");
        }
        if (query.originalInput.contains("==") || query.originalInput.contains("WHERE ")) {
            throw new WrongCommandFormatException("Использование оператора '==' недопустимо. Используйте '='.");
        }

        String wherePattern = "WHERE=\\(([^\\)]*)\\)";
        String whereMatch = extractSingleValue(query.originalInput, wherePattern);

        if (query.originalInput.contains("WHERE=()") ||
                (query.originalInput.contains("WHERE") && !query.originalInput.contains("WHERE=("))) {
            throw new WrongCommandFormatException("Пустое выражение WHERE (скобки без содержимого).");
        }

        if (whereMatch != null) {
            if (whereMatch.trim().isEmpty()) {
                throw new WrongCommandFormatException("В выражении WHERE отсутствует условие.");
            }

            if (!whereMatch.contains("=")) {
                throw new WrongCommandFormatException("В выражении WHERE отсутствует оператор '='.");
            }
        }
    }

    private static boolean inputContainsGroupBy(Query query) {
        return query.originalInput.contains("GROUPBY");
    }
}
