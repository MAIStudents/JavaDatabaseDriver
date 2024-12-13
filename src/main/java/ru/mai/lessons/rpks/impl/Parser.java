package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

public final class Parser {
  /**
   * Извлекает часть полей из команды.
   *
   * @param command команда
   * @return строка с полями
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  public static String extractFieldsPart(final String command)
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
  public static String extractFromPart(final String command)
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
  public static String extractWherePart(final String command)
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
  public static String extractGroupByPart(final String command)
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
}
