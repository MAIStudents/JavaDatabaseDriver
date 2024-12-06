package ru.mai.lessons.rpks.impl;

import lombok.Getter;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Query {
    @Getter
    private final List<String> selectColumns;
    @Getter
    private final List<String> usedFiles;
    @Getter
    private final List<ConditionalExpressionData> whereExpression;
    @Getter
    private final String groupByColumn;

    /**
     * 1 group - select list
     * 3 group - from list
     * 6 group - where expression
     * 10 group - groupby list
     */
    private static final Pattern queryPattern =
            Pattern.compile("^ *SELECT= *([a-zA-Z_]+( *, *[a-zA-Z_]+)*) +FROM= *([a-zA-Z_]+\\.csv( *, *[a-zA-Z_]+\\.csv)*)( +WHERE= *\\(([a-zA-Z_]+ *= *'[ а-яА-Яa-zA-Z0-9_]+'( +(AND|OR) +[a-zA-Z_]+ *= *'[ а-яА-Яa-zA-Z0-9_]+')*)\\))? *(GROUPBY= *([a-zA-Z_]+( *, *[a-zA-Z_]+)*))? *$");

    private static final int selectGroupId = 1;
    private static final int fromGroupId = 3;
    private static final int whereGroupId = 6;
    private static final int groupByGroupId = 10;

    private static final String crutch_path = "src/test/resources/";

    public Query(List<String> selectColumns, List<String> usedFiles, List<ConditionalExpressionData> whereExpression, String groupByColumn) {
        this.selectColumns = selectColumns;
        this.usedFiles = usedFiles;
        this.whereExpression = whereExpression;
        this.groupByColumn = groupByColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return Objects.equals(selectColumns, query.selectColumns) && Objects.equals(usedFiles, query.usedFiles) && Objects.equals(whereExpression, query.whereExpression) && Objects.equals(groupByColumn, query.groupByColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectColumns, usedFiles, whereExpression, groupByColumn);
    }

    public static class ConditionalExpressionData {
        @Getter
        private final String columnName;
        @Getter
        private final String dataValue;
        private final boolean isAndWithPrevious;

        public ConditionalExpressionData(String columnName, String dataValue, boolean isAndWithPrevious) {
            this.columnName = columnName;
            this.dataValue = dataValue;
            this.isAndWithPrevious = isAndWithPrevious;
        }

        public boolean getIsAndWithPrevious() {
            return isAndWithPrevious;
        }
    }

    private static List<String> parseStringToList(String line) {
        if (line == null)
            return new ArrayList<>();

        List<String> res = Arrays.asList(line.split(","));

        for (String str : res) {
            str = str.strip();
        }

        return res;
    }

    private static List<ConditionalExpressionData> parseStringToExpr(String line) {
        if (line == null)
            return new ArrayList<>();

        int currentInd = 0;

        List<String> lexemList = new ArrayList<>();
        List<Boolean> isAndList = new ArrayList<>();

        int orFirstInd = line.indexOf("OR");
        int andFirstInd = line.indexOf("AND");

        while ((orFirstInd != -1 || andFirstInd != -1)) {
            int current = orFirstInd;

            if (current == -1 || (andFirstInd < current && andFirstInd != -1)) {
                current = andFirstInd;
            }

            lexemList.add(line.substring(currentInd, current));
            isAndList.add(current == andFirstInd);

            currentInd = current + 3;
            orFirstInd = line.indexOf("OR", currentInd);
            andFirstInd = line.indexOf("AND", currentInd);
        }

        lexemList.add(line.substring(currentInd));

        List<ConditionalExpressionData> res = new ArrayList<>();

        for (int i = 0; i < lexemList.size(); ++i) {
            String str = lexemList.get(i);

            String[] strs = str.split("=");

            String columnName = strs[0].strip();
            String value = strs[1].strip();
            value = value.substring(1, value.length() - 1);

            res.add(new ConditionalExpressionData(columnName, value, i == 0 || isAndList.get(i - 1)));
        }

        return res;
    }

    public static Query parseQuery(String string) throws WrongCommandFormatException {
        Matcher matcher = queryPattern.matcher(string);

        if (!matcher.matches()) {
            throw new WrongCommandFormatException("Query has error!");
        }

        List<String> usedFiles = parseStringToList(matcher.group(fromGroupId));

        for (int i = 0; i < usedFiles.size(); ++i) {
            File n = new File(crutch_path + usedFiles.get(i));

            usedFiles.set(i, n.getAbsolutePath());
        }

        List<String> selectColumns = parseStringToList(matcher.group(selectGroupId));
        List<ConditionalExpressionData> whereExpression = parseStringToExpr(matcher.group(whereGroupId));
        List<String> groupByColumns = parseStringToList(matcher.group(groupByGroupId));

        if (!groupByColumns.isEmpty()) {
            for (String sc : selectColumns) {
                if (!groupByColumns.contains(sc)) {
                    throw new WrongCommandFormatException("If you use group by expression, you must select only grouped columns");
                }
            }
        }

        return new Query(selectColumns, usedFiles, whereExpression, groupByColumns.isEmpty() ? null : groupByColumns.get(0));
    }

    private Database combineDBs(Database student, Database group, Database subj, Database grade) throws FieldNotFoundInTableException, WrongCommandFormatException {

        Database res = null;

        if (usedFiles.contains(student.getName()) && usedFiles.contains(group.getName())) {
            res = student.innerJoinWith("id", group, "student_id");

        } else if (usedFiles.contains(student.getName())) {
            res = new Database(student);
        } else if (usedFiles.contains(group.getName())) {
            res = new Database(group);
        }

        if (usedFiles.contains(grade.getName()) && usedFiles.contains(student.getName()) && res != null) {
            res = res.innerJoinWith("id", grade, "student_id");
        } else if (usedFiles.contains(grade.getName()) && usedFiles.contains(group.getName()) && res != null) {
            res = res.innerJoinWith("student_id", grade, "student_id");
        } else if (usedFiles.contains(grade.getName())) {
            res = new Database(grade);
        }

        if (usedFiles.contains(subj.getName()) && usedFiles.contains(grade.getName()) && res != null) {
            res = res.innerJoinWith("subject_id", subj, "id");
        } else if (usedFiles.contains(subj.getName()) && res == null) {
            res = new Database(subj);
        } else if (usedFiles.contains(subj.getName())) {
            throw new WrongCommandFormatException("Tables were not found");
        }

        if (res == null) {
            throw new WrongCommandFormatException("Tables were not found");
        }

        return res;
    }

    public List<String> execute(File studFile, File groupFile, File subjFile, File gradeFile) throws FieldNotFoundInTableException, WrongCommandFormatException {
        List<String> res = new ArrayList<>();

        try(BufferedReader studIn = new BufferedReader(new FileReader(studFile, StandardCharsets.UTF_8))) {
            try (BufferedReader groupIn = new BufferedReader(new FileReader(groupFile, StandardCharsets.UTF_8))) {
                try (BufferedReader subjIn = new BufferedReader(new FileReader(subjFile, StandardCharsets.UTF_8))) {
                    try (BufferedReader gradeIn = new BufferedReader(new FileReader(gradeFile, StandardCharsets.UTF_8))) {

                        Database student = new Database(studIn, studFile.getAbsolutePath());
                        Database group = new Database(groupIn, groupFile.getAbsolutePath());
                        Database subject = new Database(subjIn, subjFile.getAbsolutePath());
                        Database grade = new Database(gradeIn, gradeFile.getAbsolutePath());

                        Database resultDB = combineDBs(student, group, subject, grade);

                        res = resultDB.answerToQuery(this);

                    }
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return res;
    }
}
