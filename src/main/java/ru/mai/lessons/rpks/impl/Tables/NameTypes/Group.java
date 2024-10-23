package ru.mai.lessons.rpks.impl.Tables.NameTypes;

public class Group extends NameType {
    long id;
    String groupName;
    long studentId;

    public Group(int id, String name, long studentId) {
        this.id = id;
        this.groupName = name;
        this.studentId = studentId;
    }
    public Group() {}

    public long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getStudentId() {
        return studentId;
    }
}
