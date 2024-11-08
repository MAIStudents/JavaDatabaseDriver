package ru.mai.lessons.rpks.impl.command;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.List;
import java.util.Map;

public class Command {
    public final String command;
    private final String[] tables;
    private final String[] columns;
    private final String groupBy;
    private final Map<String, List<String>> where;

    public Command(String command) throws WrongCommandFormatException {
        this.command = command;

        StringBuilder groupByBuilder = new StringBuilder();
        Parser parser = new Parser(command);
        List<String[]> parsedCommand = parser.parse(groupByBuilder);
        columns = parsedCommand.get(0);
        tables = parsedCommand.get(1);
        groupBy = groupByBuilder.toString();
        where = parser.getWhere();
    }

    public String[] getTables() {
        return tables;
    }

    public String[] getColumns() {
        return columns;
    }

    public Map<String, List<String>> getWhere() {
        return where;
    }

    public String getGroupBy() {
        return groupBy;
    }
}
