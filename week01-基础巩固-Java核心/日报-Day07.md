# Day 7 日报 —— 2026-08-01

## 今日完成
- [x] 创建 lab03-algorithms Maven 模块
- [x] LeetCode Hot 100 前 10 题全部完成并通过测试
  - 哈希表：1. 两数之和
  - 双指针：15. 三数之和
  - 滑动窗口：3. 无重复字符最长子串
  - 链表：206. 反转链表 / 21. 合并两个有序链表
  - 栈：20. 有效的括号
  - 树：94. 二叉树中序遍历（递归版）
  - DFS：200. 岛屿数量
  - 回溯：46. 全排列
  - 中心扩散/DP：5. 最长回文子串
- [x] 代码审查 + 修复
- [x] Week 1 周总结

---

## 踩坑记录

### 1. 三数之和：for 循环括号提前关闭
- **问题描述：** threeSum 方法中，`for` 循环的 `}` 写在了 `left`/`right`/`while` 之前，导致 `i` 超出作用域，编译器报了 30+ 个级联错误
- **排查过程：** 逐行对比参考答案，发现结构不对 —— `left` 和 `right` 应该在 for 循环**内部**
- **解决方案：** 将 `left`/`right`/`while` 整体移入 for 循环体内
- **学到的教训：** Java 的 `{}` 块作用域问题会导致级联编译错误，第一个报错行未必是真正的根因

### 2. 手敲代码的 typo 合集（8 处笔误）
| 类型 | 错误 | 正确 |
|------|------|------|
| 拼写 | `rught` | `right` |
| 拼写 | `retrun` | `return` |
| 拼写 | `Interger` | `Integer` |
| 拼写 | `ArrayyList` | `ArrayList` |
| 拼写 | `innorder` | `inorder` |
| 缺分号 | `int maxLen = 0` | `int maxLen = 0;` |
| 缺分号 | `new boolean[nums.length]` | `new boolean[nums.length];` |
| 缺括号 | `Arrays.asList(...` | `Arrays.asList(...)` |

**学到的教训：** 手敲算法时注意力集中在思路上，typo 是正常的。关键是要学会读懂编译错误（哪怕乱码），快速定位。

### 3. 死代码残留
- **问题：** 所有方法最后都保留了 `throw new UnsupportedOperationException(...)`，写在 `return` 后面不会执行，但编译器报"无法访问的语句"
- **解决：** 写完方法实现后，删除原来的 `throw` 占位行

---

## 本周核心收获（Day 1-7 回顾）

### 环境与工具
1. JDK17 + Maven + Git + Docker 完整开发环境
2. Git Conventional Commits 规范：`feat(lab01): xxx`
3. 分支管理策略：`week01-java-core → main`

### Java 核心
4. **HashMap 源码级理解：** hash 扰动函数、扩容机制、树化条件
5. **手写能力：** ArrayList（1.5倍扩容）、LRU Cache（HashMap+双向链表）、阻塞队列（wait/notify）
6. **JUC 并发：** 死锁 + jstack 诊断、生产者消费者、CountDownLatch/CyclicBarrier/Semaphore、线程池 7 参数 + 4 种拒绝策略

### 算法
7. **10 种算法模板：** 哈希、双指针、滑动窗口、链表反转、栈匹配、哨兵节点、递归/迭代遍历、DFS 淹没、回溯、中心扩散

---

## 下周计划
- Week 2：JVM 内存模型 + GC 日志 + MySQL 索引与慢查询优化

---

## AI 使用反思（诚实回答）
- 今天用 AI 做了什么？
  - 创建代码框架（10 题方法签名 + TODO 注释）
  - 代码审查（发现 9 个 typo/语法错误）
  - 修复编译错误
- 有没有不该用 AI 但用了的情况？
  - 没有。算法逻辑全部是自己手敲的，AI 只帮忙做了框架和审查。

---

## 今日面试题

Q: 滑动窗口的核心思想是什么？left 指针什么时候移动？

A:
滑动窗口用两个指针 left 和 right 维护一个「窗口」。
- right 不断向右扩张
- 当窗口不满足条件时（比如出现重复字符），left 向右收缩直到满足条件
- 核心是 left = max(left, map.get(c) + 1)：不能直接跳到重复位置+1，因为 left 只能前进不能后退
- 时间 O(n)，因为每个字符最多被访问两次（right 一次，left 一次）

Q: 回溯算法的模板是什么？

A:
```
void backtrack(路径, 选择列表):
    if (满足终止条件):
        result.add(路径)  // 注意要 new 一份
        return

    for 选择 in 选择列表:
        if (已选过) continue
        做选择
        backtrack(路径, 选择列表)
        撤销选择  // 这一行就是"回溯"名字的由来
```
关键点：
1. 终止条件在方法开头
2. 结果要 `new ArrayList<>(path)` 拷贝，不能直接存引用
3. `remove(path.size() - 1)` 撤销最后一步选择

Q: DFS 和回溯有什么区别？

A:
DFS 是回溯的特例。回溯 = DFS + 状态恢复。
- DFS：遍历图/树，访问过就过了，不恢复状态
- 回溯：尝试一条路，走不通就「撤销」回到上一步，尝试另一条路
- 全排列是经典回溯（选过要取消），岛屿数量是经典 DFS（淹过不恢复）
