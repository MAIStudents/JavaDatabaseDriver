package ru.mai.lessons.rpks.impl.database.joinedTables;

import lombok.Getter;
import lombok.Setter;
import ru.mai.lessons.rpks.impl.databaseManager.Request;
import ru.mai.lessons.rpks.impl.database.DataBase;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Grade;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Group;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Stud;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class JoinedTable {
    Stud student;
    Grade grade;
    Subject subject;
    Group group;

    public List<JoinedTable> createJoinedTable(DataBase dataBase, Request request) {
        List<JoinedTable> joinedTables = new ArrayList<>();
        List<Stud> students = request.getTables().length == 1 && Arrays.toString(request.getTables()).contains("groups.csv")
                ? getStudentsById(dataBase.getGroupsTable().getGroup(), dataBase)
                : dataBase.getStudentsTable().getStudents();
        for (Stud student : students) {
            List<Grade> grades = getGradesByStudentId(student, dataBase);
            for (Grade grade : grades) {
                Subject subject = getSubjectById(grade, dataBase);
                Group group = getGroupById(student, dataBase);
                JoinedTable joinedTable = new JoinedTable();
                joinedTable.student = student;
                joinedTable.grade = grade;
                joinedTable.subject = subject;
                joinedTable.group = group;
                joinedTables.add(joinedTable);
            }
        }
        return joinedTables;
    }


    private List<Grade> getGradesByStudentId(Stud student, DataBase dataBase) {
        List<Grade> result = new ArrayList<>();
        List<Grade> grades = dataBase.getGradesTable().getGrades();
        for (Grade grade : grades) {
            if (grade.getStudentId() == student.id()) {
                result.add(grade);
            }
        }
        return result;
    }

    private Subject getSubjectById(Grade grade, DataBase dataBase) {
        List<Subject> subjects = dataBase.getSubjectsTable().getSubjects();
        for (Subject subject : subjects) {
            if (subject.getSubjectId() == grade.getSubjectId()) {
                return subject;
            }
        }
        return null;
    }

    private Group getGroupById(Stud student, DataBase dataBase) {
        List<Group> groups = dataBase.getGroupsTable().getGroup();
        for (Group group : groups) {
            if (group.getStudentId() == student.id()) {
                return group;
            }
        }
        return null;
    }

    private List<Stud> getStudentsById(List<Group> groups, DataBase dataBase) {
        List<Stud> result = new ArrayList<>();
        for (Group group : groups) {
            for (Stud studs : dataBase.getStudentsTable().getStudents()) {
                if (studs.getStudentId() == group.getStudentId()) {
                    result.add(studs);
                }
            }
        }
        return result;
    }
}

