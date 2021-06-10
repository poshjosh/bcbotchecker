package com.looseboxes.botchecker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hp
 */
public class BotCacheInMemory implements BotCache{
    
    private boolean closed;
    
    private final Map<String, BotChecker.BotCategory> cache;

    public BotCacheInMemory() {
        this(new HashMap<>());
    }

    public BotCacheInMemory(Map<String, BotChecker.BotCategory> cache) {
        this.cache = Collections.synchronizedMap(cache);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void clear() {
        this.requireNotClosed();
        cache.clear();
    }

    @Override
    public void flush() { 
        this.requireNotClosed();
    }

    @Override
    public BotChecker.BotCategory getOrDefault(String key, BotChecker.BotCategory resultIfNone) {
        this.requireNotClosed();
        return cache.getOrDefault(key, resultIfNone);
    }

    @Override
    public boolean put(String key, BotChecker.BotCategory value) {
        this.requireNotClosed();
        cache.put(key, value);
        return true;
    }

    @Override
    public boolean remove(String key) {
        this.requireNotClosed();
        cache.remove(key);
        return true;
    }

    @Override
    public void close() {
        this.closed = true;
        this.clear();
    }
    
    private void requireNotClosed() {
        if(this.closed) {
            throw new IllegalStateException("Cache is closed");
        }
    }
}
