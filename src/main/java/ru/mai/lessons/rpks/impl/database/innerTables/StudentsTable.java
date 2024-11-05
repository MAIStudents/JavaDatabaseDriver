package ru.mai.lessons.rpks.impl.database.innerTables;

import com.opencsv.CSVReader;
import lombok.Getter;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Stud;
import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTable;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class StudentsTable implements TemplateTable {
    String name;
    List<String> fieldNames = List.of("id", "full_name");
    List<Stud> students = new ArrayList<>();

    public StudentsTable(String name) throws IOException {
        this.name = name;
        CSVReader reader = new CSVReader(new FileReader(name));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            students.add(new Stud(Integer.parseInt(line[0]), line[1]));
        }
    }

    public Stud getObjectByField(String fieldName, List<String> values) {
        if (fieldName.equals("full_name")) {
            for (var student : students) {
                if (values.contains(student.full_name())) {
                    return student;
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
