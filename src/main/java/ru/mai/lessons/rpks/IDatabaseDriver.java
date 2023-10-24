package ru.mai.lessons.rpks;

import java.util.List;

public interface IDatabaseDriver {
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command); // запускает проверку
}
