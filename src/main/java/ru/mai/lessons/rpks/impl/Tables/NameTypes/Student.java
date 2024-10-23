package ru.mai.lessons.rpks.impl.Tables.NameTypes;

public class Student extends NameType {
    long id;
    String fullName;
    public Student(long id, String name) {
        this.id = id;
        this.fullName = name;
    }
    public Student() {}

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}
