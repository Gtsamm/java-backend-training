# 📅 第二周：JVM 虚拟机 + MySQL 数据库核心

> **周期：** Day 8 ~ Day 14  
> **目标：** 掌握 JVM 内存模型和 GC 机制，能独立分析 OOM/内存泄漏；掌握 MySQL 索引优化和事务隔离级别
> **本周项目：** `jvm-mysql-lab`

---

## 🎯 本周目标清单

- [ ] 能画出 JVM 内存结构图并说出每块区域的作用
- [ ] 能制造 OOM + 用 MAT 分析 dump 文件
- [ ] 能解读 GC 日志并说出各收集器适用场景
- [ ] 能手写自定义类加载器
- [ ] 能用 EXPLAIN 分析 SQL 并说出每个字段的含义
- [ ] 能复现脏读/不可重复读/幻读
- [ ] 能解释 MVCC 原理（ReadView + undo log）

---

## Day 8：JVM 内存模型 + OOM 实验 💥

### 上午（3h）：JVM 内存模型精讲

#### 任务 8.1：画出 JVM 内存结构图（手绘/ProcessOn）

```
┌─────────────────────────────────────────────────┐
│                  JVM 内存结构（JDK 8+）           │
├────────────┬────────────────────────────────────┤
│  线程私有   │              线程共享               │
├────────────┼───────────┬───────────┬────────────┤
│ 程序计数器  │  Java 堆   │   元空间   │  直接内存   │
│  (PC)      │  (Heap)   │ (Metaspace)│(Direct Mem)│
├────────────┤           │           │            │
│ 虚拟机栈    │   - 新生代 │  - 类信息  │  NIO Buffer │
│ (Stack)   │   - 老年代 │  - 常量池  │            │
│           │           │  - 方法信息 │            │
├────────────┤           │           │            │
│ 本地方法栈  │           │           │            │
│ (Native)  │           │           │            │
└────────────┴───────────┴───────────┴────────────┘
```

#### 任务 8.2：堆 OOM 实验

```java
/**
 * JVM 参数: -Xmx20m -Xms20m -XX:+HeapDumpOnOutOfMemoryError
 *           -XX:HeapDumpPath=./dump/heap.hprof
 */
public class HeapOOM {
    public static void main(String[] args) {
        List<byte[]> list = new ArrayList<>();
        int i = 0;
        try {
            while (true) {
                list.add(new byte[1024 * 1024]); // 每次分配 1MB
                System.out.println("分配 " + (++i) + " MB");
            }
        } catch (OutOfMemoryError e) {
            System.out.println("OOM! 共分配 " + i + " MB");
            // dump 文件自动生成在指定路径
        }
    }
}
```

**在 IDEA 中配置 JVM 参数：**
```
Run → Edit Configurations → VM options:
-Xmx20m -Xms20m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dump/heap.hprof
```

#### 任务 8.3：栈 OOM 实验

```java
/**
 * JVM 参数: -Xss160k （设置栈大小 160KB）
 */
public class StackOOM {
    private int depth = 0;

    public void recurse() {
        depth++;
        recurse(); // 无限递归
    }

    public static void main(String[] args) {
        StackOOM oom = new StackOOM();
        try {
            oom.recurse();
        } catch (StackOverflowError e) {
            System.out.println("栈深度: " + oom.depth);
        }
    }
}
```

### 下午（3h）：MAT 分析 + 字符串常量池

#### 任务 8.4：用 MAT 分析 heap dump
```bash
# 1. 下载 Eclipse MAT: https://eclipse.dev/mat/
# 2. 打开 .hprof 文件
# 3. 查看 Leak Suspects（泄露嫌疑）
# 4. 查看 Dominator Tree（支配树，找出大对象）
# 5. 查看 Histogram（按类型聚合）
```

#### 任务 8.5：方法区（元空间）OOM

```java
/**
 * JVM 参数（JDK 8）: -XX:MaxMetaspaceSize=10m
 */
public class MetaspaceOOM {
    public static void main(String[] args) {
        int i = 0;
        try {
            while (true) {
                // 用 CGLIB 动态生成类，填满元空间
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(OOMObject.class);
                enhancer.setUseCache(false);
                enhancer.setCallback((MethodInterceptor) (obj, method, args1, proxy) ->
                    proxy.invokeSuper(obj, args1));
                enhancer.create();
                System.out.println("创建 " + (++i) + " 个动态类");
            }
        } catch (Throwable e) {
            System.out.println("元空间 OOM! 共创建 " + i + " 个类");
            e.printStackTrace();
        }
    }
    static class OOMObject {}
}
```

#### 任务 8.6：字符串常量池实验

```java
public class StringPoolExperiment {
    public static void main(String[] args) {
        // 实验 1：字面量 vs new
        String s1 = "hello";
        String s2 = "hello";
        String s3 = new String("hello");
        String s4 = s3.intern();

        System.out.println(s1 == s2); // true  —— 指向常量池同一个位置
        System.out.println(s1 == s3); // false —— s3 在堆上
        System.out.println(s1 == s4); // true  —— intern() 返回常量池引用

        // 实验 2：字符串拼接
        String a = "a";
        String b = "b";
        String ab = "ab";
        System.out.println(ab == a + b);         // false —— 运行时拼接，new StringBuilder
        System.out.println(ab == "a" + "b");     // true  —— 编译期优化
        System.out.println(ab == (a + b).intern()); // true
    }
}
```

### 晚上（2h）

1. 用 MAT 写一份 heap dump 分析报告（大对象列表 + GC Root 引用链）
2. 回答：JDK 8 为什么用元空间替代永久代？
3. 日报 + 提交代码

---

## Day 9：GC 机制 + 类加载 🔬

### 上午（3h）：GC 算法与收集器

#### 任务 9.1：GC 日志实验

```java
/**
 * JVM 参数:
 * -Xmx100m -Xms100m
 * -XX:+PrintGCDetails
 * -XX:+PrintGCDateStamps
 * -Xloggc:./logs/gc.log
 */
public class GCLab {
    public static void main(String[] args) throws Exception {
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new byte[1024 * 512]); // 512KB × 100 = 50MB
            if (i % 10 == 0) {
                list.subList(0, 5).clear(); // 每 10 次清掉前 5 个，创建垃圾
                System.gc(); // 建议 GC（不能保证一定触发）
            }
            Thread.sleep(50);
        }
    }
}
```

**GC 日志关键信息解读：**
```
[GC (Allocation Failure) [PSYoungGen: 20480K->1024K(23552K)]
 20480K->15360K(77312K), 0.005s]
 解释：新生代 GC（Allocation Failure 分配失败触发）
   PSYoungGen: 新生代 回收前 20M → 回收后 1M（总 23M）
   整体堆:     回收前 20M → 回收后 15M（总 77M）
   耗时 0.005s
```

| 收集器组合 | JVM 参数 | 特点 |
|-----------|---------|------|
| Serial + Serial Old | `-XX:+UseSerialGC` | 单线程，暂停长，客户端用 |
| Parallel + Parallel Old | `-XX:+UseParallelGC`（JDK 8 默认）| 多线程，吞吐量优先 |
| ParNew + CMS | `-XX:+UseConcMarkSweepGC` | 响应时间优先，老年代并发 |
| G1 | `-XX:+UseG1GC`（JDK 9+ 默认）| Region 化，可预测停顿 |
| ZGC | `-XX:+UseZGC`（JDK 15+）| 亚毫秒延迟，TB 级堆 |

#### 任务 9.2：jstat 实时监控 GC

```bash
# jstat -gcutil <pid> <interval(ms)> <count>
jstat -gcutil 12345 1000 30

# 输出解读：
# S0 S1 E O M YGC YGCT FGC FGCT GCT
# 0.0 12.5 50.3 30.2 95.0 10 0.05 2 0.1 0.15
# S0/S1: Survivor 区使用率
# E:      Eden 区使用率
# O:      老年代使用率
# M:      元空间使用率
# YGC:    Young GC 次数
# YGCT:   Young GC 总耗时
# FGC:    Full GC 次数
# FGCT:   Full GC 总耗时
# GCT:    所有 GC 总耗时
```

### 下午（3h）：类加载机制

#### 任务 9.3：验证双亲委派模型

```java
public class ClassLoaderExperiment {
    public static void main(String[] args) {
        ClassLoader appClassLoader = ClassLoaderExperiment.class.getClassLoader();
        System.out.println("应用类加载器: " + appClassLoader);         // AppClassLoader
        System.out.println("父加载器: " + appClassLoader.getParent());  // PlatformClassLoader (JDK 9+)
        System.out.println("祖父加载器: " + appClassLoader.getParent().getParent()); // null (Bootstrap)

        // 核心类由 Bootstrap 加载（C/C++ 实现，显示为 null）
        System.out.println(String.class.getClassLoader()); // null
    }
}
```

#### 任务 9.4：手写自定义类加载器（破坏双亲委派）

```java
/**
 * 自定义类加载器：先自己加载，加载不到再委派给父加载器
 * 这就是 Tomcat 的做法！
 */
public class MyClassLoader extends ClassLoader {
    private final String classPath;

    public MyClassLoader(String classPath) {
        this.classPath = classPath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String fileName = classPath + File.separator
                + name.replace('.', File.separatorChar) + ".class";
            byte[] data = Files.readAllBytes(Paths.get(fileName));
            return defineClass(name, data, 0, data.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name);
        }
    }

    // 重写 loadClass 破坏双亲委派
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 先检查是否已加载
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    // 先自己加载（破坏双亲委派！）
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    // 自己加载不到，再委派父加载器
                    c = getParent().loadClass(name);
                }
            }
            if (resolve) resolveClass(c);
            return c;
        }
    }
}
```

### 晚上（2h）

1. 回答：Tomcat 为什么要破坏双亲委派模型？（两个 Web 应用都依赖不同版本的 Spring）
2. Claude Code 对话：让 AI 模拟面试官问 "说说你对 JVM 类加载的理解"
3. 日报 + 提交代码

---

## Day 10：MySQL 体系结构 + 索引基础 📐

### 上午（3h）：SQL 功底练习

#### 任务 10.1：准备数据（存储过程批量生成 10 万条）

```sql
-- 1. 创建测试表
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `category_id` INT NOT NULL COMMENT '分类ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `sales` INT NOT NULL DEFAULT 0 COMMENT '销量',
    `rating` DECIMAL(2,1) DEFAULT 0.0 COMMENT '评分',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0下架 1上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `category` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `parent_id` INT DEFAULT 0,
    `level` TINYINT DEFAULT 1,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 创建存储过程批量插入
DELIMITER $$
CREATE PROCEDURE generate_products(IN num INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= num DO
        INSERT INTO product (name, category_id, price, stock, sales, rating, status, create_time)
        VALUES (
            CONCAT('商品_', i),
            FLOOR(1 + RAND() * 20),        -- 随机分类 1-20
            ROUND(10 + RAND() * 990, 2),    -- 价格 10~1000
            FLOOR(RAND() * 500),            -- 库存 0~500
            FLOOR(RAND() * 10000),          -- 销量 0~10000
            ROUND(1 + RAND() * 4, 1),       -- 评分 1.0~5.0
            IF(RAND() > 0.1, 1, 0),         -- 90% 上架
            DATE_ADD('2024-01-01', INTERVAL FLOOR(RAND() * 730) DAY) -- 随机日期
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 3. 生成 10 万条
CALL generate_products(100000);
```

#### 任务 10.2：丰富查询练习

```sql
-- 基础查询
SELECT * FROM product WHERE name = '商品_888';  -- 没有索引，走了全表扫描

-- 分页查询优化
SELECT * FROM product ORDER BY sales DESC LIMIT 100000, 10;  -- 大偏移量，慢！
-- 优化：先找出主键
SELECT * FROM product p
JOIN (SELECT id FROM product ORDER BY sales DESC LIMIT 100000, 10) t
ON p.id = t.id;

-- 聚合分析
SELECT
    category_id,
    COUNT(*) AS cnt,
    AVG(price) AS avg_price,
    MAX(sales) AS max_sales
FROM product
WHERE status = 1
GROUP BY category_id
HAVING cnt > 1000
ORDER BY avg_price DESC;

-- 窗口函数（MySQL 8.0+）
SELECT
    category_id, name, price,
    RANK() OVER (PARTITION BY category_id ORDER BY price DESC) AS rank_in_category,
    SUM(sales) OVER (PARTITION BY category_id) AS category_total_sales
FROM product
WHERE status = 1;
```

### 下午（3h）：MySQL 架构与 EXPLAIN

#### 任务 10.3：EXPLAIN 全字段精解

```sql
-- 先不加索引，看全表扫描
EXPLAIN SELECT * FROM product WHERE name = '商品_888';
-- type: ALL (全表扫描) ← 这是最差的！

-- 加索引后再看
CREATE INDEX idx_name ON product(name);
EXPLAIN SELECT * FROM product WHERE name = '商品_888';
-- type: ref 或 const ← 好很多

-- 联合索引 + 回表 vs 覆盖索引
CREATE INDEX idx_cat_status_price ON product(category_id, status, price);

-- 索引覆盖（不需要回表）
EXPLAIN SELECT category_id, status, price FROM product
WHERE category_id = 5 AND status = 1;
-- Extra: Using index ← 黄金指标！直接从索引拿数据

-- 需要回表
EXPLAIN SELECT * FROM product WHERE category_id = 5 AND status = 1;
-- Extra: Using index condition ← 需要回表查完整数据
```

**EXPLAIN 关键字段速查表：**

| 字段 | 含义 | 好 → 坏 |
|------|------|---------|
| type | 访问类型 | system > const > eq_ref > ref > range > index > ALL |
| key | 实际使用的索引 | NULL 表示没走索引 |
| rows | 预估扫描行数 | 越小越好 |
| Extra | 额外信息 | Using index（覆盖索引）> Using index condition（回表）> Using filesort（文件排序）> Using temporary（临时表）|

### 晚上（2h）

1. 为 product 表设计至少 3 个查询场景，分别创建合适的索引，用 EXPLAIN 验证
2. 回答：什么情况下索引会失效？（写至少 5 种场景 + SQL 验证）
3. 日报 + 提交代码

---

## Day 11：索引优化实战 + SQL 调优

### 上午（3h）：索引失效场景复现

```sql
-- ===== 准备环境 =====
CREATE INDEX idx_name ON product(name);
CREATE INDEX idx_price ON product(price);
CREATE INDEX idx_cat_status ON product(category_id, status);
CREATE INDEX idx_sales ON product(sales);
CREATE INDEX idx_phone ON user(phone); -- 假设 user 表有 phone 字段，类型 VARCHAR(20)

-- ===== 场景 1：对索引列做函数操作 =====
-- ❌ 不会走索引
EXPLAIN SELECT * FROM product WHERE YEAR(create_time) = 2024;
-- ✅ 改为范围查询
EXPLAIN SELECT * FROM product
WHERE create_time >= '2024-01-01' AND create_time < '2025-01-01';

-- ===== 场景 2：隐式类型转换 =====
-- ❌ phone 是 VARCHAR，传了整数，MySQL 会把所有 phone 转成数字再比较
EXPLAIN SELECT * FROM user WHERE phone = 13800138000;
-- ✅ 字符串类型就用字符串匹配
EXPLAIN SELECT * FROM user WHERE phone = '13800138000';

-- ===== 场景 3：LIKE 前导通配 =====
-- ❌ 前缀模糊，不走索引
EXPLAIN SELECT * FROM product WHERE name LIKE '%商品%';
-- ✅ 后缀模糊可以走索引
EXPLAIN SELECT * FROM product WHERE name LIKE '商品%';
-- 如果一定要前后模糊 → 用 Elasticsearch

-- ===== 场景 4：OR 连接非索引列 =====
-- ❌ 如果 OR 两边有一个没索引，整体不走索引
EXPLAIN SELECT * FROM product WHERE name = 'x' OR stock > 100;
-- ✅ 两个列都有索引才走，或者用 UNION
EXPLAIN SELECT * FROM product WHERE name = 'x'
UNION
SELECT * FROM product WHERE stock > 100;

-- ===== 场景 5：不等于/不包含 =====
-- ❌ != / <> / NOT IN / NOT EXISTS 通常不走索引
EXPLAIN SELECT * FROM product WHERE status != 1;  -- 全表扫描
EXPLAIN SELECT * FROM product WHERE status NOT IN (1, 2, 3); -- 全表扫描

-- ===== 场景 6：联合索引不满足最左前缀 =====
-- idx_cat_status 是 (category_id, status)
EXPLAIN SELECT * FROM product WHERE category_id = 5;                        -- ✅ 走索引
EXPLAIN SELECT * FROM product WHERE status = 1;                             -- ❌ 不走！缺少最左列
EXPLAIN SELECT * FROM product WHERE category_id = 5 AND status = 1;         -- ✅ 走完整索引
EXPLAIN SELECT * FROM product WHERE status = 1 AND category_id = 5;         -- ✅ 优化器会自动调整顺序
EXPLAIN SELECT * FROM product WHERE category_id > 3 AND status = 1;         -- ⚠️ 只有 category_id 走索引，status 不参与

-- ===== 场景 7：IS NULL / IS NOT NULL =====
EXPLAIN SELECT * FROM product WHERE name IS NULL;     -- ✅ 走索引
EXPLAIN SELECT * FROM product WHERE name IS NOT NULL; -- ❌ 通常不走
```

### 下午（3h）：慢查询分析流程

```bash
# 1. 开启慢查询日志
# 在 MySQL 客户端执行
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 0.1;  # 超过 100ms 就算慢
SET GLOBAL slow_query_log_file = '/var/lib/mysql/mysql-slow.log';

# 2. 执行一些慢 SQL
# （在程序中或 MySQL 客户端执行耗时查询）

# 3. 分析慢查询日志
mysqldumpslow -s t -t 10 /var/lib/mysql/mysql-slow.log
# -s t: 按总时间排序
# -t 10: 显示 top 10

# 4. 用 pt-query-digest 做更详细的分析（Percona Toolkit）
pt-query-digest /var/lib/mysql/mysql-slow.log
```

**SQL 优化案例：**

```sql
-- ❌ 原始分页（偏移量大时极慢）
SELECT * FROM product ORDER BY sales DESC LIMIT 90000, 10;
-- 耗时: ~2.5s，因为需要扫描 90010 行再丢弃前 90000 行

-- ✅ 方案 1：基于主键的延迟关联
SELECT p.* FROM product p
INNER JOIN (
    SELECT id FROM product ORDER BY sales DESC LIMIT 90000, 10
) tmp ON p.id = tmp.id;
-- 耗时: ~80ms，子查询只扫描索引

-- ✅ 方案 2：如果连续翻页，用上一页最大值
SELECT * FROM product
WHERE id < 9000  -- 上一页最后一条的 id
ORDER BY id DESC LIMIT 10;
```

### 晚上（2h）

1. 写一篇 SQL 优化实战笔记（至少记录 3 个你亲自优化过的案例）
2. Claude Code 对话：粘贴慢 SQL 让 AI 帮你分析优化建议
3. 日报 + 提交代码

---

## Day 12：MySQL 事务 + 锁机制 🔐

### 上午（3h）：事务隔离级别实验

#### 任务 12.1：复现脏读/不可重复读/幻读

```sql
-- ============ 准备环境 ============
CREATE TABLE `account` (
    `id` INT PRIMARY KEY,
    `name` VARCHAR(20),
    `balance` DECIMAL(10,2)
);

INSERT INTO account VALUES (1, '张三', 1000), (2, '李四', 500);

-- ============ 实验 1：脏读（READ UNCOMMITTED）============
-- 会话 A
SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
SELECT balance FROM account WHERE id = 1; -- 看到 1000

-- 会话 B（另一个终端/客户端）
SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
UPDATE account SET balance = 2000 WHERE id = 1; -- 还没提交！

-- 会话 A
SELECT balance FROM account WHERE id = 1; -- 看到 2000 ← 读到了未提交的数据！
-- 如果此时会话 B ROLLBACK，会话 A 读到的 2000 就是脏数据

-- ============ 实验 2：不可重复读（READ COMMITTED）============
-- 会话 A
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;
SELECT balance FROM account WHERE id = 1; -- 1000

-- 会话 B
START TRANSACTION;
UPDATE account SET balance = 3000 WHERE id = 1;
COMMIT; -- 会话 B 提交了

-- 会话 A（还在同一个事务内）
SELECT balance FROM account WHERE id = 1; -- 3000！和第一次读到的不一样

-- ============ 实验 3：幻读（REPEATABLE READ - MySQL 默认）============
-- 会话 A
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
START TRANSACTION;
SELECT COUNT(*) FROM account WHERE id BETWEEN 1 AND 10; -- 2 条

-- 会话 B
START TRANSACTION;
INSERT INTO account VALUES (3, '王五', 800);
COMMIT;

-- 会话 A
SELECT COUNT(*) FROM account WHERE id BETWEEN 1 AND 10; -- 还是 2 条！RR 下不会幻读（MVCC 快照读）
SELECT COUNT(*) FROM account WHERE id BETWEEN 1 AND 10 FOR UPDATE; -- 当前读 → 3 条！
```

### 下午（3h）：MVCC 原理 + 锁实验

#### 任务 12.2：MVCC 画图理解

```
       ReadView
     ┌──────────┐
     │ trx_ids  │ ← 活跃事务 ID 列表
     │ min_trx_id│ ← 最小活跃 ID
     │ max_trx_id│ ← 下一个将要分配的事务 ID
     │ creator_id│ ← 创建该 ReadView 的事务 ID
     └──────────┘
          ↓
    判断可见性：undo log 中某行数据的 trx_id
      < min_trx_id  → 可见（在 ReadView 创建前就提交了）
      > max_trx_id  → 不可见（在 ReadView 创建后才开始）
   在 min~max 之间 → 是否在活跃列表中：
      不在 → 可见（已提交）；在 → 不可见
```

#### 任务 12.3：行锁 + 间隙锁实验

```sql
-- 准备：给 account 表 id 列加唯一索引（主键自带）
-- 再创建一张带普通索引的表
CREATE TABLE `lock_test` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `num` INT NOT NULL,
    INDEX idx_num (num)
);

INSERT INTO lock_test (num) VALUES (10), (20), (30), (40), (50);

-- ======== 实验：间隙锁 ========
-- 会话 A
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;
START TRANSACTION;
SELECT * FROM lock_test WHERE num = 25 FOR UPDATE;
-- num=25 不存在，但锁住了 (20, 30) 间隙！

-- 会话 B
INSERT INTO lock_test (num) VALUES (21);  -- ❌ 被阻塞！间隙锁阻止插入
INSERT INTO lock_test (num) VALUES (31);  -- ❌ 被阻塞！
INSERT INTO lock_test (num) VALUES (11);  -- ✅ 可以！不在间隙范围内
INSERT INTO lock_test (num) VALUES (51);  -- ✅ 可以！

-- ======== 实验：死锁 ========
-- 会话 A
START TRANSACTION;
UPDATE lock_test SET num = 21 WHERE id = 1; -- 锁住 id=1

-- 会话 B
START TRANSACTION;
UPDATE lock_test SET num = 22 WHERE id = 2; -- 锁住 id=2

-- 会话 A
UPDATE lock_test SET num = 99 WHERE id = 2; -- 等会话 B 释放锁...

-- 会话 B
UPDATE lock_test SET num = 99 WHERE id = 1; -- 等会话 A 释放锁...
-- 💥 死锁！MySQL 自动检测并回滚其中一个事务

-- 查看死锁日志
SHOW ENGINE INNODB STATUS\G
```

### 晚上（2h）

1. 回答：MySQL 默认 RR 隔离级别下，为什么说能解决"部分"幻读？什么时候幻读仍然存在？
2. 用 Claude Code 模拟面试：让 AI 追问你 MVCC 的细节
3. 日报 + 提交代码

---

## Day 13：JDBC + 连接池 + MyBatis 缓存

### 上午（3h）：JDBC 底层实验

```java
/**
 * 原生 JDBC —— 理解底层发生了什么
 * 面试常问：JDBC 执行一条 SQL 的完整流程
 */
public class JDBCLab {
    public static void main(String[] args) throws Exception {
        // 1. 加载驱动（JDBC 4.0+ 不需要显式 Class.forName）
        // Class.forName("com.mysql.cj.jdbc.Driver");

        // 2. 获取连接
        String url = "jdbc:mysql://localhost:3306/training?useSSL=false&serverTimezone=Asia/Shanghai";
        try (Connection conn = DriverManager.getConnection(url, "root", "root123")) {

            // 3. 关闭自动提交（事务实验）
            conn.setAutoCommit(false);
            try {
                // 4. 预编译 SQL（防注入！）
                String sql = "UPDATE account SET balance = balance - ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    // 扣钱
                    ps.setBigDecimal(1, new BigDecimal("100"));
                    ps.setInt(2, 1);
                    ps.executeUpdate();
                    // 加钱
                    ps.setBigDecimal(1, new BigDecimal("-100")); // 负数 = 加钱
                    ps.setInt(2, 2);
                    ps.executeUpdate();
                }
                // 5. 提交事务
                conn.commit();
                System.out.println("转账成功！");
            } catch (Exception e) {
                conn.rollback(); // 6. 回滚
                System.out.println("转账失败，已回滚");
            }
        }
    }
}
```

### 下午（3h）：连接池对比 + MyBatis 缓存

```java
// HikariCP 配置（Spring Boot 默认，最快连接池）
@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource() {
        return new HikariDataSource();
    }
}
```

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5           # 最小空闲连接
      maximum-pool-size: 20     # 最大连接数（默认 10）
      idle-timeout: 300000      # 空闲超时 5min
      max-lifetime: 1200000     # 连接最大存活 20min
      connection-timeout: 30000 # 等待连接超时 30s
      connection-test-query: SELECT 1
```

**MyBatis 一级缓存 vs 二级缓存实验：**
```java
// 一级缓存：同一个 SqlSession 内，相同查询只执行一次 SQL
// 失效条件：SqlSession 关闭、执行了增删改、手动清除缓存
@Transactional
public void testFirstLevelCache() {
    User user1 = userMapper.selectById(1L);  // 查数据库
    User user2 = userMapper.selectById(1L);  // 从一级缓存拿（不查库）
    System.out.println(user1 == user2);      // true（缓存命中）
}
```

### 晚上（2h）

1. 回答：HikariCP 为什么快？（至少 3 个原因）
2. 日报 + 提交代码

---

## Day 14：🎯 周总结 + 整合实验

### 上午（3h）：JVM 总结文档

写一份 JVM 知识体系总结，包含：
1. 内存区域划分图
2. GC 算法对比表
3. 类加载流程
4. 常见 JVM 调优参数手册

### 下午（3h）：MySQL 总结文档

写一份 MySQL 核心知识总结，包含：
1. 索引类型与数据结构
2. EXPLAIN 字段速查表
3. 事务隔离级别与 MVCC
4. SQL 优化清单

### 晚上（2h）

1. 本周全部代码提交 GitHub
2. 写周报博客
3. 本周检查点自检

---

## ✅ 第二周检查清单

- [ ] 能手画 JVM 内存结构图
- [ ] 亲手制造过 3 种 OOM 并用 MAT 分析
- [ ] 能解读 GC 日志的关键指标
- [ ] 手写过自定义类加载器
- [ ] 用 EXPLAIN 分析过 ≥10 条 SQL
- [ ] 能复现索引失效的 ≥5 种场景
- [ ] 能复现脏读/不可重复读/幻读
- [ ] 能用白话解释 MVCC 原理
- [ ] 实验过间隙锁和死锁
- [ ] GitHub 连续 14 天有提交
