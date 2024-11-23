package ru.mai.lessons.rpks.impl.models;

import lombok.Data;
import ru.mai.lessons.rpks.impl.Entity;

import java.util.*;

@Data
public final class IdTable implements Entity {

	public static List<IdTable> resultList = new ArrayList<>();
	private String studentId;
	private String studentName;
	private String groupName;
	private String subjectName;
	private String grade;
	private String date;

	public IdTable(String studentId, String studentName, String groupId, String subjectName, String grade, String date) {
		this.studentName = studentName;
		this.groupName = groupId;
		this.subjectName = subjectName;
		this.grade = grade;
		this.date = date;
		this.studentId = studentId;
	}

	public static void loadTable(List<Student> studentList,
								 List<Group> groupList,
								 List<Subject> subjectList,
								 List<Grade> gradeList) {
		resultList = new ArrayList<>();
		if (studentList == null) {
			return;
		}
		for (Student student : studentList) {
			List<Grade> gradeTmp = null;
			if (gradeList != null) {
				gradeTmp = gradeList
						.stream()
						.filter(x -> x.getStudentId().equals(student.getId())).toList();
				for (Grade elem : gradeTmp) {
					elem.setSubjectId(subjectList.get(Integer.parseInt(elem.getSubjectId())).getSubjectName());
				}
			}
			String groupName = null;
			for (Group elem : groupList) {
				if (elem.getStudentId().equals(student.getId())) {
					groupName = elem.getGroupName();
					break;
				}
			}
			if (gradeTmp != null) {
				for (Grade result : gradeTmp) {
					resultList.add(new IdTable(
							student.getId(),
							student.getFullName(),
							groupName,
							result.getSubjectId(),
							result.getGrade(),
							result.getDate()));
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (IdTable) obj;
		return Objects.equals(this.studentName, that.studentName) &&
				Objects.equals(this.groupName, that.groupName) &&
				Objects.equals(this.subjectName, that.subjectName) &&
				Objects.equals(this.grade, that.grade);
	}

	@Override
	public int hashCode() {
		return Objects.hash(studentName, groupName, subjectName, grade);
	}

	@Override
	public String toString() {
		return "IdTable[" +
				"studentName=" + studentName + ", " +
				"groupName=" + groupName + ", " +
				"subjectName=" + subjectName + ", " +
				"grade=" + grade + ']';
	}


	@Override
	public String getField(String field) {
		return switch (field) {
			case "full_name" -> studentName;
			case "group_name" -> groupName;
			case "id", "student_id" -> studentId;
			case "grade" -> grade;
			case "date" -> date;
			case "subject_name" -> subjectName;
			default -> "";
		};
	}
}
