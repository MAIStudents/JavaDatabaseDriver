package ru.mai.lessons.rpks.impl.databaseManager;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {
    public List<String[]> parseRequest(String request, Deque<String> whereDeque, StringBuilder groupBy) throws WrongCommandFormatException {
        Pattern pattern = Pattern.compile("SELECT=(.+?)\\s+FROM=([a-zA-z._,]+?)(\\s+WHERE=\\((.+?)\\))?(\\s+GROUPBY=(.+))?$");
        List<String[]> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            result.add(matcher.group(1).split(","));
            result.add(matcher.group(2).split(","));
            if (matcher.group(3) != null) {
                PostfixNotation.convertToRPN(matcher.group(4), whereDeque);
            }
            if (matcher.group(5) != null) {
                groupBy.append(matcher.group(6));
            }
        } else {
            throw new WrongCommandFormatException("Command syntax error");
        }
        return result;
    }
}
