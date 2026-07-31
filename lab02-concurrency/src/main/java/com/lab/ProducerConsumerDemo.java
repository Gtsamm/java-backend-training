package com.lab;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 生产者-消费者模型完整演示
 *
 * ====== 核心原理（面试必答）=======
 *
 * 1. 什么是生产者-消费者模式？
 *    - 解耦：生产者不需要知道谁来消费，消费者不需要知道数据来源
 *    - 削峰：生产者快消费者慢时，阻塞队列作为缓冲区，生产者阻塞等待
 *    - 异步：生产者和消费者在不同线程中并发执行
 *
 * 2. 为什么需要阻塞队列？
 *    - 非阻塞队列（如普通 LinkedList）：队列空时消费者空转占 CPU
 *    - 阻塞队列：队列空时消费者自动 wait，队列满时生产者自动 wait，不浪费 CPU
 *
 * 3. 常见应用场景：
 *    - 线程池的 workQueue（任务队列）
 *    - 消息队列（Kafka / RabbitMQ）的内部缓冲区
 *    - 日志异步写入（Logback 的 AsyncAppender）
 *
 * 4. 线程状态变化（面试追问：wait 后线程处于什么状态？）：
 *    - 还没拿到锁 → BLOCKED
 *    - 拿到锁，队列满/空，调用 wait() → WAITING
 *    - 被 notifyAll 唤醒，重新竞争锁 → BLOCKED
 *    - 拿到锁继续执行 → RUNNABLE
 *
 * 5. 为什么 wait/notify 必须在 synchronized 块中？
 *    - 确保线程在调用 wait 前确实已经拿到了锁
 *    - 防止"假唤醒"的信号丢失 —— if thread A checks condition and is about to wait,
 *      but thread B changes condition and calls notify before A actually enters wait,
 *      then A's wait would never be woken up. synchronized ensures atomicity.
 */
public class ProducerConsumerDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 生产者-消费者模型演示 ==========\n");

        // ==== 实验 1：用手写的 MyBlockingQueue ====
        System.out.println("===== 实验 1：手写 MyBlockingQueue（容量=5，3 生产者 + 2 消费者）=====");
        demoWithMyQueue();

        Thread.sleep(3000);

        // ==== 实验 2：用 JDK 的 ArrayBlockingQueue ====
        System.out.println("\n===== 实验 2：JDK ArrayBlockingQueue（容量=5，3 生产者 + 2 消费者）=====");
        demoWithJDKQueue();
    }

    /**
     * 使用手写的 MyBlockingQueue
     */
    static void demoWithMyQueue() throws InterruptedException {
        MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(5); // ★ 队列容量只有 5

        Thread[] producers = new Thread[3];
        Thread[] consumers = new Thread[2];

        // 启动 3 个生产者
        for (int i = 0; i < producers.length; i++) {
            final int producerNo = i + 1;
            producers[i] = new Thread(() -> {
                try {
                    for (int j = 1; j <= 5; j++) {
                        int item = producerNo * 100 + j; // 编号规则：P1=101, P2=201, P3=301
                        queue.put(item);
                        // 生产速度：有时快有时慢，模拟真实场景
                        Thread.sleep((long) (Math.random() * 200));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "生产者" + producerNo);
        }

        // 启动 2 个消费者
        for (int i = 0; i < consumers.length; i++) {
            final int consumerNo = i + 1;
            consumers[i] = new Thread(() -> {
                try {
                    // 每个消费者取 7 个（共 3*5=15 个，取 14 个差不多）
                    for (int j = 0; j < 7; j++) {
                        Integer item = queue.take();
                        // 消费速度：比生产者慢，所以队列容易满
                        Thread.sleep((long) (Math.random() * 400));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "消费者" + consumerNo);
        }

        // 启动所有线程
        for (Thread p : producers) p.start();
        for (Thread c : consumers) c.start();

        // 等待生产者全部结束
        for (Thread p : producers) p.join();
        System.out.println("\n所有生产者已完成");

        // 等待消费者全部结束
        for (Thread c : consumers) c.join();
        System.out.println("所有消费者已完成");

        System.out.println("最终队列剩余: " + queue.size() + " 个元素");
    }

    /**
     * 使用 JDK ArrayBlockingQueue（结构与我们的实现一样）
     */
    static void demoWithJDKQueue() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        Thread[] producers = new Thread[3];
        Thread[] consumers = new Thread[2];

        for (int i = 0; i < producers.length; i++) {
            final int producerNo = i + 1;
            producers[i] = new Thread(() -> {
                try {
                    for (int j = 1; j <= 5; j++) {
                        int item = producerNo * 100 + j;
                        queue.put(item);
                        System.out.println("  → [生产者" + producerNo + "] 生产: " + item);
                        Thread.sleep((long) (Math.random() * 200));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "生产者" + producerNo);
        }

        for (int i = 0; i < consumers.length; i++) {
            final int consumerNo = i + 1;
            consumers[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 7; j++) {
                        Integer item = queue.take();
                        System.out.println("  ← [消费者" + consumerNo + "] 消费: " + item);
                        Thread.sleep((long) (Math.random() * 400));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "消费者" + consumerNo);
        }

        for (Thread p : producers) p.start();
        for (Thread c : consumers) c.start();
        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();
        System.out.println("\nArrayBlockingQueue 演示完成，剩余: " + queue.size());
    }
}
