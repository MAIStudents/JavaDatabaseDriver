package ru.mai.lessons.rpks.impl.DB.Tables;

public class Groups {
    private Integer id;
    private Integer studentId;
    private String groupName;

    public Groups(Integer id, Integer studentId,  String groupsName) {
        this.id = id;
        this.studentId = studentId;
        this.groupName = groupsName;
    }

    public Integer getId() {
        return id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getGroupName() {
        return groupName;
    }
}
