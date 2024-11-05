package ru.mai.lessons.rpks.impl.databaseManager;

import lombok.Getter;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.database.DataBase;
import ru.mai.lessons.rpks.impl.database.joinedTables.JoinedTable;
import ru.mai.lessons.rpks.impl.database.joinedTables.JoinedTableComparator;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.*;
import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTable;
import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTableClass;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class DatabaseManager {
    long dateOfRequest;
    static DataBase dataBase;
    TreeMap<String, Request> cashOfRequests = new TreeMap<>();
    @Getter
    private final String STUD_TABLE;
    @Getter
    private final String GROUP_TABLE;
    @Getter
    private final String SUBJECTS_TABLE;
    @Getter
    private final String GRADES_TABLE;

    public DatabaseManager(String students, String groups, String subjects, String grades) throws IOException, ParseException {
        dateOfRequest = System.currentTimeMillis();
        readFromCache();
        cleanCache(students, groups, subjects, grades);
        dataBase = new DataBase(students, groups, subjects, grades);
        STUD_TABLE = students;
        GROUP_TABLE = groups;
        SUBJECTS_TABLE = subjects;
        GRADES_TABLE = grades;
    }

    public List<String> getRequest(String request, String prefix) throws WrongCommandFormatException, FieldNotFoundInTableException {
        if (cashOfRequests.containsKey(request)) {
            return cashOfRequests.get(request).result;
        }
        Request tmpRequest = new Request(request, prefix);
        checkFields(tmpRequest);
        tmpRequest.result = getResultOfRequest(tmpRequest);
        cashOfRequests.put(request, tmpRequest);
        writeToCacheFile();
        return tmpRequest.result;
    }

    public List<String> getResultOfRequest(Request request) throws FieldNotFoundInTableException {

        JoinedTable t = new JoinedTable();
        List<JoinedTable> unionTable = t.createJoinedTable(dataBase, request);

        return getData(request, unionTable);
    }

    private TemplateTableClass getColumnsAndTables(Request request, String parameter, StringBuilder tableName) throws FieldNotFoundInTableException {
        String[] tmp = parameter.split("=");
        String column = tmp[0];
        String value = tmp[1].trim();
        value = value.substring(1, value.length() - 1);
        TemplateTable table = getTableByField(request, column);
        if (table == null) {
            throw new FieldNotFoundInTableException("Field not found in tables");

        }
        tableName.append(table.getTableName());
        return Objects.requireNonNull(getTableByField(request, column)).getObjectByField(column, List.of(value));
    }

    private List<String> getData(Request request, List<JoinedTable> unionTable) throws FieldNotFoundInTableException {
        Deque<String> tmpWhere = request.getWhereList();
        List<JoinedTable> tableWithFilers = tmpWhere == null ? unionTable : evaluateRpn(request, tmpWhere, unionTable);
        groupBy(request, tableWithFilers);

        return select(request, tableWithFilers);
    }

    private void groupBy(Request request, List<JoinedTable> unionTable) {
        if (Arrays.toString(request.getColumns()).contains("full_name")) {
            unionTable.sort(new JoinedTableComparator("id"));
        }
    }

    private List<String> select(Request request, List<JoinedTable> unionTable) {
        Set<String> tmp = new LinkedHashSet<>();
        for (JoinedTable currTable : unionTable) {
            StringBuilder currParam = new StringBuilder();
            for (String selectionParameter : request.getColumns()) {
                Stud stud = currTable.getStudent();
                Grade grade = currTable.getGrade();
                Group group = currTable.getGroup();
                Subject subject = currTable.getSubject();

                if (selectionParameter.equals(stud.getSelectionParameterId())) {
                    currParam.append(stud.id());
                    currParam.append(";");
                }
                if (selectionParameter.equals(stud.getSelectionParameterName())) {
                    currParam.append(stud.full_name());
                    currParam.append(";");
                }
                if (selectionParameter.equals(grade.getSelectionParameterSubjectId())) {
                    currParam.append(grade.getSubjectId());
                    currParam.append(";");
                }
                if (selectionParameter.equals(grade.getSelectionParameterStudentId())) {
                    currParam.append(grade.getStudentId());
                    currParam.append(";");
                }
                if (selectionParameter.equals(grade.getSelectionParameterGrade())) {
                    currParam.append(grade.grade());
                    currParam.append(";");
                }
                if (selectionParameter.equals(grade.getSelectionParameterDate())) {
                    currParam.append(grade.date());
                    currParam.append(";");
                }
                if (selectionParameter.equals(subject.getSelectionParameterId())) {
                    currParam.append(subject.id());
                    currParam.append(";");
                }
                if (selectionParameter.equals(subject.getSelectionParameterName())) {
                    currParam.append(subject.subject_name());
                    currParam.append(";");
                }
                if (selectionParameter.equals(group.getSelectionParameterId())) {
                    currParam.append(group.id());
                    currParam.append(";");
                }
                if (selectionParameter.equals(group.getSelectionParameterStudentId())) {
                    currParam.append(group.getStudentId());
                    currParam.append(";");
                }
                if (selectionParameter.equals(group.getSelectionParameterName())) {
                    currParam.append(group.group_name());
                    currParam.append(";");
                }
            }
            tmp.add(currParam.substring(0, currParam.length() - 1));
        }
        if (tmp.isEmpty()) {
            return List.of("");
        }
        return new ArrayList<>(tmp);
    }


    public List<JoinedTable> evaluateRpn(Request request, Deque<String> tokens, List<JoinedTable> unionTable) throws FieldNotFoundInTableException {
        Deque<List<JoinedTable>> deque = new ArrayDeque<>();

        Iterator<String> iterator = tokens.descendingIterator();

        while (iterator.hasNext()) {
            String token = iterator.next();
            if (token.equals("AND")) {
                List<JoinedTable> b1 = deque.removeLast();
                List<JoinedTable> b2 = deque.removeLast();
                deque.addLast(processAnd(b1, b2));
            } else if (token.equals("OR")) {
                List<JoinedTable> b1 = deque.removeLast();
                List<JoinedTable> b2 = deque.removeLast();
                deque.addLast(processOr(b1, b2));
            } else {
                deque.addLast(processSingleParameter(request, token, unionTable));
            }
        }
        return deque.pop();
    }

    private static List<JoinedTable> processAnd(List<JoinedTable> firstTable, List<JoinedTable> secondTable) {
        List<JoinedTable> result = new ArrayList<>();
        for (JoinedTable firstElement : firstTable) {
            for (JoinedTable secondElement : secondTable) {
                if (secondElement != null && secondElement.equals(firstElement)) {
                    result.add(firstElement);
                }
            }
        }
        return result;
    }


    private static List<JoinedTable> processOr(List<JoinedTable> firstTable, List<JoinedTable> secondTable) {
        List<JoinedTable> result = new ArrayList<>();

        List<JoinedTable> fst, snd;
        if (firstTable.size() > secondTable.size()) {
            fst = firstTable;
            snd = secondTable;
        } else {
            fst = secondTable;
            snd = firstTable;
        }
        for (int i = 0; i < fst.size(); i++) {
            result.add(fst.get(i));
            if (snd.size() > i && !snd.get(i).equals(fst.get(i))) {
                result.add(snd.get(i));
            }
        }
        return result;
    }

    private void checkFields(Request request) throws FieldNotFoundInTableException {
        String[] columns = request.getColumns();
        for (String column : columns) {
            boolean found = false;
            for (String currTable : request.getTables()) {
                if (currTable.equals(STUD_TABLE)) {
                    found = dataBase.getStudentsTable().getFieldNames().contains(column);
                } else if (currTable.equals(GROUP_TABLE)) {
                    found = dataBase.getGroupsTable().getFieldNames().contains(column);
                } else if (currTable.equals(GRADES_TABLE)) {
                    found = dataBase.getGradesTable().getFieldNames().contains(column);
                } else if (currTable.equals(SUBJECTS_TABLE)) {
                    found = dataBase.getSubjectsTable().getFieldNames().contains(column);
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                throw new FieldNotFoundInTableException("Field not found");
            }
        }
    }


    private List<JoinedTable> processSingleParameter(Request request, String parameter, List<JoinedTable> unionTable) throws FieldNotFoundInTableException {
        StringBuilder SbTableName = new StringBuilder();
        TemplateTableClass foundObject = getColumnsAndTables(request, parameter, SbTableName);
        List<JoinedTable> result = new ArrayList<>();
        if (foundObject == null) {
            return result;
        }

        String tableName = SbTableName.toString();

        for (JoinedTable currElement : unionTable) {
            if (tableName.equals(STUD_TABLE)) {
                if (currElement.getStudent().id() == foundObject.getStudentId()) {
                    result.add(currElement);
                }
            } else if (tableName.equals(GROUP_TABLE)) {
                if (currElement.getGroup().getGroup() == foundObject.getGroup()) {
                    result.add(currElement);
                }
            } else if (tableName.equals(SUBJECTS_TABLE)) {
                if (currElement.getSubject().id() == foundObject.getSubjectId()) {
                    result.add(currElement);
                }
            } else if (tableName.equals(GRADES_TABLE)) {
                if (foundObject instanceof Grade foundGrade) {
                    if (currElement.getGrade().getGrade() == foundGrade.getGrade()) {
                        result.add(currElement);
                    }
                }
            } else {
                throw new FieldNotFoundInTableException("Field not found in tables");
            }
        }
        return result;
    }


    private TemplateTable getTableByField(Request request, String field) {
        for (String currName : request.getTables()) {
            if (ifTableContainsColumn(currName, field)) {
                return getTableByName(currName);
            }
        }
        return null;
    }

    private TemplateTable getTableByName(String tableName) {
        if (tableName.equals(STUD_TABLE)) {
            return dataBase.getStudentsTable();
        } else if (tableName.equals(SUBJECTS_TABLE)) {
            return dataBase.getSubjectsTable();
        } else if (tableName.equals(GRADES_TABLE)) {
            return dataBase.getGradesTable();
        } else if (tableName.equals(GROUP_TABLE)) {
            return dataBase.getGroupsTable();
        }
        return null;
    }

    private boolean ifTableContainsColumn(String tableName, String column) {
        if (tableName.equals(STUD_TABLE)) {
            return dataBase.getStudentsTable().getFieldNames().contains(column);
        } else if (tableName.equals(SUBJECTS_TABLE)) {
            return dataBase.getSubjectsTable().getFieldNames().contains(column);
        } else if (tableName.equals(GRADES_TABLE)) {
            return dataBase.getGradesTable().getFieldNames().contains(column);
        } else if (tableName.equals(GROUP_TABLE)) {
            return dataBase.getGroupsTable().getFieldNames().contains(column);
        }
        return false;
    }


    long getDateOfLatestRequest(String... filePaths) {
        long result = 0;
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + filePath);
            }
            if (file.lastModified() > result) {
                result = file.lastModified();
            }
        }
        return result;
    }

    private void writeToCacheFile() {
        try (FileOutputStream fileOutputStream = new FileOutputStream("/Users/nikitatretakov/MAI_Java/tests/test.txt");
             ObjectOutputStream objectInputStream = new ObjectOutputStream(fileOutputStream)) {
            objectInputStream.writeObject(cashOfRequests);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromCache() {
        try (FileInputStream fileInputStream = new FileInputStream("/Users/nikitatretakov/MAI_Java/tests/test.txt");
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            cashOfRequests = (TreeMap<String, Request>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void cleanCache(String... filePaths) {

        long dateOfLatestRequest = getDateOfLatestRequest(filePaths);

        for (Map.Entry<String, Request> request : cashOfRequests.entrySet()) {
            if (request.getValue().getRequestDate() < dateOfLatestRequest) {
                cashOfRequests.remove(request.getKey());
            }
        }
    }
}
