package com.lab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ArrayList vs LinkedList 性能对比 Benchmark。
 * <p>
 * 【测试维度】
 * 1. 尾部追加 —— add(e)
 * 2. 头部插入 —— add(0, e)
 * 3. 随机访问 —— get(i)
 * 4. 中间插入 —— add(size/2, e)
 * 5. 中间删除 —— remove(size/2)
 * 6. 遍历 —— for-each
 * <p>
 * 【为什么不用 JMH？】
 * JMH（Java Microbenchmark Harness）是专业的微基准测试框架，能处理 JIT 预热、死代码消除等问题。
 * 但它需要独立的 Maven 模块和插件配置，对 Day 2 来说太复杂。
 * 这里用 System.nanoTime() 做简单对比，结论和趋势是正确的。
 * 后续 Week 8 性能压测时会系统学习 JMH。
 * <p>
 * 【看懂结果的关键】
 * - 每个测试跑多次取平均（跑一轮可能恰好遇到 GC 导致数据不准）
 * - 关注数量级差异（比如 1000 倍），而不是精确值
 * - 数据规模从小到大多测几组，看趋势
 */
public class ListBenchmark {

    /** 每项测试重复次数（取平均用） */
    private static final int REPEAT = 5;

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("    ArrayList vs LinkedList 性能对比");
        System.out.println("=".repeat(70));

        // 用不同数据规模测试，观察趋势
        int[] sizes = {10000, 100000, 500000};

        for (int n : sizes) {
            System.out.println("\n┌─────────────────────────────────────────────────────────────────┐");
            System.out.printf("│  数据规模: %,d                                                  │\n", n);
            System.out.println("└─────────────────────────────────────────────────────────────────┘");

            testTailAdd(n);
            testHeadAdd(n);
            testRandomAccess(n);
            testMiddleInsert(n);
            testMiddleRemove(n);
            testForEach(n);
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("测试完成！别忘了思考「为什么」会有这些差异。");
        System.out.println("=".repeat(70));
    }

    // ==================== 测试方法 ====================

    private static void testTailAdd(int n) {
        System.out.printf("  %-18s", "① 尾部追加");

        // ArrayList 尾插
        long arrayTime = benchmark(() -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) list.add(i);
        }, REPEAT);

        // LinkedList 尾插
        long linkedTime = benchmark(() -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < n; i++) list.add(i);
        }, REPEAT);

        printResult(arrayTime, linkedTime);
    }

    private static void testHeadAdd(int n) {
        System.out.printf("  %-18s", "② 头部插入");

        // ArrayList 头插 —— 每次都把所有元素后移一位！
        long arrayTime = benchmark(() -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) list.add(0, i);  // 每次插在头部
        }, REPEAT);

        // LinkedList 头插 —— 只改两个指针
        long linkedTime = benchmark(() -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < n; i++) list.add(0, i);  // 每次插在头部
        }, REPEAT);

        printResult(arrayTime, linkedTime);
    }

    private static void testRandomAccess(int n) {
        System.out.printf("  %-18s", "③ 随机访问");

        // 先建好数据
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) { arrayList.add(i); linkedList.add(i); }

        // ArrayList 随机访问 —— O(1)，直接通过下标
        long arrayTime = benchmark(() -> {
            long sum = 0;
            for (int i = 0; i < 10000; i++) {
                sum += arrayList.get(n / 2);  // 一直取中间位置
            }
            // 用 sum 防止 JIT 把循环优化掉
            if (sum < 0) System.out.println("不会执行");
        }, REPEAT);

        // LinkedList 随机访问 —— O(n)，每次从头或尾开始遍历
        long linkedTime = benchmark(() -> {
            long sum = 0;
            for (int i = 0; i < 10000; i++) {
                sum += linkedList.get(n / 2);  // 一直取中间位置
            }
            if (sum < 0) System.out.println("不会执行");
        }, REPEAT);

        printResult(arrayTime, linkedTime);
    }

    private static void testMiddleInsert(int n) {
        System.out.printf("  %-18s", "④ 中间插入");

        // 先建好数据
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) { arrayList.add(i); linkedList.add(i); }

        int insertCount = 1000;
        int mid = n / 2;

        // ArrayList 中间插入 —— O(n)，需要搬移后面一半元素
        long arrayTime = benchmark(() -> {
            for (int i = 0; i < insertCount; i++) {
                arrayList.add(mid, i);
            }
        }, REPEAT / 2 + 1);  // 少重复几次，因为会改数据

        printResult(arrayTime, -1);  // LinkedList 单独测（定位慢）
        System.out.printf("  (ArrayList 中间插入 %,d 次) ", insertCount);

        // 重建链表数据
        List<Integer> linkedList2 = new LinkedList<>();
        for (int i = 0; i < n; i++) linkedList2.add(i);

        // LinkedList 中间插入 —— 先 O(n) 定位到中间，再 O(1) 改指针
        // 这里用 iterator 方式定位更高效，但 get(n/2) 每次都要从头遍历
        long linkedTime = benchmark(() -> {
            for (int i = 0; i < insertCount; i++) {
                // 用 add(index, e) —— LinkedList 需要先遍历到 index 位置
                linkedList2.add(mid, i);
            }
        }, REPEAT / 2 + 1);

        long arrayFinal = arrayTime;
        printCompare(arrayFinal, linkedTime, insertCount);
    }

    private static void testMiddleRemove(int n) {
        System.out.printf("  %-18s", "⑤ 中间删除");

        int removeCount = 1000;

        // ArrayList 中间删除 —— O(n)，需要搬移后面一半元素
        long arrayTime = benchmark(() -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) list.add(i);
            for (int i = 0; i < removeCount; i++) {
                list.remove(list.size() / 2);
            }
        }, REPEAT / 2 + 1);

        // LinkedList 中间删除 —— 需要先 O(n) 定位
        long linkedTime = benchmark(() -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < n; i++) list.add(i);
            for (int i = 0; i < removeCount; i++) {
                list.remove(list.size() / 2);
            }
        }, REPEAT / 2 + 1);

        printResult(arrayTime, linkedTime);
    }

    private static void testForEach(int n) {
        System.out.printf("  %-18s", "⑥ for-each 遍历");

        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) { arrayList.add(i); linkedList.add(i); }

        // ArrayList 遍历 —— 连续内存，缓存友好
        long arrayTime = benchmark(() -> {
            long sum = 0;
            for (int x : arrayList) sum += x;
            if (sum < 0) System.out.println("不会执行");
        }, REPEAT);

        // LinkedList 遍历 —— 指针跳转，缓存不友好
        long linkedTime = benchmark(() -> {
            long sum = 0;
            for (int x : linkedList) sum += x;
            if (sum < 0) System.out.println("不会执行");
        }, REPEAT);

        printResult(arrayTime, linkedTime);
    }

    // ==================== 工具方法 ====================

    /**
     * 跑一段代码，返回平均耗时（纳秒）。
     *
     * @param task   要跑的代码
     * @param repeat 重复次数（取平均）
     * @return 平均耗时（纳秒）
     */
    private static long benchmark(Runnable task, int repeat) {
        // 先跑一次预热（让 JIT 编译），不计入统计
        task.run();

        long total = 0;
        for (int i = 0; i < repeat; i++) {
            long start = System.nanoTime();
            task.run();
            total += System.nanoTime() - start;
        }
        return total / repeat;  // 返回平均值
    }

    /**
     * 打印对比结果。
     */
    private static void printResult(long arrayTime, long linkedTime) {
        if (linkedTime == -1) {
            System.out.printf("ArrayList: %,d ns\n", arrayTime);
            return;
        }

        System.out.printf("ArrayList: %,d ns  │  LinkedList: %,d ns", arrayTime, linkedTime);

        // 看谁更快
        if (arrayTime < linkedTime) {
            double ratio = (double) linkedTime / arrayTime;
            System.out.printf("  → ArrayList 快 %.1f 倍", ratio);
        } else {
            double ratio = (double) arrayTime / linkedTime;
            System.out.printf("  → LinkedList 快 %.1f 倍", ratio);
        }
        System.out.println();
    }

    private static void printCompare(long arrayTime, long linkedTime, int count) {
        System.out.printf("ArrayList: %,d ns  │  LinkedList: %,d ns  (各插入/删除 %,d 次)\n",
                          arrayTime, linkedTime, count);
    }
}
