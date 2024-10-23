package ru.mai.lessons.rpks.impl.Tables;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Grade;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Group;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Student;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Subject;

import java.util.ArrayList;
import java.util.List;

public class GradeTable implements Table {
    List<Grade> grades;
    List<String> fields = List.of("subject_id", "grade", "student_id", "date");
    SubjectTable subjects;
    GroupTable groups;
    StudentTable students;
    public GradeTable(List<Grade> grades) {
        this.grades = grades;
    }

    public GradeTable setSubjects(SubjectTable subjects) {
        this.subjects = subjects;
        return this;
    }

    public GradeTable setGroups(GroupTable groups) {
        this.groups = groups;
        return this;
    }

    public GradeTable setStudents(StudentTable students) {
        this.students = students;
        return this;
    }

    @Override
    public List<String> getFields() {
        return fields;
    }
    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
        switch (nameField) {
            case "full_name":
                List<Object> students1 = new ArrayList<>();
                for (Object obj : listObj) {
                    Grade grade = (Grade) obj;
                    long student_id = grade.getStudentId();
                    List<Object> stList = students.getAllObjectsByField("id", String.valueOf(student_id));
                    for (var object : stList) {
                        Student student = (Student) object;
                        students1.add(valueOrObject.equals("value") ? student.getFullName() : student);
                    }
                }
                return students1;
            case "group_name":
                List<Object> groups1 = new ArrayList<>();
                for (Object obj : listObj) {
                    Grade grade = (Grade) obj;
                    long student_id = grade.getStudentId();
                    List<Object> stList = students.getAllObjectsByField("id", String.valueOf(student_id));
                    for (var object : stList) {
                        Student student = (Student) object;
                        Group group = (Group) groups.getAllObjectsByField("student_id", String.valueOf(student.getId())).get(0);
                        groups1.add(valueOrObject.equals("value") ? group.getGroupName() : group);
                    }
                }
                return groups1;
            case "subject_name":
                List<Object> subjects1 = new ArrayList<>();
                for (Object obj : listObj) {
                    Grade grade = (Grade) obj;
                    long subject_id = grade.getSubjectId();
                    List<Object> subList = subjects.getAllObjectsByField("subject_id", String.valueOf(subject_id));
                    for (var object : subList) {
                        Subject subject = (Subject) object;
                        subjects1.add(valueOrObject.equals("value") ? subject.getSubjectName() : subject);
                    }
                }
                return subjects1;
            default:
                return null;
        }
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
        List<Object> grades1 = new ArrayList<>();
        switch (nameField) {
            case "subject_id" -> {
                int dependingValue = Integer.parseInt(value);
                for (var grade : grades) {
                    if (grade.getSubjectId() == dependingValue) {
                        grades1.add(grade);
                    }
                }
            }
            case "date" -> {
                for (var grade : grades) {
                    if (grade.getDate().equals(value)) {
                        grades1.add(grade);
                    }
                }
            }
            case "grade" -> {
                for (var grade : grades) {
                    if (grade.getGrade() == Integer.parseInt(value)) {
                        grades1.add(grade);
                    }
                }
            }
            case "student_id" -> {
                for (var grade : grades) {
                    if (grade.getStudentId() == Integer.parseInt(value)) {
                        grades1.add(grade);
                    }
                }
            }
        }
        return grades1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
        List<Object> grades1 = new ArrayList<>();
        switch (nameField) {
            case "subject_id" -> {
                List<Integer> dependingValues = new ArrayList<>();
                for (var each : values) {
                    dependingValues.add(Integer.parseInt(each));
                }
                for (var grade : grades) {
                    if (dependingValues.contains(grade.getSubjectId())) {
                        grades1.add(valueOrObject.equals("value") ? grade.getSubjectId() : grade);
                    }
                }
            }
            case "date" -> {
                for (var grade : grades) {
                    if (values.contains(grade.getDate())) {
                        grades1.add(valueOrObject.equals("value") ? grade.getDate() : grade);
                    }
                }
            }
            case "grade" -> {
                List<Integer> dependingValues = new ArrayList<>();
                for (var each : values) {
                    dependingValues.add(Integer.parseInt(each));
                }
                for (var grade : grades) {
                    if (dependingValues.contains(grade.getGrade())) {
                        grades1.add(valueOrObject.equals("value") ? grade.getGrade() : grade);
                    }
                }
            }
            case "student_id" -> {
                List<Integer> dependingValues = new ArrayList<>();
                for (var each : values) {
                    dependingValues.add(Integer.parseInt(each));
                }
                for (var grade : grades) {
                    if (dependingValues.contains(grade.getStudentId())) {
                        grades1.add(valueOrObject.equals("value") ? grade.getStudentId() : grade);
                    }
                }
            }
        }
        return grades1;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> res = new ArrayList<>();
        switch (nameField) {
            case "grade":
                for (Grade grade : grades) {
                    res.add(grade.getGrade());
                }
                break;
            case "student_id":
                for (Grade grade : grades) {
                    res.add(grade.getStudentId());
                }
                break;
            case "subject_id":
                for (Grade grade : grades) {
                    res.add(grade.getSubjectId());
                }
                break;
            case "date":
                for (Grade grade : grades) {
                    res.add(grade.getDate());
                }
                break;
        }
        return res;
    }
}
