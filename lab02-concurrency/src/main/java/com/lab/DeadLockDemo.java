package com.lab;

import java.util.concurrent.TimeUnit;

/**
 * 死锁演示 —— 你的任务：
 * <ol>
 *   <li><b>TODO: 补全两个线程的加锁逻辑</b>，让它们产生死锁</li>
 *   <li>运行程序，观察它是不是卡住了</li>
 *   <li><b>用 jstack 诊断死锁</b>（命令写在注释里了）</li>
 *   <li><b>找坑：</b>我把 sleep 删掉了，这会导致什么？加上 sleep 后有什么不同？</li>
 * </ol>
 *
 * <h3>死锁 4 个必要条件（背下来）：</h3>
 * <ol>
 *   <li>互斥条件</li>
 *   <li>请求保持</li>
 *   <li>不可剥夺</li>
 *   <li><b>循环等待</b> ← 我们通过破坏这条来预防死锁（统一加锁顺序）</li>
 * </ol>
 *
 * <h3>jstack 诊断命令：</h3>
 * <pre>
 *   # 终端 1：运行本程序
 *   mvn exec:java -Dexec.mainClass="com.lab.DeadLockDemo"
 *
 *   # 终端 2：诊断
 *   jps -l              # 找到 DeadLockDemo 的 PID
 *   jstack &lt;PID&gt;        # 输出线程堆栈，看底部是否有 "Found one Java-level deadlock"
 * </pre>
 */
public class DeadLockDemo {

    // 两把锁
    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) {
        System.out.println("===== 死锁演示 =====\n");
        System.out.println("设计目标：Thread-1 先拿 A 再拿 B，Thread-2 先拿 B 再拿 A");
        System.out.println("如果两个线程同时拿到第一把锁，就会互相等待 → 死锁\n");

        // ---- TODO: 线程 1 —— 先拿 lockA，再拿 lockB ----
        // 提示：用 synchronized (lockA) { ... synchronized (lockB) { ... } }
        // ⚠️ 没有 sleep 的情况下，死锁不一定发生！为什么？
        //    试试加上 sleep(100) 和不加的区别
        Thread t1 = new Thread(() -> {
            // TODO: 你的代码写在这里
            // 1. 先 synchronized (lockA)
            synchronized (lockA) {
                System.out.println("Thread-1 拿到 lockA");
                sleep(100);
                // 2. 里面再 synchronized (lockB)
                synchronized (lockB) {
                System.out.println("Thread-1 拿到 lockB");
                sleep(100);
                }
            }    
        }, "Thread-1");

        // ---- TODO: 线程 2 —— 先拿 lockB，再拿 lockA（注意顺序！） ----
        Thread t2 = new Thread(() -> {
            // TODO: 你的代码写在这里
            // 1. 先 synchronized (lockB)
            synchronized (lockB) {
                System.out.println("Thread-2 拿到 lockB");
                sleep(100);
                // 2. 里面再 synchronized (lockA)
                synchronized (lockA) {
                System.out.println("Thread-2 拿到 lockA");
                sleep(100);
                }
            }
        }, "Thread-2");

        // ---- 启动线程 ----
        t1.start();
        t2.start();

        // ---- 监控线程：每 2 秒打印状态 ----
        Thread monitor = new Thread(() -> {
            while (true) {
                sleep(2000);
                System.out.printf("[监控] Thread-1=%s  Thread-2=%s%n",
                    t1.getState(), t2.getState());

                if (t1.getState() == Thread.State.BLOCKED &&
                    t2.getState() == Thread.State.BLOCKED) {
                    System.out.println("\n⚠️ 两个线程都 BLOCKED!新开终端执行：");
                    System.out.println("   jps -l");
                    System.out.println("   jstack <PID>");
                    break; // 打印一次就够了
                }
            }
        }, "monitor");
        monitor.setDaemon(true);
        monitor.start();

        // ---- 等待线程结束（正常情况永远等不到——因为死锁了） ----
        try {
            t1.join();
            t2.join();
            System.out.println("如果看到这行 → 没死锁（试试多加几次 sleep）");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleep(long millis) {
        try { TimeUnit.MILLISECONDS.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
