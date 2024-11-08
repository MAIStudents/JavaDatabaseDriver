package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.command.Command;
import ru.mai.lessons.rpks.impl.tables.Table;

import java.util.*;

public class Handler {
    private final DataBase dataBase;
    private final Command command;
    private final boolean hasConditions;
    private final boolean hasGroupBy;

    public Handler(DataBase dataBase, Command command) {
        this.dataBase = dataBase;
        this.command = command;
        hasConditions = !command.getWhere().isEmpty();
        hasGroupBy = !command.getGroupBy().isEmpty();
    }

    public List<String> handle() throws FieldNotFoundInTableException {
        List<List<Object>> data = hasConditions ? handleWithWhere() : handleWithoutWhere();
        if (hasGroupBy) {
            data = applyGroupBy(data);
        }
        return formatResult(data);
    }

    public List<String> formatResult(List<List<Object>> data) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < data.get(0).size(); i++) {
            StringJoiner joiner = new StringJoiner(";");
            for (int j = 0; j < command.getColumns().length; j++) {
                joiner.add(data.get(j).get(i).toString());
            }
            result.add(joiner.toString());
        }

        if (result.isEmpty()) {
            result.add("");
        }

        return result;
    }

    public List<List<Object>> handleWithWhere() throws FieldNotFoundInTableException {
        List<List<Object>> data = new ArrayList<>();
        List<Table> tables = new ArrayList<>();
        Set<String> keys = command.getWhere().keySet();
        List<List<Object>> filtered = parseKeys(keys, tables);
        List<List<List<Object>>> list = new ArrayList<>(command.getColumns().length);

        int index = 0;
        for (String column : command.getColumns()) {
            if (isAndCondition()) {
                System.out.println(list);
                handleAndCondition(tables, keys, list, index, column);
                ++index;
            } else {
                data.addAll(handleOrCondition(tables, filtered, column));
            }
        }

        combineColumnsValues(data, list);
        return data;
    }

    private boolean isAndCondition() {
        return "AND".equals(command.getWhere().get("info").get(0));
    }

    private void handleAndCondition(List<Table> tables, Set<String> keys,
                                    List<List<List<Object>>> list, int index, String column) throws FieldNotFoundInTableException {
        list.add(new ArrayList<>());
        int tableIndex = 0;
        for (String key : keys) {
            Table table = tables.get(tableIndex);
            if (!key.equals("info")) {
                List<String> values = command.getWhere().get(key);
                List<Object> objectList = table.getObjectsByField(key, values);
                List<Object> newList = new ArrayList<>();
                for (Object object : objectList) {
                    newList.addAll(dataBase.getRelateValues(table.getTableName(), List.of(object), column));
                }
                list.get(index).add(newList);
            }
            tableIndex = (tableIndex + 1) % tables.size();
        }
    }

    private List<List<Object>> handleOrCondition(List<Table> tables, List<List<Object>> filtered, String column) throws FieldNotFoundInTableException {
        List<List<Object>> data = new ArrayList<>();
        for (int i = 0; i < tables.size(); ++i) {
            if (!tables.get(i).getFieldNames().contains(column)) {
                data.add(dataBase.getRelateValues(tables.get(i).getTableName(), filtered.get(i), column));
            } else {
                data.add(new ArrayList<>(filtered.get(i)));
            }
        }
        return data;
    }

    private void combineColumnsValues(List<List<Object>> data, List<List<List<Object>>> list) {
        for (List<List<Object>> values : list) {
            List<Object> newList = new ArrayList<>();
            for (List<Object> value : values) {
                List<String> stringList = value.stream().map(Object::toString).toList();
                if (newList.isEmpty()) {
                    newList.addAll(stringList);
                } else {
                    newList.retainAll(stringList);
                }
            }
            data.add(newList);
        }
    }

    private List<List<Object>> parseKeys(Set<String> keys, List<Table> tables) throws FieldNotFoundInTableException {
        List<List<Object>> result = new ArrayList<>();
        for (String key : keys) {
            List<String> values = command.getWhere().get(key);
            Table tableWithField = dataBase.getTableWithField(key, command.getTables());
            if (tableWithField == null && !key.equals("info")) {
                throw new FieldNotFoundInTableException("Field not found");
            }
            if (tableWithField != null) {
                result.add(new ArrayList<>(tableWithField.getObjectsByField(key, values)));
                tables.add(tableWithField);
            }
        }
        return result;
    }

    public List<List<Object>> handleWithoutWhere() throws FieldNotFoundInTableException {
        List<List<Object>> data = new ArrayList<>();
        for (String column : command.getColumns()) {
            Table table = dataBase.getTableWithField(column, command.getTables());
            if (table != null) {
                data.add(table.getField(column));
            } else {
                throw new FieldNotFoundInTableException("Field not found in table");
            }
        }
        return data;
    }

    public List<List<Object>> applyGroupBy(List<List<Object>> data) {
        List<List<Object>> newData = new ArrayList<>();
        for (List<Object> datum : data) {
            newData.add(removeDuplicates(datum));
        }
        return newData;
    }

    List<Object> removeDuplicates(List<Object> list) {
        List<Object> result = new ArrayList<>();
        for (Object object : list) {
            if (!result.contains(object) && Collections.frequency(list, object) > 1) {
                result.add(object);
            }
        }
        return result;
    }
}
