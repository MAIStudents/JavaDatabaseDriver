package ru.mai.lessons.rpks;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface Command {
	List<String> execute() throws IOException, WrongCommandFormatException, FieldNotFoundInTableException;
}
