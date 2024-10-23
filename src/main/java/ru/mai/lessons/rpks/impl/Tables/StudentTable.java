package ru.mai.lessons.rpks.impl.Tables;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Grade;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Group;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Student;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Subject;

import java.util.ArrayList;
import java.util.List;

public class StudentTable implements Table {
    GradeTable grades;
    SubjectTable subjects;
    GroupTable groups;
    List<Student> students;
    List<String> fields = List.of("id", "full_name");
    public StudentTable(List<Student> students) {
        this.students = students;
    }

    public StudentTable setGroups(GroupTable groups) {
        this.groups = groups;
        return this;
    }

    public StudentTable setSubjects(SubjectTable subjects) {
        this.subjects = subjects;
        return this;
    }

    public StudentTable setGrades(GradeTable grades) {
        this.grades = grades;
        return this;
    }

    @Override
    public List<String> getFields() {
        return fields;
    }
    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
        List<Object> students1 = new ArrayList<>();
        if (nameField.equals("id") || nameField.equals("student_id")) {
            int dependingValue = Integer.parseInt(value);
            for (var student : students) {
                if (student.getId() == dependingValue) {
                    students1.add(student);
                }
            }
        }
        else if (nameField.equals("full_name")) {
            for (var student : students) {
                if (student.getFullName().equals(value)) {
                    students1.add(student);
                }
            }
        }
        return students1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
        List<Object> students1 = new ArrayList<>();
        if (nameField.equals("id") || nameField.equals("student_id")) {
            List<Integer> dependingValues = new ArrayList<>();
            for (var each : values) {
                dependingValues.add(Integer.parseInt(each));
            }
            for (var student : students) {
                if (dependingValues.contains(student.getId())) {
                    students1.add(valueOrObject.equals("value") ? student.getId() : student);
                }
            }
        }
        else if (nameField.equals("full_name")) {
            for (var student : students) {
                if (values.contains(student.getFullName())) {
                    students1.add(valueOrObject.equals("value") ? student.getFullName() : student);
                }
            }
        }
        return students1;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> res = new ArrayList<>();
        switch (nameField) {
            case "full_name":
                for (Student student : students) {
                    res.add(student.getFullName());
                };
                break;
            case "id", "student_id":
                for (Student student : students) {
                    res.add(student.getId());
                }
                break;
            default:
                throw new FieldNotFoundInTableException("error");
        }
        return res;
    }

    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
        List<Object> res = new ArrayList<>();
        switch (nameField) {
            case "group_name":
                for (Object obj : listObj) {
                    Student student = (Student) obj;
                    long student_id = student.getId();
                    List<Object> grList = groups.getAllObjectsByField("student_id", String.valueOf(student_id));
                    for (var object : grList) {
                        Group group = (Group) object;
                        res.add(valueOrObject.equals("value") ? group.getGroupName() : group);
                    }
                }
                return res;
            case "grade":
                for (Object obj : listObj) {
                    Student student = (Student) obj;
                    long student_id = student.getId();
                    List<Object> grList = grades.getAllObjectsByField("student_id", String.valueOf(student_id));
                    for (var object : grList) {
                        Grade grade = (Grade) object;
                        res.add(valueOrObject.equals("value") ? grade.getGrade() : grade);
                    }
                }
                return res;
            case "date":
                for (Object obj : listObj) {
                    Student student = (Student) obj;
                    long student_id = student.getId();
                    List<Object> grList = grades.getAllObjectsByField("student_id", String.valueOf(student_id));
                    for (var object : grList) {
                        Grade grade = (Grade) object;
                        res.add(valueOrObject.equals("value") ? grade.getDate() : grade);
                    }
                }
                return res;
            case "subject_name":
                for (Object obj : listObj) {
                    Student student = (Student) obj;
                    long student_id = student.getId();
                    List<Object> grList = grades.getAllObjectsByField("student_id", String.valueOf(student_id));
                    for (var object : grList) {
                        Grade grade = (Grade) object;
                        Subject subject = (Subject) subjects.getAllObjectsByField("subject_id", String.valueOf(grade.getSubjectId())).get(0);
                        res.add(valueOrObject.equals("value") ? subject.getSubjectName() : subject);
                    }
                }
                return res;
            default:
                return null;
        }
    }
}
