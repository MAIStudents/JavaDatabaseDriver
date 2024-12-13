package ru.mai.lessons.rpks.impl;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        QueryParts that = (QueryParts) obj;
        return Objects.equals(select, that.select) &&
                Objects.equals(from, that.from) &&
                Objects.equals(where, that.where) &&
                Objects.equals(groupby, that.groupby);
    }

    @Override
    public int hashCode() {
        return Objects.hash(select, from, where, groupby);
    }
}
