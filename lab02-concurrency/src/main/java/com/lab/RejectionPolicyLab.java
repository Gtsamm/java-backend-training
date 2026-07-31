package com.lab;

import java.util.concurrent.*;

/**
 * 四种拒绝策略实验
 *
 * <p>触发条件：线程池线程数达到 maximumPoolSize <b>且</b>阻塞队列已满</p>
 *
 * <table border="1">
 *   <tr><th>策略</th><th>行为</th><th>适用场景</th></tr>
 *   <tr><td>AbortPolicy（默认）</td><td>抛 RejectedExecutionException</td><td>必须感知到拒绝的场景</td></tr>
 *   <tr><td>CallerRunsPolicy</td><td>由提交任务的线程自己执行</td><td>能接受降级，不能丢任务</td></tr>
 *   <tr><td>DiscardPolicy</td><td>直接丢弃新任务，不抛异常</td><td>允许丢任务（如日志采集）</td></tr>
 *   <tr><td>DiscardOldestPolicy</td><td>丢弃队列中最老的任务，重新提交</td><td>优先保证最新数据</td></tr>
 * </table>
 *
 * @author Gtsamm
 */
public class RejectionPolicyLab {

    /**
     * 构造一个极易触发拒绝的线程池：1 核心 + 1 最大 + 队列容量 1
     * → 最多容纳 2 个任务（1 执行中 + 1 排队中）
     * → 第 3 个任务必定触发拒绝策略
     */
    private static ThreadPoolExecutor createTightPool(RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(
                1, 1,                              // 只有 1 个线程
                0, TimeUnit.SECONDS,                // 无临时线程
                new ArrayBlockingQueue<>(1),        // 队列只能放 1 个
                new ThreadFactory() {                // 自定义线程名
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "tight-pool-" + (++count));
                    }
                },
                handler
        );
    }

    public static void main(String[] args) {
        testAbortPolicy();
        testCallerRunsPolicy();
        testDiscardPolicy();
        testDiscardOldestPolicy();
        testCustomRejectionPolicy();
    }

    // ============================================================
    // 策略 1：AbortPolicy（默认）—— 抛异常，任务丢弃
    // ============================================================
    private static void testAbortPolicy() {
        System.out.println("========== ① AbortPolicy（抛异常） ==========");
        ThreadPoolExecutor pool = createTightPool(new ThreadPoolExecutor.AbortPolicy());

        for (int i = 1; i <= 4; i++) {
            final int taskNo = i;
            try {
                pool.execute(() -> {
                    System.out.printf("[%s] 任务-%d 执行中...%n",
                            Thread.currentThread().getName(), taskNo);
                    try {
                        Thread.sleep(3000); // 让任务执行久一点，方便观察
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                System.out.printf("✅ 任务-%d 提交成功 (队列=%d)%n",
                        taskNo, pool.getQueue().size());
            } catch (RejectedExecutionException e) {
                System.out.printf("❌ 任务-%d 被 AbortPolicy 拒绝！异常: %s%n",
                        taskNo, e.getClass().getSimpleName());
            }
        }

        pool.shutdown();
        waitAndSeparate(pool);
    }

    // ============================================================
    // 策略 2：CallerRunsPolicy —— 谁提交谁执行（降级方案）
    // ============================================================
    private static void testCallerRunsPolicy() {
        System.out.println("\n========== ② CallerRunsPolicy（调用者线程执行） ==========");
        ThreadPoolExecutor pool = createTightPool(new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 1; i <= 4; i++) {
            final int taskNo = i;
            pool.execute(() -> {
                // 注意：被拒绝的任务会用 main 线程执行！
                System.out.printf("[%s] 任务-%d 执行中... (isMain=%s)%n",
                        Thread.currentThread().getName(), taskNo,
                        Thread.currentThread().getName().equals("main"));
                try {
                    Thread.sleep(500); // 短暂执行
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            System.out.printf("任务-%d 已提交 (池大小=%d, 队列=%d)%n",
                    taskNo, pool.getPoolSize(), pool.getQueue().size());
        }

        pool.shutdown();
        waitAndSeparate(pool);
    }

    // ============================================================
    // 策略 3：DiscardPolicy —— 静默丢弃，不抛异常
    // ============================================================
    private static void testDiscardPolicy() {
        System.out.println("\n========== ③ DiscardPolicy（静默丢弃） ==========");
        ThreadPoolExecutor pool = createTightPool(new ThreadPoolExecutor.DiscardPolicy());

        for (int i = 1; i <= 4; i++) {
            final int taskNo = i;
            pool.execute(() -> {
                System.out.printf("[%s] 任务-%d 执行中... (这 N 个任务你会看到)%n",
                        Thread.currentThread().getName(), taskNo);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            // 注意：DiscardPolicy 不抛异常，所以这里永远不会进 catch
            // 任务被静默丢弃了！
            System.out.printf("任务-%d 提交完成（可能已被静默丢弃，不会报错！）%n", taskNo);
        }

        System.out.println("→ 上交了 4 个任务，但实际只执行了 2 个（1 执行 + 1 排队），后 2 个被静默丢弃");
        pool.shutdown();
        waitAndSeparate(pool);
    }

    // ============================================================
    // 策略 4：DiscardOldestPolicy —— 丢最老的，保最新的
    // ============================================================
    private static void testDiscardOldestPolicy() {
        System.out.println("\n========== ④ DiscardOldestPolicy（丢弃队列中最老的） ==========");
        ThreadPoolExecutor pool = createTightPool(new ThreadPoolExecutor.DiscardOldestPolicy());

        for (int i = 1; i <= 4; i++) {
            final int taskNo = i;
            pool.execute(() -> {
                System.out.printf("[%s] 任务-%d 执行中...%n",
                        Thread.currentThread().getName(), taskNo);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            System.out.printf("任务-%d 已提交%n", taskNo);
        }

        System.out.println("→ 队列中最老的任务（任务-2）被丢弃，任务-3 或 任务-4 取代了它");
        System.out.println("→ 最终执行：任务-1（在线程中），任务-3 或 4（在队列中），任务-2 被牺牲");

        pool.shutdown();
        waitAndSeparate(pool);
    }

    // ============================================================
    // 实验 5：自定义拒绝策略 —— 记录日志 + 告警
    // ============================================================
    private static void testCustomRejectionPolicy() {
        System.out.println("\n========== ⑤ 自定义拒绝策略（生产推荐） ==========");
        System.out.println("→ 生产环境通常用 CallerRunsPolicy + 限流，" +
                "或者自定义策略记录监控日志\n");

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 2, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "custom-pool-" + (++count));
                    }
                },
                // 自定义拒绝策略：记录日志 + 尝试重试
                (r, executor) -> {
                    System.err.printf(
                            "🚨 [拒绝告警] 线程池已满！active=%d, pool=%d, queue=%d, task=%s%n",
                            executor.getActiveCount(),
                            executor.getPoolSize(),
                            executor.getQueue().size(),
                            r.toString()
                    );
                    // 策略选项（按业务选择）：
                    // ① 记录到监控系统（Prometheus/ELK）
                    // ② 写入数据库异步补偿表
                    // ③ 发钉钉/企微告警
                    // ④ 降级：用当前线程执行（等同于 CallerRunsPolicy）
                    // r.run(); // 取消注释可启用降级
                    System.err.println("🚨 策略：记录告警 + 丢弃（待后续人工补偿）");
                }
        );

        for (int i = 1; i <= 5; i++) {
            final int taskNo = i;
            try {
                pool.execute(() -> {
                    System.out.printf("[%s] 任务-%d 执行%n",
                            Thread.currentThread().getName(), taskNo);
                    try { Thread.sleep(2000); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); }
                });
                System.out.printf("✅ 任务-%d 提交成功%n", taskNo);
            } catch (RejectedExecutionException e) {
                System.out.printf("❌ 任务-%d 被拒绝%n", taskNo);
            }
        }

        pool.shutdown();
        waitAndSeparate(pool);
    }

    /**
     * 等待线程池关闭，打印分隔线
     */
    private static void waitAndSeparate(ThreadPoolExecutor pool) {
        try {
            pool.awaitTermination(8, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("--- 线程池已关闭 ---");
    }
}
