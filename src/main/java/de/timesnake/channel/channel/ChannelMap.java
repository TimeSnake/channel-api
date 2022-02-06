package de.timesnake.channel.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelMap<Key, Value> {

    private final Map<Key, Value> map = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    public void put(Key key, Value value) {
        new Thread(() -> {
            lock.lock();
            try {
                this.map.put(key, value);
            } finally {
                lock.unlock();
            }
        }).start();

    }

    public Value get(Key key) {
        return this.map.get(key);
    }

    public boolean containsKey(Key key) {
        return this.map.containsKey(key);
    }

    public void remove(Key key) {
        new Thread(() -> {
            lock.lock();
            try {
                this.map.remove(key);
            } finally {
                lock.unlock();
            }
        }).start();
    }

    public void iterateValues(ChannelMapValueEditor<Value> editor) {
        new Thread(() -> {
            lock.lock();
            try {
                for (Value value : this.map.values()) {
                    editor.edit(value);
                }
            } finally {
                lock.unlock();
            }
        }).start();

    }

    public void iterateEntries(ChannelMapEntryEditor<Key, Value> editor) {
        new Thread(() -> {
            lock.lock();
            try {
                for (Map.Entry<Key, Value> entry : this.map.entrySet()) {
                    editor.edit(entry.getKey(), entry.getValue());
                }
            } finally {
                lock.unlock();
            }
        }).start();

    }

}
