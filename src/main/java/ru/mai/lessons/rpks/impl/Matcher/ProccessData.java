package ru.mai.lessons.rpks.impl.Matcher;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProccessData {
    List<String> keyWords = List.of(new String[]{"SELECT=", "FROM=", "WHERE=", "GROUPBY="});

    private String query;
    private String[] columns;
    private String[] tables;
    private Map<String, List<String>> whereList;
    private String groupBy;

    public ProccessData(String query) {
        this.query = query;
    }


    public void parse() throws WrongCommandFormatException {
        whereList = new HashMap<>();
        StringBuilder groupBySb = new StringBuilder();
        List<String[]> parseList = parseQuery(query, whereList, groupBySb);
        if (whereList.isEmpty()) {
            whereList = null;
        }
        columns = parseList.get(0);
        tables = parseList.get(1);
        groupBy =  (groupBySb.isEmpty())  ? null : groupBySb.toString();
    }

    public String getGroupBy() {
        return groupBy;
    }

    public Map<String, List<String>> getWhereList() {
        return whereList;
    }

    public String[] getTables() {
        return tables;
    }

    public String[] getColumns() {
        return columns;
    }

    public List<String[]> parseQuery(String query, Map<String, List<String>> whereList, StringBuilder groupBy) throws WrongCommandFormatException {
        validateQuery(query);
        List<String[]> res = new ArrayList<>();
        String[] columns = getColumns(query);
        String[] tables = getTables(query);
        getWhere(query, whereList);
        getGroupBy(query, groupBy);
        if (tables == null || columns == null) {
            throw new WrongCommandFormatException("command syntax error");
        }
        res.add(columns);
        res.add(tables);
        return res;
    }
    void getGroupBy(String query, StringBuilder groupBy) {
        if (!query.contains("GROUPBY=")) {
            return;
        }
        groupBy.append(query.substring(query.indexOf("GROUPBY=") + 8));
    }
    String[] getColumns(String query) {
        String columns = query.substring("SELECT=".length(), query.indexOf("FROM=") - 1);
        return (columns.isEmpty())  ? null : columns.split(",");
    }
    String[] getTables(String query) {
        int endIndex;
        if (query.contains("WHERE=")) {
            endIndex = query.indexOf(" WHERE");
        }
        else if (query.contains("GROUPBY=")) {
            endIndex = query.indexOf(" GROUPBY");
        }
        else endIndex = query.length();
        String tempQuery = query.substring(query.indexOf("FROM=") + 5, endIndex);
        if (tempQuery.length() < 2) return null;
        String[] tempQuerySplit = tempQuery.split(",");
        List<String> tables = new ArrayList<>();
        int i = 0;
        while (i < tempQuerySplit.length && !keyWords.contains(tempQuerySplit[i])) {
            tables.add(tempQuerySplit[i]);
            i++;
        }
        String[] res = new String[tables.size()];
        return tables.toArray(res);
    }
    void getWhere(String query, Map<String, List<String>> res) {
        if (!query.contains("WHERE=")) {
            return;
        }
        String querySubstring = query.substring(query.indexOf("WHERE=(") + "WHERE=(".length(), query.indexOf(")"));
        if (!querySubstring.contains("AND") && !querySubstring.contains("OR")) {
            res.put("info", List.of("-"));
            String nameColon = querySubstring.substring(0, querySubstring.indexOf("="));
            String answer = querySubstring.substring(querySubstring.indexOf("'") + 1, querySubstring.lastIndexOf("'"));
            if (res.containsKey(nameColon)) {
                res.get(nameColon).add(answer);
            } else {
                res.put(nameColon, List.of(answer));
            }
        }
        else if (!querySubstring.contains("AND") && querySubstring.contains("OR")) {
            res.put("info", List.of("OR"));
            String[] querySplitOr = querySubstring.split(" OR ");
            getMapForWhere(res, querySplitOr);
        } else if (querySubstring.contains("AND") && !querySubstring.contains("OR")) {
            res.put("info", List.of("AND"));
            String[] querySplitAnd = querySubstring.split(" AND ");
            getMapForWhere(res, querySplitAnd);
        }
        else if (querySubstring.contains("AND") && querySubstring.contains("OR")) {
        }
    }

    public void getMapForWhere(Map<String, List<String>> res, String[] querySplit) {
        for (var each : querySplit) {
            String nameColon = each.substring(0, each.indexOf("="));
            String answer = each.substring(each.indexOf("'") + 1, each.lastIndexOf("'"));
            if (res.containsKey(nameColon)) {
                res.get(nameColon).add(answer);
            } else {
                List<String> list = new ArrayList<>();
                list.add(answer);
                res.put(nameColon, list);
            }
        }
    }

    void validateQuery(String query) throws WrongCommandFormatException {
        // Регулярные выражения для частей команды
        String selectPattern = "SELECT=([a-zA-Z_]+)(,[a-zA-Z_]+)*";
        String fromPattern = "FROM=([a-zA-Z0-9_.]+)(,[a-zA-Z0-9_.]+)*";
        String wherePattern = "(WHERE=\\(\\s*((([a-zA-Z_]+=[^()=]+)(\\s+(AND|OR)\\s+([a-zA-Z_]+=[^()=]+))*)?)\\s*\\))?";
        String groupByPattern = "(GROUPBY=[a-zA-Z_]+)?";

        // Формирование общего шаблона для проверки
        String fullPattern = String.format(
                "^%s\\s+%s\\s*%s\\s*%s?$",
                selectPattern, fromPattern, wherePattern, groupByPattern
        );

        Pattern pattern = Pattern.compile(fullPattern);
        Matcher matcher = pattern.matcher(query);
        if (!matcher.matches()) {
            throw new WrongCommandFormatException("error");
        }
    }
}
