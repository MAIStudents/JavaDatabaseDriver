package ru.mai.lessons.rpks.impl.DB.Tables;

public class Students {
    private Integer id;
    private String fullName;

    public Students(Integer id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Integer getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}
