package com.lab;

import java.util.Objects;

/**
 * 手写简易 HashMap（数组 + 单向链表，不含红黑树）
 *
 * <p>目标：理解 HashMap 的核心机制 —— hash 计算、索引定位、链表查找、扩容拆分</p>
 *
 * <h3>关键设计</h3>
 * <ul>
 *   <li>默认容量 16，负载因子 0.75</li>
 *   <li>hash 计算：hashCode() 高 16 位 ^ 低 16 位</li>
 *   <li>索引定位：(capacity - 1) & hash</li>
 *   <li>扩容：新容量 = 旧容量 × 2，用 hash & oldCap 拆分高低位链</li>
 *   <li>null key 固定在 bucket[0]</li>
 *   <li>链表长度 ≥ 8 时并不转红黑树，仅扩容（简化实现）</li>
 * </ul>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class MyHashMap<K, V> {

    // ==================== 常量 ====================

    /** 默认初始容量（必须是 2 的幂） */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // 16

    /** 最大容量 */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /** 默认负载因子 */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // ==================== 字段 ====================

    /** 桶数组，每个元素是链表的头节点 */
    @SuppressWarnings("unchecked")
    private Node<K, V>[] table;

    /** 当前键值对数量 */
    private int size;

    /** 扩容阈值 = capacity * loadFactor */
    private int threshold;

    /** 负载因子 */
    private final float loadFactor;

    // ==================== 节点定义 ====================

    /**
     * 单向链表节点
     */
    static class Node<K, V> {
        final int hash;      // 缓存 hash 值，扩容拆分时不用重新计算
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // ==================== 构造方法 ====================

    public MyHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }

        this.loadFactor = loadFactor;
        int capacity = tableSizeFor(initialCapacity);
        this.threshold = (int) (capacity * loadFactor);
    }

    // ==================== 核心 API ====================

    /**
     * 计算 key 的 hash 值
     * <p>高 16 位与低 16 位异或，让高位也参与索引运算，减少碰撞</p>
     */
    static final int hash(Object key) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /**
     * 存入键值对
     *
     * @return 如果 key 已存在，返回旧值；否则返回 null
     */
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        int hash = hash(key);

        // 延迟初始化：第一次 put 时才创建数组
        if (table == null) {
            int capacity = (int) (threshold / loadFactor);
            table = new Node[capacity];
        }

        int index = (table.length - 1) & hash;

        // bucket 为空，直接放入
        if (table[index] == null) {
            table[index] = new Node<>(hash, key, value, null);
        } else {
            // 遍历链表，查找是否已有相同 key
            Node<K, V> node = table[index];
            Node<K, V> prev = null;
            while (node != null) {
                if (node.hash == hash && Objects.equals(node.key, key)) {
                    // 找到 → 覆盖 value，返回旧值
                    V old = node.value;
                    node.value = value;
                    return old;
                }
                prev = node;
                node = node.next;
            }
            // 没找到 → 尾插（JDK 1.8 风格）
            prev.next = new Node<>(hash, key, value, null);
        }

        size++;
        if (size > threshold) {
            resize();
        }
        return null;
    }

    /**
     * 根据 key 获取 value
     *
     * @return value，如果 key 不存在则返回 null
     */
    public V get(Object key) {
        if (table == null) {
            return null;
        }

        int hash = hash(key);
        int index = (table.length - 1) & hash;

        Node<K, V> node = table[index];
        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }

    /**
     * 删除 key
     *
     * @return 被删除的 value，如果 key 不存在则返回 null
     */
    public V remove(Object key) {
        if (table == null) {
            return null;
        }

        int hash = hash(key);
        int index = (table.length - 1) & hash;

        Node<K, V> node = table[index];
        Node<K, V> prev = null;

        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                // 找到了，摘除节点
                if (prev == null) {
                    // 删除的是链表头节点
                    table[index] = node.next;
                } else {
                    // 删除的是中间或尾部节点
                    prev.next = node.next;
                }
                size--;
                return node.value;
            }
            prev = node;
            node = node.next;
        }
        return null;
    }

    /**
     * 是否包含 key
     * <p>直接遍历查找节点，不调用 get() —— 避免 value 为 null 时误判</p>
     */
    public boolean containsKey(Object key) {
        if (table == null) {
            return false;
        }

        int hash = hash(key);
        int index = (table.length - 1) & hash;

        Node<K, V> node = table[index];
        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                return true;
            }
            node = node.next;
        }
        return false;
    }

    /**
     * 返回元素个数
     */
    public int size() {
        return size;
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return size == 0;
    }

    // ==================== 扩容 ====================

    /**
     * 扩容 —— 新数组大小为原来的 2 倍
     *
     * <h3>核心技巧：用 hash & oldCap 拆分高低位</h3>
     * <p>因为容量总是 2 的幂，扩容后 index 变化只取决于 hash 在 oldCap 那一位是 0 还是 1：</p>
     * <ul>
     *   <li>hash & oldCap == 0 → 留在原索引 j</li>
     *   <li>hash & oldCap != 0 → 移到 j + oldCap</li>
     * </ul>
     * <p>不需要重新计算 hash，不需要重新取模，位运算极快。</p>
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = oldTab.length;

        // 已经到最大容量，不扩容了，阈值设为无穷大
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        int newCap = oldCap << 1;                      // 容量 × 2
        threshold = (int) (newCap * loadFactor);        // 新阈值
        Node<K, V>[] newTab = new Node[newCap];

        // 遍历旧数组的每个桶
        for (int j = 0; j < oldCap; j++) {
            Node<K, V> e = oldTab[j];
            if (e == null) {
                continue;
            }

            // 单节点：直接 rehash 到新位置（hash & (newCap-1) 等价于当前位置或当前位置+oldCap）
            if (e.next == null) {
                newTab[e.hash & (newCap - 1)] = e;
                continue;
            }

            // 链表：拆分为低位链和高位链
            // 低位链（hash & oldCap == 0）→ 新数组[j]
            // 高位链（hash & oldCap != 0）→ 新数组[j + oldCap]
            Node<K, V> loHead = null, loTail = null;  // 低位链头、尾
            Node<K, V> hiHead = null, hiTail = null;  // 高位链头、尾

            do {
                if ((e.hash & oldCap) == 0) {
                    // 低位节点 —— 挂在低位链上
                    if (loTail == null) {
                        loHead = e;
                    } else {
                        loTail.next = e;
                    }
                    loTail = e;
                } else {
                    // 高位节点 —— 挂在高位链上
                    if (hiTail == null) {
                        hiHead = e;
                    } else {
                        hiTail.next = e;
                    }
                    hiTail = e;
                }
                e = e.next;
            } while (e != null);

            // 低位链放入新数组原位置
            if (loTail != null) {
                loTail.next = null;
                newTab[j] = loHead;
            }

            // 高位链放入新数组原位置 + oldCap
            if (hiTail != null) {
                hiTail.next = null;
                newTab[j + oldCap] = hiHead;
            }
        }

        table = newTab;
    }

    // ==================== 工具方法 ====================

    /**
     * 计算不小于 cap 的最小 2 的幂
     *
     * <p>例：tableSizeFor(10) = 16, tableSizeFor(17) = 32</p>
     * <p>JDK HashMap 源码中的经典位运算，原理：将最高位 1 之后的位全部刷成 1，最后 +1 进位</p>
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : n + 1;
    }

    /**
     * 调试信息 —— 打印每个桶的链表长度
     *
     * @return 示例格式："Bucket[0]: 3 nodes | Bucket[1]: 0 | Bucket[2]: 1 | ..."
     */
    public String debugInfo() {
        if (table == null) {
            return "table is null (no put yet)";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.length; i++) {
            int count = 0;
            Node<K, V> p = table[i];
            while (p != null) {
                count++;
                p = p.next;
            }
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append("Bucket[").append(i).append("]: ").append(count).append(" nodes");
        }
        return sb.toString();
    }
}
