package com.lab;

import java.util.HashMap;
import java.util.Map;

/**
 * LRU（Least Recently Used）缓存 —— HashMap + 双向链表实现
 *
 * <h3>核心思想</h3>
 * <p>HashMap 负责 O(1) 快速查找，双向链表负责 O(1) 维护访问顺序。</p>
 * <p>每次访问（get/put）都把该节点移动到链表头部；
 *    当容量超限时，淘汰链表尾部的节点（最久未使用）。</p>
 *
 * <h3>数据结构示意</h3>
 * <pre>
 *   哨兵 head ←→ 最新节点 ←→ 次新节点 ←→ ... ←→ 最旧节点 ←→ 哨兵 tail
 *  </pre>
 *  两个哨兵节点（dummy head / dummy tail）不存数据，用于简化边界操作：
 *  插入头部、删除尾部时不需要判 null。
 *
 * <h3>API 复杂度</h3>
 * <ul>
 *   <li>get(key)    → O(1)</li>
 *   <li>put(k, v)   → O(1)</li>
 * </ul>
 *
 * <p>参考：LeetCode 146. LRU Cache</p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class LRUCache<K, V> {

    // ==================== 节点定义 ====================

    /**
     * 双向链表节点
     *
     * <p>为什么用双向链表而不是单向？</p>
     * <ul>
     *   <li>删除任意节点（比如淘汰尾部）需要知道它的前驱节点</li>
     *   <li>双向链表有 prev 指针，删除任意节点是 O(1)</li>
     *   <li>单向链表删除任意节点是 O(n)，因为需要从头遍历找前驱</li>
     * </ul>
     *
     * <p>注意：节点同时持有 key 和 value，
     * 淘汰尾部时需要通过 node.key 从 HashMap 中删除对应条目。</p>
     */
    static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        /** 哨兵节点构造（不存数据） */
        Node() {}

        /** 数据节点构造 */
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    // ==================== 字段 ====================

    /** 最大容量 */
    private final int capacity;

    /**
     * HashMap：key → 链表节点
     *
     * <p>O(1) 查到任意 key 对应的链表节点，拿到节点后：
     * <ul>
     *   <li>get: 直接返回 node.value</li>
     *   <li>put: 更新 node.value</li>
     *   <li>淘汰: 通过 node.key 反查 map 删除</li>
     * </ul>
     */
    private final Map<K, Node<K, V>> map;

    /** 哨兵头节点 —— 不存数据，head.next 指向最近使用的节点 */
    private final Node<K, V> head;

    /** 哨兵尾节点 —— 不存数据，tail.prev 指向最久未使用的节点 */
    private final Node<K, V> tail;

    // ==================== 构造方法 ====================

    /**
     * 初始化 LRU 缓存
     *
     * @param capacity 最大容量，必须 > 0
     */
    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive: " + capacity);
        }

        this.capacity = capacity;
        this.map = new HashMap<>();

        // 初始化哨兵节点
        // head ←→ tail（空链表时两个哨兵互相指向）
        this.head = new Node<>();
        this.tail = new Node<>();
        head.next = tail;   // head 后面是 tail
        tail.prev = head;   // tail 前面是 head
    }

    // ==================== 核心 API ====================

    /**
     * 获取 key 对应的 value，并将该节点标记为「最近使用」（移到链表头部）
     *
     * <p>为什么 get 也要移动节点？</p>
     * <ul>
     *   <li>LRU 的定义：最近被访问过的数据不应该被淘汰</li>
     *   <li>get 是访问行为，必须更新访问时间（在链表中的位置）</li>
     *   <li>如果不移动，刚被 get 过的节点可能因"从未被 put"而被误淘汰</li>
     * </ul>
     *
     * @return value，key 不存在则返回 null
     */
    public V get(K key) {
        Node<K, V> node = map.get(key);

        if (node == null) {
            return null;           // 缓存中没有这个 key
        }

        // 访问过，移到头部（标记为最新）
        moveToHead(node);
        return node.value;
    }

    /**
     * 存入键值对
     *
     * <p>两种情况：</p>
     * <ol>
     *   <li><b>key 已存在</b> → 更新 value + 移到头部（不淘汰）</li>
     *   <li><b>key 不存在 + 未满</b> → 创建新节点 + 放入链表头部</li>
     *   <li><b>key 不存在 + 已满</b> → 淘汰尾部节点 + 创建新节点 + 放入头部</li>
     * </ol>
     */
    public void put(K key, V value) {
        Node<K, V> node = map.get(key);

        if (node != null) {
            // ── 情况 1：key 已存在 → 更新 + 移到头部 ──
            node.value = value;
            moveToHead(node);
        } else {
            // ── 情况 2/3：key 不存在 → 创建新节点 ──
            Node<K, V> newNode = new Node<>(key, value);
            addToHead(newNode);          // 新节点放入链表头部
            map.put(key, newNode);       // 登记到 HashMap

            if (map.size() > capacity) {
                // ── 情况 3：容量超了 → 淘汰最久未使用的节点 ──
                // tail.prev 就是最久未使用的节点（离 tail 最近的真实节点）
                Node<K, V> removed = removeTail();
                map.remove(removed.key); // 从 HashMap 中同步删除
                // 注意：必须是先 map.remove(removed.key) 还是先 removeTail() 都可以，
                //       但需要 removed.key 来定位，所以先拿到节点再删 map
            }
        }
    }

    /**
     * 返回当前缓存中的元素数量
     */
    public int size() {
        return map.size();
    }

    // ==================== 双向链表操作（4 个基础方法）====================

    /**
     * 从双向链表中删除一个节点
     *
     * <p>这是双向链表最基础的操作，只有 2 行代码：</p>
     * <pre>
     *   node.prev.next = node.next;   // node 的前驱跳过 node，指向 node 的后继
     *   node.next.prev = node.prev;   // node 的后继跳过 node，指向 node 的前驱
     * </pre>
     *
     * <p>为什么不需要判 null？</p>
     * <ul>
     *   <li>哨兵 head 和 tail 保证了链表中任意节点的 prev 和 next 都不为 null</li>
     *   <li>最坏情况：node 的前驱是 head，node 的后继是 tail —— prev 和 next 都有值</li>
     * </ul>
     *
     * <p>图示（删除中间的 node）：</p>
     * <pre>
     *   删除前：A ←→ node ←→ B
     *   执行：
     *     A.next = B          (node.prev.next = node.next)
     *     B.prev = A          (node.next.prev = node.prev)
     *   删除后：A ←→ B
     *   node 被断开，等待 GC 回收
     * </pre>
     */
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;   // 前驱跳过 node
        node.next.prev = node.prev;   // 后继跳过 node
    }

    /**
     * 将一个节点插入到链表头部（哨兵 head 之后、原第一个节点之前）
     *
     * <p>这是双向链表头插的标准 4 步，顺序不能乱：</p>
     * <pre>
     *   插入前：head ←→ first ←→ ...
     *   插入后：head ←→ node ←→ first ←→ ...
     * </pre>
     *
     * <p>代码：</p>
     * <pre>
     *   node.prev = head;            // ① 新节点的前驱 → head
     *   node.next = head.next;       // ② 新节点的后继 → 原 first
     *   head.next.prev = node;       // ③ 原 first 的前驱 → node
     *   head.next = node;            // ④ head 的后继 → node
     * </pre>
     *
     * <p>⚠️ <b>顺序为什么不能乱？</b></p>
     * <ul>
     *   <li>必须先执行 ①②（给 node 的两条指针赋值），因为 node 是新的，改它不会丢引用</li>
     *   <li>③④ 要改 head.next 和 first.prev，如果先把 head.next = node，
     *       原来的 first 就找不到了，③ 也就没法执行</li>
     *   <li>记忆口诀：<b>先改新节点自己的，再改别人的</b></li>
     * </ul>
     */
    private void addToHead(Node<K, V> node) {
        node.prev = head;             // ① node 前驱 → head
        node.next = head.next;        // ② node 后继 → 原来的第一个真实节点
        head.next.prev = node;        // ③ 原来第一个节点的前驱 → node
        head.next = node;             // ④ head 的后继 → node
    }

    /**
     * 将一个已有节点移动到链表头部
     *
     * <p>等价于：先从链表中摘下来（removeNode），再插到头部（addToHead）</p>
     *
     * <p>为什么不是"原地不动"？</p>
     * <ul>
     *   <li>节点在链表中的位置代表「上次访问时间」</li>
     *   <li>越靠近 head → 越新 → 越不容易被淘汰</li>
     *   <li>越靠近 tail → 越旧 → 越容易被淘汰</li>
     *   <li>每次访问（get 或 put 已存在的 key）都必须刷新这个时间</li>
     * </ul>
     */
    private void moveToHead(Node<K, V> node) {
        removeNode(node);   // 从原位置摘下
        addToHead(node);    // 插到头部
    }

    /**
     * 删除并返回链表尾部的节点（最久未使用）
     *
     * <p>tail.prev 永远指向最后一个真实数据节点（因为 tail 是哨兵）。</p>
     *
     * @return 被删除的节点，调用方需要通过 node.key 从 HashMap 中删除
     */
    private Node<K, V> removeTail() {
        Node<K, V> last = tail.prev;   // 最久未使用的节点
        removeNode(last);              // 从链表中摘除
        return last;                   // 返回给调用方（put 需要用 key 删 map）
    }

    // ==================== 调试 ====================

    /**
     * 打印链表顺序（从新到旧），用于调试
     *
     * <p>遍历：从 head.next 开始，直到 tail 结束</p>
     *
     * @return 示例：空缓存 → "(empty)"；有数据 → "[k1=v1] → [k2=v2] → [k3=v3]"
     */
    public String debugList() {
        if (head.next == tail) {
            return "(empty)";
        }

        StringBuilder sb = new StringBuilder();
        Node<K, V> p = head.next;     // 跳过哨兵 head
        while (p != tail) {           // 遇到哨兵 tail 停止
            if (sb.length() > 0) {
                sb.append(" → ");
            }
            sb.append("[").append(p.key).append("=").append(p.value).append("]");
            p = p.next;
        }
        return sb.toString();
    }
}
