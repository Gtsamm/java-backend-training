# MAT 堆转储分析报告 —— HeapOOM

> 分析日期：2026-08-08
> 工具：Eclipse MAT 1.14（JDK 17）
> 转储文件：`lab04-jvm/dump/heap.hprof`（G1 默认收集器，OOM 时由 JVM 自动生成，12MB）
> 实验代码：`lab04-jvm/src/main/java/com/lab/jvm/oom/HeapOOM.java`

---

## 一、实验背景

- **JVM 参数**：`-Xmx20m -Xms20m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump/heap.hprof`
- **现象**：循环往 `List<byte[]> list` 里塞 1MB 数组，**分配第 9 个**后抛出 `java.lang.OutOfMemoryError: Java heap space`
- **问题**：20MB 堆为什么只分配了 9MB 就 OOM？（G1 的 Region + humongous 大对象机制导致，对比 Parallel GC 可分配 17MB —— 详见 Day 8 上午实验）

## 二、结论速览

OOM 现场：堆上所有大对象**全部存活**（被 main 线程局部变量 `list` 持有），GC 一个都收不掉。
真正的内存**累积点（accumulation point）**是 `ArrayList` 的底层数组 `Object[] elementData`（容量 10），它持有了 9 个 1MB 的 `byte[]`。

## 三、Leak Suspects 发现

| 项目 | 内容 |
|------|------|
| 怀疑对象 | `java.lang.Object[]`（即 ArrayList 的 elementData） |
| 占用 | 9,437,384 字节 ≈ **9.0 MB**（占已用堆 **92.04%**） |
| 持有者 | main 线程栈帧的**局部变量**（`HeapOOM.main()` 的 `list`） |

> 注：92.04% 是「占**已用**堆」的比例，不是占 `-Xmx20m` 的比例。dump 时已用堆约 10.2MB，剩余 10MB 左右是 G1 预留但没来得及分配/浪费掉的 Region——这正好呼应上午「G1 在 9MB 就 OOM」的实验结论。

## 四、大对象列表（Histogram）

| 类 | 对象数 | Shallow Heap | Retained Heap | 说明 |
|----|--------|--------------|---------------|------|
| `byte[]` | 2,601 | 9,615,160 | ≥ 9,615,160 | **其中 9 个 1MB 是实验数据**；其余 2,592 个是 JVM 内部小数组 |
| `java.lang.Object[]` | 968 | 65,584 | ≥ 9,532,832 | **其中 1 个是 ArrayList.elementData**（容量 10），真正的累积点 |
| `java.lang.String` | 2,506 | 60,144 | ≥ 208,208 | JVM 内部对象 |
| `char[]` | 266 | 251,192 | ≥ 251,192 | JVM 内部对象 |

**byte[] 拆账**：9,615,160 = 9 × 1,048,576（9 个 1MB 数组 ≈ 9,437,184 字节）+ ~178,000（2,592 个内部小数组，平均 ~69 字节/个）。

## 五、GC Root 引用链（Shortest Paths To the Accumulation Point）

```
java.lang.Thread @ 0xff601d00 (main)           ← GC Root（线程栈）
 └─ <Java Local>                               ← HeapOOM.main() 的局部变量 list
    └─ java.util.ArrayList @ 0xff6001f0
       └─ elementData                          ← ArrayList 底层数组字段
          └─ java.lang.Object[10] @ 0xff68da40 ← 累积点：容量 10，持有 9 个引用
             └─ [0..8] → byte[] @ ...（各 1MB）
```

> `Object[10]` 印证了 ArrayList 的懒加载：`new ArrayList<>()` 时是共享空数组，**第一次 add 才扩容成默认容量 10**，塞满第 10 个才会触发 1.5 倍扩容（Week 1 源码知识在 dump 里验证）。

## 六、为什么这 9MB 收不掉（关键认知）

1. 9 个 `byte[]` 都从 GC Root（main 线程栈）**可达** → 全是存活对象
2. Minor GC / Full GC 无法回收被根引用的对象 → 堆被"无意识持有"占满
3. 这才是最常见的"内存泄漏"真相：**不是真泄漏，是对象被长生命周期引用一直拽着**

**容器 vs 货**：
- MAT 找的累积点是 `Object[]`（**容器** = ArrayList.elementData）
- `byte[]` 只是**货**（payload）
- 判断能否被回收，只看它是否还有 GC Root 可达引用

## 七、学到的教训（我的心得）

1. 分析 OOM 的套路：Histogram 找大头 → Path to GC Roots 看谁持有 → 判断是"真泄漏"还是"无意识持有"
2. heap.hprof 文件是怎么生成的：
   - `-XX:+HeapDumpOnOutOfMemoryError` 是个**开关**：一发生 OOM，JVM 自动把当前堆快照写到磁盘
   - `-XX:HeapDumpPath=dump/heap.hprof` 指定输出路径和文件名
   - 触发时机：`OutOfMemoryError` 抛出的**瞬间**，JVM 拦截住并调用 HeapDumper 做堆快照
   - 格式是 **HPROF**（MAT 能解析的格式），记录那一刻的存活对象、类信息、GC Roots、引用关系
   - 所以 dump 是「OOM 现场」：能看到 9 个 byte[] 全部存活、被 ArrayList 的 elementData 持有
   - 文件 12MB < 20MB 堆：dump 只记录对象，G1 预留但没用上的 Region 不会写进文件
   - 生产环境手动触发：`jcmd <pid> GC.heap_dump /path/heap.hprof` 或 `jmap -dump:format=b,file=xxx <pid>`（不用等 OOM）
3. 2,601 vs 9 的教训：MAT 的对象数包含 JVM 内部小对象，不能只看总数，要按 **Retained Size** 找真正的大头；AI 报出的数字要用实验已知事实交叉验证

## 八、今日面试题

Q: MAT 分析 OOM 的完整流程是什么？

A: 第一是打开heap.hprof文件，生成leak suspects报告，查看怀疑泄漏的对象是什么类型？占用多少字节/MB？第二是看Histogram，看哪些对象占用空间大？第三查看Path to GC Roots整条引用链（类名、方法名、数组索引都要），最顶端的 GC Root 是什么（蓝色高亮线程栈？还是静态字段？），最终看定位出泄露的地方，最终用 Path to GC Roots 判断引用链：如果根是线程栈的局部变量（如 main 的 list），说明是"无意识持有"而不是真泄漏——断开引用就能解决。

Q: Shallow Heap 和 Retained Heap 的区别？

A: Shallow = 对象自己占的内存（不含引用到的对象）；Retained = 如果把该对象回收，连带能被一起回收的所有对象的总大小。分析泄漏看 Retained——比如 
 ArrayList 的 elementData（Object[]）Shallow 只有几十字节，Retained 却有 9.4MB，因为它拽住了 9 个 byte[]。