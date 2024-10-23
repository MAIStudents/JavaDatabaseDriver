package ru.mai.lessons.rpks.impl.Tables.NameTypes;

public class Subject extends NameType {
    long id;
    String subjectName;

    public Subject(int id, String name) {
        this.id = id;
        this.subjectName = name;
    }
    public Subject() {}

    public long getId() {
        return id;
    }

    public String getSubjectName() {
        return subjectName;
    }
}
