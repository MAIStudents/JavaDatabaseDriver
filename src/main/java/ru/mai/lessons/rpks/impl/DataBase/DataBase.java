package ru.mai.lessons.rpks.impl.DataBase;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.Parser.Query;
import ru.mai.lessons.rpks.impl.Parser.ConditionNode;

import java.util.*;

public class DataBase {

    public String name = "";
    public List<String> columnsNames = new ArrayList<>();
    public List<Line> lines = new ArrayList<>();

    public DataBase() {}

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
        result.columnsNames.addAll(columnsNames);

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
        result.columnsNames.addAll(columnsNames);
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
        result.columnsNames.addAll(columnsNames);

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
        result.name = "merged";
        result.columnsNames.addAll(columnsNames);
        result.columnsNames.addAll(right.columnsNames);

        int index = result.findColumnIndex(columnName);

        for (var line : lines) {
            String thisLine = line.getElement(index);
            DataBase db = right.findData(rightColumnName, line1 -> line1.equals(thisLine));
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
    private DataBase executeCondition(ConditionNode statement)
            throws WrongCommandFormatException, FieldNotFoundInTableException {
        if (statement.type == ConditionNode.NodeType.EXECUTED) {
            return new DataBase(statement.db);
        }
        if (statement.type == ConditionNode.NodeType.CONDITION) {
            return findData(statement.column, line -> line.equals(statement.value));
        }
        throw new WrongCommandFormatException("Not executable statement");
    }
    private static DataBase executeOperator(DataBase left, DataBase right, ConditionNode op)
            throws WrongCommandFormatException {
        if (op.type != ConditionNode.NodeType.OPERATOR) {
            throw new WrongCommandFormatException("Not operator statement");
        }
        if (op.operator.equals("AND")) {
            return left.andCondition(right);
        }
        if (op.operator.equals("OR")) {
            return left.orCondition(right);
        }
        throw new WrongCommandFormatException("Not existing operator");
    }
    private DataBase processTree(ConditionNode node)
            throws FieldNotFoundInTableException, WrongCommandFormatException {
        // Изначально была идея строить дерево выражения, но всем оказалось всё равно
        // на приоритет операторов и нет вложенных условий и важен лишь порядок слева направо, а я старался. :((((((
        if (node == null) {
            throw new WrongCommandFormatException("Wrong condition");
        }
        if (node.type == ConditionNode.NodeType.CONDITION) {
            return executeCondition(node);
        }
        DataBase right = processTree(node.right);
        DataBase left = processTree(node.left);

        return executeOperator(left, right, node);
    }
    public void sortBy(String primaryColumn) throws FieldNotFoundInTableException {
        int primaryIndex = findColumnIndex(primaryColumn);

        lines.sort((line1, line2) -> {
            String primaryValue1 = null;
            String primaryValue2 = null;
            try {
                primaryValue1 = line1.getElement(primaryIndex);
                primaryValue2 = line2.getElement(primaryIndex);
            } catch (FieldNotFoundInTableException e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
                throw new RuntimeException("how you get here");
            }
            return primaryValue1.compareTo(primaryValue2);

        });
    }

    public void sortBy(String primaryColumn, String secondaryColumn) throws FieldNotFoundInTableException {
        int primaryIndex = findColumnIndex(primaryColumn);
        int secondaryIndex = findColumnIndex(secondaryColumn);

        lines.sort((line1, line2) -> {
            try {
                int primaryComparison = line1.getElement(primaryIndex).compareTo(line2.getElement(primaryIndex));
                return primaryComparison != 0
                        ? primaryComparison
                        : line1.getElement(secondaryIndex).compareTo(line2.getElement(secondaryIndex));
            } catch (FieldNotFoundInTableException e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
                throw new RuntimeException("how you get here");
            }
        });
    }

    public DataBase whereInDB(Query query) throws WrongCommandFormatException, FieldNotFoundInTableException {
        if (query.whereConditionNodes.isEmpty()) {
            return new DataBase(this);
        }
        DataBase result = new DataBase();
        result.columnsNames.addAll(columnsNames);
        List<ConditionNode> lst = new ArrayList<>(query.whereConditionNodes);
        Collections.reverse(lst);

        DataBase temp = executeCondition(lst.get(0));

        for (int i = 1; i < lst.size(); ++i) {
            if (lst.get(i).type == ConditionNode.NodeType.OPERATOR) {
                var next = lst.get(i + 1);
                DataBase right = executeCondition(next);
                temp = executeOperator(temp, right, lst.get(i));
                ++i;
            } else {
                throw new WrongCommandFormatException("Wrong sequence");
            }
        }
        if (columnsNames.contains("id") && columnsNames.contains("date")){
            temp.sortBy("id", "date");
        }

        return temp;
    }

    public DataBase groupByColumn(String columnName) throws FieldNotFoundInTableException {
        if (columnName == null) {
            return new DataBase(this);
        }

        DataBase result = new DataBase();
        result.columnsNames.addAll(columnsNames);

        if (columnsNames.contains("student_id")) {
            this.sortBy("student_id");
        } else if (columnsNames.contains("id")) {
            this.sortBy("id");
        }


        int index = findColumnIndex(columnName);

        Set<String> seenValues = new HashSet<>();

        for (var line : lines) {
            var val = line.getElement(index);

            if (!seenValues.contains(val)) {
                result.lines.add(line);
                seenValues.add(val);
            }
        }

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
