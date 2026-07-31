package com.lab;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch 实验 —— "等所有人都到齐再开会"
 *
 * 核心原理（面试必答）：
 *   - 底层基于 AQS（AbstractQueuedSynchronizer）共享模式
 *   - 构造时传入一个 count，每个线程完成工作后调用 countDown() 让 count - 1
 *   - 主线程调用 await() 阻塞，直到 count 减到 0
 *   - 是一次性的 —— count 到 0 后不能重置，想要复用请用 CyclicBarrier
 *
 * 典型场景：
 *   1. 主线程等待多个子线程初始化完成
 *   2. 压测时等待所有线程准备就绪再同时发起请求
 *   3. 分布式系统中等待多个服务就绪
 *
 * 面试追问：CountDownLatch 和 join() 有什么区别？
 *   答：join() 只能等线程结束，CountDownLatch 可以在线程执行到某个阶段时就 countDown，
 *      不需要等线程完全结束，更灵活。
 */
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== CountDownLatch 演示 =====");
        demoBasic();
        System.out.println("\n===== 类比场景：员工到达会议室 =====");
        demoMeeting();
    }

    /**
     * 基础用法：主线程等 5 个子线程全部完成
     */
    static void demoBasic() throws InterruptedException {
        int workerCount = 5;
        CountDownLatch latch = new CountDownLatch(workerCount);

        System.out.println("主线程：启动 " + workerCount + " 个子线程...");

        for (int i = 0; i < workerCount; i++) {
            final int no = i + 1;
            new Thread(() -> {
                try {
                    // 模拟每个线程执行不同任务
                    long sleepTime = (long) (Math.random() * 2000);
                    Thread.sleep(sleepTime);
                    System.out.println("  子线程-" + no + " 完成工作（耗时 " + sleepTime + "ms）");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown(); // ★ 关键：无论如何都要 countDown，防止主线程永久阻塞
                }
            }, "worker-" + no).start();
        }

        System.out.println("主线程：等待所有子线程完成...");
        latch.await(); // ★ 阻塞直到 count == 0
        System.out.println("主线程：所有子线程已完成，继续执行！");
    }

    /**
     * 类比场景：等人开会
     */
    static void demoMeeting() throws InterruptedException {
        int peopleCount = 5;
        CountDownLatch everyoneArrived = new CountDownLatch(peopleCount);

        for (int i = 0; i < peopleCount; i++) {
            final int no = i + 1;
            new Thread(() -> {
                try {
                    // 每个人到达的时间不同
                    Thread.sleep((long) (Math.random() * 3000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("员工" + no + " 到达会议室 ✅");
                everyoneArrived.countDown();
            }, "员工" + no).start();
        }

        System.out.println("主持人：等人到齐...");
        everyoneArrived.await();
        System.out.println("主持人：所有人都到了，开始开会！🎤");
    }
}
