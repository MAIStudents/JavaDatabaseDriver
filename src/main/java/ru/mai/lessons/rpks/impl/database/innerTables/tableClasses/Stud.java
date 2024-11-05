package ru.mai.lessons.rpks.impl.database.innerTables.tableClasses;

import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTableClass;

public record Stud(int id, String full_name) implements TemplateTableClass {

    @Override
    public int getStudentId() {
        return id;
    }

    public int getSubjectId() {
        return -1;
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
        return "id";
    }

    public String getSelectionParameterName() {
        return "full_name";
    }


}
