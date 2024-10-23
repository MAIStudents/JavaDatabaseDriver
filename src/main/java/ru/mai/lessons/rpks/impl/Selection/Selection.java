package ru.mai.lessons.rpks.impl.Selection;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Tables.LoadTables;
import ru.mai.lessons.rpks.impl.Tables.Table;

import java.util.*;

public class Selection {
    private String[] columns;
    private String[] tables;
    private Map<String, List<String>> whereList;
    private String groupBy;
    private LoadTables loader;

    public Selection(String[] columns, String[] tables, Map<String, List<String>> whereList, String groupBy, LoadTables loader) {
        this.columns = columns;
        this.tables = tables;
        this.whereList = whereList;
        this.groupBy = groupBy;
        this.loader = loader;
    }

    public List<String> getSelection() throws FieldNotFoundInTableException {
        List<String> result = getData(columns, tables, whereList, groupBy);
        if (result.isEmpty()) {
            result = List.of("");
        }
        return result;
    }
    public void parseKeys(Set<String> keySet, List<List<Object>> filteredListsByWhere, List<Table> tablesWithValues,
                          Map<String, List<String>> whereList, String[] tables) throws FieldNotFoundInTableException {
        int index = 0;
        for (var key : keySet) { //key - название колонки
            List<String> values = whereList.get(key); //возможные значения
            Table tableWithRelateFieldsWithField = loader.getTableWithField(key, tables); //таблица которая содержит колонку
            if (tableWithRelateFieldsWithField == null && !key.equals("info")) {
                throw new FieldNotFoundInTableException("error");
            }
            if (tableWithRelateFieldsWithField != null) {
                filteredListsByWhere.add(new ArrayList<>());
                filteredListsByWhere.get(index).addAll(tableWithRelateFieldsWithField.getAllObjectsByField(key, values, "object"));
                tablesWithValues.add(tableWithRelateFieldsWithField);
            }
            index++;
        }
    }
    public void fillListForCollumns(Set<String> keySet, List<Table> tablesWithValues, Map<String,
            List<String>> whereList, List<List<List<Object>>> listWithValuesForCollumns, int indexAll, String column) {
        listWithValuesForCollumns.add(new ArrayList<>());
        int indexTable = 0;
        for (var key : keySet) { //[full_name, subject]
            Table table = tablesWithValues.get(indexTable);
            if (!key.equals("info")) {
                List<String> values = whereList.get(key); //[ivanov ivan, petrov kolya]
                List<Object> listObj = table.getAllObjectsByField(key, values, "object");//[student1, student2, ..]
                List<Object> list = new ArrayList<>();
                for (var each : listObj) {
                    List<Object> listVal = table.getRelateObjects(List.of(each), column, "value"); //[m8o-313, v90-454]
                    list.addAll(listVal);
                }
                listWithValuesForCollumns.get(indexAll).add(list);
            }
            indexTable++;
            if (indexTable == tablesWithValues.size()) {
                indexTable = 0;
            }
        }
    }
    public List<String> fromObjectToStringList(List<Object> list) {
        List<String> stringList = new ArrayList<>();
        for (Object obj : list) {
            stringList.add(obj.toString());
        }
        return stringList;
    }
    public List<Object> getListIntersections(List<List<Object>> data) {
        List<Object> res = new ArrayList<>();
        for (List<Object> each : data) {
            List<String> list = fromObjectToStringList(each);
            if (res.isEmpty()) {
                res.addAll(list);
            } else {
                res.retainAll(list);
            }
        }
        return res;
    }
    public void getIntersections(List<List<List<Object>>> allList, List<List<Object>> data) {
        for (List<List<Object>> valuesForWhere : allList) {
            List<Object> listIntersections = getListIntersections(valuesForWhere);
            data.add(listIntersections);
        }
    }
    public List<List<Object>> getDataWithWhere(Map<String, List<String>> whereList, String[] tables, String[] columns) throws FieldNotFoundInTableException {
        Set<String> keySet = whereList.keySet();
        List<List<Object>> data = new ArrayList<>();
        List<List<Object>> filteredListsByWhere = new ArrayList<>();
        List<Table> tablesWithValues = new ArrayList<>();
        parseKeys(keySet, filteredListsByWhere, tablesWithValues, whereList, tables);
        //filteredListsByWhere - листы со значениями соотв-щим where
        //tablesWithValues - соответствующие им таблицы для получения нужных полей
        List<List<List<Object>>> listWithValuesForColumns = new ArrayList<>(columns.length); //[ col1[  [values for where1],[values for where2]  ], col2[  ], ..      ]
        int indexAll = 0;
        for (var column : columns) {
            if (whereList.get("info").get(0).equals("AND")) {
                fillListForCollumns(keySet, tablesWithValues, whereList, listWithValuesForColumns, indexAll, column);
                indexAll++;
            } else {
                for (int i = 0; i < tablesWithValues.size(); i++) {
                    if (!tablesWithValues.get(i).getFields().contains(column)) {
                        Table table = tablesWithValues.get(i);
                        List<Object> list = table.getRelateObjects(filteredListsByWhere.get(i), column, "value");
                        data.add(list);
                    } else {
                        List<Object> list = new ArrayList<>(filteredListsByWhere.get(i));
                        data.add(list);
                    }
                }
            }
        }
        getIntersections(listWithValuesForColumns, data);
        return data;
    }
    public List<String> getRes(List<List<Object>> data, int length) {
        List<String> res = new ArrayList<>();
        int j = 0;
        while (j < data.get(0).size()) {
            StringBuilder sb = new StringBuilder();
            for (int num = 0; num < length; num++) {
                sb.append(data.get(num).get(j));
                if (num != length - 1) {sb.append(";");}
            }
            res.add(sb.toString());
            j++;
        }
        return res;
    }
    List<Object> getSetOfList(List<Object> list) {
        List<Object> res = new ArrayList<>();
        for (Object obj : list) {
            if (!res.contains(obj) && Collections.frequency(list, obj) > 1) {
                res.add(obj);
            }
        }
        return res;
    }
    public List<String> getData(String[] columns, String[] tables, Map<String, List<String>> whereList, String groupBy)
            throws FieldNotFoundInTableException {
        List<List<Object>> data = new ArrayList<>();
        if (whereList != null) {
            data = getDataWithWhere(whereList, tables, columns);
        } else {
            for (String column : columns) {
                Table table = loader.getTableWithField(column, tables);
                if (table != null) {
                    data.add(table.getField(column));
                } else {
                    throw new FieldNotFoundInTableException("error");
                }
            }
        }
        if (groupBy != null) {
            List<List<Object>> newData = new ArrayList<>();
            for (List<Object> datum : data) {
                newData.add(getSetOfList(datum));
            }
            data = newData;
        }
        return getRes(data, columns.length);
    }
    void getGroupBy(String query, StringBuilder groupBy) {
        if (!query.contains("GROUPBY=")) {
            return;
        }
        groupBy.append(query.substring(query.indexOf("GROUPBY=") + 8));
    }
}
