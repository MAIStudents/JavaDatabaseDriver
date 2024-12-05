package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class DatabaseDriver implements IDatabaseDriver {
  /**
   * Словарь, который содержит в качестве ключа - запрос, значения - результат
   */
  private final Map<String, List<String>> resultCache = new HashMap<>();

  /**
   * Список, который содержит имена переданных файлов данных
   */
  private List<String> fromFiles = new ArrayList<>();

  @Override
  public List<String> find(final String studentsFile, final String groupsFile,
                           final String subjectsFile, final String gradeFile,
                           final String command)
      throws FieldNotFoundInTableException, WrongCommandFormatException {
    Checker.validateCommand(command);

    String normalizedCommand = normalizeCommand(command);

    if (resultCache.containsKey(normalizedCommand)) {
      return resultCache.get(normalizedCommand);
    }

    final String fieldsPart = Parser.extractFieldsPart(command);
    final String fromPart = Parser.extractFromPart(command);
    final String wherePart = Parser.extractWherePart(command);
    final String groupByPart = Parser.extractGroupByPart(command);

    final List<String> selectedFields = Arrays.asList(fieldsPart.split(","));
    fromFiles = Arrays.asList(fromPart.split(","));

    final Map<String, List<Map<String, String>>> data = DataLoader.loadData(fromFiles);
    Checker.checkFieldsExistence(selectedFields, data);

    List<Map<String, String>> filteredData = filterData(data, wherePart);

    if (groupByPart != null) {
      filteredData = groupData(filteredData, groupByPart);
    }

    final List<String> result = formatResult(filteredData, selectedFields);
    resultCache.put(normalizedCommand, result);

    return result.isEmpty() ? Collections.singletonList("") : result;
  }

  /**
   * Нормализует строку.
   *
   * @param command команда для нормализации
   * @return нормализованная строка
   */
  private String normalizeCommand(final String command) {
    return command.trim().replaceAll("\\s+", " ");
  }

  /**
   * Соединяет две таблицы.
   *
   * @param firstTableName названия первой таблицы
   * @param firstTable  первая таблица
   * @param secondTableName  название второй таблицы
   * @param secondTable вторая таблица
   */
  private List<Map<String, String>> mergeTables(Set<String> firstTableName,
                                                List<Map<String, String>> firstTable,
                                                String secondTableName,
                                                final List<Map<String, String>> secondTable) {
    boolean ableToMerge = false;
    boolean rightTableMergeWithLeft = true;
    String foreignKey = "";

    Set<String> firstTableKeys = firstTable.get(0).keySet();
    Set<String> secondTableKeys = secondTable.get(0).keySet();

    for (String key : firstTableKeys) {
      if (key.endsWith("_id") && secondTableName.contains(key.substring(0, key.length() - 3))) {
        ableToMerge = true;
        foreignKey = key;
        break;
      }
    }

    if (!ableToMerge) {
      for (String key : secondTableKeys) {
        if (key.endsWith("_id") && firstTableName.contains(key.substring(0, key.length() - 3))) {
          ableToMerge = true;
          rightTableMergeWithLeft = false;
          foreignKey = key;
          break;
        }
      }

      if (!ableToMerge) {
        return null;
      }
    }

    if (rightTableMergeWithLeft) {
      linkedTablesByID(firstTable, secondTable, foreignKey);
      return firstTable;
    }

    linkedTablesByID(secondTable, firstTable, foreignKey);
    return secondTable;
  }

  /**
   * Соединяет две таблицы по внешнему ключу.
   *
   * @param firstTable  первая таблица
   * @param secondTable вторая таблица
   * @param foreignKey  внешний ключ
   */
  private void linkedTablesByID(final List<Map<String, String>> firstTable,
                                final List<Map<String, String>> secondTable,
                                final String foreignKey
  ) {
    for (Map<String, String> firstTableRow : firstTable) {
      for (Map<String, String> secondTableRow : secondTable) {
        if (firstTableRow.get(foreignKey).equals(secondTableRow.get("id"))) {
          firstTableRow.putAll(secondTableRow);
        }
      }
    }
  }

  /**
   * Фильтрует данные на основе условий WHERE.
   *
   * @param data  словарь с данными
   * @param where условия WHERE
   * @return отфильтрованный список записей
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private List<Map<String, String>> filterData(
      final Map<String, List<Map<String, String>>> data,
      final String where
  )
      throws WrongCommandFormatException {
    final Set<String> linkedTablesName = new HashSet<>();
    linkedTablesName.add(fromFiles.get(0).substring(19, fromFiles.get(0).length() - 5));

    List<Map<String, String>> linkedTable
        = new ArrayList<>(List.copyOf(data.get(fromFiles.get(0))));

    final List<Map<String, String>> result = new ArrayList<>();

    List<Integer> linkedTables = new ArrayList<>();

    int maxIterations = data.size() * 3;

    while (linkedTables.size() != data.size() - 1 && maxIterations != 0) {
      for (int i = 1; i < data.size(); i++) {
        if (!linkedTables.contains(i)) {
          var mergeResult = mergeTables(linkedTablesName, linkedTable, fromFiles.get(i), data.get(fromFiles.get(i)));

          if (mergeResult != null) {
            linkedTable = mergeResult;
            linkedTables.add(i);
            linkedTablesName.add(fromFiles.get(i).substring(19, fromFiles.get(i).length() - 5));
          }
        }

        maxIterations--;
      }
    }


    for (var rows : linkedTable) {
      if (where == null || Checker.evaluateCondition(rows, where)) {
        result.add(rows);
      }
    }

    return result;
  }

  /**
   * Группирует данные по указанному полю.
   *
   * @param records список записей
   * @param groupBy поле для группировки
   * @return список сгруппированных записей
   */
  private List<Map<String, String>> groupData(
      final List<Map<String, String>> records,
      final String groupBy
  ) {
    final Map<String, List<Map<String, String>>> groupedData = new LinkedHashMap<>();
    for (final Map<String, String> record : records) {
      final String key = record.get(groupBy);
      if (!groupedData.containsKey(key)) {
        groupedData.put(key, new ArrayList<>());
      }
      groupedData.get(key).add(record);
    }

    final List<Map<String, String>> result = new ArrayList<>();
    for (final Map.Entry<String, List<Map<String, String>>> entry
        : groupedData.entrySet()) {
      final Map<String, String> aggregatedRecord = new HashMap<>();
      aggregatedRecord.put(groupBy, entry.getKey());
      result.add(aggregatedRecord);
    }
    return result;
  }

  /**
   * Форматирует результат в виде списка строк.
   *
   * @param records        список записей
   * @param selectedFields список выбранных полей
   * @return список строк с отформатированным результатом
   */
  private List<String> formatResult(final List<Map<String, String>> records,
                                    final List<String> selectedFields) {
    return records.stream()
        .map(record -> selectedFields.stream()
            .map(record::get)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(";")))
        .filter(result -> !result.isEmpty())
        .collect(Collectors.toList());
  }
}
