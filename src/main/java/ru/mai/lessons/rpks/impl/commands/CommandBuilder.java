package ru.mai.lessons.rpks.impl.commands;

import java.util.ArrayList;
import java.util.List;

public class CommandBuilder {

    private List<String> select = new ArrayList<>();
    private List<String> from = new ArrayList<>();
    private List<String> where = new ArrayList<>();
    private List<String> files = new ArrayList<>();

    private String groupBy = null;

    public CommandBuilder() {
    }

    public CommandBuilder select(List<String> select) {
        this.select = select;
        return this;
    }

    public CommandBuilder from(List<String> from) {
        this.from = from;
        return this;
    }

    public CommandBuilder files(List<String> files) {
        this.files = files;
        return this;
    }

    public CommandBuilder where(List<String> where) {
        this.where = where;
        return this;
    }

    public CommandBuilder groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public Command build() {
        return new Command(select, from, where, groupBy, files);
    }


}
