# Day 4 日报 —— 2026-07-24

## 今日完成
- [x] 线程创建方式实验（Thread / Runnable / Callable 三种方式对比）
- [x] synchronized 锁类型实验（对象锁 vs 类锁 —— 能并发吗？）
- [x] 死锁演示 + jstack 诊断（两个线程以相反顺序获取锁）
- [x] 可重入性验证（synchronized 和 ReentrantLock 都是可重入的）
- [x] 4 个 Java 文件 + 运行验证通过
- [x] Git 提交

---

## 踩坑记录

### 1. `Thread.State` 导入问题
- 问题描述：在 DeadLockDemo 中直接写 `State`，编译报 "找不到符号"
- 排查过程：当前类是 DeadLockDemo 没有 State 内部类，需要写成 `Thread.State`
- 解决方案：`State s1` → `Thread.State s1`
- 学到的教训：静态内部类的引用要写完整限定名

---

## 今日核心收获

### 1. Java 三种线程创建方式

| 方式 | 核心 | 返回值 | 异常 |
|------|------|--------|------|
| extends Thread | 继承 Thread 类 | 无 | 不能抛受检异常 |
| implements Runnable | 实现接口，传入 Thread | 无 | 不能抛受检异常 |
| implements Callable | 实现接口 + FutureTask | **有** | **可抛异常** |

### 2. 对象锁 vs 类锁（面试高频）

```
实例方法 synchronized → 锁的是 this（当前实例对象）
静态方法 synchronized → 锁的是 Xxx.class（类对象）

关键结论：两类锁互不影响，可以并发执行！
```

**实验结果验证：**
- 对象锁 + 类锁 → 总耗时 ~2s（并发）✅
- 两个实例方法同一个对象 → 总耗时 ~4s（串行）✅

### 3. 死锁四要素 + 诊断方法

**四个必要条件（面试必背）：**
```
1. 互斥条件 —— 资源只能被一个线程持有
2. 请求保持 —— 持有资源的同时请求另一个资源
3. 不可剥夺 —— 不能强制释放其他线程持有的锁
4. 循环等待 —— A→B, B→A 形成环路

破坏任意一个即可预防死锁。
实际开发中：统一加锁顺序（破坏第 4 条）。
```

**jstack 诊断流程：**
```bash
jps -l              # 找到 PID
jstack <PID>        # 导出线程堆栈
# 看底部：Found one Java-level deadlock:
#   "Thread-1": waiting to lock ... held by "Thread-2"
#   "Thread-2": waiting to lock ... held by "Thread-1"
```

### 4. 可重入性

```
synchronized     → 可重入（JVM 对象头记录持有者 + 计数器）
ReentrantLock    → 可重入（AQS state 记录重入次数）

可重入不会破坏互斥性！
线程 A 持有锁期间，其他线程仍然被阻塞。
只是同一个线程再次获取同一个锁时不会被自己阻塞。
```

### 5. 面试话术速记
- **start() vs run()**：start() 创建新线程并调用 run()；直接调 run() 只是普通方法调用，不会创建新线程
- **对象锁 vs 类锁**：锁的不是同一个对象，可以并发执行
- **死锁怎么预防**：统一加锁顺序、tryLock 设置超时、减少同时持有的锁数量
- **synchronized vs ReentrantLock 区别**：
  1. synchronized 是关键字，ReentrantLock 是 API
  2. synchronized 自动释放，ReentrantLock 需要 finally unlock
  3. ReentrantLock 支持 tryLock 超时
  4. ReentrantLock 支持公平锁
  5. ReentrantLock 可以中断等待（lockInterruptibly）

---

## 明日计划
- Day 5: JUC 工具类（CountDownLatch / CyclicBarrier / Semaphore） + 手写阻塞队列 + 生产者消费者

---

## AI 使用反思（诚实回答）
- 今天用 AI 做了什么？
  - 代码编译和运行验证
  - DeadLockDemo 编译报错修复（Thread.State）
- 有没有不该用 AI 但用了的情况？
  - 没有，所有 Java 代码都是自己手写的，AI 只辅助编译运行
