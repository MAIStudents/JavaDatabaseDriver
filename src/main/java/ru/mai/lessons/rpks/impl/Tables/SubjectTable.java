package ru.mai.lessons.rpks.impl.Tables;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Grade;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Group;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Student;
import ru.mai.lessons.rpks.impl.Tables.NameTypes.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectTable implements Table {
    List<Subject> subjects;
    List<String> fields = List.of("id", "subject_name");
    GradeTable grades;
    GroupTable groups;
    StudentTable students;
    public SubjectTable(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public SubjectTable setGrades(GradeTable grades) {
        this.grades = grades;
        return this;
    }

    public SubjectTable setGroups(GroupTable groups) {
        this.groups = groups;
        return this;
    }

    public SubjectTable setStudents(StudentTable students) {
        this.students = students;
        return this;
    }

    @Override
    public List<String> getFields() {
        return fields;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
        List<Object> subjects1 = new ArrayList<>();
        if (nameField.equals("id") || nameField.equals("subject_id")) {
            int dependingValue = Integer.parseInt(value);
            for (var subject : subjects) {
                if (subject.getId() == dependingValue) {
                    subjects1.add(subject);
                }
            }
        }
        else if (nameField.equals("subject_name")) {
            for (var subject : subjects) {
                if (subject.getSubjectName().equals(value)) {
                    subjects1.add(subject);
                }
            }
        }
        return subjects1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
        List<Object> subjects1 = new ArrayList<>();
        if (nameField.equals("id") || nameField.equals("subject_id")) {
            List<Integer> depenegingValues = new ArrayList<>();
            for (var each : values) {
                depenegingValues.add(Integer.parseInt(each));
            }
            for (var subject : subjects) {
                if (depenegingValues.contains(subject.getId())) {
                    subjects1.add(valueOrObject.equals("value") ? subject.getId() : subject);
                }
            }
        }
        else if (nameField.equals("subject_name")) {
            for (var subject : subjects) {
                if (values.contains(subject.getSubjectName())) {
                    subjects1.add(valueOrObject.equals("value") ? subject.getSubjectName() : subject);
                }
            }
        }
        return subjects1;
    }


    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
        List<Object> res = new ArrayList<>();
        switch (nameField) {
            case "subject_name":
                for (Subject subject : subjects) {
                    res.add(subject.getSubjectName());
                }
                break;
            case "id", "subject_id":
                for (Subject subject : subjects) {
                    res.add(subject.getId());
                }
                break;
            default:
                throw new FieldNotFoundInTableException("error");
        }
        return res;
    }

    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
        switch (nameField) {
            case "full_name":
                List<Object> fullNames = new ArrayList<>();
                for (Object obj : listObj) {
                    Subject subject = (Subject) obj;
                    long subject_id = subject.getId();
                    List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
                    for (var object : gradeList) {
                        long student_id = ((Grade) object).getStudentId();
                        Student student = (Student) students.getAllObjectsByField("id", String.valueOf(student_id)).get(0);
                        fullNames.add(valueOrObject.equals("value") ? student.getFullName() : student);
                    }
                }
                return fullNames;
            case "group_name":
                List<Object> groupNames = new ArrayList<>();
                for (Object obj : listObj) {
                    Subject subject = (Subject) obj;
                    long subject_id = subject.getId();
                    List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
                    for (var object : gradeList) {
                        long student_id = ((Grade) object).getStudentId();
                        Group group = (Group) groups.getAllObjectsByField("student_id", String.valueOf(student_id)).get(0);
                        groupNames.add(valueOrObject.equals("value") ? group.getGroupName() : group);
                    }
                }
                return groupNames;
            case "grade":
                List<Object> gradesList = new ArrayList<>();
                for (Object obj : listObj) { //предметы
                    Subject subject = (Subject) obj;
                    long subject_id = subject.getId();
                    List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
                    for (var object : gradeList) {
                        Grade grade = (Grade) object;
                        gradesList.add(valueOrObject.equals("value") ? grade.getGrade() : grade);
                    }
                }
                return gradesList;
            case "date":
                List<Object> dates = new ArrayList<>();
                for (Object obj : listObj) { //предметы
                    Subject subject = (Subject) obj;
                    long subject_id = subject.getId();
                    List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
                    for (var object : gradeList) {
                        Grade grade = (Grade) object;
                        dates.add(valueOrObject.equals("value") ? grade.getDate() : grade);
                    }
                }
                return dates;
            default:
                return null;
        }
    }
}
