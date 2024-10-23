package ru.mai.lessons.rpks.impl.Tables;

import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.util.List;

public interface Table {
    List<Object> getField(String nameField) throws FieldNotFoundInTableException;
    List<String> getFields();
    List<Object> getAllObjectsByField(String nameField, String value);
    List<Object> getAllObjectsByField(String nameField, List<String> value, String valueOrObject);
    List<Object> getRelateObjects(List<Object> listObj, String nameField, String valueOrObject);
}
