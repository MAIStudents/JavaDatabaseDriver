package ru.mai.lessons.rpks.impl.database.innerTables.tableClasses;

import lombok.Getter;
import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTableClass;

public record Grade(int subject_id, int student_id, @Getter int grade, String date) implements TemplateTableClass {

    @Override
    public int getStudentId() {
        return student_id;
    }

    @Override
    public int getSubjectId() {
        return subject_id;
    }

    @Override
    public int getGroup() {
        return -1;
    }

    public String getSelectionParameterSubjectId() {
        return "subject_id";
    }

    public String getSelectionParameterStudentId() {
        return "student_id";
    }

    public String getSelectionParameterGrade() {
        return "grade";
    }

    public String getSelectionParameterDate() {
        return "date";
    }

}

