package ru.mai.lessons.rpks;

import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.List;

@FunctionalInterface
public interface DataBaseParser {
    List<String> parse(String line) throws WrongCommandFormatException;
}
