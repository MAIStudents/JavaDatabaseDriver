package ru.mai.lessons.rpks.impl.Tables;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Group;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Student;

import java.util.ArrayList;
import java.util.List;

public class GroupTable implements Table {
    List<Group> groups;
    List<String> fields = List.of("id", "group_name", "student_id");
    GradeTable grades;
    SubjectTable subjects;
    StudentTable students;
    public GroupTable(List<Group> groups) {
        this.groups = groups;
    }

    public GroupTable setGrades(GradeTable grades) {
        this.grades = grades;
        return this;
    }

    public GroupTable setSubjects(SubjectTable subjects) {
        this.subjects = subjects;
        return this;
    }

    public GroupTable setStudents(StudentTable students) {
        this.students = students;
        return this;
    }

    @Override
    public List<String> getFields() {
        return fields;
    }
    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> res = new ArrayList<>();
        switch (nameField) {
            case "group_name":
                for (Group group : groups) {
                    res.add(group.getGroupName());
                }
                break;
            case "student_id":
                for (Group group : groups) {
                    res.add(group.getStudentId());
                }
                break;
            case "id", "group_id":
                for (Group group : groups) {
                    res.add(group.getId());
                }
                break;
            default:
                throw new FieldNotFoundInTableException("error");
        }
        return res;
    }
    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
        List<Object> groups1 = new ArrayList<>();
        switch (nameField) {
            case "id", "group_id" -> {
                int dependingValue = Integer.parseInt(value);
                for (var group : groups) {
                    if (group.getId() == dependingValue) {
                        groups1.add(group);
                    }
                }
            }
            case "student_id" -> {
                int dependingValue = Integer.parseInt(value);
                for (var group : groups) {
                    if (group.getStudentId() == dependingValue) {
                        groups1.add(group);
                    }
                }
            }
            case "group_name" -> {
                for (var group : groups) {
                    if (group.getGroupName().equals(value)) {
                        groups1.add(group);
                    }
                }
            }
        }
        return groups1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
        List<Object> groups1 = new ArrayList<>();
        switch (nameField) {
            case "id", "group_id" -> {
                List<Integer> dependingValues = new ArrayList<>();
                for (var each : values) {
                    dependingValues.add(Integer.parseInt(each));
                }
                for (var group : groups) {
                    if (dependingValues.contains(group.getId())) {
                        groups1.add(valueOrObject.equals("value") ? group.getId() : group);
                    }
                }
            }
            case "student_id" -> {
                List<Integer> dependingValues = new ArrayList<>();
                for (var each : values) {
                    dependingValues.add(Integer.parseInt(each));
                }
                for (var group : groups) {
                    if (dependingValues.contains(group.getStudentId())) {
                        groups1.add(valueOrObject.equals("value") ? group.getStudentId() : group);
                    }
                }
            }
            case "group_name" -> {
                for (var group : groups) {
                    if (values.contains(group.getGroupName())) {
                        groups1.add(valueOrObject.equals("value") ? group.getGroupName() : group);
                    }
                }
            }
        }
        return groups1;
    }

    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
        List<Object> res = new ArrayList<>();
        switch (nameField) {
            case "full_name":
                for (Object obj : listObj) {
                    Group group = (Group) obj;
                    long student_id = group.getStudentId();
                    List<Object> stList = students.getAllObjectsByField("id", String.valueOf(student_id));
                    for (var object : stList) {
                        Student student = (Student) object;
                        res.add(valueOrObject.equals("value") ? student.getFullName() : student);
                    }
                }
                return res;
            default:
                return null;
        }
    }
}
