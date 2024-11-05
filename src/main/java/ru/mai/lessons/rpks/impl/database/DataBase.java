package ru.mai.lessons.rpks.impl.database;

import lombok.Getter;
import ru.mai.lessons.rpks.impl.database.innerTables.GradesTable;
import ru.mai.lessons.rpks.impl.database.innerTables.GroupsTable;
import ru.mai.lessons.rpks.impl.database.innerTables.StudentsTable;
import ru.mai.lessons.rpks.impl.database.innerTables.SubjectsTable;

import java.io.IOException;

@Getter
public class DataBase {
    StudentsTable studentsTable;
    GroupsTable groupsTable;
    SubjectsTable subjectsTable;
    GradesTable gradesTable;

    public DataBase(String students, String groups, String subjects, String grades) throws IOException {

        studentsTable = new StudentsTable(students);
        groupsTable = new GroupsTable(groups);
        subjectsTable = new SubjectsTable(subjects);
        gradesTable = new GradesTable(grades);
    }
}
