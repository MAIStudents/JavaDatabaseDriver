package ru.mai.lessons.rpks.impl;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class AmaCashMachine {
    private final String from;
    private final String select;
    private final String where;
    private final String groupBy;

    public AmaCashMachine(String from, String select, String where, String groupBy) {
        this.from = correction(from);
        this.select = correction(select);
        this.where = correction(where);
        this.groupBy = correction(groupBy);
    }

    private String correction(String input) {
        if (input == null) return null;
        return input.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
