package ru.mai.lessons.rpks.impl;

import lombok.Getter;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Database {
    @Getter
    private List<String> columnNames;
    @Getter
    private List<List<String>> data;
    @Getter
    private String name = "";

    public Database() {
        columnNames = new ArrayList<>();
        data = new ArrayList<>();
    }

    public Database(BufferedReader in, String name) throws IOException {
        columnNames = new ArrayList<>(Arrays.asList(in.readLine().split(";")));
        data = new ArrayList<>();
        this.name = name;

        String nextLine = in.readLine();

        while (nextLine != null) {

            data.add(Arrays.asList(nextLine.split(";")));

            nextLine = in.readLine();
        }
    }

    public Database(Database other) {
        name = other.name;
        data = new ArrayList<>();
        columnNames = new ArrayList<>();

        data.addAll(other.data);
        columnNames.addAll(other.columnNames);
    }

    public List<String> answerToQuery(Query q) throws FieldNotFoundInTableException {

        Database db = new Database(this);

        db.executeWhere(q);
        db.executeGroupBy(q);
        db.executeSelect(q);

        return db.convertToOutputFormat();
    }

    private List<String> convertToOutputFormat() {
        List<String> res = new ArrayList<>();

        for(List<String> line : data) {
            StringBuilder sb = new StringBuilder();

            boolean isFirst = true;

            for(String elem : line) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(";");
                }
                sb.append(elem);
            }

            res.add(sb.toString());
        }

        return res;
    }

    private void executeWhere(Query q) throws FieldNotFoundInTableException {
        if (q.getWhereExpression().isEmpty()) {
            return;
        }

        List<List<String>> newData = new ArrayList<>();

        for (List<String> line : data) {
            if (checkLine(q, line)) {
                newData.add(line);
            }
        }

        data = newData;
    }

    private boolean checkLine(Query q, List<String> line) throws FieldNotFoundInTableException {
        boolean res = true;

        for (int i = 0; i < q.getWhereExpression().size(); ++i) {

            Query.ConditionalExpressionData expr = q.getWhereExpression().get(i);

            int index = searchColumnIndex(expr.getColumnName());

            if (expr.getIsAndWithPrevious()) {
                res = res && (line.get(index).equals(expr.getDataValue()));
            } else {
                res = res || (line.get(index).equals(expr.getDataValue()));
            }
        }

        return res;
    }

    private void sortBy(String columnName) throws FieldNotFoundInTableException {
        int primaryIndex = searchColumnIndex(columnName);

        data.sort((lhs, rhs) -> {
            String primaryValue1 = null;
            String primaryValue2 = null;

            primaryValue1 = lhs.get(primaryIndex);
            primaryValue2 = rhs.get(primaryIndex);

            return primaryValue1.compareTo(primaryValue2);

        });
    }

    private void executeGroupBy(Query q) throws FieldNotFoundInTableException {
        if (q.getGroupByColumn() == null) {
            return;
        }

        Database res = new Database();
        res.columnNames.addAll(columnNames);

        if (columnNames.contains("student_id")) {
            this.sortBy("student_id");
        } else if (columnNames.contains("id")) {
            this.sortBy("id");
        }


        int index = searchColumnIndex(q.getGroupByColumn());

        Set<String> seenValues = new HashSet<>();

        for (List<String> line : data) {
            String val = line.get(index);

            if (!seenValues.contains(val)) {
                res.data.add(line);
                seenValues.add(val);
            }
        }

        this.columnNames = res.columnNames;
        this.data = res.data;
    }

    private void executeSelect(Query q) throws FieldNotFoundInTableException {
        List<Integer> indexes = new ArrayList<>(q.getSelectColumns().size());

        for(String name : q.getSelectColumns()) {
            indexes.add(searchColumnIndex(name));
        }

        for(int i = 0; i < data.size(); ++i) {
            List<String> old = data.get(i);
            List<String> newVal = new ArrayList<>();

            for(int ind : indexes) {
                newVal.add(old.get(ind));
            }

            data.set(i, newVal);
        }
    }

    public Database innerJoinWith(String thisColumnName, Database otherDB, String otherColumnName) throws FieldNotFoundInTableException {
        Database res = new Database();

        res.columnNames.addAll(columnNames);
        res.columnNames.addAll(otherDB.columnNames);

        int index = searchColumnIndex(thisColumnName);

        for(List<String> thisLine : data) {
            String elem = thisLine.get(index);
            Database db = otherDB.searchByPredicate(otherColumnName, elem::equals);

            for(List<String> otherLine : db.data) {
                res.data.add(mergeLines(thisLine, otherLine));
            }
        }

        return res;
    }

    private static List<String> mergeLines(List<String> lhs, List<String> rhs) {
        List<String> res = new ArrayList<>();
        res.addAll(lhs);
        res.addAll(rhs);

        return res;
    }

    private int searchColumnIndex(String columnName) throws FieldNotFoundInTableException {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equals(columnName)) {
                return i;
            }
        }
        throw new FieldNotFoundInTableException(columnName);
    }

    private Database searchByPredicate(String columnName, IPredicate<String> pred) throws FieldNotFoundInTableException {
        int index = searchColumnIndex(columnName);

        Database res = new Database();
        res.columnNames.addAll(columnNames);

        for (List<String> line : data) {
            if (pred.check(line.get(index))) {
                res.data.add(new ArrayList<>(line));
            }
        }

        return res;
    }
}
