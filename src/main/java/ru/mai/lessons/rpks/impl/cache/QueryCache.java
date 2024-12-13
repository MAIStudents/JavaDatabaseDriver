package ru.mai.lessons.rpks.impl.cache;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class QueryCache {

    private final Map<String, List<String>> cache = new HashMap<>();

    public List<String> get(String queryKey) {
        return cache.get(queryKey);
    }

    public void put(String queryKey, List<String> result) {
        cache.put(queryKey, result);
        if (cache.containsKey(queryKey)) {
            System.out.println("The query with the key <" + queryKey + "> is written to the cache.");
        } else {
            System.out.println("Data could not be written to the cache for the key: " + queryKey);
        }
    }

    public boolean containsKey(String queryKey) {
        return cache.containsKey(queryKey);
    }
}
