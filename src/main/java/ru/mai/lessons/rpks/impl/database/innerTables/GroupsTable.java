package ru.mai.lessons.rpks.impl.database.innerTables;

import com.opencsv.CSVReader;
import lombok.Getter;
import ru.mai.lessons.rpks.impl.database.innerTables.tableClasses.Group;
import ru.mai.lessons.rpks.impl.database.joinedTables.TemplateTable;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GroupsTable implements TemplateTable {
    String name;
    List<String> fieldNames = List.of("id", "group_name", "student_id");
    List<Group> group = new ArrayList<>();

    public GroupsTable(String name) throws IOException {
        this.name = name;
        CSVReader reader = new CSVReader(new FileReader(name));
        reader.readNext();
        String[] line;
        while ((line = reader.readNext()) != null) {
            line = line[0].split(";");
            group.add(new Group(Integer.parseInt(line[0]), line[1], Integer.parseInt(line[2])));
        }
    }

    public Group getObjectByField(String fieldName, List<String> values) {
        if (fieldName.equals("full_name")) {
            for (var currGroup : group) {
                if (values.contains(currGroup.group_name())) {
                    return currGroup;
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
