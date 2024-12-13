package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Checker {
  /**
   * Проверяет корректность формата команды.
   *
   * @param command команда для проверки
   * @throws WrongCommandFormatException если команда некорректна
   */
  public static void validateCommand(final String command)
      throws WrongCommandFormatException {
    if (command == null || command.trim().isEmpty()) {
      throw new WrongCommandFormatException("Неверный формат команды");
    }
  }

  /**
   * Проверяет существование указанных полей в загруженных данных.
   *
   * @param fields список полей
   * @param data   карта с данными
   * @throws FieldNotFoundInTableException если поле не найдено
   */
  public static void checkFieldsExistence(final List<String> fields,
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
   * Оценивает условие для записи.
   *
   * @param record    запись для оценки
   * @param condition условие
   * @return true, если условие выполнено, иначе false
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  public static boolean evaluateCondition(final Map<String, String> record,
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
}
