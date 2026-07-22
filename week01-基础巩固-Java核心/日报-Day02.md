# Day 2 日报 —— 2026-07-22

## 今日完成
- [x] 手写 MyArrayList（增删改查 + 1.5 倍扩容 + 边界检查 + debugInfo）
- [x] ArrayList vs LinkedList 6 维度性能 Benchmark
- [x] 分析 Benchmark 数据，理解反直觉结论
- [x] Git 规范化提交 + 推送 week01-java-core

---

## 踩坑记录

### 1. 第一次接触benchmark(() -> { ... })不知道是干嘛的
- `benchmark` 是一个方法，作用：接收一段代码，运行、统计耗时，返回花费时间
- 方法需要传入**一段可以执行的代码**作为参数


---

## 今日核心收获

### 1. ArrayList 扩容机制
ArrayList扩容机制是1.5倍，不是2倍的原因是，1.5扩容倍可以让让释放的内存更有可能被其他数组复用，避免了内存碎片化。

### 2. Benchmark 结果——最大的反直觉
## 1. 尾部追加

小数据量下 `LinkedList` 更快；数据量变大后 **ArrayList 反超**。
原因：`LinkedList` 每次新增都要创建 Node 对象，开销随数据累积；ArrayList 扩容次数很少，扩容成本被摊薄。

> 
> ✅面试话术：ArrayList 尾插均摊 O(1)，LinkedList 尾插 O(1)。但实际开发中 ArrayList 通常更快，因为 LinkedList 存在 Node 对象分配与指针维护的额外开销。

## 2. 头部插入

`LinkedList` 性能碾压 ArrayList。
原因：ArrayList 头插需要整体搬移所有元素，数据量越大差距越悬殊，头部插入是 LinkedList 的优势场景。

## 3. 随机下标访问 `get(index)`

**ArrayList 大幅碾压 LinkedList，差距可达数万倍。**
原因：ArrayList 依靠数组下标直接定位；LinkedList 必须从头 / 尾逐个遍历节点。

> 
> 业务开发 99% 场景选用 ArrayList，就是因为代码大多存在频繁随机访问。

## 4. 中间位置插入、删除（高频反直觉考点）

实测：**ArrayList 速度远快于 LinkedList**。
原理：
LinkedList：先 O (n) 遍历寻找目标节点 + O (1) 修改指针；
ArrayList：O (1) 下标定位 + O (n) 数组元素搬移。
二者时间复杂度同为 O (n)，但有巨大性能差距：
`System.arraycopy` 是底层 native 方法，连续内存布局，CPU 缓存命中率高；
LinkedList 遍历节点是零散内存指针跳转，极易发生缓存失效。

> 
> ✅面试话术：不要死记 “LinkedList 插入更快”。随机位置插入时，LinkedList 先要 O (n) 遍历定位节点，之后才是 O (1) 修改指针；多数场景下 ArrayList 的数组批量复制效率更高。

## 5. for-each 遍历

两者性能接近，ArrayList 小幅领先，得益于连续内存带来的 CPU 预取优势。

### 3. 什么时候该用 LinkedList？
LinkedList 适合头部插入场景，Deque/ArrayDeque 更好，ArrayList 则适合尾部追加场景。

### 4. Arrays.copyOf 与 System.arraycopy 的区别
1. `System.arraycopy` 是 native 底层方法，**需要提前准备目标数组**，支持自定义源起始位置、目标起始位置，复制范围灵活；
2. `Arrays.copyOf` 是工具类封装，内部调用 `System.arraycopy`；它自动创建新数组，**只能从原数组下标 0 开始拷贝**；
3. 需要灵活复制区间使用 `System.arraycopy`；简单扩容 / 截取生成新数组使用 `Arrays.copyOf`。


## Benchmark 关键数据速查（面试备用）

| 操作 | 10 万数据量 | 赢家 | 倍数 |
|------|-----------|------|------|
| 尾部追加 | ArrayList 1.7ms vs LinkedList 2.7ms | ArrayList | 1.6x |
| 头部插入 | ArrayList 358ms vs LinkedList 1.6ms | LinkedList | 222x |
| 随机访问 | ArrayList 0.1ms vs LinkedList 761ms | ArrayList | 5977x |
| 中间插入(1000次) | ArrayList 3.8ms vs LinkedList 66ms | ArrayList | 17x |
| 中间删除(1000次) | ArrayList 4.4ms vs LinkedList 66ms | ArrayList | 15x |

---

## 明日计划
- Day 3: HashMap 源码精读（7 个核心问题）
- 手写简易 HashMap（数组 + 链表版本）
- 手写 LRU Cache（HashMap + 双向链表）
- LeetCode 146. LRU 缓存

---

## AI 使用反思（诚实回答）
- 今天用 AI 做了什么？
做了手写 MyArrayList、 LinkedList 6 维度性能 Benchmark、分析 Benchmark 数据，理解反直觉结论。
- 有没有不该用 AI 但用了的情况？
无
