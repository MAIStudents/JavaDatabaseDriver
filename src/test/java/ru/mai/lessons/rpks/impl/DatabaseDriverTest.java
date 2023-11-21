package ru.mai.lessons.rpks.impl;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class DatabaseDriverTest {
  private static final String GRADE_FILENAME = "grade.csv";
  private static final String GROUPS_FILENAME = "groups.csv";
  private static final String STUDENTS_FILENAME = "students.csv";
  private static final String SUBJECTS_FILENAME = "subjects.csv";

  private IDatabaseDriver databaseDriver;

  @BeforeClass
  public void setUp() {
    databaseDriver = new DatabaseDriver();
  }

  @DataProvider(name = "selectFromCases")
  private Object[][] getSelectFromCase() {
    return new Object[][] {
        {"SELECT=full_name FROM=" + STUDENTS_FILENAME,
         List.of("Иванов Иван", "Петров Олег", "Игнатова Ольга", "Сидоров Николай",
                 "Калинина Дарья", "Кузнецов Михаил", "Орлов Виктор", "Никитина Ирина")},
        {"SELECT=id,full_name FROM=" + STUDENTS_FILENAME,
         List.of("0;Иванов Иван", "1;Петров Олег", "2;Игнатова Ольга", "3;Сидоров Николай",
                 "4;Калинина Дарья", "5;Кузнецов Михаил", "6;Орлов Виктор", "7;Никитина Ирина")},
        {"SELECT=full_name,id FROM=" + STUDENTS_FILENAME,
         List.of("Иванов Иван;0", "Петров Олег;1", "Игнатова Ольга;2", "Сидоров Николай;3",
                 "Калинина Дарья;4", "Кузнецов Михаил;5", "Орлов Виктор;6", "Никитина Ирина;7")},
        {"SELECT=subject_name FROM=" + SUBJECTS_FILENAME,
         List.of("РПКС", "Матан", "История", "Английский")},
        {"SELECT=group_name,student_id FROM=" + GROUPS_FILENAME,
         List.of("5ИНТ-001;5", "5ИНТ-002;2", "5ПМИ-001;4", "5ИНТ-001;0", "5ИНТ-001;1", "5ИНТ-002;7",
                 "5ПМИ-001;3", "5ПМИ-001;6")}
    };
  }

  @Test(dataProvider = "selectFromCases",
        description = "Проверяем успешное выполнение простых запроса SELECT+FROM")
  public void testPositiveFindDataBySimpleSelectFrom(String command, List<String> expectedResult)
      throws FieldNotFoundInTableException {
    // WHEN
    List<String> actualResult = databaseDriver.find(STUDENTS_FILENAME, GROUPS_FILENAME,
                                                    SUBJECTS_FILENAME, GRADE_FILENAME, command);

    // THEN
    assertEquals(actualResult, expectedResult);
  }

  @Test(expectedExceptions = FieldNotFoundInTableException.class,
        description = "Проверяем реакцию на попытку получить данные из поля, которого нет в таблице")
  public void testNegativeTryFindUnknownFieldInTable() throws FieldNotFoundInTableException {
    // GIVEN
    String command = "SELECT=group_name FROM=" + STUDENTS_FILENAME;

    // WHEN
    List<String> actualResult = databaseDriver.find(STUDENTS_FILENAME, GROUPS_FILENAME,
                                                    SUBJECTS_FILENAME, GRADE_FILENAME, command);
    // THEN ожидаем получение исключения
  }
}