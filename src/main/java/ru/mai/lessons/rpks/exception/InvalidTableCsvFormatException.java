package ru.mai.lessons.rpks.exception;

public class InvalidTableCsvFormatException extends RuntimeException {
  public InvalidTableCsvFormatException(String message) {
    super(message);
  }
}
