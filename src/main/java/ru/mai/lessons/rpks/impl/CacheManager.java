package ru.mai.lessons.rpks.impl;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CacheManager {
    private final Map<QueryParts, CacheEntry> cache = new HashMap<>();

    public CacheEntry getCache(QueryParts QueryParts) {
        return cache.get(QueryParts);
    }

    public void putCache(QueryParts QueryParts, CacheEntry cacheEntry) {
        cache.put(QueryParts, cacheEntry);
    }

    public static class CacheEntry {
        public final List<String> result;
        protected final Map<String, FileTime> fileTimestamps;

        public CacheEntry(List<String> result, Map<String, FileTime> fileTimestamps) {
            this.result = result;
            this.fileTimestamps = fileTimestamps;
        }
    }
}
