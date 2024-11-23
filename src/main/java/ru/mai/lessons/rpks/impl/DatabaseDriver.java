package ru.mai.lessons.rpks.impl;

import ru.mai.lessons.rpks.Command;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;
import ru.mai.lessons.rpks.impl.commands.CommandBuilder;
import ru.mai.lessons.rpks.impl.parsers.CommandParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDriver implements IDatabaseDriver {

    private static final List<String> fileList = List.of("grade.csv", "groups.csv", "students.csv", "subjects.csv");

    @Override
    public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                             String gradeCsvFile, String command) throws WrongCommandFormatException, FieldNotFoundInTableException {
        CommandParser parser = new CommandParser();
        List<String> select = new ArrayList<>();
        List<String> where = new ArrayList<>();
        List<String> from = new ArrayList<>();
        List<String> groupBy = new ArrayList<>();
        parser.parseToLists(command, select, from, where, groupBy);
        Command executing = new CommandBuilder()
                .select(select)
                .from(from)
                .where(where)
                .groupBy((!groupBy.isEmpty()) ? groupBy.get(0) : null)
                .files(fileList)
                .build();
        try {
            return executing.execute();
        } catch (IOException e) {
            throw new WrongCommandFormatException(e.getMessage());
        } catch (FieldNotFoundInTableException e) {
            throw new FieldNotFoundInTableException(e.getMessage());
        }
    }
}
