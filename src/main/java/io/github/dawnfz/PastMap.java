package io.github.dawnfz;

import java.util.*;
import java.util.function.BiConsumer;

public class PastMap<K, V>
{
    private final Map<K, V> data = new HashMap<>();
    private final Map<K, Long> timeMap = new HashMap<>();
    private final Timer timer = new Timer();

    public void clearPastValue()
    {
        synchronized (this)
        {
            try
            {
                Set<K> ks = new HashSet<>(data.keySet());
                Long time;
                for (K k : ks)
                {
                    time = timeMap.get(k);
                    if (time != null && System.currentTimeMillis() < time) continue;
                    data.remove(k);
                    timeMap.remove(k);
                }
            }
            catch (IllegalStateException ise)
            {
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    public PastMap<K, V> timingClear(int second)
    {
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

    public void put(K key, V value, Long time)
    {
        data.put(key, value);
        timeMap.put(key, time + System.currentTimeMillis());
    }

    public V get(K key)
    {
        synchronized (this)
        {
            V v = data.get(key);
            Long time = timeMap.get(key);
            if (time != null && System.currentTimeMillis() < time) return v;
            data.remove(key);
            timeMap.remove(key);
        }
        return null;
    }


    public void forEach(BiConsumer<? super K, ? super V> action)
    {
        synchronized (this)
        {
            for (Map.Entry<K, V> entry : entrySet())
            {
                K k;
                V v;
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
    }

    public void clear()
    {
        synchronized (this)
        {
            data.clear();
            timeMap.clear();
        }
    }

    public V remove(K key)
    {
        synchronized (this)
        {
            timeMap.remove(key);
            return data.remove(key);
        }
    }

    public boolean remove(K key, V value)
    {
        synchronized (this)
        {
            timeMap.remove(key);
            return data.remove(key, value);
        }
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

    @Override
    public boolean equals(Object o)
    {
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

    public void closeTiming()
    {
        timer.cancel();
    }
}