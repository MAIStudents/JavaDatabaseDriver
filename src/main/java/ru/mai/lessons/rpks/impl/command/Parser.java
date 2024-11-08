package ru.mai.lessons.rpks.impl.command;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final String command;
    private final Map<String, List<String>> where = new HashMap<>();

    public Parser(String command) {
        this.command = command;
    }

    public List<String[]> parse(StringBuilder groupBy) throws WrongCommandFormatException {
        Pattern pattern = Pattern.compile("SELECT=(.+?)\\s+FROM=([a-zA-z._,]+?)(\\s+WHERE=\\((.+?)\\))?(\\s+GROUPBY=(.+))?$");
        List<String[]> parsedCommand = new ArrayList<>();
        Matcher matcher = pattern.matcher(command);
        if (matcher.matches()) {
            parsedCommand.add(matcher.group(1).split(","));
            parsedCommand.add(matcher.group(2).split(","));
            if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
                addWhere(matcher.group(4));
            }
            if (matcher.group(5) != null) {
                groupBy.append(matcher.group(6));
            }
        } else {
            throw new WrongCommandFormatException("Wrong command format");
        }
        return parsedCommand;
    }

    private void addWhere(String conditions) throws WrongCommandFormatException {
        validateWhere(conditions);

        if (!conditions.contains("AND") && !conditions.contains("OR")) {
            where.put("info", List.of("-"));
            String name = conditions.substring(0, conditions.indexOf("="));
            String answer = conditions.substring(conditions.indexOf("'") + 1, conditions.lastIndexOf("'"));
            if (where.containsKey(name)) {
                where.get(name).add(answer);
            } else {
                where.put(name, List.of(answer));
            }
        } else if (!conditions.contains("AND") && conditions.contains("OR")) {
            addCondition("OR", conditions);
        } else if (conditions.contains("AND") && !conditions.contains("OR")) {
            addCondition("AND", conditions);
        }
    }

    private void validateWhere(String conditions) throws WrongCommandFormatException {
        Pattern conditionPattern = Pattern.compile("([a-zA-Z_]+)=('.*?')");
        Matcher matcher = conditionPattern.matcher(conditions);

        if (!matcher.matches()) {
            throw new WrongCommandFormatException("Wrong command format");
        }
    }

    public void addCondition(String operator, String substring) {
        where.put("info", List.of(operator));
        String[] commandSplit = substring.split(" " + operator + " ");
        for (String split : commandSplit) {
            String nameColon = split.substring(0, split.indexOf("="));
            String answer = split.substring(split.indexOf("'") + 1, split.lastIndexOf("'"));
            if (where.containsKey(nameColon)) {
                where.get(nameColon).add(answer);
            } else {
                List<String> list = new ArrayList<>();
                list.add(answer);
                where.put(nameColon, list);
            }
        }
    }

    public Map<String, List<String>> getWhere() {
        return where;
    }
}
