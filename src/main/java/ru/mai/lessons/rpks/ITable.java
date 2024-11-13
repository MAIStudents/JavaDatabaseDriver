package ru.mai.lessons.rpks;

import ru.mai.lessons.rpks.enums.FieldType;
import ru.mai.lessons.rpks.impl.FieldFullName;

import java.util.List;
import java.util.Map;

public interface ITable {
  String getTableName();
  Map<FieldFullName, FieldType> getFields();
  Map<String, String> getReferences();
  List<FieldFullName> getArrangedFields();
  List<List<?>> getData();

  void setReference(String field, String referencedTable);
}
