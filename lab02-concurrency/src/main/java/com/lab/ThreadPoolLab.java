package com.lab;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池 7 参数实验
 *
 * <p>线程池工作流程（面试必问）：</p>
 * <pre>
 *   提交任务 →
 *     ① 核心线程有空闲？→ 直接执行
 *     ② 核心线程忙 → 任务入队
 *     ③ 队列满了 → 创建临时线程（不超过 maximumPoolSize）
 *     ④ 池满 + 队列满 → 触发拒绝策略
 * </pre>
 *
 * <p>7 个参数：</p>
 * <ol>
 *   <li>corePoolSize     — 核心线程数（常驻，即使空闲也不回收）</li>
 *   <li>maximumPoolSize  — 最大线程数（核心 + 临时）</li>
 *   <li>keepAliveTime    — 临时线程空闲存活时间</li>
 *   <li>unit             — 时间单位</li>
 *   <li>workQueue        — 阻塞队列（任务缓冲区）</li>
 *   <li>threadFactory    — 线程工厂（自定义线程名/优先级/是否为守护线程）</li>
 *   <li>handler          — 拒绝策略（队列满 + 线程满时的处理方式）</li>
 * </ol>
 *
 * @author Gtsamm
 */
public class ThreadPoolLab {

    /**
     * 自定义线程工厂 —— 给线程起有意义的名字，方便排查问题
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger(0);
        private final String prefix;

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + count.incrementAndGet());
            // 设置为非守护线程（默认就是 false，显式写上更清晰）
            t.setDaemon(false);
            return t;
        }
    }

    public static void main(String[] args) {
        // ============================================================
        // 实验 1：观察线程池的 ①→②→③→④ 任务分流过程
        // ============================================================
        // 配置：2 核心 + 5 最大 + 队列容量 3
        // 提交 10 个任务，观察每个任务的去向
        //
        // 预期：
        //   任务 1-2   → 核心线程直接执行（count < corePoolSize）
        //   任务 3-5   → 进入队列等待（核心忙，队列未满）
        //   任务 6-8   → 创建临时线程（队列满了！触发扩容到 max）
        //   任务 9-10  → 触发拒绝策略（池满 5 + 队列满 3 = 8 个席位已占）
        // ============================================================
        System.out.println("========== 实验 1：线程池任务分流观察 ==========\n");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                              // corePoolSize
                5,                              // maximumPoolSize
                60, TimeUnit.SECONDS,           // keepAliveTime
                new LinkedBlockingQueue<>(3),   // 有界队列，容量 3
                new NamedThreadFactory("worker"),
                new ThreadPoolExecutor.AbortPolicy()  // 拒绝策略：抛异常
        );

        printStatus(executor, "初始状态");

        // 提交 10 个任务（每个任务执行 2 秒）
        for (int i = 1; i <= 10; i++) {
            final int taskNo = i;
            try {
                executor.execute(() -> {
                    System.out.printf("[%s] 任务-%d 开始执行%n",
                            Thread.currentThread().getName(), taskNo);
                    try {
                        Thread.sleep(2000); // 模拟业务耗时
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.printf("[%s] 任务-%d 执行完成%n",
                            Thread.currentThread().getName(), taskNo);
                });
                System.out.printf("✅ 任务-%d 提交成功%n", taskNo);
            } catch (RejectedExecutionException e) {
                System.out.printf("❌ 任务-%d 被拒绝！原因：%s%n", taskNo, e.getMessage());
            }
            printStatus(executor, "提交任务-" + taskNo + "后");
        }

        // 等待所有任务执行完
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        printStatus(executor, "所有任务完成后");

        executor.shutdown();
        System.out.println("\n✅ 线程池已关闭");

        // ============================================================
        // 实验 2：对比不同的队列类型
        // ============================================================
        System.out.println("\n\n========== 实验 2：队列类型对比 ==========\n");
        compareQueueTypes();
    }

    /**
     * 对比三种常见队列对线程池行为的影响
     */
    private static void compareQueueTypes() {
        // --- 场景 A：SynchronousQueue（无容量，直接交接） ---
        // 每个任务必须有一个线程立即处理，不会排队
        // 效果：任务一多就疯狂创建线程（直到 max），然后拒绝
        System.out.println("--- A. SynchronousQueue（无缓冲，直接交接） ---");
        ThreadPoolExecutor syncPool = new ThreadPoolExecutor(
                1, 3, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(),  // ★ 不存储任务，直接交给线程
                new NamedThreadFactory("sync"),
                new ThreadPoolExecutor.AbortPolicy()
        );
        for (int i = 1; i <= 5; i++) {
            final int no = i;
            try {
                syncPool.execute(() -> {
                    System.out.printf("[%s] sync-%d 执行%n",
                            Thread.currentThread().getName(), no);
                    try { Thread.sleep(1000); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); }
                });
                System.out.printf("✅ sync-%d 提交成功 (池大小=%d)%n",
                        no, syncPool.getPoolSize());
            } catch (RejectedExecutionException e) {
                System.out.printf("❌ sync-%d 被拒绝%n", no);
            }
        }
        syncPool.shutdown();
        try { Thread.sleep(3000); } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); }

        // --- 场景 B：LinkedBlockingQueue（无界队列，默认 Integer.MAX_VALUE） ---
        // 任务无限排队，永远不会创建超过 corePoolSize 的线程
        // 效果：max 和 keepAliveTime 形同虚设！
        System.out.println("\n--- B. LinkedBlockingQueue 无界（永不创建临时线程） ---");
        ThreadPoolExecutor linkedPool = new ThreadPoolExecutor(
                1, 100,                  // max=100，但永远不会用到！
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),  // ★ 无界队列
                new NamedThreadFactory("linked"),
                new ThreadPoolExecutor.AbortPolicy()
        );
        for (int i = 1; i <= 5; i++) {
            final int no = i;
            linkedPool.execute(() -> {
                System.out.printf("[%s] linked-%d 执行%n",
                        Thread.currentThread().getName(), no);
                try { Thread.sleep(1000); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); }
            });
        }
        // 关键观察：poolSize 永远是 1（只有核心线程），队列里堆着 4 个任务
        System.out.printf("池大小=%d, 队列大小=%d (max=100 但永远用不到!)%n",
                linkedPool.getPoolSize(), linkedPool.getQueue().size());
        linkedPool.shutdown();
        try { Thread.sleep(3000); } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); }

        // --- 场景 C：ArrayBlockingQueue（有界队列，最推荐） ---
        // 既能缓冲流量，又不会无限堆积导致 OOM
        System.out.println("\n--- C. ArrayBlockingQueue 有界（推荐生产使用） ---");
        ThreadPoolExecutor arrayPool = new ThreadPoolExecutor(
                1, 3, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),  // ★ 有界队列，容量 2
                new NamedThreadFactory("array"),
                new ThreadPoolExecutor.AbortPolicy()
        );
        for (int i = 1; i <= 5; i++) {
            final int no = i;
            try {
                arrayPool.execute(() -> {
                    System.out.printf("[%s] array-%d 执行%n",
                            Thread.currentThread().getName(), no);
                    try { Thread.sleep(1000); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); }
                });
                System.out.printf("✅ array-%d 提交成功 (池大小=%d, 队列=%d)%n",
                        no, arrayPool.getPoolSize(), arrayPool.getQueue().size());
            } catch (RejectedExecutionException e) {
                System.out.printf("❌ array-%d 被拒绝%n", no);
            }
        }
        arrayPool.shutdown();
        try { Thread.sleep(3000); } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); }
    }

    /**
     * 打印线程池状态 —— 排查线程池问题的必备技能
     */
    private static void printStatus(ThreadPoolExecutor executor, String tag) {
        System.out.printf("[%s] core=%d | max=%d | active=%d | pool=%d | queue=%d/%d | completed=%d%n",
                tag,
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getActiveCount(),       // 正在执行任务的线程数
                executor.getPoolSize(),           // 当前池中线程总数（含空闲）
                executor.getQueue().size(),       // 队列中等待的任务数
                // remainingCapacity 在无界队列时返回 Integer.MAX_VALUE
                executor.getQueue().remainingCapacity() + executor.getQueue().size(),
                executor.getCompletedTaskCount()  // 已完成任务总数
        );
    }
}
