package ru.mai.lessons.rpks.enums;

import java.util.Scanner;
import java.util.function.Function;

public enum FieldType {
  INT(Scanner::nextInt),
  STRING(Scanner::next);

  private final Function<Scanner, ?> reader;

  FieldType(Function<Scanner, ?> reader) {
    this.reader = reader;
  }

  public Function<Scanner, ?> getReader() {
    return reader;
  }
}
