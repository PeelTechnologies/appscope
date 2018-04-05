/*
 * Copyright (C) 2018 Peel Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple replacement of android LruCache. This is needed because the default
 * implementation for Junit tests is stubbed.
 *
 * @author Inderjeet Singh
 */
public class LruCache<K, V> {
    class CacheEntry {
        V item;
        long timestamp;
        CacheEntry(V item, long timestamp) {
            this.item = item;
            this.timestamp = timestamp;
        }
    }
    private final Map<K, CacheEntry> map;
    private final int maxSize;
    public LruCache(int maxSize) {
        this.maxSize = maxSize;
        map = new HashMap<>(maxSize);
    }

    public final V get(K key) {
        LruCache<K, V>.CacheEntry cacheEntry = map.get(key);
        return cacheEntry == null ? null : cacheEntry.item;
    }

    public final V put(K key, V value) {
        map.put(key, new CacheEntry(value, System.currentTimeMillis()));
        if (map.size() > maxSize) {
            evictOldest();
        }
        return value;
    }

    public final V remove(K key) {
        LruCache<K, V>.CacheEntry cacheEntry = map.remove(key);
        return cacheEntry == null ? null : cacheEntry.item;
    }

    public void evictOldest() {
        K key = null;
        long max = 0;
       for (Map.Entry<K, CacheEntry> entry : map.entrySet()) {
           CacheEntry value = entry.getValue();
           if (value.timestamp > max) {
               max = value.timestamp;
               key = entry.getKey();
           }
       }
       if (key != null) map.remove(key);
    }

    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
        // nothing needed
    }

    protected V create(K key) {
        return null;
        // nothing needed
    }

    protected int sizeOf(K key, V value) {
        return 0;
        // nothing needed
    }

    public final void evictAll() {
        map.clear();
    }

    public final int size() {
        return map.size();
    }

    public final int maxSize() {
        return map.size();
    }

    public final synchronized int hitCount() {
        return 0;
    }

    public final synchronized int missCount() {
        return 0;
    }

    public final synchronized int createCount() {
        return 0;
    }

    public final synchronized int putCount() {
        return 0;
    }

    public final synchronized int evictionCount() {
        return 0;
    }

    public final synchronized Map<K, V> snapshot() {
        Map<K, V> snapshot = new HashMap<>();
        for (Map.Entry<K, CacheEntry> entry : map.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().item);
        }
        return snapshot;
    }

    public final synchronized String toString() {
        return map.toString();
    }
}
