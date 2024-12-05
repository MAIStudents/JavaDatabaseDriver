package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataLoader {
  /**
   * Загружает данные из указанных файлов.
   *
   * @param files список файлов
   * @return словарь с данными
   * @throws WrongCommandFormatException если формат команды некорректен
   */
  public static Map<String, List<Map<String, String>>>
  loadData(final List<String> files)
      throws WrongCommandFormatException {
    final Map<String, List<Map<String, String>>> data = new HashMap<>();

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
}
