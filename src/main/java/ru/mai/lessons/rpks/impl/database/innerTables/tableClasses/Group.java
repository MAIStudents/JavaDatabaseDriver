package ru.mai.lessons.rpks.impl.database.innerTables.tableClasses;

import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTableClass;

public record Group(int id, String group_name, int student_id) implements TemplateTableClass {

    @Override
    public int getStudentId() {
        return student_id;
    }

    public int getSubjectId() {
        return -1;
    }

    @Override
    public int getGroup() {
        return id;
    }

    @Override
    public String date() {
        return null;
    }

    public String getSelectionParameterId() {
        return "g_id";
    }

    public String getSelectionParameterName() {
        return "group_name";
    }

    public String getSelectionParameterStudentId() {
        return "s_id";
    }

}