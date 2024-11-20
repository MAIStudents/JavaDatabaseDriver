package ru.mai.lessons.rpks.impl.DataBase;

public class CompareCells implements ITableCompare<String> {
    public String condition;
    public CompareCells(String condition) {
        this.condition = condition;
    }
    @Override
    public boolean predicate(String tableElement) {
        if (tableElement == null) {
            throw new NullPointerException("tableElement is null");
        }
        return condition.equals(tableElement);
    }
}

