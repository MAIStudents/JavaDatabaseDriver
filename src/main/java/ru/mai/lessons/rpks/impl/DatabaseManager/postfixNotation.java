package ru.mai.lessons.rpks.impl.DatabaseManager;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class postfixNotation {
    private static final Map<String, Integer> precedence = new HashMap<>();

    static {
        precedence.put("OR", 1);
        precedence.put("AND", 2);
    }

    public static Deque<String> convertToRPN(String expression, Deque<String> result) throws WrongCommandFormatException {
        Deque<String> operators = new ArrayDeque<>();
        if (expression == null || expression.isEmpty()) {
            return result;
        }
        List<String> tokens = parseConditions(expression);

        for (String token : tokens) {
            Pattern pattern = Pattern.compile("[a-zA-Z_]+='[а-яА-Яa-zA-Z0-9\\s]+'");
            Matcher matcher = pattern.matcher(token);
            if (!matcher.matches()) {
                if (!token.equals("OR") && !token.equals("AND")) {
                    throw new WrongCommandFormatException("Wrong command format");
                }
            }
            if (isOperand(token)) {
                result.push(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    result.push(operators.pop());
                }
                operators.pop();
            } else {
                while (!operators.isEmpty() && !isOperand(operators.peek())  && precedence.get(token) <= precedence.get(operators.peek())) {
                    result.push(operators.pop());
                }
                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {
            result.push(operators.pop());
        }
        return result;
    }

    private static boolean isOperand(String token) {
        return !token.equals("AND") && !token.equals("OR") && !token.equals("(") && !token.equals(")");
    }

    public static List<String> parseConditions(String input) {
        List<String> conditions = new ArrayList<>();
        StringBuilder currentCondition = new StringBuilder();

        String[] tokens = input.split("\\s+");

        for (String token : tokens) {
            if (token.equals("(")) {
                conditions.add(token);
            } else if (token.equals(")")) {
                if (!currentCondition.isEmpty()) {
                    conditions.add(currentCondition.toString().trim());
                    currentCondition.setLength(0);
                }
                conditions.add(token);
            } else if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                if (!currentCondition.isEmpty()) {
                    conditions.add(currentCondition.toString().trim());
                    currentCondition.setLength(0);
                }
                conditions.add(token);
            } else {
                currentCondition.append(token).append(" ");
            }
        }
        if (!currentCondition.isEmpty()) {
            conditions.add(currentCondition.toString().trim());
        }

        return conditions;
    }



}
