package ru.mai.lessons.rpks.impl;
import lombok.Getter;

import java.util.Map;

@Getter
public class QueryParts {
    private final String select;
    private final String from;
    private final String where;
    private final String groupby;

    public QueryParts(Map<String, String> query) {
        this.select = query.getOrDefault("SELECT", null);
        this.from = query.getOrDefault("FROM", null);
        this.where = query.getOrDefault("WHERE", null);
        this.groupby = query.getOrDefault("GROUPBY", null);
    }
}
