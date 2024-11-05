package ru.mai.lessons.rpks.impl.database.joinedTables;

import java.util.List;

public interface TemplateTable {
    TemplateTableClass getObjectByField(String nameField, List<String> value);

    String getTableName();
}
