package ru.mai.lessons.rpks.impl.parsers;

import ru.mai.lessons.rpks.DataBaseParser;

import java.util.List;

public class FileParser implements DataBaseParser {
	@Override
	public List<String> parse(String line) {
		if (line == null || line.isEmpty()) {
			return null;
		}
		return List.of(line.split(";"));
	}
}
