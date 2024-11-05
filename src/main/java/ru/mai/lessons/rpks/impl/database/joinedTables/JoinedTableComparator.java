package ru.mai.lessons.rpks.impl.database.joinedTables;

import java.util.Comparator;

public class JoinedTableComparator implements Comparator<JoinedTable> {
    private final String sortBy;

    public JoinedTableComparator(String sortBy) {
        this.sortBy = sortBy;
    }

    @Override
    public int compare(JoinedTable t1, JoinedTable t2) {
        return switch (sortBy) {
            case "id" -> t1.getStudent().id() == t2.getStudent().id()
                    ? t2.getSubject().subject_name().compareTo(t1.getSubject().subject_name())
                    : t1.getStudent().id() - t2.getStudent().id();
            case "students" -> t1.getStudent().full_name().compareTo(t2.getStudent().full_name());
            case "groups" -> t1.getGroup().group_name().compareTo(t2.getGroup().group_name());
            case "subjects" -> t1.getSubject().subject_name().compareTo(t2.getSubject().subject_name());
            case "grade" -> t1.getGrade().getGrade() - t2.getGrade().getGrade();
            default -> throw new RuntimeException("Unsupported sort by " + sortBy);
        };
    }
}
