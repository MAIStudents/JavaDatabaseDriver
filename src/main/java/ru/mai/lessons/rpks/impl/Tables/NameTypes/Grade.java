package ru.mai.lessons.rpks.impl.Tables.NameTypes;

public class Grade extends NameType {
    long subjectId;
    int grade;
    long studentId;
    String date;

    public Grade(int subjectId, int grade, long studentId, String date) {
        this.subjectId = subjectId;
        this.grade = grade;
        this.studentId = studentId;
        this.date = date;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public int getGrade() {
        return grade;
    }

    public long getStudentId() {
        return studentId;
    }

    public String getDate() {
        return date;
    }

    public Grade() {}
}
