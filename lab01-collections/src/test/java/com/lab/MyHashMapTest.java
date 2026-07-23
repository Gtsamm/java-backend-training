package com.lab;

/**
 * MyHashMap 测试类
 *
 * <p>每个测试方法独立验证一个功能点，注释中标注了：
 * <ul>
 *   <li>【目的】这个测试在测什么</li>
 *   <li>【预期】正确的结果应该是什么</li>
 *   <li>【面试】这个测试对应哪个面试考点</li>
 * </ul>
 */
public class MyHashMapTest {

    public static void main(String[] args) {
        MyHashMapTest t = new MyHashMapTest();

        t.testPutAndGet();          // 基础写入与读取
        t.testSizeAndIsEmpty();     // 元素数量与判空
        t.testKeyOverride();        // 相同 key 覆盖旧值
        t.testNullKey();            // null key 处理
        t.testHashCollision();      // 哈希碰撞（不同 key 落同一个桶）
        t.testResize();             // 扩容 + 数据不丢失
        t.testRemove();             // 删除节点（头节点 / 中间节点 / 尾节点）
        t.testContainsKey();        // containsKey 不依赖 value 是否为 null
        t.testLargeDataset();       // 大批量数据综合压力

        System.out.println("\n========================================");
        System.out.println("  全部测试通过 ✅");
        System.out.println("========================================");
    }

    // ==================== 辅助方法 ====================

    /** 断言相等，不相等则抛出异常 */
    private static void assertEq(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(
                "❌ " + msg + " —— 期望: " + expected + "，实际: " + actual);
        }
        System.out.println("  ✅ " + msg);
    }

    /** 打印分隔线 */
    private static void title(String name) {
        System.out.println("\n━━━ " + name + " ━━━");
    }

    // ==================== 测试方法 ====================

    /**
     * 测试 1：基础 put / get / size / isEmpty
     *
     * 【目的】验证最核心的写入和读取功能
     * 【面试】HashMap put 和 get 的时间复杂度是多少？（均摊 O(1)）
     */
    void testPutAndGet() {
        title("测试 1：基础 put / get");

        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("apple", 1);
        map.put("banana", 2);
        map.put("cherry", 3);

        // 验证能读到正确的 value
        assertEq(1, map.get("apple"), "get('apple') 返回 1");
        assertEq(2, map.get("banana"), "get('banana') 返回 2");
        assertEq(3, map.get("cherry"), "get('cherry') 返回 3");

        // 不存在的 key 返回 null
        assertEq(null, map.get("not-exist"), "get('not-exist') 返回 null");

        System.out.println("  📊 debugInfo → " + map.debugInfo());
    }

    /**
     * 测试 2：size / isEmpty
     *
     * 【目的】验证计数器正确性
     * 【面试】HashMap 的 size() 是 O(1) 吗？（是，直接返回维护的 size 字段）
     */
    void testSizeAndIsEmpty() {
        title("测试 2：size / isEmpty");

        MyHashMap<String, Integer> map = new MyHashMap<>();

        // 空表
        assertEq(0, map.size(), "空表 size() == 0");
        assertEq(true, map.isEmpty(), "空表 isEmpty() == true");

        // 添加元素后
        map.put("a", 1);
        assertEq(1, map.size(), "添加 1 个后 size() == 1");
        assertEq(false, map.isEmpty(), "添加后 isEmpty() == false");

        map.put("b", 2);
        assertEq(2, map.size(), "添加 2 个后 size() == 2");

        // 覆盖已有 key，size 不应该变化
        map.put("a", 100);
        assertEq(2, map.size(), "覆盖 key 后 size() 不变 == 2");
    }

    /**
     * 测试 3：相同 key 覆盖旧值
     *
     * 【目的】验证 put 在 key 已存在时返回旧值并覆盖
     * 【面试】put 方法的返回值含义是什么？
     *        （返回该 key 之前关联的旧值，如果是新 key 则返回 null）
     */
    void testKeyOverride() {
        title("测试 3：key 覆盖");

        MyHashMap<String, String> map = new MyHashMap<>();

        // 第一次 put 新 key → 返回 null
        String old1 = map.put("name", "张三");
        assertEq(null, old1, "新 key 第一次 put 返回 null");

        // 第二次 put 相同 key → 返回旧值
        String old2 = map.put("name", "李四");
        assertEq("张三", old2, "相同 key 再次 put 返回旧值 '张三'");

        // value 已被覆盖
        assertEq("李四", map.get("name"), "get 拿到的是覆盖后的新值 '李四'");
    }

    /**
     * 测试 4：null key
     *
     * 【目的】验证 null key 固定在 bucket[0]，且可以正常存取
     * 【面试】
     *   Q: HashMap 允许 null key 吗？允许几个？放在哪？
     *   A: 允许 1 个 null key，hash 值固定为 0，一定在 bucket[0]
     */
    void testNullKey() {
        title("测试 4：null key");

        MyHashMap<String, String> map = new MyHashMap<>();

        // null key 可以正常写入
        map.put(null, "null-key-value");
        assertEq("null-key-value", map.get(null), "get(null) 返回 value");

        // size 正确计数
        assertEq(1, map.size(), "null key 也计入 size == 1");

        // null key 只有一个，第二次 put 应该覆盖
        String old = map.put(null, "new-null-value");
        assertEq("null-key-value", old, "新 null key put 返回旧值");
        assertEq("new-null-value", map.get(null), "null key 被覆盖");
        assertEq(1, map.size(), "覆盖 null key，size 保持 1");
    }

    /**
     * 测试 5：哈希碰撞
     *
     * 【目的】验证不同 key 落在同一个桶时，链表能正确存取
     * 【原理】
     *   我们故意构造一组 hash 相同的对象（通过重写 hashCode 返回固定值），
     *   它们一定会落在同一个 bucket 上，形成链表。
     *   正确实现应该能遍历链表找到每个不同的 key。
     * 【面试】
     *   Q: 哈希碰撞是怎么解决的？
     *   A: 链地址法 —— 同一个桶上的元素用链表串联，查找时先定位桶再遍历链表
     */
    void testHashCollision() {
        title("测试 5：哈希碰撞（链地址法）");

        // 这三个对象的 hashCode 固定为 1，保证落同一个桶
        MyHashMap<FixedHash, String> map = new MyHashMap<>();
        FixedHash k1 = new FixedHash("A");
        FixedHash k2 = new FixedHash("B");
        FixedHash k3 = new FixedHash("C");

        map.put(k1, "value-A");
        map.put(k2, "value-B");
        map.put(k3, "value-C");

        // 验证三个都能正确取出（需要 equals 区分不同 key）
        assertEq("value-A", map.get(k1), "碰撞桶中找到 k1");
        assertEq("value-B", map.get(k2), "碰撞桶中找到 k2");
        assertEq("value-C", map.get(k3), "碰撞桶中找到 k3");
        assertEq(3, map.size(), "碰撞桶 size 正确 == 3");

        // 相同 key(k1) 覆盖
        String old = map.put(k1, "new-value-A");
        assertEq("value-A", old, "碰撞桶中覆盖 key 返回旧值");
        assertEq("new-value-A", map.get(k1), "碰撞桶中 get 覆盖后的新值");

        System.out.println("  📊 debugInfo → " + map.debugInfo());
    }

    /**
     * 测试 6：扩容（resize）
     *
     * 【目的】验证扩容后所有数据不丢失，size 不变
     * 【原理】
     *   默认容量 16，负载因子 0.75，阈值 = 12
     *   当 size > 12 时触发扩容，容量翻倍到 32
     *   扩容时链表被拆分为低位链（留在原索引）和高位链（移到原索引 + oldCap）
     * 【面试】
     *   Q: 为什么扩容是 2 倍而不是 1.5 倍？
     *   A: 因为容量是 2 的幂，扩容后可以用 hash & oldCap 快速拆分链表，
     *      不需要对每个节点重新取模。如果是 1.5 倍则会失去这个特性。
     */
    void testResize() {
        title("测试 6：扩容");

        // capacity 设为 4（非常小），阈值 = 4 * 0.75 = 3，方便快速触发扩容
        MyHashMap<String, Integer> map = new MyHashMap<>(4);

        // 第 1~3 个元素，不会扩容
        map.put("k1", 1);
        map.put("k2", 2);
        map.put("k3", 3);
        assertEq(3, map.size(), "扩容前 size == 3");

        System.out.println("  📊 扩容前 → " + map.debugInfo());

        // 第 4 个元素 → size=4 > threshold=3 → 触发扩容（4→8）
        map.put("k4", 4);
        assertEq(4, map.size(), "扩容后 size 保持不变 == 4");

        System.out.println("  📊 扩容后 → " + map.debugInfo());

        // 扩容后所有旧数据必须能正确取出
        assertEq(1, map.get("k1"), "扩容后 get('k1') == 1");
        assertEq(2, map.get("k2"), "扩容后 get('k2') == 2");
        assertEq(3, map.get("k3"), "扩容后 get('k3') == 3");
        assertEq(4, map.get("k4"), "扩容后 get('k4') == 4");

        // 再塞一批，触发第二次扩容（8→16）
        for (int i = 5; i <= 10; i++) {
            map.put("k" + i, i);
        }
        assertEq(10, map.size(), "二次扩容后 size == 10");

        // 验证全部数据
        for (int i = 1; i <= 10; i++) {
            assertEq(i, map.get("k" + i), "二次扩容后 get('k" + i + "') == " + i);
        }

        System.out.println("  📊 二次扩容后 → " + map.debugInfo());
    }

    /**
     * 测试 7：remove
     *
     * 【目的】验证删除的三种情况：删头节点 / 删中间节点 / 删尾节点
     * 【原理】
     *   删除头节点：bucket[index] = node.next（改了桶的入口）
     *   删除中间/尾节点：prev.next = node.next（绕过被删节点）
     * 【面试】
     *   Q: HashMap remove 时间复杂度是多少？
     *   A: O(1) —— 先定位桶 O(1)，再遍历链表（链表短时接近 O(1)）
     */
    void testRemove() {
        title("测试 7：remove");

        // 用 FixedHash 让所有 key 落同一个桶，方便测试链表中不同位置的删除
        MyHashMap<FixedHash, String> map = new MyHashMap<>();
        FixedHash a = new FixedHash("A");
        FixedHash b = new FixedHash("B");
        FixedHash c = new FixedHash("C");
        FixedHash d = new FixedHash("D");

        map.put(a, "va");
        map.put(b, "vb");
        map.put(c, "vc");
        map.put(d, "vd");
        assertEq(4, map.size(), "删除前 size == 4");

        // 情况 1：删除链表头节点
        String removedA = map.remove(a);
        assertEq("va", removedA, "删除头节点 A 返回 'va'");
        assertEq(null, map.get(a), "删除后 get(A) 返回 null");
        assertEq(3, map.size(), "删除 A 后 size == 3");
        // 其余节点不受影响
        assertEq("vb", map.get(b), "删除头节点不影响 B");
        assertEq("vc", map.get(c), "删除头节点不影响 C");
        assertEq("vd", map.get(d), "删除头节点不影响 D");

        // 情况 2：删除链表尾节点
        String removedD = map.remove(d);
        assertEq("vd", removedD, "删除尾节点 D 返回 'vd'");
        assertEq(null, map.get(d), "删除后 get(D) 返回 null");
        assertEq(2, map.size(), "删除 D 后 size == 2");

        // 情况 3：删除中间节点
        String removedC = map.remove(c);
        assertEq("vc", removedC, "删除中间节点 C 返回 'vc'");
        assertEq(null, map.get(c), "删除后 get(C) 返回 null");
        assertEq(1, map.size(), "删除 C 后 size == 1");
        assertEq("vb", map.get(b), "其余节点 B 不受影响");

        // 情况 4：删除不存在的 key
        String removedNone = map.remove(new FixedHash("NOT-EXIST"));
        assertEq(null, removedNone, "删除不存在的 key 返回 null");
        assertEq(1, map.size(), "删除不存在 key 后 size 不变");
    }

    /**
     * 测试 8：containsKey
     *
     * 【目的】验证 containsKey 不依赖 value 是否为 null
     * 【原理】
     *   get(key) 返回 null 有两种情况：① key 不存在 ② key 存在但 value 就是 null
     *   所以 containsKey 不能直接用 get(key) != null 来判断
     *   必须亲自遍历链表，判断节点是否存在
     * 【面试】
     *   Q: 为什么 HashMap 有 containsKey() 还要有 containsValue()？
     *   A: containsKey O(1)，containsValue O(n)，用途不同。
     *      containsKey 只看一个桶的链表，containsValue 要遍历全部节点。
     */
    void testContainsKey() {
        title("测试 8：containsKey");

        MyHashMap<String, String> map = new MyHashMap<>();
        map.put("has-null", null);   // key 存在，但 value = null
        map.put("has-value", "ok");

        // key 存在（即使 value 为 null）
        assertEq(true, map.containsKey("has-null"),
            "containsKey('has-null') == true（value 为 null 但 key 存在）");
        assertEq(true, map.containsKey("has-value"),
            "containsKey('has-value') == true");

        // key 不存在
        assertEq(false, map.containsKey("not-exist"),
            "containsKey('not-exist') == false");

        // 用 get() 来对比：get("has-null") 返回 null，但 key 确实存在
        // 这就说明了为什么 containsKey 不能简单调 get() 判断
        assertEq(null, map.get("has-null"),
            "get('has-null') 返回 null（但 key 存在！这就是 containsKey 不能调 get 的原因）");
    }

    /**
     * 测试 9：大批量数据综合压力
     *
     * 【目的】验证在较大数据量下所有操作的正确性
     * 【面试】
     *   Q: HashMap 的默认容量 16 够吗？什么情况下需要指定初始容量？
     *   A: 如果能预估数据量，建议指定 initialCapacity = 预期数量 / 0.75 + 1，
     *      避免频繁扩容带来的性能损耗。
     */
    void testLargeDataset() {
        title("测试 9：大批量数据（1000 条）");

        MyHashMap<Integer, String> map = new MyHashMap<>();

        // 写入 1000 条
        for (int i = 0; i < 1000; i++) {
            map.put(i, "value-" + i);
        }
        assertEq(1000, map.size(), "写入 1000 条后 size == 1000");

        // 随机读取验证
        for (int i = 0; i < 1000; i += 97) {  // 抽样检查（97 是质数，打散分布）
            assertEq("value-" + i, map.get(i), "get(" + i + ") 正确");
        }

        // 删除偶数 key
        for (int i = 0; i < 1000; i += 2) {
            map.remove(i);
        }
        assertEq(500, map.size(), "删除 500 个偶数 key 后 size == 500");

        // 验证奇数 key 还在，偶数 key 没了
        for (int i = 0; i < 1000; i++) {
            if (i % 2 == 0) {
                assertEq(null, map.get(i), "偶数 key " + i + " 已被删除");
            } else {
                assertEq("value-" + i, map.get(i), "奇数 key " + i + " 还在");
            }
        }

        System.out.println("  📊 最终桶分布 → " + map.debugInfo());
    }

    // ==================== 碰撞测试辅助类 ====================

    /**
     * hashCode 固定为 1 的测试用类
     *
     * <p>用于模拟哈希碰撞场景。
     * 所有 FixedHash 对象的 hashCode 都为 1，必定落在同一个桶上，
     * 但 equals 仍按 id 区分，从而形成"同桶不同 key"的链表。</p>
     */
    static class FixedHash {
        private final String id;

        FixedHash(String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 1;  // 固定 hash → 必然碰撞
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FixedHash)) return false;
            FixedHash other = (FixedHash) obj;
            return java.util.Objects.equals(this.id, other.id);
        }

        @Override
        public String toString() {
            return "FH{" + id + "}";
        }
    }
}
