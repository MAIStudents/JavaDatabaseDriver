package ru.mai.lessons.rpks.impl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class DatabaseDriverTest {
  private static final String STUDENTS_FILENAME = "students.csv";

  @BeforeMethod
  public void setUp() {
  }

  @AfterMethod
  public void tearDown() {
  }

  @Test
  public void testFind() {
  }

  //region Вспомогательные методы
  private String getOutputFilename(String keyWord, int lineCount) {
    return String.format("outputFilename_%s_%d_lines.txt", keyWord, lineCount);
  }

  private Path getPath(String filename) throws URISyntaxException {
    File file = new File(filename);
    return Paths.get(Objects.requireNonNull(getClass().getResource("/" + file)).toURI());
  }
  //endregion
}