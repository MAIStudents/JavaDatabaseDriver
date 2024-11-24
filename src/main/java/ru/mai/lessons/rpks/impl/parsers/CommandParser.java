package ru.mai.lessons.rpks.impl.parsers;

import ru.mai.lessons.rpks.DataBaseParser;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.ArrayList;
import java.util.List;

public class CommandParser implements DataBaseParser {

    private static final List<String> commands = List.of("SELECT", "WHERE", "GROUPBY", "FROM");

    private enum State {
        START, IN_QUOTES, ERROR,
        CHECK_NEXT
    }

    @Override
    public List<String> parse(String input) throws WrongCommandFormatException {
        List<String> result = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        State currentState = State.START;

        for (char c : input.toCharArray()) {
            switch (currentState) {
                case CHECK_NEXT:
                    if (c == '=' || c == ',' || Character.isWhitespace(c)) {
                        throw new WrongCommandFormatException("Неверный запрос");
                    }
                    currentState = State.START;
                case START:
                    if (c == '\'') {
                        currentState = State.IN_QUOTES;
                    } else if (Character.isWhitespace(c) || c == ',' || c == '=') {
                        if (!currentToken.isEmpty()) {
                            result.add(currentToken.toString().trim());
                            currentToken.setLength(0);
                        }
                        currentState = State.CHECK_NEXT;
                    } else if (c == '(') {
                        result.add(Character.toString(c));
                        if (result.get(result.size() - 1).contains(" ")) {
                            throw new WrongCommandFormatException("Неверный запрос");
                        }
                        currentToken.setLength(0);
                    } else if (c == ')') {
                        result.add(currentToken.toString().trim());
                        if (result.get(result.size() - 1).contains(" ")) {
                            throw new WrongCommandFormatException("Неверный запрос");
                        }
                        result.add(Character.toString(c));
                        currentToken.setLength(0);
                    } else {
                        currentToken.append(c);
                    }
                    break;
                case IN_QUOTES:

                    if (c == '\'') {
                        currentState = State.START;
                        break;
                    }
                    currentToken.append(c);
                    break;
                case ERROR:
                    throw new WrongCommandFormatException("Неверный запрос");
            }
        }
        result.add(currentToken.toString().trim());
        if (result.get(result.size() - 1).isEmpty()) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    public void parseToLists(String command,
                             List<String> select,
                             List<String> from,
                             List<String> where,
                             List<String> groupBy) throws WrongCommandFormatException {
        if (command.contains("WHERE ")
                || command.contains("SELECT ")
                || command.contains("GROUPBY ")
                || command.contains("FROM ")) {
            throw new WrongCommandFormatException("Неверный запрос");
        }
        int count_commas = 0;
        List<String> parsed = parse(command);
        for (int i = 0; i < parsed.size(); ) {
            switch (parsed.get(i)) {
                case "SELECT" -> {
                    ++i;
                    while (i < parsed.size() && !commands.contains(parsed.get(i))) {
                        select.add(parsed.get(i++));
                        ++count_commas;
                    }
                    --count_commas;
                }
                case "WHERE" -> {
                    ++i;
                    if (!parsed.get(i++).equals("(")) {
                        throw new WrongCommandFormatException("Неверный запрос");
                    }
                    while (i < parsed.size() && !parsed.get(i).equals(")")) {
                        where.add(parsed.get(i++));
                    }
                    if (i >= parsed.size()) {
                        throw new WrongCommandFormatException("Неверный запрос");
                    }
                    ++i;
                }
                case "FROM" -> {
                    ++i;
                    while (i < parsed.size() && !commands.contains(parsed.get(i))) {
                        from.add(parsed.get(i++));
                        ++count_commas;
                    }
                    --count_commas;
                }
                case "GROUPBY" -> {
                    ++i;
                    boolean flag = false;
                    while (i < parsed.size() && !commands.contains(parsed.get(i))) {
                        groupBy.add(parsed.get(i++));
                        ++count_commas;
                        flag = true;
                    }
                    if (!flag) {
                        throw new WrongCommandFormatException("неверный запрос с groupby");
                    }
                    --count_commas;
                }
            }
        }
        if (count_commas != command.chars().filter(x -> x == ',').count()) {
            throw new WrongCommandFormatException("неверный запрос с запятыми");
        }

    }
}
