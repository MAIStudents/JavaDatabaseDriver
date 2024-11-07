package ru.mai.lessons.rpks.impl.Parsing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ParseCommand {
    public static boolean checkCommand(String command) {
        Pattern pattern = Pattern.compile(
            "^SELECT=[\\w,]+\\s+" +
            "FROM=[\\w,\\.]+\\s*" +
            "(WHERE=\\(([^()=\\s]+=('[^']*'|\\d+)(\\s+(AND|OR)\\s+[^()=\\s]+=('[^']*'|\\d+))*)\\))?\\s*" +
            "(GROUPBY=[\\w,]+)?$"
        );
        return pattern.matcher(command).matches();
    }
    public static Map<String, String> parseCommand(String command) {
        Map<String, String> queryData = new LinkedHashMap<>();
        int startQueryindex = 0;
        int firstIndex = command.indexOf('=');
        int secondIndex = command.indexOf(' ');
        while (firstIndex != -1) {
            boolean hasBrackets = command.charAt(firstIndex + 1) == '(';
            String partOfQuery = command.substring(startQueryindex, firstIndex);
            firstIndex += hasBrackets ? 2 : 1;
            secondIndex = hasBrackets ? command.indexOf(')', firstIndex) : command.indexOf(' ', firstIndex);
            startQueryindex = hasBrackets ? secondIndex + 2 : secondIndex + 1;
            if (secondIndex == -1) {
                secondIndex = command.length();
            }
            queryData.put(partOfQuery, command.substring(firstIndex, secondIndex));
            firstIndex = command.indexOf('=', secondIndex);
        }
        return queryData;
    }
}
