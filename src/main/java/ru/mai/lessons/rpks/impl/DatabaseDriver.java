package ru.mai.lessons.rpks.impl;

import lombok.Getter;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseDriver implements IDatabaseDriver {

  private static final String crutch_path = "src/test/resources/";

  private final Map<Query, AnswerWithDataSource> cachedQueries = new HashMap<>();

  private static class AnswerWithDataSource {
    @Getter
    private final List<String> answer;
    @Getter
    private final String studFilePath;
    @Getter
    private final long studModifyTime;
    @Getter
    private final String groupFilePath;
    @Getter
    private final long groupModifyTime;
    @Getter
    private final String subjFilePath;
    @Getter
    private final long subjModifyTime;
    @Getter
    private final String gradeFilePath;

    public AnswerWithDataSource(List<String> answer, String studFilePath, long studModifyTime, String groupFilePath, long groupModifyTime, String subjFilePath, long subjModifyTime, String gradeFilePath, long gradeModifyTime) {
      this.answer = answer;
      this.studFilePath = studFilePath;
      this.studModifyTime = studModifyTime;
      this.groupFilePath = groupFilePath;
      this.groupModifyTime = groupModifyTime;
      this.subjFilePath = subjFilePath;
      this.subjModifyTime = subjModifyTime;
      this.gradeFilePath = gradeFilePath;
      this.gradeModifyTime = gradeModifyTime;
    }

    @Getter
    private final long gradeModifyTime;


  }

  private static boolean checkInputFiles(File first, File second, File third, File fourth) {
    return first.isFile() && first.canRead() && second.isFile() && second.canRead() && third.isFile() && third.canRead() && fourth.isFile() && fourth.canRead();
  }


  private static boolean checkAnswer(AnswerWithDataSource ans, File studFile, File groupFile, File subjFile, File gradeFile) {
    return studFile.getAbsolutePath() == ans.getStudFilePath() && groupFile.getAbsolutePath() == ans.getGroupFilePath() && subjFile.getAbsolutePath() == ans.getSubjFilePath() && gradeFile.getAbsolutePath() == ans.getGradeFilePath() &&
            studFile.lastModified() == ans.getStudModifyTime() && groupFile.lastModified() == ans.getGroupModifyTime() && subjFile.lastModified() == ans.getSubjModifyTime() && gradeFile.lastModified() == ans.getGradeModifyTime();
  }

  @Override
  public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                           String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {

    File studFile = new File(crutch_path + studentsCsvFile);
    File groupFile = new File(crutch_path + groupsCsvFile);
    File subjFile = new File(crutch_path + subjectsCsvFile);
    File gradeFile = new File(crutch_path + gradeCsvFile);



    if (!checkInputFiles(studFile, groupFile, subjFile, gradeFile)) {
      throw new WrongCommandFormatException("Passed paths to data files are incorrect");
    }

    Query q = Query.parseQuery(command);

    if (cachedQueries.containsKey(q) && checkAnswer(cachedQueries.get(q), studFile, groupFile, subjFile, gradeFile)) {
      return cachedQueries.get(q).getAnswer();
    }

    AnswerWithDataSource ans = new AnswerWithDataSource(q.execute(studFile, groupFile, subjFile, gradeFile), studFile.getAbsolutePath(),
            studFile.lastModified(), groupFile.getAbsolutePath(), groupFile.lastModified(), subjFile.getAbsolutePath(), subjFile.lastModified(),
            gradeFile.getAbsolutePath(), gradeFile.lastModified());

    cachedQueries.put(q, ans);

    return ans.getAnswer(); // реализовать проверку
  }
}
