package ru.mai.lessons.rpks.impl.Parser;

import ru.mai.lessons.rpks.impl.DataBase.DataBase;
import ru.mai.lessons.rpks.impl.DataBase.Line;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class FileParser {
    private static final String PATH_PREFIX = "src/test/resources/";
    public static DataBase parseFile(String path) {
        DataBase db = new DataBase();
        db.name = path;
        path = PATH_PREFIX + path;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            if ((line = reader.readLine()) != null) {
                db.columnsNames = Arrays.asList(line.split(";"));
            }

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                db.insertData(new Line(Arrays.asList(values)));
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }

        return db;
    }
}
