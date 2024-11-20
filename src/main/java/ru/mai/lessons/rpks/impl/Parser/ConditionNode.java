package ru.mai.lessons.rpks.impl.Parser;

public class ConditionNode {
    public enum NodeType {
        CONDITION, OPERATOR
    }

    public NodeType type;
    public String column;
    public String value;
    public String operator;

    // Для условий
    public ConditionNode(String column, String value) {
        this.type = NodeType.CONDITION;
        this.column = column;
        this.value = value;
    }

    // Для операторов
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
