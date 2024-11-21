package ru.mai.lessons.rpks.impl.CsvParser;

import java.util.List;
import java.util.Map;

public interface ICsvParser {
  List<Map<String, String>> parseCsv(String filePath);
}
