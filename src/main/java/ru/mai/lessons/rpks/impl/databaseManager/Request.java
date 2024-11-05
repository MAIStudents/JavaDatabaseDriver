package ru.mai.lessons.rpks.impl.databaseManager;

import lombok.Getter;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.Serializable;
import java.util.*;

@Getter
public class Request implements Serializable {
    public String requestName;
    private final String[] tables;
    private final String[] columns;
    private final String groupBy;
    private Deque<String> whereList = new ArrayDeque<>();
    long requestDate;
    List<String> result;


    public Request(String requestName, String prefix) throws WrongCommandFormatException {
        this.requestName = requestName;
        requestDate = new Date().getTime();
        CommandParser parser = new CommandParser();
        StringBuilder groupBySb = new StringBuilder();
        List<String[]> parseList = parser.parseRequest(requestName, whereList, groupBySb);
        if (whereList.isEmpty()) {
            whereList = null;
        }
        columns = parseList.get(0);

        tables = parseList.get(1);
        for (int i = 0; i < tables.length; i++) {
            tables[i] = prefix + tables[i];
        }
        if (Arrays.toString(tables).equals("[]")) {
            throw new WrongCommandFormatException("Wrong format");
        }
        groupBy = groupBySb.isEmpty() ? null : groupBySb.toString();
    }
}
