package ru.mai.lessons.rpks.impl.DataBase;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.Parser.QueryParser;
import ru.mai.lessons.rpks.impl.Parser.Query;
import ru.mai.lessons.rpks.impl.Parser.ConditionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataBase {

    public String name = "";
    public List<String> columnsNames = new ArrayList<>();
    public List<Line> lines = new ArrayList<>();

    public DataBase(){};

    public DataBase(DataBase other) {
        this.name = other.name;
        this.columnsNames = new ArrayList<>(other.columnsNames);

        this.lines = new ArrayList<>();
        for (Line line : other.lines) {
            this.lines.add(new Line(line));
        }
    }


    public int findColumnIndex(String columnName) throws FieldNotFoundInTableException {
        for (int i = 0; i < columnsNames.size(); i++) {
            if (columnsNames.get(i).equals(columnName)) {
                return i;
            }
        }
        throw new FieldNotFoundInTableException(columnName);
    }

    public DataBase findData(String columnName, ITableCompare<String> comparator)
            throws FieldNotFoundInTableException {
        DataBase result = new DataBase();
        int index = findColumnIndex(columnName);

        for (var line : lines) {
            if (comparator.predicate(line.getElement(index))) {
                result.lines.add(new Line(line));
            }
        }
        return result;
    }
    public DataBase orCondition(DataBase dataBase) {
        DataBase result = new DataBase();
        result.lines.addAll(lines);
        for (var line : dataBase.lines) {
            if (!result.lines.contains(line)) {
                result.lines.add(line);
            }
        }
        return result;
    }

    public DataBase andCondition(DataBase dataBase) {
        DataBase result = new DataBase();
        for (var line : lines) {
            if (dataBase.lines.contains(line)) {
                result.lines.add(line);
            }
        }
        return result;
    }

    public void insertData(Line val) {
        lines.add(val);
    }

    public DataBase joinDatabases(String columnName, DataBase right, String rightColumnName)
            throws FieldNotFoundInTableException { // cross join
        DataBase result = new DataBase();
        result.columnsNames.addAll(columnsNames);
        result.columnsNames.addAll(right.columnsNames);

        int index = findColumnIndex(columnName);

        for (var line : lines) {
            String thisLine = line.getElement(index);
            DataBase db = right.findData(rightColumnName, new ITableCompare<String>() {
                @Override
                public boolean predicate(String line) {
                    return line.equals(thisLine);
                }
            });
            for (var extraLine : db.lines) {
                result.lines.add(Line.mergeLines(line, extraLine));
            }
        }

        return result;
    }

    public List<String> selectColumns(List<String> names) throws FieldNotFoundInTableException {
        List<String> result = new ArrayList<>();
        for (Line line : lines) {
            List<String> builder = new ArrayList<>();
            for (String columnName : names) {
                int index = findColumnIndex(columnName);
                builder.add(line.getElement(index));
            }
            result.add(String.join(";", builder));
        }
        return result;
    }

    public static DataBase joinSelectedTables(Query query, DataBase students, DataBase groups,
                                       DataBase subjects, DataBase grades)
            throws FieldNotFoundInTableException, WrongCommandFormatException {
        List<String> fromFiles = query.fromFiles;
        DataBase result = null;

        if (fromFiles.contains(students.name) && fromFiles.contains(groups.name)) {
            result = students.joinDatabases("id", groups, "student_id");
        } else if (fromFiles.contains(students.name)) {
            result = new DataBase(students);
        } else if (fromFiles.contains(groups.name)) {
            result = new DataBase(groups);
        }

        if (fromFiles.contains(grades.name) && fromFiles.contains(students.name) && result != null) {
            result = result.joinDatabases("id", grades, "student_id");
        } else if (fromFiles.contains(grades.name) && fromFiles.contains(groups.name) && result != null) {
            result = result.joinDatabases("student_id", grades, "student_id");
        } else if (fromFiles.contains(grades.name)) {
            result = new DataBase(grades);
        }

        if (fromFiles.contains(subjects.name) && fromFiles.contains(grades.name) && result != null) {
            result = result.joinDatabases("subject_id", subjects, "id");
        } else if (fromFiles.contains(subjects.name) && result == null) {
            result = new DataBase(subjects);
        } else if (fromFiles.contains(subjects.name)) {
            throw new WrongCommandFormatException("Не удалось найти подходящие таблицы для объединения.");
        }

        if (result == null) {
            throw new WrongCommandFormatException("Не удалось найти подходящие таблицы для объединения.");
        }

        return result;
    }



    public DataBase groupByColumn(String columnName) throws FieldNotFoundInTableException {
        DataBase result = new DataBase();
        result.columnsNames.addAll(columnsNames);
        int index = findColumnIndex(columnName);

        // idk what it means

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\nName: ").append(name).append("\n");
        columnsNames.forEach(line -> result.append(line).append(" "));
        result.append("\n");
        lines.forEach(line -> result.append(line.toString()).append("\n"));
        return result.toString();
    }

}
