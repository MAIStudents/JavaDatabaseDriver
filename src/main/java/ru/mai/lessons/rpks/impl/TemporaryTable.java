package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.IRequestTable;
import ru.mai.lessons.rpks.ITable;
import ru.mai.lessons.rpks.exception.AmbiguousRequestException;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.util.*;

@Slf4j
public final class TemporaryTable extends DatabaseTable implements IRequestTable {

  final Set<String> joinedTables;

//  final String tableName;
//  final Map<FieldFullName, FieldType> fields;
//  final Map<String, String> references;
//
//  List<FieldFullName> arrangedFields;
//  List<List<?>> data;

  public TemporaryTable() {
    super("Temp");
    joinedTables = new HashSet<>();
  }

  @Override
  public List<List<?>> select(List<String> fieldNames) throws FieldNotFoundInTableException {
    List<List<?>> queryResult = new ArrayList<>();
    List<Integer> fieldIndices = new ArrayList<>();

    for (String fieldName : fieldNames) {
      int idx = -1;
      for (int i = 0; i < arrangedFields.size(); ++i) {
        if (arrangedFields.get(i).fieldName().equals(fieldName)) {
          if (idx != -1) {
            throw new AmbiguousRequestException(
                    String.format("There is more than one field '%s' in the table", fieldName));
          }
          idx = i;
        }
      }
      if (idx == -1) {
        throw new FieldNotFoundInTableException(
                String.format("There is no field '%s' in the table", fieldName));
      }
      fieldIndices.add(idx);
    }

    for (List<?> record : data) {
      List<? super Object> queryRecord = new ArrayList<>();
      for (int idx : fieldIndices) {
        queryRecord.add(record.get(idx));
      }
      queryResult.add(queryRecord);
    }

    return queryResult;
  }

  @Override
  public void join(ITable table) {

    if (joinedTables.isEmpty()) {
      joinedTables.add(table.getTableName());
      joinedTables.add(table.getTableName());
      fields.putAll(table.getFields());
      references.putAll(table.getReferences());
      arrangedFields.addAll(table.getArrangedFields());
      data.addAll(table.getData());
      return;
    }

    if (joinedTables.contains(tableName)) {
      throw new IllegalArgumentException(
              String.format("Table '%s' already joined to table '%s'", tableName, this.tableName));
    }

    Map<String, String> otherReferences = table.getReferences();
    String innerConnectionTableName = null;
    String innerConnectionFieldName = references.get(table.getTableName());
    String outerConnectionFieldName = null;

    for (String tableName : otherReferences.keySet()) {
      if (joinedTables.contains(tableName)) {
        if (outerConnectionFieldName != null) {
          throw new AmbiguousRequestException(
                  String.format("There is more than one reference to joining table %s", table.getTableName()));
        }
        innerConnectionTableName = tableName;
        outerConnectionFieldName = otherReferences.get(tableName);
      }
    }

    int innerConnectionFieldIdx = arrangedFields.indexOf(
            new FieldFullName(innerConnectionTableName, innerConnectionFieldName));
    int outerConnectionFieldIdx = table.getArrangedFields().indexOf(
            new FieldFullName(table.getTableName(), outerConnectionFieldName));

    if (innerConnectionFieldIdx == -1 || outerConnectionFieldIdx == -1) {
      throw new IllegalArgumentException(
              String.format("There is no reference to table '%s'", table.getTableName()));
    }

    List<List<?>> otherData = table.getData();
    List<List<?>> newData = new ArrayList<>();

    for (List<?> oldRecord : data) {
      for (List<?> otherRecord : otherData) {
        if (oldRecord.get(innerConnectionFieldIdx).equals(otherRecord.get(outerConnectionFieldIdx))) {
          List<? super Object> newRecord = new ArrayList<>(oldRecord);
          newRecord.addAll(otherRecord);
          newData.add(newRecord);
        }
      }
    }

    joinedTables.add(table.getTableName());
    fields.putAll(table.getFields());
    references.putAll(table.getReferences());
    arrangedFields.addAll(table.getArrangedFields());
    data = newData;
  }

  @Override
  public void where() {

  }

  @Override
  public void groupBy() {

  }
}
