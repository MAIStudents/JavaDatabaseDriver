package ru.mai.lessons.rpks.impl.database.innerTables;

import com.opencsv.CSVReader;
import lombok.Getter;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Grade;
import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTable;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GradesTable implements TemplateTable {
    String name;
    List<String> fieldNames = List.of("subject_id", "student_id", "grade", "date");
    List<Grade> grades = new ArrayList<>();

    public GradesTable(String name) throws IOException {
        this.name = name;
        CSVReader reader = new CSVReader(new FileReader(name));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            grades.add(new Grade(Integer.parseInt(line[0]), Integer.parseInt(line[1]), Integer.parseInt(line[2]), line[3]));
        }
    }


    @Override
    public Grade getObjectByField(String fieldName, List<String> values) {
        if (fieldName.equals("grade")) {
            List<Integer> dependingValues = new ArrayList<>();
            for (String value : values) {
                dependingValues.add(Integer.parseInt(value));
            }
            for (Grade grade : grades) {
                if (dependingValues.contains(grade.grade())) {
                    return grade;
                }
            }
        }
        return null;
    }

    @Override
    public String getTableName() {
        return name;
    }
}
