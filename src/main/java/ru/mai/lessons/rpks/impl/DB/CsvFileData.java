package ru.mai.lessons.rpks.impl.DB;
import ru.mai.lessons.rpks.impl.DB.CsvFileData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

public class CsvFileData {
  private Map<String, List<String>> columnNames;
  private Map<String, List<?>> data;
  private Map<Pair<String, String>, String> relationships;

  public CsvFileData() {
    columnNames = new HashMap<>();
    data = new HashMap<>();
    relationships = new HashMap<>();
  }

  public void loadCsvFiles(List<String> csvFiles) {
    initializeRelationships(csvFiles);
  }

  private void addRelationship(String file1, String file2, String field) {
    relationships.put(Pair.of(file1, file2), field);
  }

  private void initializeRelationships(List<String> csvFiles) {
    Map<String, List<Pair<String, String>>> relationshipsMap = new HashMap<>();
    
    relationshipsMap.put("students.csv", List.of(
      Pair.of("grade.csv", "student_id"),
      Pair.of("groups.csv", "student_id")
    ));
    
    relationshipsMap.put("grade.csv", List.of(
      Pair.of("students.csv", "id"),
      Pair.of("subjects.csv", "id")
    ));
    
    relationshipsMap.put("groups.csv", List.of(
      Pair.of("students.csv", "id")
    ));
    
    relationshipsMap.put("subjects.csv", List.of(
      Pair.of("grade.csv", "subject_id")
    ));
    
    for (String csvFile : csvFiles) {
        if (relationshipsMap.containsKey(csvFile)) {
            for (Pair<String, String> relation : relationshipsMap.get(csvFile)) {
                if (csvFiles.contains(relation.getKey())) {
                  addRelationship(csvFile, relation.getKey(), relation.getValue());
                }
            }
        }
    }
  }

  public void addFileData(String fileName, List<String> headers, List<?> fileData) {
    columnNames.put(fileName, headers);
    data.put(fileName, fileData);
  }

  public List<String> getcolumnNames(String fileName) {
    return columnNames.get(fileName);
  }

  public List<?> getData(String fileName) {
    return data.get(fileName);
  }
  public String getRelation(Pair<String, String> pair) {
    return relationships.get(pair);
  }
  
  public String toCamelCase(String str) {
    String[] words = str.split("[\\W_]+");
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
        String word = words[i];
        if (i == 0) {
            word = word.isEmpty() ? word : word.toLowerCase();
        } else {
            word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();      
        }
        builder.append(word);
    }
    return builder.toString();
  }

  public List<Integer> findObject(List<Pair<List<String>, Object>> listWithObj, List<String> key) {
    List<Integer> matchingIndices = new ArrayList<>();
    for (int i = 0; i < listWithObj.size(); i++) {
        Pair<List<String>, Object> pairInList = listWithObj.get(i);
        if (pairInList.getKey().equals(key)) {
          matchingIndices.add(i);
        }
    }
    return matchingIndices;
  }

  public List<Object> getFieldValues(String fileName, String fieldName, 
                                    Map<Object, Map<String, List<Object>>> valuesMap,
                                    List<Pair<List<String>, Object>> mapWithConditions) {
    List<Object> values = new ArrayList<>();
    List<?> dataList = data.get(fileName);
    Map<String, Pair<List<String>, Object>> indexMap = new TreeMap<>();
    if (dataList != null && !dataList.isEmpty()) {
      fieldName = toCamelCase(fieldName);
        for (Object obj : dataList) {
          try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            if (mapWithConditions == null || mapWithConditions.isEmpty()) {
              values.add(fieldValue);
            } else {
              for (Pair<List<String>, Object> pairInMap : mapWithConditions) {
                List<String> keyList = pairInMap.getKey();
                if (keyList.contains(fieldValue.toString())) {
                  values.add(obj);
                  List<Integer> matchingIndices = findObject(mapWithConditions, keyList);
                  for (int index : matchingIndices) {
                    Pair<List<String>, Object> matchingPair = mapWithConditions.get(index);
                    if (matchingPair.getKey() != null && matchingPair.getValue() != null) {
                      Map<String, List<Object>> temp = valuesMap.get(matchingPair.getValue());
                      if (temp != null && !temp.get(fileName).contains(obj)) {
                        temp.get(fileName).add(obj);
                        indexMap.put(fieldValue.toString(), Pair.of(keyList, obj));
                      }
                    }
                  }
                }
              }
            }
          } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
      if (mapWithConditions != null && mapWithConditions.get(0).getValue() != null) {
        int count = 0;
        for (Map.Entry<String, Pair<List<String>, Object>> entryOut : indexMap.entrySet()) {
          List<Integer> matchingIndices = findObject(mapWithConditions, entryOut.getValue().getKey());
          for (int index : matchingIndices) {
            Pair<List<String>, Object> matchingPair = mapWithConditions.get(index);
            if (matchingPair != null && matchingPair.getValue() != null) {
              Map<String, List<Object>> temp = valuesMap.get(matchingPair.getValue());
              if (temp.get(fileName).size() == indexMap.size()) {
                temp.get(fileName).set(count, entryOut.getValue().getValue());
                count++;
              }
            }
          }
        }
      }
      return values;
    }
  
  public List<Object> getFieldValues(List<?> listOfClassEL, String fieldName, 
                                    Map<Object, Map<String, List<Object>>> valuesMap,
                                    List<Pair<List<String>, Object>> mapWithConditions, String fileName) {
    List<Object> values = new ArrayList<>();
    Map<String, Pair<List<String>, Object>> indexMap = new TreeMap<>();
    if (listOfClassEL != null && !listOfClassEL.isEmpty()) {
      fieldName = toCamelCase(fieldName);
      for (Object obj : listOfClassEL) {
        try {
          Field field = obj.getClass().getDeclaredField(fieldName);
          field.setAccessible(true);
          Object fieldValue = field.get(obj);
          if (mapWithConditions == null || mapWithConditions.isEmpty()) {
            values.add(fieldValue);
          } else {
            for (Pair<List<String>, Object> pairInMap : mapWithConditions) {
              List<String> keyList = pairInMap.getKey();
              if (keyList.contains(fieldValue.toString())) {
                values.add(obj);
                List<Integer> matchingIndices = findObject(mapWithConditions, keyList);
                for (int index : matchingIndices) {
                  Pair<List<String>, Object> matchingPair = mapWithConditions.get(index);
                  if (matchingPair.getKey() != null && matchingPair.getValue() != null) {
                    Map<String, List<Object>> temp = valuesMap.get(matchingPair.getValue());
                    if (temp != null && !temp.get(fileName).contains(obj)) {
                      temp.get(fileName).add(obj);
                      indexMap.put(fieldValue.toString(), Pair.of(keyList, obj));
                    }
                  }
                }
              }
            }
          }
        } catch (NoSuchFieldException | IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    if (mapWithConditions != null && mapWithConditions.get(0).getValue() != null) {
      int count = 0;
      for (Map.Entry<String, Pair<List<String>, Object>> entryOut : indexMap.entrySet()) {
        List<Integer> matchingIndices = findObject(mapWithConditions, entryOut.getValue().getKey());
        for (int index : matchingIndices) {
          Pair<List<String>, Object> matchingPair = mapWithConditions.get(index);
          if (matchingPair != null && matchingPair.getValue() != null) {
            Map<String, List<Object>> temp = valuesMap.get(matchingPair.getValue());
            if (temp.get(fileName).size() == indexMap.size()) {
              temp.get(fileName).set(count, entryOut.getValue().getValue());
              count++;
            }
          }
        }
      }
    }
    return values;
  }
}
