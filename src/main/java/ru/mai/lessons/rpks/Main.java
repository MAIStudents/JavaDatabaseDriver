package ru.mai.lessons.rpks;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.DatabaseDriver;

import java.text.Format;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Main {
  public static void main(String[] args)
      throws FieldNotFoundInTableException, WrongCommandFormatException {

    Pattern queryPattern =
            Pattern.compile("^ *SELECT= *([a-zA-Z_]+( *, *[a-zA-Z_]+)*) +FROM= *([a-zA-Z_]+\\.csv( *, *[a-zA-Z_]+\\.csv)*)( +WHERE= *([a-zA-Z_]+ *= *'[a-zA-Z0-9_]+'( +(AND|OR) +[a-zA-Z_]+ *= *'[a-zA-Z0-9_]+')*))? *(GROUPBY= *([a-zA-Z_]+( *, *[a-zA-Z_]+)*))? *$");


    String q = "SELECT=group_name, b FROM=students.csv, g.csv WHERE= a='x' AND b = 'x' GROUPBY= a, b";

    Matcher m = queryPattern.matcher(q);
    m.matches();

    System.out.println(m.groupCount());

    for (int i = 1; i <= m.groupCount(); ++i) {
      System.out.printf("%d ", i);
      System.out.println(m.group(i));
    }

//    log.info("Start service DatabaseDriver");
//    IDatabaseDriver service = new DatabaseDriver(); // ваша реализация service
//    String studentsCsvFile = args[0];
//    String groupsCsvFile = args[1];
//    String subjectsCsvFile = args[2];
//    String gradeCsvFile = args[3];
//    String command = args[4];
//    List<String> results = service.find(studentsCsvFile, groupsCsvFile, subjectsCsvFile,
//                                        gradeCsvFile, command);
//    log.info("Found data: {}", results);
//    log.info("Terminate service DatabaseDriver");
  }
}