package ru.mai.lessons.rpks;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.util.List;

public interface IDatabaseDriver {
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command)
      throws FieldNotFoundInTableException; // запускает проверку
}
