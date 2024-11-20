package ru.mai.lessons.rpks.impl.DataBase;

import lombok.EqualsAndHashCode;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode
public final class Line {

    public final List<String> line = new ArrayList<>();

    public Line(List<String> vals){
        line.addAll(vals);
    }

    public String getElement(int index) throws FieldNotFoundInTableException {
        if (index > line.size() || index < 0) {
            throw new FieldNotFoundInTableException("Not existing index");
        }
        return line.get(index);
    }

    public Line(){}

    public Line(Line line){
        this.line.addAll(line.line);
    }

    public static Line mergeLines(Line line1, Line line2) {
        Line result = new Line();
        result.line.addAll(line1.line);
        result.line.addAll(line2.line);
        return result;
    }

    @Override
    public String toString() {
        return String.join(" ", line);
    }

}
