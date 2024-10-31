package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.DatabaseManager.DatabaseManager;
import ru.mai.lessons.rpks.impl.dataBase.DataBase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class DatabaseDriver implements IDatabaseDriver {

  private static String PREFIX = "src/test/resources/";
  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) {
    if (studentsCsvFile == null || studentsCsvFile.isEmpty()) {
      throw new IllegalArgumentException("studentsCsvFile is empty");
    }
    if (groupsCsvFile == null || groupsCsvFile.isEmpty()) {
      throw new IllegalArgumentException("groupsCsvFile is empty");
    }
    if (subjectsCsvFile == null || subjectsCsvFile.isEmpty()) {
      throw new IllegalArgumentException("subjectsCsvFile is empty");
    }
    if (gradeCsvFile == null || gradeCsvFile.isEmpty()) {
      throw new IllegalArgumentException("gradeCsvFile is empty");
    }
    if (command == null || command.isEmpty()) {
      throw new IllegalArgumentException("command is empty");
    }
    try {
//      command = "SELECT=full_name,subject_name FROM=%s,%s,%s WHERE=(grade='3' OR popa='4' OR grade='2')";
      DatabaseManager manager = new DatabaseManager(PREFIX + studentsCsvFile, PREFIX + groupsCsvFile, PREFIX + subjectsCsvFile, PREFIX + gradeCsvFile);
      manager.getRequest(command);

    } catch (IOException | ParseException e) {
      e.printStackTrace();
    } catch (WrongCommandFormatException | FieldNotFoundInTableException e) {
        throw new RuntimeException(e);
    }


      return null; // реализовать проверку
  }
}
