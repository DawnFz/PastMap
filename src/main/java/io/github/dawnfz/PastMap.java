package io.github.dawnfz;

import java.util.*;
import java.util.function.BiConsumer;

// not extends Map
public class PastMap<K, V>
{
    private final Map<K, V> data = new HashMap<>();
    private final Map<K, Long> timeMap = new HashMap<>();
    private Timer timer;

    public synchronized void clearPastValue()
    {
        try
        {
            Set<K> ks = new HashSet<>(data.keySet());
            Long time;
            for (K k : ks)
            {
                time = timeMap.get(k);
                if (time != null && (time == -1 || System.currentTimeMillis() < time)) continue;
                data.remove(k);
                timeMap.remove(k);
            }
        }
        catch (IllegalStateException ise)
        {
            throw new ConcurrentModificationException(ise);
        }
    }

    public PastMap<K, V> timingClear(int second)
    {
        timer = new Timer();
        TimerTask task = new TimerTask()
        {
            public void run()
            {
                clearPastValue();
            }
        };
        long delay = 0;
        long period = second * 1000L;
        timer.scheduleAtFixedRate(task, delay, period);
        return this;
    }

    public void closeTiming()
    {
        timer.cancel();
    }

    public synchronized void put(K key, V value)
    {
        data.put(key, value);
        timeMap.put(key, -1L);
    }

    public synchronized void put(K key, V value, long millis)
    {
        data.put(key, value);
        timeMap.put(key, millis + System.currentTimeMillis());
    }

    public synchronized void put(K key, V value, int second)
    {
        data.put(key, value);
        timeMap.put(key, second * 1000L + System.currentTimeMillis());
    }

    public synchronized boolean againSetExpired(K key, long millis)
    {
        V v = get(key);
        if (v != null)
        {
            timeMap.put(key, millis + System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public synchronized V get(K key)
    {
        V v = data.get(key);
        Long time = timeMap.get(key);
        if (time != null && (time == -1 || System.currentTimeMillis() < time)) return v;
        data.remove(key);
        timeMap.remove(key);
        return null;
    }

    public synchronized void forEach(BiConsumer<? super K, ? super V> action)
    {
        K k;
        V v;
        for (Map.Entry<K, V> entry : entrySet())
        {
            try
            {
                k = entry.getKey();
                v = entry.getValue();
            }
            catch (IllegalStateException ise)
            {
                continue;
            }
            action.accept(k, v);
        }
    }

    public synchronized void clear()
    {
        data.clear();
        timeMap.clear();
    }

    public synchronized V remove(K key)
    {
        timeMap.remove(key);
        return data.remove(key);
    }

    public synchronized boolean remove(K key, V value)
    {
        timeMap.remove(key);
        return data.remove(key, value);
    }

    public int size()
    {
        clearPastValue();
        synchronized (this)
        {
            return data.size();
        }
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public Collection<V> values()
    {
        clearPastValue();
        return data.values();
    }

    public Set<K> keySet()
    {
        clearPastValue();
        return data.keySet();
    }

    public Set<Map.Entry<K, V>> entrySet()
    {
        clearPastValue();
        return data.entrySet();
    }

    public boolean containsKey(K key)
    {
        clearPastValue();
        return data.containsKey(key);
    }

    public boolean containsValue(V value)
    {
        clearPastValue();
        return data.containsValue(value);
    }

    public boolean replace(K key, V oldValue, V newValue)
    {
        boolean replace;
        synchronized (this)
        {
            replace = data.replace(key, oldValue, newValue);
        }
        clearPastValue();
        return replace;
    }

    public Map<K, V> toMap()
    {
        return new HashMap<>(data);
    }

    @Override
    public boolean equals(Object o)
    {
        clearPastValue();
        if (o.getClass() == PastMap.class)
        {
            PastMap<?, ?> pastMap = (PastMap<?, ?>) o;
            boolean dataEq = pastMap.data.equals(this.data);
            boolean timeEq = pastMap.timeMap.equals(this.timeMap);
            return dataEq && timeEq;
        }
        else if (o.getClass() == HashMap.class) return data.equals(o);
        return false;
    }
}