package ru.mai.lessons.rpks.impl.DBService;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.List;
import java.util.Map;

public interface IDBService {
  List<String> executeQuery(Map<String, String> query, Map<String, List<Map<String, String>>> tables) throws FieldNotFoundInTableException, WrongCommandFormatException;
}
