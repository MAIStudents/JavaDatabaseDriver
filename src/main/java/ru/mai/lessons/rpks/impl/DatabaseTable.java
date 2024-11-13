package ru.mai.lessons.rpks.impl;

import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.ITable;
import ru.mai.lessons.rpks.enums.FieldType;
import ru.mai.lessons.rpks.exception.InvalidTableCsvFormatException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class DatabaseTable implements ITable {

  final String tableName;
  final Map<FieldFullName, FieldType> fields;
  final Map<String, String> references;

  List<FieldFullName> arrangedFields;
  List<List<?>> data;

  // TODO: Validate tableName
  public DatabaseTable(String tableName, Map<String, FieldType> fields, String tableCsvFile) {
    this.tableName = tableName;
    this.fields = new HashMap<>();
    this.references = new HashMap<>();
    fields.forEach((fieldName, type) -> this.fields.put(new FieldFullName(tableName, fieldName), type));

    try {
      readData(tableCsvFile);
    } catch (IOException ex) {
      log.error("IOException occurred while reading data of table '{}'", tableName, ex);
    }

  }

  protected DatabaseTable(String tableName) {
    this.tableName = tableName;
    fields = new HashMap<>();
    references = new HashMap<>();
    arrangedFields = new ArrayList<>();
    data = new ArrayList<>();
  }

  void readData(String tableCsvFile) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(tableCsvFile))) {
      List<FieldFullName> newArrangedFields = new ArrayList<>();
      Scanner scanner = new Scanner(reader.readLine()).useDelimiter(";");

      for (int i = 0; i < fields.size(); ++i) {
        try {
          newArrangedFields.add(new FieldFullName(tableName, scanner.next()));
        } catch (NoSuchElementException ex) {
          throw new InvalidTableCsvFormatException(
                  String.format("Not all fields are present in the file %s", tableCsvFile));
        }

        for (int j = 0; j < i; ++j) {
          if (newArrangedFields.get(i).equals(newArrangedFields.get(j))) {
            throw new InvalidTableCsvFormatException(
                    String.format("There is field duplication '%s' in the file %s",
                            newArrangedFields.get(i).fieldName(), tableCsvFile));
          }
        }

        if (!fields.containsKey(newArrangedFields.get(i))) {
          throw new InvalidTableCsvFormatException(
                  String.format("There is wrong field '%s' in the file %s",
                          newArrangedFields.get(i).fieldName(), tableCsvFile));
        }
      }

      if (scanner.hasNext()) {
        throw new InvalidTableCsvFormatException(
                String.format("There is redundant fields in the file %s", tableCsvFile));
      }

      Function<String, List<?>> readData = str -> {
        Scanner recordScanner = new Scanner(str).useDelimiter(";");
        List<? super Object> record = new ArrayList<>();
        for (FieldFullName fieldFullName : newArrangedFields) {
          record.add(fields.get(fieldFullName).getReader().apply(recordScanner));
        }
        return record;
      };

      arrangedFields = newArrangedFields;
      data = reader.lines().map(readData).toList();
    } catch (NoSuchElementException ex) {
      throw new InvalidTableCsvFormatException(
              String.format("There is wrong formatted data in the file %s", tableCsvFile));
    }
  }

  @Override
  public String getTableName() {
    return tableName;
  }

  @Override
  public Map<FieldFullName, FieldType> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  @Override
  public Map<String, String> getReferences() {
    return Collections.unmodifiableMap(references);
  }

  @Override
  public List<List<?>> getData() {
    return Collections.unmodifiableList(data);
  }

  @Override
  public List<FieldFullName> getArrangedFields() {
    return Collections.unmodifiableList(arrangedFields);
  }

  // TODO: validate reference
  @Override
  public void setReference(String field, String referencedTable) {
    references.put(referencedTable, field);
  }
}
