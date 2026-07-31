# Day 6 日报 —— 2026-07-30

## 今日完成
- [x] 线程池 7 参数实验（任务分流过程观察）
- [x] 队列类型对比（SynchronousQueue / LinkedBlockingQueue / ArrayBlockingQueue）
- [x] 四种拒绝策略实验（Abort / CallerRuns / Discard / DiscardOldest）
- [x] 自定义拒绝策略（告警日志 + 降级思路）

---

## 踩坑记录
1. 问题描述：控制台中文输出乱码，无法直接观察实验结果
   排查过程：PowerShell 终端编码默认不是 UTF-8，Maven 输出时中文字符被替换为问号
   解决方案：重点关注数字变化（pool size、queue size、active count）而非中文标签；关键指标是数字不是文字
   学到的教训：日志分析的核心是**数据趋势**而非文字描述，面试时看 GC 日志、线程堆栈也一样——先看数字，再看文字

2. 问题描述：LinkedBlockingQueue 无界队列实验中 max=100 从未生效
   排查过程：运行后发现 poolSize 永远是 1，4 个任务全在队列里排队，线程池根本没创建临时线程
   解决方案：理解原理——线程池只在「队列满了」之后才会创建超过 corePoolSize 的线程；无界队列永远不会满，所以永远不会扩容
   学到的教训：这就是 `Executors.newFixedThreadPool` 和 `Executors.newSingleThreadExecutor` 的坑——它们用无界队列，表面上限制了线程数，实际上队列可以无限堆积导致 OOM

---

## 今日核心收获

### 1. 线程池任务分流流程（面试必画）

```
提交任务 →
  ① 核心线程有空闲？→ 直接执行
  ② 核心线程忙 → 任务入队
  ③ 队列满了 → 创建临时线程（不超过 maximumPoolSize）
  ④ 池满 + 队列满 → 触发拒绝策略
```

### 2. 队列类型对比

| 队列 | 缓冲能力 | OOM 风险 | max 参数是否生效 |
|------|---------|---------|-----------------|
| SynchronousQueue | 无缓冲 | 低 | ✅ 生效 |
| LinkedBlockingQueue 无界 | 无限 | ⚠️ 高（会 OOM） | ❌ 形同虚设 |
| ArrayBlockingQueue 有界 | 有限 | ✅ 低 | ✅ 生效 |

### 3. 四种拒绝策略速记

| 策略 | 口诀 | 适用场景 |
|------|------|---------|
| AbortPolicy | 抛异常，硬拒绝 | 必须感知到拒绝 |
| CallerRunsPolicy | 谁交谁干，不丢活 | 能接受降级 |
| DiscardPolicy | 闷声丢，不吭气 | 可丢弃（日志） |
| DiscardOldestPolicy | 丢旧保新 | 优先最新数据 |

---

## 明日计划
- Day 7：算法刷题（LeetCode Hot 100 前 10 题）+ Week 1 周总结

---

## AI 使用反思（诚实回答）
- 今天用 AI 做了什么？
  - 写了代码框架
- 有没有不该用 AI 但用了的情况？
  - 无

---

## 今日面试题

> 每天学完知识点后，把相关内容转化为面试问答，自己先回答再整理。
> 格式要求：每个问题用 `Q:` 开头，回答用 `A:` 开头，问题和回答之间用空行分隔，方便复习时遮挡答案。

Q: 线程池的 7 个参数分别是什么？任务提交后的执行流程是怎样的？

A:
7 个参数（按构造方法顺序）：
  1. corePoolSize     — 核心线程数，常驻线程，即使空闲也不回收（除非 allowCoreThreadTimeOut）
  2. maximumPoolSize  — 最大线程数 = 核心线程 + 临时线程的上限
  3. keepAliveTime    — 临时线程空闲存活时间，超过此时间没活干就被销毁
  4. unit             — keepAliveTime 的时间单位（秒/毫秒等）
  5. workQueue        — 阻塞队列，核心线程忙时任务先排队
  6. threadFactory    — 线程工厂，用来创建新线程（一般用于自定义线程名）
  7. handler          — 拒绝策略，队列满 + 线程满时对新任务的处理方式

任务提交流程（4 步）：
  ① 核心线程有空闲？→ 直接交给核心线程执行
  ② 核心线程都忙 → 任务进入 workQueue 排队
  ③ 队列也满了 → 创建临时线程执行（线程总数不超过 maximumPoolSize）
  ④ 池中线程达到 max + 队列满 → 触发 RejectedExecutionHandler

Q: 为什么不推荐用 `Executors` 工厂方法创建线程池？

A:
Executors 提供了三种快捷工厂方法，但都有隐患：

  1. newFixedThreadPool / newSingleThreadExecutor
     → 使用无界 LinkedBlockingQueue（容量 = Integer.MAX_VALUE）
     → 队列永远不会满 → 永远不会创建超过 core 的线程
     → 请求堆积时队列无限增长 → OOM

  2. newCachedThreadPool
     → 使用 SynchronousQueue（无缓冲）+ max = Integer.MAX_VALUE
     → 来一个任务就创建一个线程，没有上限
     → 高并发下疯狂创建线程 → 线程数爆炸 → OOM

  3. newScheduledThreadPool
     → 同 Fixed，使用无界 DelayedWorkQueue → 可能 OOM

《阿里巴巴 Java 开发手册》强制规定：
  「线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式，
  这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。」

Q: 线程池大小怎么设置？CPU 密集型和 IO 密集型的公式分别是什么？

A:
CPU 密集型（计算为主，几乎不阻塞）：
  corePoolSize = CPU 核数 + 1
  原因：CPU 核数就够了，+1 是为了防止某个线程因缺页中断等原因暂停时多一个线程顶上

IO 密集型（大量网络/磁盘 IO，线程大部分时间在等待）：
  公式一（简单版）：corePoolSize = CPU 核数 × 2
  公式二（精确版）：corePoolSize = CPU 核数 / (1 - 阻塞系数)
     → 阻塞系数一般取 0.8 ~ 0.9（表示线程 80%~90% 时间在等待 IO）
     → 例如 8 核 CPU，阻塞系数 0.9：8 / (1 - 0.9) = 80 个线程
  实际项目：先根据公式算一个初始值，再通过压测调整

Q: `submit()` 和 `execute()` 有什么区别？

A:
三种区别，面试常考：

  1. 参数不同：
     → execute(Runnable) —— 只接受 Runnable
     → submit(Callable) / submit(Runnable) —— 两者都接受

  2. 返回值不同（核心区别）：
     → execute() 返回 void，提交后拿不到结果，也不知道任务是否成功
     → submit() 返回 Future<?>，可以通过 Future.get() 获取结果或异常

  3. 异常处理不同（坑点！）：
     → execute()：任务抛异常 → 直接打印堆栈，线程终止
     → submit()：任务抛异常 → 异常被吞掉，不会打印！
       只有调用 Future.get() 时才会抛出 ExecutionException 包裹的异常
       常见坑：用 submit() 提交任务但没调用 get()，任务静默失败了都不知道

  最佳实践：
     → 不关心结果：用 execute()
     → 关心结果或需要取消任务：用 submit()，并在 finally 中调用 get()

Q: 线程池里的线程是如何复用的？核心原理是什么？

A:
核心原理：Worker 线程 + 循环从队列取任务（不是执行完就死）

  1. 线程池里的线程不是普通的 Thread，而是 Worker 对象（实现了 Runnable）
  2. Worker 的 run() 方法里有一个 while 循环：
     → 不断调用 getTask() 从 workQueue 中取任务
     → 取到任务就执行 task.run()
     → 执行完不退出，继续取下一个

  3. getTask() 的行为决定线程生死：
     → 核心线程：调用 workQueue.take()（阻塞等待），取不到就等，不会死
     → 临时线程：调用 workQueue.poll(keepAliveTime)（超时等待），超时没取到就返回 null，Worker 退出循环，线程销毁

  一句话总结：线程池的线程不是在「复用」，而是「压根没死」——它一直活着，循环取任务。
