package com.lab;

/**
 * 手写阻塞队列 —— 用 wait/notify 实现
 *
 * ====== 核心原理（面试必答）=======
 *
 * 1. 数据结构：Object[] 数组 + 两个指针（putIndex / takeIndex）+ 计数器（count）
 *    - putIndex：下一个元素放哪里
 *    - takeIndex：下一个元素从哪里取
 *    - count：当前队列中有几个元素
 *    - 这是环形缓冲区（Ring Buffer）：put 到末尾就回到开头
 *
 * 2. 阻塞 vs 非阻塞：
 *    - put()：队列满了 → wait() 释放锁，等消费者取走
 *    - take()：队列空了 → wait() 释放锁，等生产者放入
 *
 * 3. ★ wait/notify 机制（面试高频！）：
 *    - wait()：释放锁 + 当前线程进入 WAITING 状态（在 Condition 等待队列中）
 *    - notifyAll()：唤醒所有在 Condition 等待队列中的线程，它们重新竞争锁
 *    - 注意：被唤醒的线程必须重新拿到锁才能继续执行
 *
 * 4. ★ 为什么用 while 而不是 if？（最重要的面试题之一）
 *    - 虚假唤醒（Spurious Wakeup）—— 线程可能在没被 notify 的情况下被唤醒
 *    - 多消费者场景：消费者 A 和 B 都在 wait，生产者放入一个元素后 notifyAll，
 *      A 和 B 都被唤醒，A 先抢到锁拿走元素，B 随后抢到锁时队列又空了。
 *      if 判断下 B 会直接取，数组越界；while 判断下 B 重新检查 count==0，继续 wait。
 *
 * 5. ★ 为什么用 notifyAll 而不是 notify？
 *    - 多生产者和多消费者场景下，notify 可能唤醒同类型的线程导致"信号丢失"
 *    - 例如：队列满了，生产者 P1 在 wait，消费者 C1 取走一个后 notify 了 P2（也在 wait），
 *      P1 永远不被唤醒（丢失信号）
 *    - notifyAll 唤醒所有等待线程，确保该被唤醒的一定被唤醒
 *
 * 6. ★ wait() 和 sleep() 的区别：
 *    - wait() 是 Object 的方法，sleep() 是 Thread 的方法
 *    - wait() 释放锁，sleep() 不释放锁
 *    - wait() 必须在 synchronized 块中调用，sleep() 没有限制
 *    - wait() 通过 notify/notifyAll 唤醒，sleep() 到时间自动醒
 *
 * @param <T> 队列中元素的类型
 */
public class MyBlockingQueue<T> {

    private final Object[] items;       // 底层数组（环形缓冲区）
    private int putIndex;               // 放入位置指针
    private int takeIndex;              // 取出位置指针
    private int count;                  // 当前元素个数

    /**
     * @param capacity 队列最大容量
     */
    public MyBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("容量必须 > 0，实际传入：" + capacity);
        }
        this.items = new Object[capacity];
    }

    /**
     * 阻塞式放入元素
     *
     * 流程：
     *   1. 获得锁（synchronized）
     *   2. 如果队列满了 → wait() 释放锁并等待
     *   3. 放入元素 + 移动 putIndex（环形移动）
     *   4. count++
     *   5. notifyAll() 唤醒等待的消费者
     *   6. 释放锁
     *
     * @param item 要放入的元素
     * @throws InterruptedException 如果线程在等待时被中断
     */
    public synchronized void put(T item) throws InterruptedException {
        // ★ 用 while 而不是 if —— 防止虚假唤醒
        // 被 notifyAll 唤醒后重新检查 count == items.length，如果还是满的继续 wait
        while (count == items.length) {
            System.out.println("  [" + Thread.currentThread().getName() + "] 队列满了，生产者阻塞等待... (count=" + count + ")");
            this.wait(); // ★ 释放锁 + 进入等待
        }

        // 放入元素
        items[putIndex] = item;
        System.out.println("  → [" + Thread.currentThread().getName() + "] 生产: " + item + " (位置:" + putIndex + ", 当前数量:" + (count + 1) + ")");

        // 环形移动 putIndex
        if (++putIndex == items.length) {
            putIndex = 0;
        }
        count++;

        // ★ 用 notifyAll 而不是 notify —— 防止唤醒同类导致信号丢失
        this.notifyAll();
    }

    /**
     * 阻塞式取出元素
     *
     * 流程：
     *   1. 获得锁（synchronized）
     *   2. 如果队列空了 → wait() 释放锁并等待
     *   3. 取出元素 + 移动 takeIndex（环形移动）
     *   4. 清理引用（防止内存泄漏）+ count--
     *   5. notifyAll() 唤醒等待的生产者
     *   6. 返回元素
     *
     * @return 取出的元素
     * @throws InterruptedException 如果线程在等待时被中断
     */
    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        // ★ 用 while 而不是 if
        while (count == 0) {
            System.out.println("  [" + Thread.currentThread().getName() + "] 队列空了，消费者阻塞等待... (count=0)");
            this.wait(); // ★ 释放锁 + 进入等待
        }

        // 取出元素
        T item = (T) items[takeIndex];
        System.out.println("  ← [" + Thread.currentThread().getName() + "] 消费: " + item + " (位置:" + takeIndex + ", 剩余数量:" + (count - 1) + ")");

        // 清理引用，防止内存泄漏
        items[takeIndex] = null;

        // 环形移动 takeIndex
        if (++takeIndex == items.length) {
            takeIndex = 0;
        }
        count--;

        // ★ 唤醒所有等待线程
        this.notifyAll();

        return item;
    }

    /**
     * 获取当前元素个数
     */
    public synchronized int size() {
        return count;
    }

    /**
     * 获取队列容量
     */
    public int capacity() {
        return items.length;
    }
}
