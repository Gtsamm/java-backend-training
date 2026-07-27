package com.lab;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Semaphore 实验 —— "停车场，只有 N 个车位"
 *
 * 核心原理（面试必答）：
 *   - 底层基于 AQS 共享模式
 *   - permit（许可证）概念：构造时设定许可证数量
 *   - acquire()：获取一个许可证，没有则阻塞等待
 *   - release()：归还一个许可证，唤醒等待线程
 *   - 公平模式 vs 非公平模式（默认非公平，吞吐量更高）
 *
 * 典型场景：
 *   1. 数据库连接池限流（只有 N 个连接，用完等待归还）
 *   2. API 接口限流（每秒最多 N 个请求）
 *   3. 停车场（只有 N 个车位，满了排队等）
 *
 * 面试追问：Semaphore 和 ReentrantLock 有什么区别？
 *   答：ReentrantLock 是互斥锁（一次只允许一个线程），Semaphore 是信号量（允许多个线程同时访问）
 *      Semaphore(1) 可以当成互斥锁用，但不保证可重
 *
 * 面试追问：acquire() 和 tryAcquire() 区别？
 *   答：acquire() 阻塞等待直到获取许可；tryAcquire() 立即返回，获取成功返回 true，失败返回 false
 *      tryAcquire(timeout) 只等待指定时间
 */
public class SemaphoreDemo {

    public static void main(String[] args) {
        System.out.println("===== Semaphore 演示：停车场 =====");
        demoParkingLot();

        try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        System.out.println("\n===== Semaphore 演示：限流 =====");
        demoRateLimiter();
    }

    /**
     * 停车场：3 个车位，10 辆车
     */
    static void demoParkingLot() {
        Semaphore parking = new Semaphore(3);
        //Semaphore parking = new Semaphore(3, true); // ★ 公平模式：先到先停
        int carCount = 10;

        for (int i = 0; i < carCount; i++) {
            final int carNo = i + 1;
            new Thread(() -> {
                try {
                    System.out.println("🚗 车" + carNo + " 到达停车场，当前可用车位：" + parking.availablePermits());

                    parking.acquire(); // ★ 申请车位，没车位就排队等
                    System.out.println("  ✅ 车" + carNo + " 停入，剩余车位：" + parking.availablePermits());

                    // 模拟停车时间
                    long parkTime = ThreadLocalRandom.current().nextLong(1000, 3000);
                    Thread.sleep(parkTime);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println("  🚙 车" + carNo + " 驶出");
                    parking.release(); // ★ 必须归还！放在 finally 中确保一定会还
                }
            }, "车" + carNo).start();

            // ★ 车先后到达，模拟真实停车场：车1 先到，车2 后到...
            //try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    /**
     * 限流场景：用 Semaphore 限制每秒最多 3 个并发请求
     *
     * 注意：这只是演示"并发数限制"，不是"速率限制"。
     * Semaphore 适合限制"同一时刻最多 N 个请求在执行"，
     * 真正的 QPS 限流需要结合令牌桶/漏桶算法。
     */
    static void demoRateLimiter() {
        // 同一时刻最多 3 个请求在处理
        Semaphore limiter = new Semaphore(3);

        for (int i = 0; i < 8; i++) {
            final int reqNo = i + 1;
            new Thread(() -> {
                try {
                    System.out.println("📩 请求" + reqNo + " 等待处理...");

                    limiter.acquire();
                    System.out.println("  🔄 请求" + reqNo + " 开始处理（当前并发：" + (3 - limiter.availablePermits()) + "）");

                    // 模拟处理时间
                    Thread.sleep(2000);

                    System.out.println("  ✅ 请求" + reqNo + " 处理完成");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    limiter.release();
                }
            }).start();

            // 每隔 300ms 发一个请求
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}
