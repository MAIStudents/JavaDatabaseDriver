package ru.mai.lessons.rpks.impl.DB.Tables;

public class Subjects {
    private Integer id;
    private String subjectName;
    
    public Subjects(Integer id, String subjectName) {
        this.id = id;
        this.subjectName = subjectName;
    }
    
    public Integer getId() {
        return id;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
}
