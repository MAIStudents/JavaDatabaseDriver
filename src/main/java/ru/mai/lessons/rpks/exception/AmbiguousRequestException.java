package ru.mai.lessons.rpks.exception;

public class AmbiguousRequestException extends RuntimeException {
  public AmbiguousRequestException(String message) {
    super(message);
  }
}
