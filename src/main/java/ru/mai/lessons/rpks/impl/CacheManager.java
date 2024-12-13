package ru.mai.lessons.rpks.impl;
import java.nio.file.attribute.FileTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CacheManager {
    private final Map<QueryParts, CacheEntry> cache = new HashMap<>();

    public CacheEntry getCache(QueryParts queryParts, List<Path> files) {
        CacheEntry cacheEntry = cache.get(queryParts);
        if (cacheEntry != null) {
            if (haveFilesBeenUpdated(files, cacheEntry.fileTimestamps)) {
                return null;
            }
            return cacheEntry;
        }
        return null;
    }

    public void putCache(QueryParts queryParts, CacheEntry cacheEntry) {
        cache.put(queryParts, cacheEntry);
    }

    private boolean haveFilesBeenUpdated(List<Path> files, Map<String, FileTime> cachedTimestamps) {
        for (Path file : files) {
            try {
                FileTime currentTimestamp = Files.getLastModifiedTime(file);
                String filePath = file.toString();

                if (!currentTimestamp.equals(cachedTimestamps.get(filePath))) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
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

