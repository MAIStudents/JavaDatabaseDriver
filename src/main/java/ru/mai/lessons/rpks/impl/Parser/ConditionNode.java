package ru.mai.lessons.rpks.impl.Parser;

import lombok.EqualsAndHashCode;
import ru.mai.lessons.rpks.impl.DataBase.DataBase;

@EqualsAndHashCode
public class ConditionNode {

    public enum NodeType {
        CONDITION, OPERATOR, EXECUTED
    }

    public ConditionNode left = null;
    public ConditionNode right = null;
    public DataBase db = null;
    public NodeType type;
    public String column;
    public String value;
    public String operator;

    public ConditionNode(String column, String value) {
        this.type = NodeType.CONDITION;
        this.column = column;
        this.value = value;
    }

    public ConditionNode(DataBase db) {
        this.type = NodeType.EXECUTED;
        this.db = db;
    }

    public ConditionNode(String operator) {
        this.type = NodeType.OPERATOR;
        this.operator = operator;
    }

    @Override
    public String toString() {
        if (type == NodeType.CONDITION) {
            return "Condition {column='" + column + "', value='" + value + "'}";
        } else {
            return "Operator {operator='" + operator + "'}";
        }
    }
}
