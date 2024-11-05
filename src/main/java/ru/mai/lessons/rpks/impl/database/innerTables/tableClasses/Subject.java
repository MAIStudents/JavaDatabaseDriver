package ru.mai.lessons.rpks.impl.database.innerTables.tableClasses;

import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTableClass;

public record Subject(int id, String subject_name) implements TemplateTableClass {

    @Override
    public int getStudentId() {
        return -1;
    }

    @Override
    public int getSubjectId() {
        return id;
    }

    @Override
    public int getGroup() {
        return -1;
    }

    @Override
    public String date() {
        return null;
    }

    public String getSelectionParameterId() {
        return "s_id";
    }

    public String getSelectionParameterName() {
        return "subject_name";
    }

}