# Day 5 日报 —— 2026-07-26

## 今日完成
- [x] CountDownLatch 实验（等人开会 / 等子线程完成）
- [x] CyclicBarrier 实验（赛跑 / 多轮对战，验证可复用特性）
- [x] Semaphore 实验（停车场 / 限流场景）
- [x] 手写 MyBlockingQueue（wait/notify 环形缓冲区）
- [x] 生产者-消费者完整模型（手写队列 + JDK ArrayBlockingQueue 对比）
- [x] 5 个 Java 文件编译通过 + 运行验证

---

## 踩坑记录

### 1. 终端中文编码问题
- 问题描述：Windows Git Bash 终端运行 Java 程序，中文输出乱码
- 排查过程：不是代码问题，是终端字符集的问题
- 解决方案：不影响运行结果，可以接受。或者在 JVM 参数加 `-Dfile.encoding=UTF-8`
- 学到的教训：环境编码问题很常见，不要因为乱码就怀疑代码逻辑

---

## 今日核心收获

### 1. 三大 JUC 工具类核心区别

| 维度 | CountDownLatch | CyclicBarrier | Semaphore |
|------|---------------|---------------|-----------|
| 谁等谁 | 主线程等子线程 | 线程间互相等 | 竞争许可证 |
| 可复用 | ❌ 一次性 | ✅ 可循环 | ✅ 许可可回收 |
| 底层 | AQS 共享模式 | ReentrantLock+Condition | AQS 共享模式 |

### 2. 手写阻塞队列的关键点
- **数据结构**：Object[] + 环形指针（putIndex/takeIndex）+ count 计数器
- **阻塞机制**：queue full → put() 中 wait()；queue empty → take() 中 wait()
- **用 while 不是 if**：防止虚假唤醒
- **用 notifyAll 不是 notify**：防止信号丢失
- **环形指针**：到头了就回 0，避免数组越界

### 3. 线程状态流转
```
没拿到锁 → BLOCKED
拿到锁 + wait() → WAITING
被唤醒 + 抢锁中 → BLOCKED
拿到锁执行 → RUNNABLE
```

---

## 明日计划
- Day 6: 线程池原理 + 7 参数详解 + 4 种拒绝策略实验

---

## AI 使用反思（诚实回答）
- 今天用 AI 做了什么？
  - 代码编译 + 运行验证
  - Markdown 文件写入（日报）
- 有没有不该用 AI 但用了的情况？
  - 没有，所有 Java 代码都是自己手写的，AI 只是辅助编译运行和文档

---

## 今日面试题

Q: CountDownLatch 和 CyclicBarrier 有什么区别？

A: 三个维度回答：
1. **谁等谁**：CountDownLatch 是主线程等子线程完成任务（一个等 N 个），CyclicBarrier 是线程间互相等（N 个互相等）
2. **可复用**：CountDownLatch 的计数器到 0 就结束，不可重置；CyclicBarrier 在所有线程到达后自动重置，可以循环使用
3. **底层实现**：CountDownLatch 基于 AQS 共享模式；CyclicBarrier 基于 ReentrantLock + Condition
4. **额外能力**：CyclicBarrier 可以设置 barrierAction（人到齐后自动执行的动作），CountDownLatch 没有

Q: wait() 和 sleep() 有什么区别？

A: 四个核心区别：
1. **所属类**：wait() 是 java.lang.Object 的方法，sleep() 是 java.lang.Thread 的方法
2. **锁释放**：wait() 会释放锁，让出 CPU；sleep() **不释放锁**，抱着锁睡
3. **调用限制**：wait() 必须在 synchronized 块中调用（必须先持有锁），sleep() 没有限制
4. **唤醒方式**：wait() 通过 notify/notifyAll 唤醒，sleep() 到时间自动醒或被 interrupt()

Q: 为什么 wait() 的条件判断要用 while 而不是 if？

A: 两个原因：
1. **虚假唤醒（Spurious Wakeup）**：线程可能在没被 notify 的情况下被 JVM 唤醒，while 能重新检查条件，if 不行
2. **多线程争抢**：比如两个消费者都在 wait，生产者放入 1 个元素后 notifyAll 唤醒两者。A 抢到锁拿走元素，B 随后拿到锁时队列又空了。用 if 判断的话 B 直接取元素会出错（数组越界），用 while 判断 B 会重新发现 count==0 继续 wait

Q: 为什么 wait/notify 必须放在 synchronized 块里？

A: 保证"检查条件 → 进入 wait"这个操作的原子性。如果不加 synchronized，可能出现：线程 A 检查到条件不满足，准备进入 wait，但还没真正 wait；此时线程 B 修改了条件并调用 notify，没人在 wait 所以 notify 无效；然后 A 进入 wait 永远不被唤醒。synchronized 保证了检查和等待是不可分割的原子操作。

Q: 为什么用 notifyAll 而不是 notify？

A: 多生产者多消费者场景下，notify 只唤醒一个等待线程，可能唤醒同类线程导致"信号丢失"。例如：队列满了，生产者 P1 和 P2 都在 wait，消费者 C 取走一个元素后 notify 了 P1，然后 C 又取走一个但 notify 只能唤醒 P2 中的一个。如果用 notify，假设唤醒了同样在 wait 的消费者 C2，C2 发现队列空继续 wait，而 P1/P2 永远不被唤醒。notifyAll 唤醒所有等待线程，确保该被唤醒的不会被漏掉。

Q: BlockingQueue 的 put() 和 offer() 有什么区别？

A:
- `put()`：阻塞方法，队列满时一直等待直到有空间，响应中断
- `offer()`：非阻塞方法，队列满时立即返回 false，不会等待
- `offer(timeout)`：折中方案，队列满时等待指定时间，超时返回 false
- 选哪个看业务：不能丢数据的用 put；允许丢弃的用 offer；有时效要求的用 offer(timeout)

CountDownLatch
构造数给 State，Await 检查零不零；非零排队 Park 住，CountDown CAS 减；减到零时 unpark 叫，切记 finally 保平安。

CyclicBarrier 
Lock 住减数，Cond 队列等；归零执行 Action，Signal 唤醒所有人；换代重置破旧代，中断超时即打碎。

Semaphore
State 即车位，CAS 抢锁妙；非公平吞吐高，公平防饿到；Release 无限制，谨防超发要；Try 拿不到，降级保命跑。

MyBlockingQueue
Synchronized 加锁保安全，双指针环形 O(1) 效率赞；
Wait 释放锁进等待，While 轮询防假醒是关键；
All 唤醒宁可惊群，绝不丢失信号酿死锁；
取出置空助 GC，Wait/Sleep 锁异同记心间。

ProducerConsumerDemo
生产者快消费者慢，队列做缓冲防崩断；
Sync 包住检查与等待，原子操作信号不乱；
JDK 双锁 Cond 精准唤，手写 All 唤醒虽糙但能战；
线程状态 RUN 变 WAIT，抢锁变 BLOCKED，环环相扣面试赞！