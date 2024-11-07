package ru.mai.lessons.rpks.impl.DB.Tables;

public class Grade {
    private Integer subjectId;
    private Integer studentId;
    private Integer grade;
    private String date;

    public Grade(Integer subjectId, Integer studentId, Integer grade, String date) {
        this.subjectId = subjectId;
        this.studentId = studentId;
        this.grade = grade;
        this.date = date;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public Integer getGrade() {
        return grade;
    }

    public String getDate() {
        return date;
    }
}
