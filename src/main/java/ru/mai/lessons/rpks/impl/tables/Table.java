package ru.mai.lessons.rpks.impl.tables;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.util.List;

public interface Table {
    List<String> getFieldNames();

    List<Object> getObjectsByField(String nameField, String value) throws FieldNotFoundInTableException;

    List<Object> getObjectsByField(String nameField, List<String> value) throws FieldNotFoundInTableException;

    List<Object> getField(String nameField) throws FieldNotFoundInTableException;

    String getTableName();
}
