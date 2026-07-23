# Day 3 日报 —— 2026-07-24

## 今日完成
- [x] 手写 MyHashMap（数组 + 单向链表，含扩容）
- [x] 手写 LRU Cache（HashMap + 双向链表，LeetCode 146）
- [x] MyHashMap 9 个测试 + LRUCache 6 个测试
- [x] Git 提交

---

## 踩坑记录

### 1. put() 延迟初始化的 NPE
- 问题描述：第一次 put 时，table 还是 null，先算 `table.length - 1` 直接 NPE
- 排查过程：编译报错提示 `table` 可能为 null，定位到 `table.length - 1` 这一行，发现延迟初始化的代码写在了 index 计算之后
- 解决方案：把延迟初始化移到 index 计算之前
- 学到的教训：空指针检查要在使用引用之前，不能之后

### 2. 扩容时数组大小用了 threshold 而不是 capacity
- 问题描述：`table = new Node[threshold]`，threshold=12，数组只有 12 个桶
- 排查过程：debugInfo 打印出来只有 12 个桶不是 16 个，发现把 threshold 当 capacity 用了
- 解决方案：`capacity = (int)(threshold / loadFactor)`，从阈值反推容量
- 学到的教训：threshold 和 capacity 是两个不同的概念，不能混用

### 3. else 没加大括号导致逻辑错误
- 问题描述：else 后面没加 `{}`，只有一行代码属于 else 分支，后面的 while 和尾插逻辑不管 bucket 空不空都执行
- 排查过程：...
- 解决方案：else 后面必须用 `{}` 把所有逻辑包起来
- 学到的教训：if/else 后面永远加大括号，不要省

### 4. LRU 测试：get() 的副作用改变了 LRU 顺序
- 问题描述：测试中用 `get()` 验证值是否正确，但 get 会触发 moveToHead，改变了链表顺序，导致后续验证时淘汰了不该淘汰的 key
- 排查过程：...
- 解决方案：验证状态优先用 debugList()（无副作用），或者把 get 验证放在最后
- 学到的教训：**get() 在 LRU 中有副作用**，不是只读操作

---

## 今日核心收获

### 1. HashMap hash 扰动函数
```java
// 不是直接用 hashCode()，而是高 16 位 ^ 低 16 位
// 目的：让高位特征也参与索引运算，减少碰撞
static final int hash(Object key) {
    int h = key.hashCode();
    return h ^ (h >>> 16);
}
```

### 2. 扩容高低位拆分（最高频面试考点）
```
旧容量 = 16 (0b10000)，新容量 = 32 (0b100000)

旧 index = hash & 15   (取低 4 位)
新 index = hash & 31   (取低 5 位)

第 5 位（oldCap 对应位）：
  0 → 留在原位
  1 → 移到原位 + oldCap

判断第 5 位：hash & oldCap → 一行代码代替重新取模
```

### 3. LRU 双向链表的哨兵技巧
```
不加哨兵：删除头/尾要判 null，头插空链表是特殊情况
加哨兵后：head 和 tail 永远存在，所有操作统一处理，不需要判 null
```

### 4. LRU 三个基础操作
```
removeNode():  2 行 —— node.prev.next = node.next; node.next.prev = node.prev
addToHead():   4 行 —— 顺序不能乱，先改新节点自己的指针，再改别人的
removeTail():  1 行 —— 删 tail.prev
```

### 5. 面试话术速记
- HashMap put 流程：hash → 定位 → 空则放 / 非空遍历 → 覆盖或尾插 → 检查扩容
- 扩容为什么 2 倍：2 的幂可以用位运算代替取模，扩容时 hash & oldCap 快速拆分
- 容量为什么是 2 的幂：`hash & (n-1)` 等价于 `hash % n`，位运算比取模快
- LRU 为什么双向链表：删除任意节点需要前驱，双向 O(1)，单向 O(n)
- LRU get 为什么移动节点：读也是使用（Used），必须刷新时间戳

---

## 明日计划
- Day 4: JUC 并发 —— 手写死锁 + 生产者消费者 + 线程池

---

## AI 使用反思（诚实回答）
- 今天用 AI 做了什么？

  - 测试 MyHashMap 和 LRU Cache
- 有没有不该用 AI 但用了的情况？
  - 让AI写了LRU Cache
