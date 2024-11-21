package ru.mai.lessons.rpks.impl.QueryParser;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.Map;

public interface IQueryParser {
  Map<String, String> parseCommand (String command) throws WrongCommandFormatException;
}
