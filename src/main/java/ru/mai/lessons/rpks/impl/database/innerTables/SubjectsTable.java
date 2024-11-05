package ru.mai.lessons.rpks.impl.database.innerTables;

import com.opencsv.CSVReader;
import lombok.Getter;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Subject;
import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTable;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SubjectsTable implements TemplateTable {
    String name;
    List<String> fieldNames = List.of("id", "subject_name");
    List<Subject> subjects = new ArrayList<>();

    public SubjectsTable(String name) throws IOException {
        this.name = name;
        CSVReader reader = new CSVReader(new FileReader(name));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            subjects.add(new Subject(Integer.parseInt(line[0]), line[1]));
        }
    }

    @Override
    public Subject getObjectByField(String nameField, List<String> values) {
        if (nameField.equals("subject_name")) {
            for (var subject : subjects) {
                if (values.contains(subject.subject_name())) {
                    return subject;
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
