package ru.mai.lessons.rpks.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.Matcher.ProccessData;
import ru.mai.lessons.rpks.impl.Selection.Selection;
import ru.mai.lessons.rpks.impl.Tables.LoadTables;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseDriver implements IDatabaseDriver {

  List<String> keyWords = List.of(new String[]{"SELECT=", "FROM=", "WHERE=", "GROUPBY="});
  Map<String, List<String>> cash = null;

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {
    if (cash == null) {
      cash = new HashMap<>();
    } else if (cash.containsKey(command)) {
      return cash.get(command);
    }
    LoadTables loader = new LoadTables(gradeCsvFile, subjectsCsvFile, groupsCsvFile, studentsCsvFile);
    loader.loadTables();

    ProccessData processor = new ProccessData(command);
    processor.parse();

    Selection selection = new Selection(processor.getColumns(), processor.getTables(), processor.getWhereList(),
            processor.getGroupBy(), loader);

    List<String> result = selection.getSelection();
    cash.put(command, result);
    return result;
  }
}
