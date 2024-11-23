package ru.mai.lessons.rpks.impl.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mai.lessons.rpks.impl.Entity;
import ru.mai.lessons.rpks.impl.parsers.FileParser;

import java.io.*;
import java.util.*;


@Data
@AllArgsConstructor
public class Student implements Entity {
	private String id;
	private String fullName;

	public Student(List<String> tmp) {
		this(tmp.get(0), tmp.get(1));
	}

	public static List<Student> loadEntityList(String fileName) throws FileNotFoundException {
		List<Student> studentList = new ArrayList<>();
		try (BufferedReader file = new BufferedReader(new FileReader(PATH + fileName))) {
			String currentLine = file.readLine();
			while ((currentLine = file.readLine()) != null) {
				List<String> tmp = new FileParser().parse(currentLine);
				studentList.add(new Student(tmp));
			}
			return studentList;
		}
		catch (IOException e) {
			throw new FileNotFoundException(e.getMessage());
		}
	}

	@Override
	public String getField(String field) {
		return switch (field) {
			case "id" -> getId();
			case "full_name" -> getFullName();
			default -> "";
		};
	}
}
