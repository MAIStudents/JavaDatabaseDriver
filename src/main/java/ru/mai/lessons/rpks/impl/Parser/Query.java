package ru.mai.lessons.rpks.impl.Parser;

import java.util.List;
import ru.mai.lessons.rpks.impl.Parser.ConditionNode;

public class Query {
    public String originalInput;
    public List<String> selectColumns;
    public List<String> fromFiles;
    public List<ConditionNode> whereConditionNodes;
    public String groupByColumn;

    @Override
    public String toString() {
        return "Query {" +
                "selectColumns=" + selectColumns +
                ", fromFiles=" + fromFiles +
                ", whereConditionNodes=" + whereConditionNodes +
                ", groupByColumn='" + groupByColumn + '\'' +
                '}';
    }

}
