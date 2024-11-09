package ru.mai.lessons.rpks.impl.Parsing;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;
import ru.mai.lessons.rpks.impl.DB.Tables.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ParseFiles {
    private static final String PATH = "src/test/resources/";
    public static CsvFileData getCsvData(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile, String gradeCsvFile) {
        CsvFileData csvData = new CsvFileData();
        CSVFormat csvFormat = CSVFormat.DEFAULT
          .builder()
          .setHeader()
          .setSkipHeaderRecord(true)
          .setDelimiter(';')
          .build();
        try (Reader readerStudents = new FileReader(PATH + studentsCsvFile);
            Reader readerGroups = new FileReader(PATH + groupsCsvFile);
            Reader readerSubjects = new FileReader(PATH + subjectsCsvFile);
            Reader readerGrade = new FileReader(PATH + gradeCsvFile);
            CSVParser csvParserStudents = new CSVParser(readerStudents, csvFormat);
            CSVParser csvParserGroups = new CSVParser(readerGroups, csvFormat);
            CSVParser csvParserSubjects = new CSVParser(readerSubjects, csvFormat);
            CSVParser csvParserGrade = new CSVParser(readerGrade, csvFormat)) {
          List<Students> studentsData = new ArrayList<>();
          List<String> nameColumns = new ArrayList<>();
          if (csvParserStudents.getHeaderNames() != null) {
            nameColumns.addAll(csvParserStudents.getHeaderNames());
          }
          for (CSVRecord csvRecord : csvParserStudents) {
            int id = Integer.parseInt(csvRecord.get(nameColumns.get(0)));
            String studentName = csvRecord.get(nameColumns.get(1));
            studentsData.add(new Students(id, studentName));  
          }
          csvData.addFileData(studentsCsvFile, new ArrayList<>(nameColumns), studentsData);
          nameColumns.clear();
    
          if (csvParserGroups.getHeaderNames() != null) {
            nameColumns.addAll(csvParserGroups.getHeaderNames());
          }
          List<Groups> groupsData = new ArrayList<>();
          for (CSVRecord csvRecord : csvParserGroups) {
            int id = Integer.parseInt(csvRecord.get(nameColumns.get(0)));
            String groupsName = csvRecord.get(nameColumns.get(1));
            int studentId = Integer.parseInt(csvRecord.get(nameColumns.get(2)));
            groupsData.add(new Groups(id, studentId, groupsName));  
          }
          csvData.addFileData(groupsCsvFile, new ArrayList<>(nameColumns), groupsData);
          nameColumns.clear();
    
          if (csvParserGrade.getHeaderNames() != null) {
            nameColumns.addAll(csvParserGrade.getHeaderNames());
          }
          List<Grade> gradesData = new ArrayList<>();
          for (CSVRecord csvRecord : csvParserGrade) {
            int subjectId = Integer.parseInt(csvRecord.get(nameColumns.get(0)));
            int studentId = Integer.parseInt(csvRecord.get(nameColumns.get(1)));
            int grade = Integer.parseInt(csvRecord.get(nameColumns.get(2)));
            String date = csvRecord.get(nameColumns.get(3));
            gradesData.add(new Grade(subjectId, studentId, grade, date));  
          }
          csvData.addFileData(gradeCsvFile, new ArrayList<>(nameColumns), gradesData);
          nameColumns.clear();
    
          if (csvParserSubjects.getHeaderNames() != null) {
            nameColumns.addAll(csvParserSubjects.getHeaderNames());
          }
          List<Subjects> subjectsData = new ArrayList<>();
          for (CSVRecord csvRecord : csvParserSubjects) {
            int id = Integer.parseInt(csvRecord.get(nameColumns.get(0)));
            String subjectName = csvRecord.get(nameColumns.get(1));
            subjectsData.add(new Subjects(id, subjectName));  
          }
          csvData.addFileData(subjectsCsvFile, new ArrayList<>(nameColumns), subjectsData);
          nameColumns.clear();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return csvData;
    }
}
