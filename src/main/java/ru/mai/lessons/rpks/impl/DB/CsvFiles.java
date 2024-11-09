package ru.mai.lessons.rpks.impl.DB;

public class CsvFiles {
    private String studentsCsvFile;
    private String groupsCsvFile;
    private String subjectsCsvFile;          
    private String gradeCsvFile;
    public CsvFiles(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile, String gradeCsvFile) {
        this.studentsCsvFile = studentsCsvFile;
        this.groupsCsvFile = groupsCsvFile;
        this.subjectsCsvFile = subjectsCsvFile;
        this.gradeCsvFile = gradeCsvFile;
    }

    public String getStudentsCsvFile() {
        return studentsCsvFile;
    }

    public String getGroupsCsvFile() {
        return groupsCsvFile;
    }

    public String getSubjectsCsvFile() {
        return subjectsCsvFile;
    }

    public String getGradeCsvFile() {
        return gradeCsvFile;
    }

    public boolean equalsAnyField(String str) {
    if (str == null) {
        return false;
    }

    return str.equals(studentsCsvFile) ||
        str.equals(groupsCsvFile) ||
        str.equals(subjectsCsvFile) ||
        str.equals(gradeCsvFile);
    }
}
