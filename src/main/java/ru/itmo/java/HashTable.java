package ru.itmo.java;

public final class HashTable<K, V> {

    private static final int HASH_MASK = -1 >>> 1;
    private static final int MAX_CAPACITY = 1 << 30;
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    private static final float DEFAULT_LOAD_FACTOR = 0.5f;

    private static int hash(final Object key) {
        return key == null ? 0 : key.hashCode() & HASH_MASK;
    }

    private final float loadFactor;

    private Entry<K, V>[] table;
    private int size;
    private int threshold;

    public HashTable(final int initialCapacity, final float loadFactor) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be positive");
        }

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Invalid load factor");
        }

        final int cap = Math.min(initialCapacity, MAX_CAPACITY);

        //noinspection unchecked
        this.table = new Entry[cap];
        this.size = 0;

        this.loadFactor = loadFactor;
        this.threshold = cap == MAX_CAPACITY
            ? MAX_CAPACITY
            : (int) (cap * loadFactor);
    }

    public HashTable(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashTable() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public V put(final K key, final V value) {
        final int hash = hash(key);
        final int idx = findIndex(hash, key);

        if (table[idx] != null) {
            return table[idx].setValue(value);
        }

        if (size >= MAX_CAPACITY - 1) {
            throw new RuntimeException("Maximum size of hash-table has been reached");
        }

        table[idx] = new Entry<>(hash, key, value);

        if (++size >= threshold) {
            resize();
        }

        return null;
    }

    public V get(final K key) {
        final Entry<K, V> entry = table[findIndex(hash(key), key)];

        return entry == null ? null : entry.value;
    }

    public V remove(final K key) {
        final int hash = hash(key);
        final int idx = findIndex(hash, key);

        if (table[idx] == null) return null;

        final V value = table[idx].value;

        removeByIndex(idx);
        --size;

        return value;
    }

    public boolean containsKey(final K key) {
        return table[findIndex(hash(key), key)] != null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void resize() {
        final int oldCap = table.length;
        int newCap = oldCap << 1;

        if (newCap >= MAX_CAPACITY) {
            newCap = MAX_CAPACITY;
        }

        threshold = newCap == MAX_CAPACITY
            ? MAX_CAPACITY
            : (int) (newCap * loadFactor);

        @SuppressWarnings("unchecked")
        final Entry<K, V>[] newTable = new Entry[newCap];

        Entry<K, V> entry;
        for (int i = 0; i < oldCap; ++i) {
            entry = table[i];
            if (entry == null) continue;

            table[i] = null;

            int idx = entry.hash % newCap;
            while (newTable[idx] != null) {
                idx = (idx + 1) % newCap;
            }

            newTable[idx] = entry;
        }

        table = newTable;
    }

    private int findIndex(final int hash, final K key) {
        final int cap = table.length;

        int idx = hash % cap;
        while (table[idx] != null
            && table[idx].key != key
            && (table[idx].key == null || !table[idx].key.equals(key))
        ) {
            idx = (idx + 1) % cap;
        }

        return idx;
    }

    private void removeByIndex(final int index) {
        final int cap = table.length;

        table[index] = null;

        int current = index;
        int hashIdx;
        do {
            current = (current + 1) % cap;

            if (table[current] == null) return;

            hashIdx = table[current].hash % cap;

        } while (index <= current
            ? index < hashIdx && hashIdx <= current
            : index < hashIdx || hashIdx <= current
        );

        table[index] = table[current];

        removeByIndex(current);
    }

    private static class Entry<K, V> {

        private final int hash;
        private final K key;
        private V value;

        private Entry(final int hash, final K key, final V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        private V setValue(final V value) {
            final V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
