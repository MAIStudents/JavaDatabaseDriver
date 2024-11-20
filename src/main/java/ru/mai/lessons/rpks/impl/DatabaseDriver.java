package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.DataBase.DataBase;
import ru.mai.lessons.rpks.impl.Parser.FileParser;
import ru.mai.lessons.rpks.impl.Parser.QueryParser;
import ru.mai.lessons.rpks.impl.Parser.Query;
import ru.mai.lessons.rpks.impl.Parser.ConditionNode;

import java.util.List;

public class DatabaseDriver implements IDatabaseDriver {

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {

    Query query = QueryParser.parse(command);
    System.out.printf(query.toString());

    DataBase students = FileParser.parseFile(studentsCsvFile);
    DataBase groups = FileParser.parseFile(groupsCsvFile);
    DataBase subjects = FileParser.parseFile(subjectsCsvFile);
    DataBase grades = FileParser.parseFile(gradeCsvFile);

    DataBase resulting = DataBase.joinSelectedTables(query, students, groups, subjects, grades);
    System.out.printf(resulting.toString());

    return resulting.selectColumns(query.selectColumns);
  }
}
