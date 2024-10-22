package ru.mai.lessons.rpks.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseDriver implements IDatabaseDriver {

  private static final String PREFIX = "src/test/resources/";
  StudentTable students;
  GradeTable grades;
  SubjectTable subjects;
  GroupTable groups;
  List<String> keyWords = List.of(new String[]{"SELECT=", "FROM=", "WHERE=", "GROUPBY="});

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {
    try {
      students = new StudentTable(readStudentsCSV(PREFIX + studentsCsvFile));
      grades = new GradeTable(readGradeCSV(PREFIX + gradeCsvFile));
      subjects = new SubjectTable(readSubjectsCSV(PREFIX + subjectsCsvFile));
      groups = new GroupTable(readGroupsCSV(PREFIX + groupsCsvFile));
    } catch (IOException e) {
      e.printStackTrace();
    }
    Map<String, List<String>> whereList = new HashMap<>();
    StringBuilder groupBySb = new StringBuilder();
    List<String[]> parseList = parseQuery(command, whereList, groupBySb);
    if (whereList.isEmpty()) {
      whereList = null;
    }
    String[] columns = parseList.get(0);
    String[] tables = parseList.get(1);
    String groupBy =  (groupBySb.isEmpty())  ? null : groupBySb.toString();
    List<String> result = getData(columns, tables, whereList, groupBy);
    if (result.isEmpty()) {
      result = List.of("");
    }
    return result;
  }
  Table getTableByName(String tableName) {
    return switch (tableName) {
      case "students.csv" -> students;
      case "grade.csv" -> grades;
      case "subjects.csv" -> subjects;
      case "groups.csv" -> groups;
      default -> null;
    };
  }
  String[] getColumns(String query) {
    String columns = query.substring("SELECT=".length(), query.indexOf("FROM=") - 1);
    return (columns.isEmpty())  ? null : columns.split(",");
  }
  String[] getTables(String query) {
    int endIndex;
    if (query.contains("WHERE=")) {
      endIndex = query.indexOf(" WHERE");
    }
    else if (query.contains("GROUPBY=")) {
      endIndex = query.indexOf(" GROUPBY");
    }
    else endIndex = query.length();
    String tempQuery = query.substring(query.indexOf("FROM=") + 5, endIndex);
    if (tempQuery.length() < 2) return null;
    String[] tempQuerySplit = tempQuery.split(",");
    List<String> tables = new ArrayList<>();
    int i = 0;
    while (i < tempQuerySplit.length && !keyWords.contains(tempQuerySplit[i])) {
      tables.add(tempQuerySplit[i]);
      i++;
    }
    String[] res = new String[tables.size()];
    return tables.toArray(res);
  }
  void getWhere(String query, Map<String, List<String>> res) {
    if (!query.contains("WHERE=")) {
      return;
    }
    String querySubstring = query.substring(query.indexOf("WHERE=(") + "WHERE=(".length(), query.indexOf(")"));
    if (!querySubstring.contains("AND") && !querySubstring.contains("OR")) {
      res.put("info", List.of("-"));
      String nameColon = querySubstring.substring(0, querySubstring.indexOf("="));
      String answer = querySubstring.substring(querySubstring.indexOf("'") + 1, querySubstring.lastIndexOf("'"));
      if (res.containsKey(nameColon)) {
        res.get(nameColon).add(answer);
      } else {
        res.put(nameColon, List.of(answer));
      }
    }
    else if (!querySubstring.contains("AND") && querySubstring.contains("OR")) {
      res.put("info", List.of("OR"));
      String[] querySplitOr = querySubstring.split(" OR ");
      getMapForWhere(res, querySplitOr);
    } else if (querySubstring.contains("AND") && !querySubstring.contains("OR")) {
      res.put("info", List.of("AND"));
      String[] querySplitAnd = querySubstring.split(" AND ");
      getMapForWhere(res, querySplitAnd);
    }
    else if (querySubstring.contains("AND") && querySubstring.contains("OR")) {
    }
  }

  public void getMapForWhere(Map<String, List<String>> res, String[] querySplit) {
    for (var each : querySplit) {
      String nameColon = each.substring(0, each.indexOf("="));
      String answer = each.substring(each.indexOf("'") + 1, each.lastIndexOf("'"));
      if (res.containsKey(nameColon)) {
        res.get(nameColon).add(answer);
      } else {
        List<String> list = new ArrayList<>();
        list.add(answer);
        res.put(nameColon, list);
      }
    }
  }

  void validateQuery(String query) throws WrongCommandFormatException {
    // Регулярные выражения для частей команды
    String selectPattern = "SELECT=([a-zA-Z_]+)(,[a-zA-Z_]+)*";
    String fromPattern = "FROM=([a-zA-Z0-9_.]+)(,[a-zA-Z0-9_.]+)*";
    String wherePattern = "(WHERE=\\(\\s*((([a-zA-Z_]+=[^()=]+)(\\s+(AND|OR)\\s+([a-zA-Z_]+=[^()=]+))*)?)\\s*\\))?";
    String groupByPattern = "(GROUPBY=[a-zA-Z_]+)?";

    // Формирование общего шаблона для проверки
    String fullPattern = String.format(
            "^%s\\s+%s\\s*%s\\s*%s?$",
            selectPattern, fromPattern, wherePattern, groupByPattern
    );

    Pattern pattern = Pattern.compile(fullPattern);
    Matcher matcher = pattern.matcher(query);
    if (!matcher.matches()) {
      throw new WrongCommandFormatException("error");
    }
  }
  public Table getTableWithField(String field, String[] tables) {
    for (var table : tables) {
      if (getTableByName(table).getFields().contains(field)) {
        return getTableByName(table);
      }
    }
    return null;
  }
  public Table getTableByClass(Class class_) {
    if (class_.equals(GradeTable.class)) {
      return grades;
    } else if (class_.equals(Group.class)) {
      return groups;
    }
    else {
      return null;
    }
  }
  public void parseKeys(Set<String> keySet, List<List<Object>> filteredListsByWhere, List<Table> tablesWithValues,
                        Map<String, List<String>> whereList, String[] tables) throws FieldNotFoundInTableException {
    int index = 0;
    for (var key : keySet) { //key - название колонки
      List<String> values = whereList.get(key); //возможные значения
      Table tableWithRelateFieldsWithField = getTableWithField(key, tables); //таблица которая содержит колонку
      if (tableWithRelateFieldsWithField == null && !key.equals("info")) {
        throw new FieldNotFoundInTableException("error");
      }
      if (tableWithRelateFieldsWithField != null) {
        filteredListsByWhere.add(new ArrayList<>());
        filteredListsByWhere.get(index).addAll(tableWithRelateFieldsWithField.getAllObjectsByField(key, values, "object"));
        tablesWithValues.add(tableWithRelateFieldsWithField);
      }
      index++;
    }
  }
  public void fillListForCollumns(Set<String> keySet, List<Table> tablesWithValues, Map<String,
          List<String>> whereList, List<List<List<Object>>> listWithValuesForCollumns, int indexAll, String column) {
    listWithValuesForCollumns.add(new ArrayList<>());
    int indexTable = 0;
    for (var key : keySet) { //[full_name, subject]
      Table table = tablesWithValues.get(indexTable);
      if (!key.equals("info")) {
        List<String> values = whereList.get(key); //[ivanov ivan, petrov kolya]
        List<Object> listObj = table.getAllObjectsByField(key, values, "object");//[student1, student2, ..]
        List<Object> list = new ArrayList<>();
        for (var each : listObj) {
          List<Object> listVal = table.getRelateObjects(List.of(each), column, "value"); //[m8o-313, v90-454]
          list.addAll(listVal);
        }
        listWithValuesForCollumns.get(indexAll).add(list);
      }
      indexTable++;
      if (indexTable == tablesWithValues.size()) {
        indexTable = 0;
      }
    }
  }
  public List<String> fromObjectToStringList(List<Object> list) {
    List<String> stringList = new ArrayList<>();
    for (Object obj : list) {
      stringList.add(obj.toString());
    }
    return stringList;
  }
  public List<Object> getListIntersections(List<List<Object>> data) {
    List<Object> res = new ArrayList<>();
    for (List<Object> each : data) {
      List<String> list = fromObjectToStringList(each);
      if (res.isEmpty()) {
        res.addAll(list);
      } else {
        res.retainAll(list);
      }
    }
    return res;
  }
  public void getIntersections(List<List<List<Object>>> allList, List<List<Object>> data) {
    for (List<List<Object>> valuesForWhere : allList) {
      List<Object> listIntersections = getListIntersections(valuesForWhere);
      data.add(listIntersections);
    }
  }
  public List<List<Object>> getDataWithWhere(Map<String, List<String>> whereList, String[] tables, String[] columns) throws FieldNotFoundInTableException {
    Set<String> keySet = whereList.keySet();
    List<List<Object>> data = new ArrayList<>();
    List<List<Object>> filteredListsByWhere = new ArrayList<>();
    List<Table> tablesWithValues = new ArrayList<>();
    parseKeys(keySet, filteredListsByWhere, tablesWithValues, whereList, tables);
    //filteredListsByWhere - листы со значениями соотв-щим where
    //tablesWithValues - соответствующие им таблицы для получения нужных полей
    List<List<List<Object>>> listWithValuesForColumns = new ArrayList<>(columns.length); //[ col1[  [values for where1],[values for where2]  ], col2[  ], ..      ]
    int indexAll = 0;
    for (var column : columns) {
      if (whereList.get("info").get(0).equals("AND")) {
        fillListForCollumns(keySet, tablesWithValues, whereList, listWithValuesForColumns, indexAll, column);
        indexAll++;
      } else {
        for (int i = 0; i < tablesWithValues.size(); i++) {
          if (!tablesWithValues.get(i).getFields().contains(column)) {
            Table table = tablesWithValues.get(i);
            List<Object> list = table.getRelateObjects(filteredListsByWhere.get(i), column, "value");
            data.add(list);
          } else {
            List<Object> list = new ArrayList<>(filteredListsByWhere.get(i));
            data.add(list);
          }
        }
      }
    }
    getIntersections(listWithValuesForColumns, data);
    return data;
  }
  public List<String> getRes(List<List<Object>> data, int length) {
    List<String> res = new ArrayList<>();
    int j = 0;
    while (j < data.get(0).size()) {
      StringBuilder sb = new StringBuilder();
      for (int num = 0; num < length; num++) {
        sb.append(data.get(num).get(j));
        if (num != length - 1) {sb.append(";");}
      }
      res.add(sb.toString());
      j++;
    }
    return res;
  }
  List<Object> getSetOfList(List<Object> list) {
    List<Object> res = new ArrayList<>();
    for (Object obj : list) {
      if (!res.contains(obj) && Collections.frequency(list, obj) > 1) {
        res.add(obj);
      }
    }
    return res;
  }
  public List<String> getData(String[] columns, String[] tables, Map<String, List<String>> whereList, String groupBy)
          throws FieldNotFoundInTableException {
    List<List<Object>> data = new ArrayList<>();
    if (whereList != null) {
      data = getDataWithWhere(whereList, tables, columns);
    } else {
      for (String column : columns) {
        Table table = getTableWithField(column, tables);
        if (table != null) {
          data.add(table.getField(column));
        } else {
          throw new FieldNotFoundInTableException("error");
        }
      }
    }
    if (groupBy != null) {
      List<List<Object>> newData = new ArrayList<>();
      for (List<Object> datum : data) {
        newData.add(getSetOfList(datum));
      }
      data = newData;
    }
    return getRes(data, columns.length);
  }
  void getGroupBy(String query, StringBuilder groupBy) {
    if (!query.contains("GROUPBY=")) {
      return;
    }
    groupBy.append(query.substring(query.indexOf("GROUPBY=") + 8));
  }
  public List<String[]> parseQuery(String query, Map<String, List<String>> whereList, StringBuilder groupBy) throws WrongCommandFormatException {
    validateQuery(query);
    List<String[]> res = new ArrayList<>();
    String[] columns = getColumns(query);
    String[] tables = getTables(query);
    getWhere(query, whereList);
    getGroupBy(query, groupBy);
    if (tables == null || columns == null) {
      throw new WrongCommandFormatException("command syntax error");
    }
    res.add(columns);
    res.add(tables);
    return res;
  }

  public interface Table {
    List<Object> getField(String nameField) throws FieldNotFoundInTableException;
    List<String> getFields();
    List<Object> getAllObjectsByField(String nameField, String value);
    List<Object> getAllObjectsByField(String nameField, List<String> value, String valueOrObject);
    List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject);
  }

  public class StudentTable implements Table {
    List<Student> students;
    List<String> fields = List.of("id", "full_name");
    public StudentTable(List<Student> students) {
      this.students = students;
    }
    @Override
    public List<String> getFields() {
      return fields;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
      List<Object> students1 = new ArrayList<>();
      if (nameField.equals("id") || nameField.equals("student_id")) {
        int dependingValue = Integer.parseInt(value);
        for (var student : students) {
          if (student.id == dependingValue) {
            students1.add(student);
          }
        }
      }
      else if (nameField.equals("full_name")) {
        for (var student : students) {
          if (student.fullName.equals(value)) {
            students1.add(student);
          }
        }
      }
      return students1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
      List<Object> students1 = new ArrayList<>();
      if (nameField.equals("id") || nameField.equals("student_id")) {
        List<Integer> dependingValues = new ArrayList<>();
        for (var each : values) {
          dependingValues.add(Integer.parseInt(each));
        }
        for (var student : students) {
          if (dependingValues.contains(student.id)) {
            students1.add(valueOrObject.equals("value") ? student.id : student);
          }
        }
      }
      else if (nameField.equals("full_name")) {
        for (var student : students) {
          if (values.contains(student.fullName)) {
            students1.add(valueOrObject.equals("value") ? student.fullName : student);
          }
        }
      }
      return students1;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
      List<Object> res = new ArrayList<>();
      switch (nameField) {
        case "full_name":
          for (Student student : students) {
            res.add(student.fullName);
          };
          break;
        case "id", "student_id":
          for (Student student : students) {
            res.add(student.id);
          }
          break;
        default:
          throw new FieldNotFoundInTableException("error");
      }
      return res;
    }

    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
      List<Object> res = new ArrayList<>();
      switch (nameField) {
        case "group_name":
          for (Object obj : listObj) {
            Student student = (Student) obj;
            long student_id = student.id;
            List<Object> grList = groups.getAllObjectsByField("student_id", String.valueOf(student_id));
            for (var object : grList) {
              Group group = (Group) object;
              res.add(valueOrObject.equals("value") ? group.groupName : group);
            }
          }
          return res;
        case "grade":
          for (Object obj : listObj) {
            Student student = (Student) obj;
            long student_id = student.id;
            List<Object> grList = grades.getAllObjectsByField("student_id", String.valueOf(student_id));
            for (var object : grList) {
              Grade grade = (Grade) object;
              res.add(valueOrObject.equals("value") ? grade.grade : grade);
            }
          }
          return res;
        case "date":
          for (Object obj : listObj) {
            Student student = (Student) obj;
            long student_id = student.id;
            List<Object> grList = grades.getAllObjectsByField("student_id", String.valueOf(student_id));
            for (var object : grList) {
              Grade grade = (Grade) object;
              res.add(valueOrObject.equals("value") ? grade.date : grade);
            }
          }
          return res;
        case "subject_name":
          for (Object obj : listObj) {
            Student student = (Student) obj;
            long student_id = student.id;
            List<Object> grList = grades.getAllObjectsByField("student_id", String.valueOf(student_id));
            for (var object : grList) {
              Grade grade = (Grade) object;
              Subject subject = (Subject) subjects.getAllObjectsByField("subject_id", String.valueOf(grade.subjectId)).get(0);
              res.add(valueOrObject.equals("value") ? subject.subjectName : subject);
            }
          }
          return res;
        default:
          return null;
      }
    }
  }

  public class GroupTable implements Table {
    List<Group> groups;
    List<String> fields = List.of("id", "group_name", "student_id");
    public GroupTable(List<Group> groups) {
      this.groups = groups;
    }
    @Override
    public List<String> getFields() {
      return fields;
    }
    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
      List<Object> res = new ArrayList<>();
      switch (nameField) {
        case "group_name":
          for (Group group : groups) {
            res.add(group.groupName);
          }
          break;
        case "student_id":
          for (Group group : groups) {
            res.add(group.studentId);
          }
          break;
        case "id", "group_id":
          for (Group group : groups) {
            res.add(group.id);
          }
          break;
        default:
          throw new FieldNotFoundInTableException("error");
      }
      return res;
    }
    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
      List<Object> groups1 = new ArrayList<>();
      switch (nameField) {
        case "id", "group_id" -> {
          int dependingValue = Integer.parseInt(value);
          for (var group : groups) {
            if (group.id == dependingValue) {
              groups1.add(group);
            }
          }
        }
        case "student_id" -> {
          int dependingValue = Integer.parseInt(value);
          for (var group : groups) {
            if (group.studentId == dependingValue) {
              groups1.add(group);
            }
          }
        }
        case "group_name" -> {
          for (var group : groups) {
            if (group.groupName.equals(value)) {
              groups1.add(group);
            }
          }
        }
      }
      return groups1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
      List<Object> groups1 = new ArrayList<>();
      switch (nameField) {
        case "id", "group_id" -> {
          List<Integer> dependingValues = new ArrayList<>();
          for (var each : values) {
            dependingValues.add(Integer.parseInt(each));
          }
          for (var group : groups) {
            if (dependingValues.contains(group.id)) {
              groups1.add(valueOrObject.equals("value") ? group.id : group);
            }
          }
        }
        case "student_id" -> {
          List<Integer> dependingValues = new ArrayList<>();
          for (var each : values) {
            dependingValues.add(Integer.parseInt(each));
          }
          for (var group : groups) {
            if (dependingValues.contains(group.studentId)) {
              groups1.add(valueOrObject.equals("value") ? group.studentId : group);
            }
          }
        }
        case "group_name" -> {
          for (var group : groups) {
            if (values.contains(group.groupName)) {
              groups1.add(valueOrObject.equals("value") ? group.groupName : group);
            }
          }
        }
      }
      return groups1;
    }

    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
      List<Object> res = new ArrayList<>();
      switch (nameField) {
        case "full_name":
          for (Object obj : listObj) {
            Group group = (Group) obj;
            long student_id = group.studentId;
            List<Object> stList = students.getAllObjectsByField("id", String.valueOf(student_id));
            for (var object : stList) {
              Student student = (Student) object;
              res.add(valueOrObject.equals("value") ? student.fullName : student);
            }
          }
          return res;
        default:
          return null;
      }
    }
  }
  public class SubjectTable implements Table {
    List<Subject> subjects;
    List<String> fields = List.of("id", "subject_name");
    public SubjectTable(List<Subject> subjects) {
      this.subjects = subjects;
    }
    @Override
    public List<String> getFields() {
      return fields;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
      List<Object> subjects1 = new ArrayList<>();
      if (nameField.equals("id") || nameField.equals("subject_id")) {
        int dependingValue = Integer.parseInt(value);
        for (var subject : subjects) {
          if (subject.id == dependingValue) {
            subjects1.add(subject);
          }
        }
      }
      else if (nameField.equals("subject_name")) {
        for (var subject : subjects) {
          if (subject.subjectName.equals(value)) {
            subjects1.add(subject);
          }
        }
      }
      return subjects1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
      List<Object> subjects1 = new ArrayList<>();
      if (nameField.equals("id") || nameField.equals("subject_id")) {
        List<Integer> depenegingValues = new ArrayList<>();
        for (var each : values) {
          depenegingValues.add(Integer.parseInt(each));
        }
        for (var subject : subjects) {
          if (depenegingValues.contains(subject.id)) {
            subjects1.add(valueOrObject.equals("value") ? subject.id : subject);
          }
        }
      }
      else if (nameField.equals("subject_name")) {
        for (var subject : subjects) {
          if (values.contains(subject.subjectName)) {
            subjects1.add(valueOrObject.equals("value") ? subject.subjectName : subject);
          }
        }
      }
      return subjects1;
    }


    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
      List<Object> res = new ArrayList<>();
      switch (nameField) {
        case "subject_name":
          for (Subject subject : subjects) {
            res.add(subject.subjectName);
          }
          break;
        case "id", "subject_id":
          for (Subject subject : subjects) {
            res.add(subject.id);
          }
          break;
        default:
          throw new FieldNotFoundInTableException("error");
      }
      return res;
    }

    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
      switch (nameField) {
        case "full_name":
          List<Object> fullNames = new ArrayList<>();
          for (Object obj : listObj) {
            Subject subject = (Subject) obj;
            long subject_id = subject.id;
            List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
            for (var object : gradeList) {
              long student_id = ((Grade) object).studentId;
              Student student = (Student) students.getAllObjectsByField("id", String.valueOf(student_id)).get(0);
              fullNames.add(valueOrObject.equals("value") ? student.fullName : student);
            }
          }
          return fullNames;
        case "group_name":
          List<Object> groupNames = new ArrayList<>();
          for (Object obj : listObj) {
            Subject subject = (Subject) obj;
            long subject_id = subject.id;
            List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
            for (var object : gradeList) {
              long student_id = ((Grade) object).studentId;
              Group group = (Group) groups.getAllObjectsByField("student_id", String.valueOf(student_id)).get(0);
              groupNames.add(valueOrObject.equals("value") ? group.groupName : group);
            }
          }
          return groupNames;
        case "grade":
          List<Object> gradesList = new ArrayList<>();
          for (Object obj : listObj) { //предметы
            Subject subject = (Subject) obj;
            long subject_id = subject.id;
            List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
            for (var object : gradeList) {
              Grade grade = (Grade) object;
              gradesList.add(valueOrObject.equals("value") ? grade.grade : grade);
            }
          }
          return gradesList;
        case "date":
          List<Object> dates = new ArrayList<>();
          for (Object obj : listObj) { //предметы
            Subject subject = (Subject) obj;
            long subject_id = subject.id;
            List<Object> gradeList = grades.getAllObjectsByField("subject_id", String.valueOf(subject_id));
            for (var object : gradeList) {
              Grade grade = (Grade) object;
              dates.add(valueOrObject.equals("value") ? grade.date : grade);
            }
          }
          return dates;
        default:
          return null;
      }
    }
  }

  public class GradeTable implements Table {
    List<Grade> grades;
    List<String> fields = List.of("subject_id", "grade", "student_id", "date");
    public GradeTable(List<Grade> grades) {
      this.grades = grades;
    }
    @Override
    public List<String> getFields() {
      return fields;
    }
    @Override
    public List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject) {
      switch (nameField) {
        case "full_name":
          List<Object> students1 = new ArrayList<>();
          for (Object obj : listObj) {
            Grade grade = (Grade) obj;
            long student_id = grade.studentId;
            List<Object> stList = students.getAllObjectsByField("id", String.valueOf(student_id));
            for (var object : stList) {
              Student student = (Student) object;
              students1.add(valueOrObject.equals("value") ? student.fullName : student);
            }
          }
          return students1;
        case "group_name":
          List<Object> groups1 = new ArrayList<>();
          for (Object obj : listObj) {
            Grade grade = (Grade) obj;
            long student_id = grade.studentId;
            List<Object> stList = students.getAllObjectsByField("id", String.valueOf(student_id));
            for (var object : stList) {
              Student student = (Student) object;
              Group group = (Group) groups.getAllObjectsByField("student_id", String.valueOf(student.id)).get(0);
              groups1.add(valueOrObject.equals("value") ? group.groupName : group);
            }
          }
          return groups1;
        case "subject_name":
          List<Object> subjects1 = new ArrayList<>();
          for (Object obj : listObj) {
            Grade grade = (Grade) obj;
            long subject_id = grade.subjectId;
            List<Object> subList = subjects.getAllObjectsByField("subject_id", String.valueOf(subject_id));
            for (var object : subList) {
              Subject subject = (Subject) object;
              subjects1.add(valueOrObject.equals("value") ? subject.subjectName : subject);
            }
          }
          return subjects1;
        default:
          return null;
      }
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, String value) {
      List<Object> grades1 = new ArrayList<>();
      switch (nameField) {
        case "subject_id" -> {
          int dependingValue = Integer.parseInt(value);
          for (var grade : grades) {
            if (grade.subjectId == dependingValue) {
              grades1.add(grade);
            }
          }
        }
        case "date" -> {
          for (var grade : grades) {
            if (grade.date.equals(value)) {
              grades1.add(grade);
            }
          }
        }
        case "grade" -> {
          for (var grade : grades) {
            if (grade.grade == Integer.parseInt(value)) {
              grades1.add(grade);
            }
          }
        }
        case "student_id" -> {
          for (var grade : grades) {
            if (grade.studentId == Integer.parseInt(value)) {
              grades1.add(grade);
            }
          }
        }
      }
      return grades1;
    }

    @Override
    public List<Object> getAllObjectsByField(String nameField, List<String> values, String valueOrObject) {
      List<Object> grades1 = new ArrayList<>();
      switch (nameField) {
        case "subject_id" -> {
          List<Integer> dependingValues = new ArrayList<>();
          for (var each : values) {
            dependingValues.add(Integer.parseInt(each));
          }
          for (var grade : grades) {
            if (dependingValues.contains(grade.subjectId)) {
              grades1.add(valueOrObject.equals("value") ? grade.subjectId : grade);
            }
          }
        }
        case "date" -> {
          for (var grade : grades) {
            if (values.contains(grade.date)) {
              grades1.add(valueOrObject.equals("value") ? grade.date : grade);
            }
          }
        }
        case "grade" -> {
          List<Integer> dependingValues = new ArrayList<>();
          for (var each : values) {
            dependingValues.add(Integer.parseInt(each));
          }
          for (var grade : grades) {
            if (dependingValues.contains(grade.grade)) {
              grades1.add(valueOrObject.equals("value") ? grade.grade : grade);
            }
          }
        }
        case "student_id" -> {
          List<Integer> dependingValues = new ArrayList<>();
          for (var each : values) {
            dependingValues.add(Integer.parseInt(each));
          }
          for (var grade : grades) {
            if (dependingValues.contains(grade.studentId)) {
              grades1.add(valueOrObject.equals("value") ? grade.studentId : grade);
            }
          }
        }
      }
      return grades1;
    }

    @Override
    public List<Object> getField(String nameField) throws FieldNotFoundInTableException {
      List<Object> res = new ArrayList<>();
      switch (nameField) {
        case "grade":
          for (Grade grade : grades) {
            res.add(grade.grade);
          }
          break;
        case "student_id":
          for (Grade grade : grades) {
            res.add(grade.studentId);
          }
          break;
        case "subject_id":
          for (Grade grade : grades) {
            res.add(grade.subjectId);
          }
          break;
        case "date":
          for (Grade grade : grades) {
            res.add(grade.date);
          }
          break;
      }
      return res;
    }
  }

  public class NameType extends Object {

  }

  public class Student extends NameType {
    long id;
    String fullName;
    public Student(long id, String name) {
      this.id = id;
      this.fullName = name;
    }
    public Student() {}

  }

  public class Group extends NameType {
    long id;
    String groupName;
    long studentId;

    public Group(int id, String name, long studentId) {
      this.id = id;
      this.groupName = name;
      this.studentId = studentId;
    }
    public Group() {}
  }

  public class Subject extends NameType {
    long id;
    String subjectName;

    public Subject(int id, String name) {
      this.id = id;
      this.subjectName = name;
    }
    public Subject() {}
  }

  public class Grade extends NameType {
    long subjectId;
    int grade;
    long studentId;
    String date;

    public Grade(int subjectId, int grade, long studentId, String date) {
      this.subjectId = subjectId;
      this.grade = grade;
      this.studentId = studentId;
      this.date = date;
    }
    public Grade() {}
  }


  private List<Student> readStudentsCSV(String filename) throws IOException {
    List<Student> students = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filename))) {
      String[] headers = reader.readNext();
      String[] line;
      while ((line = reader.readNext()) != null) {
        for (int i = 0; i < headers.length; i++) {
          String[] values = line[i].split(";");
          Student student = new Student(Integer.parseInt(values[0]), values[1]);
          students.add(student);
        }
      }
    } catch (CsvValidationException e) {
      throw new RuntimeException(e);
    }
    return students;
  }

  private List<Subject> readSubjectsCSV(String filename) throws IOException {
    List<Subject> subjects = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filename))) {
      String[] headers = reader.readNext();
      String[] line;
      while ((line = reader.readNext()) != null) {
        for (int i = 0; i < headers.length; i++) {
          String[] values = line[i].split(";");
          Subject subject = new Subject(Integer.parseInt(values[0]), values[1]);
          subjects.add(subject);
        }
      }
    } catch (CsvValidationException e) {
      throw new RuntimeException(e);
    }
    return subjects;
  }

  private List<Group> readGroupsCSV(String filename) throws IOException {
    List<Group> groups = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader(filename))) {
      String[] headers = reader.readNext();
      String[] line;
      while ((line = reader.readNext()) != null) {
        for (int i = 0; i < headers.length; i++) {
          String[] values = line[i].split(";");
          Group group = new Group(Integer.parseInt(values[0]), values[1], Integer.parseInt(values[2]));
          groups.add(group);
        }
      }
    } catch (CsvValidationException e) {
      throw new RuntimeException(e);
    }
    return groups;
  }

  private List<Grade> readGradeCSV(String filename) throws IOException {
    List<Grade> grades = new ArrayList<>(); //subject_id;student_id;grade;date
    try (CSVReader reader = new CSVReader(new FileReader(filename))) {
      String[] headers = reader.readNext();
      String[] line;
      while ((line = reader.readNext()) != null) {
        for (int i = 0; i < headers.length; i++) {
          String[] values = line[i].split(";");
          Grade grade = new Grade(Integer.parseInt(values[0]), Integer.parseInt(values[2]),
                  Integer.parseInt(values[1]), values[3]);
          grades.add(grade);
        }
      }
    } catch (CsvValidationException e) {
      throw new RuntimeException(e);
    }
    return grades;
  }
}
