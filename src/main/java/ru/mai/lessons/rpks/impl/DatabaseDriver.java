package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.DataBase.DataBase;
import ru.mai.lessons.rpks.impl.Parser.FileParser;
import ru.mai.lessons.rpks.impl.Parser.QueryParser;
import ru.mai.lessons.rpks.impl.Parser.Query;

import java.util.HashMap;
import java.util.List;

public class DatabaseDriver implements IDatabaseDriver {
  private static final HashMap<Integer, List<String>> queries = new HashMap<>();

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {

    Query query = QueryParser.parse(command);
    if (queries.containsKey(query.hashCode())) {
      return queries.get(query.hashCode());
    }

    DataBase students = FileParser.parseFile(studentsCsvFile);
    DataBase groups = FileParser.parseFile(groupsCsvFile);
    DataBase subjects = FileParser.parseFile(subjectsCsvFile);
    DataBase grades = FileParser.parseFile(gradeCsvFile);

    DataBase joined = DataBase.joinSelectedTables(query, students, groups, subjects, grades);
    DataBase withWhere = joined.whereInDB(query);
    DataBase withGroupBy = withWhere.groupByColumn(query.groupByColumn);
    var resulting = withGroupBy.selectColumns(query.selectColumns);

    if (resulting.isEmpty()) {
      resulting.add("");
    }

    queries.put(query.hashCode(), resulting);
    return resulting;
  }
}
