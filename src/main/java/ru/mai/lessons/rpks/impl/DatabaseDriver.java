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

  @Override
  public List<String> find(final String studentsFile, final String groupsFile,
                           final String subjectsFile, final String gradeFile,
                           final String command)
      throws FieldNotFoundInTableException, WrongCommandFormatException {
    validateCommand(command);

    String normalizedCommand = normalizeCommand(command);

    // Проверка кэша
    if (resultCache.containsKey(normalizedCommand)) {
      return resultCache.get(normalizedCommand);
    }

    final String fieldsPart = extractFieldsPart(command);
    final String fromPart = extractFromPart(command);
    final String wherePart = extractWherePart(command);
    final String groupByPart = extractGroupByPart(command);

    final List<String> selectedFields = Arrays.asList(fieldsPart.split(","));
    final List<String> fromFiles = Arrays.asList(fromPart.split(","));

    final Map<String, List<Map<String, String>>> data = loadData(fromFiles);
    checkFieldsExistence(selectedFields, data);
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
   * Проверяет корректность формата команды.
   *
   * @param command команда для проверки
   * @throws WrongCommandFormatException если команда некорректна
   */
  private void validateCommand(final String command)
      throws WrongCommandFormatException {
    if (command == null || command.trim().isEmpty()) {
      throw new WrongCommandFormatException("Неверный формат команды");
    }
  }

  /**
   * Извлекает часть полей из команды.
   *
   * @param command команда
   * @return строка с полями
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private String extractFieldsPart(final String command)
      throws WrongCommandFormatException {
    final int selectIndex = command.indexOf("SELECT=");
    final int fromIndex = command.indexOf("FROM=");

    if (selectIndex == -1 || fromIndex == -1) {
      throw new WrongCommandFormatException("Неверный формат "
          + "команды: отсутствует SELECT или FROM");
    }

    final String fieldsPart = command.substring(selectIndex
        + "SELECT=".length(), fromIndex).trim();
    if (fieldsPart.isEmpty()) {
      throw new WrongCommandFormatException("Неверный формат "
          + "команды: отсутствуют поля после SELECT");
    }
    return fieldsPart;
  }

  /**
   * Извлекает часть FROM из команды.
   *
   * @param command команда
   * @return строка с таблицами
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private String extractFromPart(final String command)
      throws WrongCommandFormatException {
    final int fromIndex = command.indexOf("FROM=");
    final int whereIndex = command.indexOf("WHERE=(");
    final int groupByIndex = command.indexOf("GROUPBY=");

    final int nextPartIndex = whereIndex != -1
        ? whereIndex
        : groupByIndex != -1 ? groupByIndex : command.length();
    final String fromPart = command.substring(fromIndex
        + "FROM=".length(), nextPartIndex).trim();
    if (fromPart.isEmpty()) {
      throw new WrongCommandFormatException("Неверный формат "
          + "команды: отсутствуют таблицы после FROM");
    }
    return fromPart;
  }

  /**
   * Извлекает часть WHERE из команды.
   *
   * @param command команда
   * @return строка с условиями WHERE или null, если не указано
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private String extractWherePart(final String command)
      throws WrongCommandFormatException {
    final int whereIndex = command.indexOf("WHERE=(");

    if (whereIndex != -1) {
      final int whereEndIndex = command.indexOf(")", whereIndex);
      if (whereEndIndex == -1) {
        throw new WrongCommandFormatException("Неверный формат команды: "
            + "отсутствует закрывающая скобка для WHERE");
      }
      final String wherePart = command.substring(whereIndex
          + "WHERE=(".length(), whereEndIndex).trim();
      if (wherePart.isEmpty()) {
        throw new WrongCommandFormatException("Неверный формат команды: "
            + "отсутствует условие после WHERE");
      }
      return wherePart;
    } else if (command.contains("WHERE")) {
      throw new WrongCommandFormatException("Неверный формат команды: "
          + "некорректная запись WHERE");
    }
    return null;
  }

  /**
   * Извлекает часть GROUPBY из команды.
   *
   * @param command команда
   * @return строка с полем GROUPBY или null, если не указано
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private String extractGroupByPart(final String command)
      throws WrongCommandFormatException {
    final int groupByIndex = command.indexOf("GROUPBY=");

    if (groupByIndex != -1) {
      final String groupByPart
          = command.substring(groupByIndex + "GROUPBY=".length()).trim();
      if (groupByPart.isEmpty()) {
        throw new WrongCommandFormatException("Неверный формат команды: "
            + "отсутствует поле после GROUPBY");
      }
      return groupByPart;
    } else if (command.contains("GROUPBY")) {
      throw new WrongCommandFormatException("Неверный формат команды: "
          + "некорректная запись GROUPBY");
    }
    return null;
  }

  /**
   * Загружает данные из указанных файлов.
   *
   * @param files список файлов
   * @return карта с данными
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private Map<String, List<Map<String, String>>>
  loadData(final List<String> files)
      throws WrongCommandFormatException {
    final Map<String, List<Map<String, String>>>
        data = new HashMap<>();
    for (final String file : files) {
      if (file.contains(" ")) {
        throw new WrongCommandFormatException("Неверный формат команды: "
            + "лишний пробел");
      }

      final List<Map<String, String>> records = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        final String headerLine = br.readLine();
        if (headerLine != null) {
          final String[] headers = headerLine.split(";");
          String line;
          while ((line = br.readLine()) != null) {
            final String[] values = line.split(";");
            final Map<String, String> record = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
              record.put(headers[i], values[i]);
            }
            records.add(record);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      data.put(file, records);
    }
    return data;
  }

  /**
   * Проверяет существование указанных полей в загруженных данных.
   *
   * @param fields список полей
   * @param data   карта с данными
   * @throws FieldNotFoundInTableException если поле не найдено
   */
  private void checkFieldsExistence(final List<String> fields,
                                    final Map<String,
                                    List<Map<String, String>>> data)
      throws FieldNotFoundInTableException {
    final Set<String> availableFields = new HashSet<>();
    for (final List<Map<String, String>> records : data.values()) {
      if (!records.isEmpty()) {
        availableFields.addAll(records.get(0).keySet());
      }
    }
    for (final String field : fields) {
      if (!availableFields.contains(field)) {
        throw new FieldNotFoundInTableException("Поле "
            + field + " не найдено в таблице");
      }
    }
  }

  /**
   * Фильтрует данные на основе условий WHERE.
   *
   * @param data  карта с данными
   * @param where условия WHERE
   * @return отфильтрованный список записей
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private List<Map<String, String>> filterData(final Map<String,
                                               List<Map<String, String>>> data,
                                               final String where)
      throws WrongCommandFormatException {
    final List<Map<String, String>> result = new ArrayList<>();

    for (final List<Map<String, String>> records : data.values()) {
      for (final Map<String, String> record : records) {
        final Map<String, String> resultRecord = new HashMap<>(record);
        addRelatedRecords(record, resultRecord, data);

        if (where == null || evaluateCondition(resultRecord, where)) {
          result.add(resultRecord);
        }
      }
    }
    return result;
  }

  /**
   * Собирает связанные записи из данных.
   *
   * @param data карта с данными
   * @return карта с собранными связанными записями
   */
  private Map<String, List<Map<String, String>>>
  collectLinkedRecords(final Map<String, List<Map<String, String>>> data) {
    final Map<String, List<Map<String, String>>> linkedRecords
        = new HashMap<>();
    for (final String key : data.keySet()) {
      final List<Map<String, String>> records = data.get(key);
      for (final Map<String, String> record : records) {
        for (final String field : record.keySet()) {
          if (field.endsWith("_id")) {
            final String linkedKey = field.substring(0, field.length() - 3);
            linkedRecords.putIfAbsent(linkedKey, new ArrayList<>());
            linkedRecords.get(linkedKey).add(record);
          }
        }
      }
    }
    return linkedRecords;
  }

  /**
   * Добавляет связанные записи к результату.
   *
   * @param record       исходная запись
   * @param resultRecord результирующая запись
   * @param data         карта с данными
   */
  private void addRelatedRecords(final Map<String, String> record,
                                 final Map<String, String> resultRecord,
                                 final Map<String,
                                 List<Map<String, String>>> data) {
    for (final String field : record.keySet()) {
      if (field.endsWith("_id")) {
        final String linkedKey = field.substring(0, field.length() - 3);
        final String linkedId = record.get(field);
        final List<Map<String, String>> relatedRecords
            = data.get("src/test/resources/" + linkedKey + "s.csv");
        if (relatedRecords != null) {
          for (final Map<String, String> relatedRecord : relatedRecords) {
            if (relatedRecord.get("id").equals(linkedId)) {
              for (final String relatedField : relatedRecord.keySet()) {
                if (!resultRecord.containsKey(relatedField)) {
                  resultRecord.put(relatedField,
                      relatedRecord.get(relatedField));
                }
              }
              addInnerRelatedRecords(relatedRecord, resultRecord, data);
            }
          }
        }
      }
    }
  }

  /**
   * Добавляет внутренние связанные записи к результату.
   *
   * @param relatedRecord связанная запись
   * @param resultRecord  результирующая запись
   * @param data          карта с данными
   */
  private void addInnerRelatedRecords(final Map<String, String> relatedRecord,
                                      final Map<String, String> resultRecord,
                                      final Map<String,
                                      List<Map<String, String>>> data) {
    for (final String relatedField : relatedRecord.keySet()) {
      if (relatedField.endsWith("_id")) {
        final String innerLinkedKey
            = relatedField.substring(0, relatedField.length() - 3);
        final String innerLinkedId = relatedRecord.get(relatedField);
        final List<Map<String, String>> innerRecords
            = data.get("src/test/resources/" + innerLinkedKey + "s.csv");
        if (innerRecords != null) {
          for (final Map<String, String> innerRecord : innerRecords) {
            if (innerRecord.get("id").equals(innerLinkedId)) {
              for (final String innerField : innerRecord.keySet()) {
                if (!resultRecord.containsKey(innerField)) {
                  resultRecord.put(innerField, innerRecord.get(innerField));
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Оценивает условие для записи.
   *
   * @param record    запись для оценки
   * @param condition условие
   * @return true, если условие выполнено, иначе false
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  private boolean evaluateCondition(final Map<String, String> record,
                                    final String condition)
      throws WrongCommandFormatException {
    String trimmedCondition = condition.trim();

    final String[] orConditions = trimmedCondition.split("\\s+OR\\s+");
    for (final String orCondition : orConditions) {
      final String[] andConditions = orCondition.split("\\s+AND\\s+");
      boolean allConditionsTrue = true;

      for (final String andCondition : andConditions) {
        final String[] parts = andCondition.split("=");
        if (parts.length != 2) {
          throw new WrongCommandFormatException("Неверный формат команды: "
              + "некорректное условие WHERE");
        }

        final String field = parts[0].trim();
        final String value = parts[1].trim().replace("'", "");

        if (!record.containsKey(field) || !record.get(field).equals(value)) {
          allConditionsTrue = false;
          break;
        }
      }

      if (allConditionsTrue) {
        return true;
      }
    }

    return false;
  }

  /**
   * Группирует данные по указанному полю.
   *
   * @param records список записей
   * @param groupBy поле для группировки
   * @return список сгруппированных записей
   */
  private List<Map<String, String>> groupData(final List<Map<String,
                                              String>> records,
                                              final String groupBy) {
    final Map<String, List<Map<String, String>>> groupedData = new HashMap<>();
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
