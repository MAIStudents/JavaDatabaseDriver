package ru.mai.lessons.rpks.impl.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import ru.mai.lessons.rpks.impl.Entity;
import ru.mai.lessons.rpks.exception.*;
import ru.mai.lessons.rpks.impl.models.*;

public record Command(List<String> select, List<String> from, List<String> where, String groupBy,
					  List<String> files)
		implements ru.mai.lessons.rpks.Command {

	enum WhereMode {
		AND, OR, NONE
	}

	private static WhereMode mode = WhereMode.NONE;

	@Override
	public List<String> execute() throws IOException, WrongCommandFormatException, FieldNotFoundInTableException {
		if (select.isEmpty() || from.isEmpty() || (groupBy != null && groupBy.isEmpty())) {
			throw new WrongCommandFormatException("Неверно указан запрос");
		}
		Map<String, List<String>> columnsMap = whereMap();
		List<String> answer = new ArrayList<>();
		QueryLists queryLists = getQueryLists();
		IdTable.loadTable(queryLists.studentList(), queryLists.groupList(), queryLists.subjectList(), queryLists.gradeList());
		if (from.size() != 1) {
			if (groupBy != null) {
				sortByGroup(IdTable.resultList);
			}
			addToAnswer(IdTable.resultList, columnsMap, answer);
		} else {
			List<? extends Entity> toSortList;
			toSortList = getEntities(queryLists);
			assert toSortList != null;
			if (toSortList.stream().filter(x -> !x.getField(select.get(0)).isEmpty()).toList().isEmpty()) {
				throw new FieldNotFoundInTableException("Не найдено поле");
			}

			if (groupBy != null) {
				toSortList.sort((x1, x2) -> x1.getField(groupBy).compareTo(x2.getField(groupBy)));
			}
			addToAnswer(toSortList, columnsMap, answer);
		}
		if (answer.isEmpty()) {
			return List.of("");
		}
		answer = answer.stream().distinct().collect(Collectors.toList());
		return answer;
	}

	private List<? extends Entity> getEntities(QueryLists queryLists) throws WrongCommandFormatException {
		List<? extends Entity> toSortList;
		switch (from.get(0)) {
			case "students.csv" -> toSortList = queryLists.studentList();
			case "groups.csv" -> toSortList = queryLists.groupList();
			case "subjects.csv" -> toSortList = queryLists.subjectList();
			case "grade.csv" -> toSortList = queryLists.gradeList();
			default -> throw new WrongCommandFormatException("Нет такого Массива");
		}
		return toSortList;
	}

	private record QueryLists(List<Student> studentList, List<Group> groupList, List<Subject> subjectList,
							  List<Grade> gradeList) {
	}

	private QueryLists getQueryLists() throws FileNotFoundException, WrongCommandFormatException {
		List<Student> studentList = List.of();
		List<Group> groupList = List.of();
		List<Subject> subjectList = List.of();
		List<Grade> gradeList = List.of();
		for (String selection : files) {
			switch (selection) {
				case "students.csv" -> studentList = Student.loadEntityList("students.csv");
				case "groups.csv" -> groupList = Group.loadEntityList("groups.csv");
				case "subjects.csv" -> subjectList = Subject.loadEntityList("subjects.csv");
				case "grade.csv" -> gradeList = Grade.loadEntityList("grade.csv");
				default -> throw new WrongCommandFormatException("Нет такого файла");
			}
		}
		return new QueryLists(studentList, groupList, subjectList, gradeList);
	}

	private void addToAnswer(List<? extends Entity> toSortList, Map<String, List<String>> columnsMap, List<String> answer) {
		for (var result : toSortList) {
			boolean needToSkip = isNeedToSkip(result, columnsMap);
			if (!needToSkip) {
				StringBuilder outString = new StringBuilder();
				for (int i = 0; i < select.size(); ++i) {
					outString.append(result.getField(select.get(i)));
					if (i != select.size() - 1) {
						outString.append(';');
					}
				}
				if (!outString.isEmpty()) {
					answer.add(outString.toString());
				}
			}
		}
	}

	private void sortByGroup(List<? extends Entity> list) {
		list.sort((x1, x2) ->
				Math.toIntExact(list
						.stream()
						.filter(x -> x.getField(groupBy).equals(x1.getField(groupBy)))
						.count() -
						list
								.stream()
								.filter(x -> x.getField(groupBy).equals(x2.getField(groupBy)))
								.count())
		);
	}

	private static boolean isNeedToSkip(Entity result, Map<String, List<String>> columnsMap) {
		if (columnsMap.isEmpty()) return false;
		boolean needToSkip = true;
		boolean breaked = false;
		for (Map.Entry<String, List<String>> wherePair : columnsMap.entrySet()) {
			for (String value : wherePair.getValue()) {
				if (!result.getField(wherePair.getKey()).equals(value)) {
					if (mode == WhereMode.AND) {
						needToSkip = true;
						breaked = true;
						break;
					}
				} else {
					if (mode == WhereMode.OR) {
						needToSkip = false;
						break;
					}
				}
			}
		}
		if (mode == WhereMode.AND && !breaked) {
			needToSkip = false;
		}
		return needToSkip;
	}

	//получает map ключ-значение из where
	Map<String, List<String>> whereMap() {
		Map<String, List<String>> map = new HashMap<>();
		for (int i = 0; i < where.size(); i += 2) {
			if (where.get(i).equals("AND")) {
				i += 1;
				mode = WhereMode.AND;
			} else if (where.get(i).equals("OR")) {
				i += 1;
				mode = WhereMode.OR;
			}
			if (!map.containsKey(where.get(i))) {
				map.put(where.get(i), new ArrayList<>());
			}
			map.get(where.get(i)).add(where.get(i + 1));
		}
		if (mode == WhereMode.NONE) {
			mode = WhereMode.OR;
		}
		return map;
	}
}
