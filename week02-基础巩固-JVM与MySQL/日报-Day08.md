# Day 8 日报 —— 2026-08-08

> 本周主题：Week 2 —— JVM 虚拟机 + MySQL 数据库核心

## 今日完成
- [x] 任务 8.1：JVM 内存结构图画图（线程私有：PC 寄存器 / 虚拟机栈 / 本地方法栈；线程共享：堆 / 元空间 / 直接内存）
- [x] 任务 8.2：堆 OOM 实验 HeapOOM —— G1 下 `-Xmx20m` 只分配 9MB 就 OOM；对比 Parallel GC 可分配 17MB
- [x] 任务 8.3：栈溢出实验 StackOOM —— `-Xss160k` 深度 19,206；默认 1MB 深度 1,835；`-Xint` 解释执行深度 1,546
- [x] 任务 8.4：MAT 分析 heap dump + 撰写《MAT分析报告.md》—— Leak Suspects 指向 Object[] elementData，占已用堆 92%
- [x] 任务 8.5：元空间 OOM 实验 MetaspaceOOM —— CGLIB 动态生成 945 个类填满 `-XX:MaxMetaspaceSize=10m`
- [x] 任务 8.6：字符串常量池实验 StringPoolExperiment —— 6 个 `==` 结果全部验证通过

---

## 踩坑记录

### 1. PowerShell 行续行符和 bash 不一样
- **问题描述：** `java -cp target/classes \` 报"找不到或无法加载主类 `\`"，把反斜杠当成了类名
- **排查过程：** 回想三种 shell 的差异，PowerShell 的续行符是反引号，不是反斜杠
- **解决方案：** 一行写完命令，或用 PowerShell 反引号续行
- **学到的教训：** PowerShell（`` ` ``）、bash（`\`）、cmd（`^`）三种 shell 的续行符完全不同，跨平台命令要先确认 shell

### 2. Maven settings.xml 端口号带逗号
- **问题描述：** `<port>7,897</port>` 报整数格式无效警告
- **解决方案：** 改成 `<port>7897</port>`
- **学到的教训：** 配置文件里的端口是整数，不能写千分位逗号

### 3. MAT 版本和 JDK 版本强绑定
- **问题描述：** 最新 MAT 提示 "Version: 21 or greater is required"，本机是 JDK 17
- **排查过程：** 查官方文档确认 MAT 1.14.0 要求 JDK 17+，最新版要求 JDK 21+
- **解决方案：** 下载 MAT 1.14.0
- **学到的教训：** 装工具前先查兼容矩阵，版本要求和 JDK 强绑定

### 4. PowerShell 把 `-Dmdep.outputFile=cp.txt` 拆成了两个参数
- **问题描述：** Maven 报 `Unknown lifecycle phase ".outputFile=cp.txt"`
- **排查过程：** 错误信息说明 Maven 收到的只有 `.outputFile=cp.txt`，而不是完整的 `-D` 参数
- **解决方案：** 用引号包住 `"-Dmdep.outputFile=cp.txt"`
- **学到的教训：** PowerShell 对 `.` 有特殊解析（成员访问），会把含点的参数拆开；bash 只按空格切分，不会踩这个坑

### 5. 中文用户名导致 classpath 文件乱码（UTF-8 vs GBK）
- **问题描述：** `Get-Content cp.txt` 显示 `C:\Users\娣嬮洦\...`，用户名 `淋雨` 变成乱码，java 依然找不到 cglib jar
- **排查过程：** Maven(Java) 写文件默认 UTF-8，PowerShell 5.1 读文件默认 GBK（中文系统）→ 中文路径被解错 → 路径不存在 → `NoClassDefFoundError`
- **解决方案：** `$cp = (Get-Content cp.txt -Encoding UTF8 -Raw).Trim()`
- **学到的教训：** 中文用户名在工具链里频繁踩编码坑。要么显式指定编码，要么用 `mvn exec:java` 让 Maven 自己管理 classpath，绕开编码问题

### 6. CGLIB + JDK 17 模块系统冲突
- **问题描述：** 先 `NoClassDefFoundError: net/sf/cglib/proxy/Callback`（classpath 问题），修好后变 `InaccessibleObjectException: module java.base does not "opens java.lang" to unnamed module`
- **排查过程：** JDK 9+ 模块系统不允许反射访问未开放的包
- **解决方案：** `java --add-opens java.base/java.lang=ALL-UNNAMED ...`
- **学到的教训：** CGLIB 是 JDK 8 时代的老库，需要 `--add-opens` 才能继续反射；现代框架（Spring 新版）改用 ByteBuddy 就是为避开这个问题

### 7. 假 OOM —— catch(Throwable) 给异常贴错标签
- **问题描述：** catch 里打印"元空间 OOM! 共创建 0 个类"，实际抓到的是 `InaccessibleObjectException`，根本不是 OOM
- **排查过程：** 诊断口诀第一步"看什么异常"—— 先看异常类型，count=0 说明一个类都没生成，元空间根本没满
- **解决方案：** catch 里打印 `e.getClass().getName()`，不要写死文案
- **学到的教训：** 真 OOM 的异常文本必须是 `OutOfMemoryError: Metaspace` / `Java heap space`，其他异常不能贴"OOM"标签

### 8. 元空间 OOM 后，catch 里的 println 也会二次爆炸
- **问题描述：** catch 里 `System.out.println("元空间 OOM! 共创建 " + i + " 个类")` 自己抛出了 OOM，汇总句根本没打印
- **排查过程：** 堆栈显示 `StringConcatFactory.generateMHInlineCopy` —— JDK 9+ 字符串拼接是 invokedynamic，运行时生成字节码，**也吃元空间**
- **解决方案：** 用 `println(i)`（int 版不拼接，结果分配在堆上）或把打印挪到 create 之前
- **学到的教训：** OOM 之后的代码必须"零分配"；拼接吃元空间，不拼接吃堆 —— 元空间满了堆未必满

### 9. 字符串拼接的注释写成了 JDK 8 机制
- **问题描述：** 注释写 `a + b` 是 "new StringBuilder"，但上午元空间实验的堆栈已经证明 JDK 9+ 是 invokedynamic + StringConcatFactory，没有 StringBuilder
- **学到的教训：** 跨版本知识要用当前环境验证；自己写注释时要有版本意识

---

## 明日计划
- Day 9：GC 机制 + 类加载
  - 任务 9.1：GC 日志实验（`-XX:+PrintGCDetails` + `-Xloggc`）
  - 任务 9.2：jstat 实时监控 GC
  - 任务 9.3：验证双亲委派模型
  - 任务 9.4：手写自定义类加载器（破坏双亲委派）

---

## AI 使用反思（诚实回答）
- 今天用 AI 做了什么？
  - 解释各种报错背后的原理（NoClassDefFoundError / InaccessibleObjectException / 中文乱码）
  - 帮忙诊断 PowerShell 拆参、编码不一致等环境问题
  - 查证 MAT 版本兼容性
  - 今天太晚，日报由 AI 代笔整理，需次日复查
- 有没有不该用 AI 但用了的情况？
  - 没有。实验代码全部手敲，AI 只用于排错诊断和原理讲解，符合"先排查再问 AI"原则

---

## 今日面试题

Q: JVM 运行时数据区有哪些？哪些线程私有、哪些线程共享？

A: 线程私有：程序计数器（PC 寄存器）、虚拟机栈、本地方法栈。线程共享：Java 堆、元空间（方法区实现）、直接内存。元空间存类元数据 / 常量池 / 方法信息。

Q: G1 收集器下为什么 `-Xmx20m` 堆只分配了 9MB 就 OOM？

A: G1 把堆分成 1MB 的 Region。对象大于 Region 一半（512KB）就是 Humongous 大对象，必须占用连续的多个 Region。分配第 9 个 1MB 数组时找不到足够连续 Region，即使堆总用量才 9MB 也 OOM。Parallel GC 没有 Region 概念，连续分配，所以能用满到 17MB。

Q: `new String("abc")` 创建了几个对象？`s1 == s3` 为什么是 false？

A: 最多 2 个：字面量 "abc" 在常量池 1 个，new 在堆上 1 个（池里已有则只 1 个）。s3 指向堆上新对象，s1 指向常量池对象，`==` 比较引用地址，不同所以 false。

Q: 字符串常量池在 JDK 6 / 7 / 8 分别在哪？为什么改？

A: JDK 6 在 PermGen（方法区），大量 intern() 会 PermGen OOM。JDK 7 挪到堆，成为普通堆对象，缓解 OOM。JDK 8 用元空间替代 PermGen，但常量池仍在堆。这是常考的"版本演进"题，和 Day 8 的元空间实验能串起来。

Q: 元空间 OOM 之后，为什么 catch 里的 println 也会挂？

A: 因为 OOM 表示元空间已耗尽，而 JDK 9+ 的字符串拼接是 invokedynamic + StringConcatFactory，运行时生成拼接字节码**同样需要元空间**。catch 里的任何拼接 / 分配动作都可能二次 OOM。正确做法是"零分配"：用不触发拼接的方式打印，或预分配资源。
