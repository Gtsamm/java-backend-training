package com.lab;

/**
 * LRUCache 测试
 *
 * <p>每个测试覆盖一个核心场景，通过 debugList() 可视化链表状态。</p>
 */
public class LRUCacheTest {

    public static void main(String[] args) {
        LRUCacheTest t = new LRUCacheTest();

        t.testBasicPutAndGet();       // 基础写入读取
        t.testCapacityEviction();     // 容量满了触发淘汰
        t.testGetUpdatesOrder();      // get 也会更新访问顺序（关键！）
        t.testPutUpdatesOrder();      // put 已存在 key 也会更新顺序
        t.testLRUOrder();            // 完整 LRU 顺序验证
        t.testLeetCodeExample();      // LeetCode 146 官方示例

        System.out.println("\n========================================");
        System.out.println("  全部测试通过 ✅");
        System.out.println("========================================");
    }

    private static void assertEq(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(
                "❌ " + msg + " —— 期望: " + expected + "，实际: " + actual);
        }
        System.out.println("  ✅ " + msg);
    }

    private static void title(String name) {
        System.out.println("\n━━━ " + name + " ━━━");
    }

    // ================================================================

    /**
     * 测试 1：基础 put / get
     *
     * 【验证】能正常存取数据，get 不存在的 key 返回 null
     */
    void testBasicPutAndGet() {
        title("测试 1：基础 put / get");

        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);

        assertEq(1, cache.get("a"), "get('a') == 1");
        assertEq(2, cache.get("b"), "get('b') == 2");
        assertEq(null, cache.get("c"), "get('c') 不存在 → null");
        assertEq(2, cache.size(), "size == 2");
    }

    /**
     * 测试 2：容量满时触发淘汰
     *
     * 【验证】容量 3，放第 4 个 key 时，最久未使用的被删掉
     *
     * 【过程演示】
     *   put(a) → 链表: [a]          size=1
     *   put(b) → 链表: [b]→[a]      size=2
     *   put(c) → 链表: [c]→[b]→[a]  size=3 (满)
     *   put(d) → 淘汰 a，链表: [d]→[c]→[b]  size=3
     */
    void testCapacityEviction() {
        title("测试 2：容量满 → 淘汰最久未使用");

        LRUCache<String, String> cache = new LRUCache<>(3);
        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");

        System.out.println("  放入 a,b,c 后: " + cache.debugList());
        assertEq(3, cache.size(), "size == 3（满）");

        // 放入第 4 个，a 是最久未使用的，应该被淘汰
        cache.put("d", "D");

        System.out.println("  放入 d 后:     " + cache.debugList());

        assertEq(null, cache.get("a"), "a 被淘汰 → get('a') == null");
        assertEq("B", cache.get("b"), "b 还在");
        assertEq("C", cache.get("c"), "c 还在");
        assertEq("D", cache.get("d"), "d 是最新的");
        assertEq(3, cache.size(), "size 保持 3");
    }

    /**
     * 测试 3：get 会更新 LRU 顺序（最容易踩的坑！）
     *
     * 【验证】只读不写的 get() 也会把节点移到头部
     *
     * 【错误理解】get 只是读，不应该改顺序
     * 【正确理解】get 是访问行为，LRU 的"U"就是 Used（使用），必须更新
     *
     * 【过程演示】
     *   put(a), put(b), put(c) → [c]→[b]→[a]，a 最旧
     *   get(a)                 → [a]→[c]→[b]，a 被移到头部，b 变成最旧！
     *   put(d)                 → 淘汰 b（不是 a！因为 a 刚被访问过）
     */
    void testGetUpdatesOrder() {
        title("测试 3：get 会刷新访问顺序");

        LRUCache<String, String> cache = new LRUCache<>(3);
        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        System.out.println("  初始: " + cache.debugList() + "  ← a 最旧");

        // 访问 a —— 这是读操作，但会影响 LRU 顺序！
        cache.get("a");
        System.out.println("  get(a) 后: " + cache.debugList() + "  ← a 被移到头部，b 变成最旧");

        // 再放一个新 key，淘汰的应该是 b（不是 a）
        cache.put("d", "D");
        System.out.println("  put(d) 后: " + cache.debugList() + "  ← b 被淘汰");

        assertEq("A", cache.get("a"), "a 刚被 get 过，不应被淘汰");
        assertEq(null, cache.get("b"), "b 变成最旧的，被淘汰");
        assertEq("C", cache.get("c"), "c 还在");
        assertEq("D", cache.get("d"), "d 是最新的");
    }

    /**
     * 测试 4：put 已存在的 key 也会更新顺序
     *
     * 【验证】更新 value 也是"使用"，节点应移到头部
     *
     * 【过程演示】
     *   put(a), put(b) → [b]→[a]
     *   put(a, NEW)   → [a]→[b]   a 更新并移到头部
     */
    void testPutUpdatesOrder() {
        title("测试 4：put 已存在 key 更新顺序");

        LRUCache<String, String> cache = new LRUCache<>(2);
        cache.put("a", "old-A");
        cache.put("b", "B");
        System.out.println("  初始: " + cache.debugList());

        // 更新 a 的值，a 应该被移到头部
        cache.put("a", "new-A");
        System.out.println("  put(a, new) 后: " + cache.debugList());

        // ⚠️ 用 debugList() 验证状态，不能用 get()！
        //    因为 get() 会触发 moveToHead，改变 LRU 顺序
        assertEq(2, cache.size(), "size 保持 2（更新不增加元素）");

        // 此时顺序: [a=new-A] → [b=B]，b 是最旧的
        // 放入 c 应该淘汰 b（不是 a）
        cache.put("c", "C");
        System.out.println("  put(c) 后: " + cache.debugList());

        assertEq("new-A", cache.get("a"), "a 没被淘汰（put 更新后 a 在头部）");
        assertEq(null, cache.get("b"), "b 被淘汰（put 更新后 b 在尾部）");
        assertEq("C", cache.get("c"), "c 是最新的");
    }

    /**
     * 测试 5：完整 LRU 顺序验证
     *
     * 【目的】逐步验证每一步链表状态，不留盲区
     */
    void testLRUOrder() {
        title("测试 5：完整 LRU 顺序");

        LRUCache<Integer, String> cache = new LRUCache<>(3);

        // 步骤 1：逐个放入
        cache.put(1, "one");
        System.out.println("  put(1): " + cache.debugList());

        cache.put(2, "two");
        System.out.println("  put(2): " + cache.debugList());

        cache.put(3, "three");
        System.out.println("  put(3): " + cache.debugList());
        assertEq(3, cache.size(), "size == 3");

        // 步骤 2：访问 1，把 1 移到头部
        cache.get(1);
        System.out.println("  get(1): " + cache.debugList() + "  ← 1 移到头部");
        // 此时顺序: 1(head) → 3 → 2(tail附近，最旧)

        // 步骤 3：放入 4，应该淘汰 2（因为 1 刚被访问，3 次之，2 最旧）
        cache.put(4, "four");
        System.out.println("  put(4): " + cache.debugList() + "  ← 2 被淘汰");
        assertEq("one", cache.get(1), "1 还在");
        assertEq(null, cache.get(2), "2 被淘汰");
        assertEq("three", cache.get(3), "3 还在");
        assertEq("four", cache.get(4), "4 是最新的");
    }

    /**
     * 测试 6：LeetCode 146 官方示例
     *
     * 【目的】对照 LeetCode 官方示例验证
     *
     * 输入：
     * ["LRUCache","put","put","get","put","get","put","get","get","get"]
     * [[2],[1,1],[2,2],[1],[3,3],[2],[4,4],[1],[3],[4]]
     *
     * 输出：
     * [null,null,null,1,null,-1,null,-1,3,4]
     */
    void testLeetCodeExample() {
        title("测试 6：LeetCode 146 官方示例");

        LRUCache<Integer, Integer> cache = new LRUCache<>(2);

        cache.put(1, 1);  // 缓存:{1=1}
        cache.put(2, 2);  // 缓存:{1=1, 2=2}
        assertEq(1, cache.get(1), "get(1) == 1");  // 返回 1，1→头部

        cache.put(3, 3);  // 淘汰 2，缓存:{1=1, 3=3}
        assertEq(-1, cache.get(2) == null ? -1 : cache.get(2), "get(2) == -1（已淘汰）");

        cache.put(4, 4);  // 淘汰 1，缓存:{4=4, 3=3}
        assertEq(-1, cache.get(1) == null ? -1 : cache.get(1), "get(1) == -1（已淘汰）");
        assertEq(3, cache.get(3), "get(3) == 3");
        assertEq(4, cache.get(4), "get(4) == 4");
    }
}
