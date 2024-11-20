package ru.mai.lessons.rpks.impl.DataBase;

public interface ITableCompare<Type> {
    public boolean predicate(Type tableElement);
}
