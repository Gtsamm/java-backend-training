package com.lab;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * CyclicBarrier 实验 —— "等人齐了一起出发，可以再来一轮"
 *
 * 核心原理（面试必答）：
 *   - 底层基于 ReentrantLock + Condition（不是 AQS！）
 *   - 构造时传入 parties（参与线程数）和一个可选的 barrierAction（人到齐后执行的动作）
 *   - 每个线程调用 await() 阻塞，当 await() 的线程数达到 parties 时，所有线程同时释放
 *   - ★ 可重复使用！一轮完成后计数器自动重置，可以进入下一轮
 *
 * CyclicBarrier vs CountDownLatch（面试高频！）：
 *   ┌──────────────┬─────────────────────┬──────────────────────┐
 *   │ 维度          │ CountDownLatch       │ CyclicBarrier         │
 *   ├──────────────┼─────────────────────┼──────────────────────┤
 *   │ 计数器        │ 只减不增，到 0 即止   │ 可重置，循环使用       │
 *   │ 底层实现      │ AQS 共享模式          │ ReentrantLock+Condition│
 *   │ 等待方        │ 一个/几个线程等 N 个  │ N 个线程互相等         │
 *   │ 触发动作      │ 无（count=0 后 await 直接过）│ 可设 barrierAction │
 *   └──────────────┴─────────────────────┴──────────────────────┘
 *
 * 典型场景：
 *   1. 多轮游戏匹配（等够 5 个人就开一局）
 *   2. 并行计算中多个线程分段处理数据，每一段结束需要汇总
 *   3. 赛跑 —— 所有人就位后同时起跑
 */
public class CyclicBarrierDemo {

    public static void main(String[] args) {
        System.out.println("===== CyclicBarrier 演示：赛跑 =====");
        demoRace();

        // 等第一阶段输出完
        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        System.out.println("\n===== CyclicBarrier 演示：多轮对战 =====");
        demoMultiRound();
    }

    /**
     * 赛跑场景：3 名选手，人到齐后一起起跑
     */
    static void demoRace() {
        int runnerCount = 3;
        // ★ barrierAction：所有选手到位后由最后一个到达的线程执行
        CyclicBarrier barrier = new CyclicBarrier(runnerCount, () -> {
            System.out.println("  🔫 所有选手就位，发令枪响！");
        });

        for (int i = 0; i < runnerCount; i++) {
            final int no = i + 1;
            new Thread(() -> {
                try {
                    // 选手准备时间各不相同
                    long prepareTime = ThreadLocalRandom.current().nextLong(500, 2000);
                    Thread.sleep(prepareTime);
                    System.out.println("选手" + no + " 已就位（准备耗时 " + prepareTime + "ms），等待其他人...");
                    barrier.await(); // ★ 阻塞，等所有人都到

                    // 人到齐了，开始跑
                    System.out.println("选手" + no + " 起跑！");
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }, "选手" + no).start();
        }
    }

    /**
     * 多轮对战：3 个队伍对战，共打 3 轮
     * 展示 CyclicBarrier 可复用的特性
     */
    static void demoMultiRound() {
        int teamCount = 3;
        final int totalRounds = 3;

        CyclicBarrier barrier = new CyclicBarrier(teamCount, () -> {
            System.out.println("  ⚔️  本轮所有队伍准备完毕，开战！");
        });

        for (int i = 0; i < teamCount; i++) {
            final int teamNo = i + 1;
            new Thread(() -> {
                for (int round = 1; round <= totalRounds; round++) {
                    try {
                        // 模拟每轮准备时间
                        long prepare = ThreadLocalRandom.current().nextLong(300, 1500);
                        Thread.sleep(prepare);
                        System.out.println("战队" + teamNo + " 第" + round + "轮准备完成");

                        barrier.await(); // ★ 等本轮所有队伍到位
                        // barrier 自动重置，下一轮可以继续用

                    } catch (InterruptedException | BrokenBarrierException e) {
                        System.out.println("战队" + teamNo + " 退出比赛");
                        return;
                    }
                }
                System.out.println("战队" + teamNo + " 打完收工！");
            }, "战队" + teamNo).start();
        }
    }
}
