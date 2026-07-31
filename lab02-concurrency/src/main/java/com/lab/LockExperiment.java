package com.lab;

import java.util.concurrent.TimeUnit;

/**
 * synchronized 锁类型实验 —— 对象锁 vs 类锁
 *
 * <p>你的任务：
 * <ol>
 *   <li>先运行代码看结果，回答：两个线程能并发执行吗？为什么？</li>
 *   <li><b>TODO: 补全实验 2</b> —— 测试两个实例方法在<b>同一个对象</b>上能否并发</li>
 *   <li>对比两次实验的耗时，写出结论</li>
 * </ol>
 *
 * <p>核心知识点：
 * <ul>
 *   <li>实例方法的 synchronized → 锁的是 <b>this</b>（当前实例）</li>
 *   <li>静态方法的 synchronized → 锁的是 <b>Xxx.class</b>（类对象）</li>
 *   <li>两个是不同的锁 → 可以并发</li>
 * </ul>
 */
public class LockExperiment {

    // ========== 实例方法：锁 this ==========
    public synchronized void instanceMethod(String tag) {
        System.out.println("[" + tag + "] 进入实例方法 (锁 = this 对象)");
        sleep(2);
        System.out.println("[" + tag + "] 离开实例方法");
    }

    // ========== 静态方法：锁 LockExperiment.class ==========
    public static synchronized void staticMethod(String tag) {
        System.out.println("[" + tag + "] 进入静态方法 (锁 = LockExperiment.class)");
        sleep(2);
        System.out.println("[" + tag + "] 离开静态方法");
    }

    public static void main(String[] args) throws Exception {
        LockExperiment obj = new LockExperiment();

        // ========== 实验 1：对象锁 vs 类锁（已写好） ==========
        System.out.println("===== 实验 1: 对象锁 vs 类锁 =====\n");

        Thread tA = new Thread(() -> obj.instanceMethod("线程A"), "Thread-A");
        Thread tB = new Thread(() -> LockExperiment.staticMethod("线程B"), "Thread-B");

        long start1 = System.currentTimeMillis();
        tA.start();
        tB.start();
        tA.join();
        tB.join();
        long cost1 = System.currentTimeMillis() - start1;

        System.out.println("实验 1 总耗时: " + cost1 + "ms");
        System.out.println("如果约等于 2s → 并发执行（锁不同，互不影响）");
        System.out.println("如果约等于 4s → 串行执行\n");

        // ========== 实验 2：TODO 你来写 ==========
        // 目标：两个线程调用<b>同一个对象</b>的两个实例方法
        // 问题：它们能并发执行吗？为什么？
        //不能并发，锁的是同一个 this 对象，耗时约 4s
        // 提示：
        //   1. 创建两个线程 C 和 D
        //   2. 都调用 obj.instanceMethod(...) （注意：是同一个 obj！）
        //   3. 记录耗时，对比实验 1 的结论

        // TODO: 你的代码写在这里
        Thread tC = new Thread(() -> obj.instanceMethod("线程C"), "Thread-C");
        Thread tD = new Thread(() -> obj.instanceMethod("线程D"), "Thread-D");

        long start2 = System.currentTimeMillis();
        tC.start();
        tD.start();
        tC.join();
        tD.join();
        long cost2 = System.currentTimeMillis() - start2;

        System.out.println("实验 2 总耗时: " + cost2 + "ms");
        System.out.println("如果约等于 2s → 并发执行（锁不同，互不影响）");
        System.out.println("如果约等于 4s → 串行执行\n");

        System.out.println("\n===== 实验结束 =====");
    }

    private static void sleep(int seconds) {
        try { TimeUnit.SECONDS.sleep(seconds); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
