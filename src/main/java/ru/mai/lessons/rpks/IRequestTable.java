package ru.mai.lessons.rpks;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.util.List;

public interface IRequestTable extends ITable {
  List<List<?>> select(List<String> fieldNames) throws FieldNotFoundInTableException;
  void join(ITable table);
  void where();
  void groupBy();
}
