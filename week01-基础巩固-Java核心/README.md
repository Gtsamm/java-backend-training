# 📅 第一周：Java 核心 + 集合 + 多线程 + 算法入门

> **周期：** Day 1 ~ Day 7  
> **目标：** 搭建完整开发环境，深入掌握 Java 集合框架和 JUC 并发编程，开启算法刷题习惯  
> **IDE：** Trae CN（字节跳动 AI IDE，基于 VS Code）  
> **本周项目：** `java-core-lab` —— 一个 Maven 多模块项目，包含所有本周实验代码

---

## 🎯 本周目标清单

- [已完成] 完成 JDK17 + Trae CN + Maven + Git + Docker 环境搭建
- [已完成] 能手写 ArrayList、LRU Cache 的简易实现
- [已完成] 能对着源码讲清楚 HashMap 的 put 流程
- [ ] 能用 JUC 工具类写出生产者-消费者模型
- [ ] 能手动配置线程池并说出每个参数的作用
- [ ] LeetCode Hot 100 刷完 10 题
- [已完成] 掌握 Git 分支管理 + 规范化提交（Conventional Commits）
- [ ] GitHub 提交记录连续 7 天 ✅

---

## 🔧 Git 版本控制规范（全培训周期适用）

> **重要：** 这不只是"把代码推上去"，而是养成团队协作的版本管理习惯。
> 面试时面试官会看你的 GitHub，规范的提交记录是加分项。

### Git 仓库初始化（第一天完成）

```bash
# 1. 在 GitHub 上创建仓库（不要勾选 "Add a README file"）
#    仓库名：java-backend-training
#    描述：Java 后端开发 3 个月实操培训

# 2. 本地初始化
cd W:\claude\AIJava
git init
git branch -M main  # 默认分支命名为 main

# 3. 关联远程仓库
git remote add origin git@github.com:你的用户名/java-backend-training.git

# 4. 创建 .gitignore（Java 项目必备）
```

### .gitignore 模板（Java + Maven + IDE）

```gitignore
# ====== IDE ======
# Trae CN / VS Code
.vscode/
*.code-workspace

# IntelliJ IDEA（如果切换到 IDEA 时排除）
.idea/
*.iml
*.iws
*.ipr

# ====== Java ======
*.class
*.jar
*.war
*.log
*.tmp

# ====== Maven ======
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml

# ====== OS ======
.DS_Store
Thumbs.db
*.swp
*.swo

# ====== Docker ======
docker-data/

# ====== 环境变量（敏感信息）=======
.env
application-local.yml
```

### 分支管理策略

```
main                    ← 主分支，只接受经过验证的代码
├── week01-java-core    ← 每周一个功能分支
├── week02-jvm-mysql
├── week03-redis-docker
├── ...
└── week12-final
```

**日常开发流程（每天至少走一遍）：**

```bash
# ① 早上开始工作前，切到本周分支
git checkout week01-java-core

# ② 做完一个完整的小任务后（比如写完 MyArrayList）
git add lab01-collections/src/main/java/com/lab/MyArrayList.java
git commit -m "feat(lab01): 手写 MyArrayList 实现 —— 支持增删改查 + 1.5倍扩容"

# ③ 做完第二个任务
git add lab01-collections/src/main/java/com/lab/ListBenchmark.java
git commit -m "feat(lab01): ArrayList vs LinkedList 性能 Benchmark"

# ④ 全天工作结束前，统一推送
git push origin week01-java-core

# ⑤ 晚上提交日报（可选：用 Issue 或 README 记录）
git add week01-基础巩固-Java核心/日报.md
git commit -m "docs: Day 1 日报 —— 环境搭建 + Spring Boot CRUD 完成"
git push origin week01-java-core
```

### 提交信息规范（Conventional Commits）

> **要求：** 每次提交必须遵循以下格式，不允许 `git commit -m "update"` 这种模糊信息。

```
格式：<type>(<scope>): <简短描述>

type（必选）：
  feat     → 新功能（feature）
  fix      → 修复 bug
  docs     → 文档更新
  refactor → 重构（不增删功能，只改结构）
  test     → 添加测试
  chore    → 杂项（依赖升级、配置调整）
  perf     → 性能优化

scope（可选，表示哪个模块）：
  lab01, lab02, lab03  → 实验模块
  week01, week02       → 周计划文档
  project              → 项目代码

示例：
  ✅ feat(lab01): 手写 MyArrayList —— 支持 add/get/remove + 1.5倍扩容
  ✅ feat(lab02): 生产者-消费者模型 —— 3个生产者 + 2个消费者
  ✅ fix(lab02): 修复死锁演示中 Thread-1 和 Thread-2 锁顺序相反的问题
  ✅ docs: Day 1 日报 + 环境搭建踩坑记录
  ✅ refactor(lab01): 抽取 Benchmark 公共方法，消除重复代码
  ❌ update code
  ❌ fix bug
  ❌ 提交
```

### 常用 Git 命令速查

| 场景 | 命令 |
|------|------|
| 查看状态 | `git status` |
| 查看提交历史 | `git log --oneline --graph --all` |
| 查看某个文件的改动 | `git diff <file>` |
| 撤销未 add 的改动 | `git checkout -- <file>` |
| 撤销已 add 但未 commit | `git reset HEAD <file>` |
| 修改最近一次提交信息 | `git commit --amend -m "新的信息"` |
| 查看远程仓库地址 | `git remote -v` |
| 从远程拉取最新 | `git pull origin main` |
| 暂存当前工作（切分支前） | `git stash` → `git stash pop` |

---

## 📂 本周产出物

```
java-core-lab/
├── pom.xml                          # 父 POM
├── lab01-collections/               # Day 2-3: 集合框架实验
│   └── src/main/java/com/lab/
│       ├── MyArrayList.java         # 手写 ArrayList
│       ├── ListBenchmark.java       # ArrayList vs LinkedList 性能测试
│       ├── HashMapAnalysis.java     # HashMap 源码分析注释
│       └── MyLRUCache.java          # 手写 LRU Cache
├── lab02-concurrency/               # Day 4-6: 并发实验
│   └── src/main/java/com/lab/
│       ├── DeadLockDemo.java        # 死锁演示
│       ├── ProducerConsumer.java    # 生产者-消费者
│       ├── MyBlockingQueue.java     # 手写阻塞队列
│       └── ThreadPoolLab.java       # 线程池实验
└── lab03-algorithms/                # Day 7: 算法练习
    └── src/main/java/com/lab/
        └── Week01Solutions.java     # 10 道算法题解
```

---

## Day 1：Trae CN 环境搭建 + Spring Boot + MyBatis-Plus CRUD 🔧

> **今日重点：** 把整个开发环境跑通，写出第一个 CRUD 接口，并完成第一次 Git 规范化提交。

### 上午（3h）：Trae CN 环境安装

#### 任务 1.1：安装 JDK 17（30 分钟）

**具体操作步骤：**

1. 打开浏览器，访问 https://adoptium.net/download/
2. 选择 **Temurin 17 (LTS)** → 操作系统选 Windows → 下载 `.msi` 安装包
3. 双击安装，**记住安装路径**（默认 `C:\Program Files\Eclipse Adoptium\jdk-17.0.xx-hotspot\`）
4. 配置环境变量：
   - 按 `Win + R` → 输入 `sysdm.cpl` → 回车
   - 点击「高级」→「环境变量」
   - 系统变量 → 新建：
     - 变量名：`JAVA_HOME`
     - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-17.0.xx-hotspot\`（改成你的实际路径）
   - 找到 `Path` → 编辑 → 新建 → 输入 `%JAVA_HOME%\bin`
   - 确定保存
5. 验证安装：
   - 按 `Win + R` → 输入 `cmd` → 回车
   - 输入 `java -version`，看到 `openjdk version "17.0.x"` 即成功
   - 输入 `javac -version`，看到 `javac 17.0.x` 即成功

#### 任务 1.2：安装 Trae CN（20 分钟）

**具体操作步骤：**

1. 打开浏览器，访问 https://www.trae.ai/cn
2. 点击「下载」→ 选择 Windows 版本 → 下载 `.exe` 安装包
3. 双击安装，一路下一步（建议勾选「添加到 PATH」和「添加到右键菜单」）
4. 启动 Trae CN，完成初始设置：
   - 选择主题（深色/浅色）
   - 登录账号（用手机号或邮箱注册）
5. **安装必要的扩展（Extensions）：**
   - 按 `Ctrl+Shift+X` 打开扩展面板
   - 搜索并安装以下扩展（点击 Install）：

| 扩展名 | 用途 | 是否必须 |
|--------|------|----------|
| **Extension Pack for Java** (Microsoft) | Java 全套支持（语法高亮、代码补全、调试、Maven） | ✅ 必须 |
| **Spring Boot Extension Pack** (VMware) | Spring Boot 支持（application.yml 提示、端点导航） | ✅ 必须 |
| **Lombok Annotations Support** | Lombok 注解支持（消除 @Data 报错） | ✅ 必须 |
| **MyBatisX** | MyBatis Mapper 和 XML 跳转 | 推荐 |
| **GitLens** | Git 增强（行级提交历史、作者信息） | 推荐 |
| **Chinese Language Pack** (Microsoft) | 汉化界面 | 可选 |

6. 配置 Java 设置：
   - 按 `Ctrl+,` 打开设置
   - 搜索 `java.home`
   - 设置为你的 JDK 17 路径（和 JAVA_HOME 一样）
   - 搜索 `java.configuration.runtimes`，在 `settings.json` 中添加：
   ```json
   "java.configuration.runtimes": [
       {
           "name": "JavaSE-17",
           "path": "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.xx-hotspot",
           "default": true
       }
   ],
   "java.jdt.ls.java.home": "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.xx-hotspot"
   ```

#### 任务 1.3：安装 Maven（20 分钟）

**具体操作步骤：**

1. 打开浏览器，访问 https://maven.apache.org/download.cgi
2. 下载 **Binary zip archive**（选最新 3.9.x 版本）
3. 解压到 `C:\maven\`（或其他路径，**不要有中文和空格**）
4. 配置环境变量：
   - 系统变量 → 新建：
     - 变量名：`MAVEN_HOME`
     - 变量值：`C:\maven`
   - `Path` → 新建 → `%MAVEN_HOME%\bin`
5. 配置阿里云镜像（下载依赖快 10 倍）：
   - 用 Trae CN 打开 `C:\maven\conf\settings.xml`
   - 找到 `<mirrors>` 标签，在里面添加：
   ```xml
   <mirror>
       <id>aliyun</id>
       <mirrorOf>central</mirrorOf>
       <name>Aliyun Maven</name>
       <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```
   - 保存文件
6. 验证：打开 Trae CN 终端（按 `Ctrl+`` `）→ 输入 `mvn -version`→ 看到版本号即成功

#### 任务 1.4：安装 Git + 配置 SSH（20 分钟）

**具体操作步骤：**

1. 下载 Git：https://git-scm.com/download/win → 下载 64-bit 版本
2. 双击安装，一路默认即可（注意：编辑器选 VS Code）
3. 在 Trae CN 终端中配置：
   ```bash
   git config --global user.name "你的真实姓名"
   git config --global user.email "你的邮箱@example.com"
   git config --global init.defaultBranch main
   ```
4. 生成 SSH Key（免密推送）：
   ```bash
   ssh-keygen -t ed25519 -C "你的邮箱@example.com"
   # 一路回车即可（使用默认路径和空密码）
   ```
5. 查看公钥并添加到 GitHub：
   ```bash
   cat ~/.ssh/id_ed25519.pub
   # 复制输出的全部内容（以 ssh-ed25519 开头）
   ```
   - 打开 https://github.com/settings/keys
   - 点击「New SSH Key」→ Title 填 `Trae CN 笔记本`→ Key 粘贴 → Add SSH Key
6. 测试连接：
   ```bash
   ssh -T git@github.com
   # 看到 "Hi 你的用户名！" 即成功
   ```

#### 任务 1.5：Docker 安装 MySQL 8.0 + Redis（30 分钟）

**具体操作步骤：**

1. 下载 Docker Desktop：https://www.docker.com/products/docker-desktop/
2. 双击安装，按提示操作（可能需要开启 Hyper-V / WSL2）
3. 安装完成后重启电脑
4. 启动 Docker Desktop（桌面快捷方式），等待右下角鲸鱼图标变绿
5. 在 Trae CN 终端中运行：

```bash
# 拉取并启动 MySQL 8.0
docker run -d --name mysql8 ^
  -p 3306:3306 ^
  -e MYSQL_ROOT_PASSWORD=root123 ^
  -v mysql-data:/var/lib/mysql ^
  mysql:8.0

# 拉取并启动 Redis 7
docker run -d --name redis7 ^
  -p 6379:6379 ^
  redis:7-alpine

# 验证容器运行状态
docker ps
# 应该看到 mysql8 和 redis7 两个容器，STATUS 列显示 "Up"
```

6. 测试 MySQL 连接：
   ```bash
   docker exec -it mysql8 mysql -uroot -proot123
   # 看到 mysql> 提示符即成功，输入 exit 退出
   ```

### 下午（3h）：Spring Boot 项目实战

#### 任务 1.6：在 Trae CN 中创建 Spring Boot 项目（30 分钟）

**具体操作步骤（用 Maven 命令行创建）：**

1. 在 Trae CN 中按 `Ctrl+`` ` 打开终端
2. 进入工作目录并创建项目：
   ```bash
   cd W:\claude\AIJava

   # 用 Maven archetype 生成 Spring Boot 项目
   mvn archetype:generate ^
     -DgroupId=com.training ^
     -DartifactId=week01-springboot ^
     -DarchetypeArtifactId=maven-archetype-quickstart ^
     -DarchetypeVersion=1.5 ^
     -DinteractiveMode=false
   ```
3. 在 Trae CN 中打开项目文件夹：
   - 点击左侧「资源管理器」图标
   - 点击「打开文件夹」→ 选择 `W:\claude\AIJava\week01-springboot`
4. 修改 `pom.xml`——替换为 Spring Boot 项目配置：

> **Trae CN 操作提示：** 在资源管理器中双击 `pom.xml` 打开，全选删除，粘贴以下内容。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>

    <groupId>com.training</groupId>
    <artifactId>week01-springboot</artifactId>
    <version>1.0.0</version>
    <name>week01-springboot</name>
    <description>第一周 Spring Boot 项目</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.6</mybatis-plus.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

5. 创建启动类：
   - 在 Trae CN 资源管理器中，右键 `src/main/java/com/training` → 新建文件 → `App.java`
   - 写入：

```java
package com.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

6. 在终端中验证项目能启动：
   ```bash
   mvn clean compile
   # 看到 BUILD SUCCESS 即项目结构正确

   mvn spring-boot:run
   # 看到 "Started App in X seconds" 即启动成功
   # 按 Ctrl+C 停止
   ```

#### 任务 1.7：配置 application.yml（20 分钟）

**具体操作步骤：**

1. 在 Trae CN 资源管理器中：
   - 右键 `src/main/resources` → 新建文件 → `application.yml`
2. 粘贴配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/training?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印 SQL（调试用）
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

#### 任务 1.8：创建 user 表 + 写完整 CRUD（90 分钟）

**第一步：创建数据库和表**

在 Trae CN 终端中：

```bash
docker exec -it mysql8 mysql -uroot -proot123
```

然后执行 SQL：

```sql
CREATE DATABASE IF NOT EXISTS training;
USE training;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入一条测试数据
INSERT INTO `user` (username, password, email, phone) VALUES
('admin', '123456', 'admin@test.com', '13800138000');

-- 验证
SELECT * FROM `user`;

-- 退出
exit;
```

**第二步：创建包结构**

在 Trae CN 资源管理器中，右键 `src/main/java/com/training` → 新建文件夹，创建以下包：

```
com.training
├── entity        ← 实体类
├── mapper        ← MyBatis-Plus Mapper
├── service       ← 业务层接口
│   └── impl      ← 业务层实现
├── controller    ← 控制器
├── dto           ← 数据传输对象
└── common        ← 公共类（统一返回结果）
```

> **Trae CN 操作提示：** 右键点击 `com.training` 文件夹 →「新建文件夹」→ 输入 `entity` → 回车。依次创建所有包。

**第三步：逐文件编写代码**

> **Trae CN 操作提示：** 右键点击对应的包 →「新建文件」→ 输入文件名（如 `User.java`）→ 回车 → 粘贴代码。

**① `entity/User.java`：**

```java
package com.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer gender;
    private String avatar;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

**② `mapper/UserMapper.java`：**

```java
package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

**③ `dto/UserCreateDTO.java`：**

```java
package com.training.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private Integer gender;
}
```

**④ `dto/UserUpdateDTO.java`：**

```java
package com.training.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Email(message = "邮箱格式不正确")
    private String email;
    private String phone;
    private Integer gender;
    private Integer status;
}
```

**⑤ `common/Result.java`：**

```java
package com.training.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

**⑥ `common/GlobalExceptionHandler.java`：**

```java
package com.training.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.fail(500, "服务器内部错误: " + e.getMessage());
    }
}
```

**⑦ `service/UserService.java`：**

```java
package com.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.training.dto.UserCreateDTO;
import com.training.dto.UserUpdateDTO;
import com.training.entity.User;

public interface UserService extends IService<User> {
    User createUser(UserCreateDTO dto);
    User updateUser(Long id, UserUpdateDTO dto);
}
```

**⑧ `service/impl/UserServiceImpl.java`：**

```java
package com.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.dto.UserCreateDTO;
import com.training.dto.UserUpdateDTO;
import com.training.entity.User;
import com.training.mapper.UserMapper;
import com.training.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User createUser(UserCreateDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setStatus(1); // 默认启用
        save(user);
        return user;
    }

    @Override
    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        BeanUtils.copyProperties(dto, user);
        updateById(user);
        return user;
    }
}
```

**⑨ `controller/UserController.java`：**

```java
package com.training.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.training.common.Result;
import com.training.dto.UserCreateDTO;
import com.training.dto.UserUpdateDTO;
import com.training.entity.User;
import com.training.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public Result<User> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping
    public Result<IPage<User>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userService.page(new Page<>(page, size), wrapper));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}
```

**第四步：添加 MyBatis-Plus 分页插件配置**

创建 `config/MybatisPlusConfig.java`：

```java
package com.training.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

**第五步：启动并测试**

```bash
# 在 Trae CN 终端中启动
mvn spring-boot:run
```

### 晚上（2h）：测试 + Git 规范化提交

#### ① 用 Apifox/Postman 测试全部 5 个接口

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `http://localhost:8080/api/users` | 新增用户（Body: JSON） |
| GET | `http://localhost:8080/api/users/1` | 查询单个用户 |
| GET | `http://localhost:8080/api/users?page=1&size=10&keyword=admin` | 分页搜索 |
| PUT | `http://localhost:8080/api/users/1` | 更新用户 |
| DELETE | `http://localhost:8080/api/users/1` | 删除用户（逻辑删除） |

#### ② 用 Trae CN 内置 AI 做代码审查

选中 `UserController.java` 文件，按 `Ctrl+I` 打开 Trae CN AI 对话框，输入：

```
帮我 review 这段 UserController 代码，检查：
1. 异常处理是否完善
2. 参数校验是否完备
3. 是否存在 SQL 注入风险
4. RESTful 设计是否规范
```

#### ③ Git 规范化提交（重要！第一次走完整流程）

```bash
# 步骤 1：先在 GitHub 上创建远程仓库
# 浏览器打开 https://github.com/new
# Repository name: java-backend-training
# 描述：Java 后端开发 3 个月实操培训记录
# 选择 Private 或 Public（建议 Public，面试官会看）
# ⚠️ 不要勾选 "Add a README file"
# ⚠️ 不要勾选 "Add .gitignore"
# 点击 "Create repository"

# 步骤 2：初始化本地仓库并关联远程
cd W:\claude\AIJava
git init
git branch -M main

# 步骤 3：创建 .gitignore 文件
# 在 Trae CN 中，右键 AIJava 根目录 → 新建文件 → .gitignore
# 粘贴上面"Git 版本控制规范"章节中的 .gitignore 模板

# 步骤 4：关联远程仓库
git remote add origin git@github.com:你的用户名/java-backend-training.git

# 步骤 5：首次提交（分步提交，展示专业度）
# 先提交项目基础设施
git add week01-springboot/pom.xml
git add week01-springboot/.gitignore
git commit -m "chore(week01): Spring Boot 3.2 + MyBatis-Plus 3.5 项目初始化"

# 提交数据库表结构
git add week01-springboot/src/main/resources/
git commit -m "feat(week01): user 表 DDL + application.yml 数据源配置"

# 提交实体类和 Mapper
git add week01-springboot/src/main/java/com/training/entity/
git add week01-springboot/src/main/java/com/training/mapper/
git add week01-springboot/src/main/java/com/training/dto/
git commit -m "feat(week01): User 实体类 + Mapper + DTO"

# 提交 Service 和 Controller
git add week01-springboot/src/main/java/com/training/service/
git add week01-springboot/src/main/java/com/training/controller/
git add week01-springboot/src/main/java/com/training/common/
git add week01-springboot/src/main/java/com/training/config/
git commit -m "feat(week01): User CRUD 完整实现 —— Service + Controller + 统一异常处理"

# 提交启动类和本周计划文档
git add week01-springboot/src/main/java/com/training/App.java
git add week01-基础巩固-Java核心/README.md
git commit -m "chore(week01): 启动类 + 本周培训计划文档"

# 步骤 6：创建本周分支并推送
git checkout -b week01-java-core
git push -u origin week01-java-core

# 步骤 7：回到 main 分支（保持 main 干净）
git checkout main
```

**验证：** 打开 https://github.com/你的用户名/java-backend-training ，确认能看到：
- `week01-java-core` 分支
- 5 次提交记录，每次都有清晰的 commit message
- `week01-springboot/` 目录下的所有代码
- `.gitignore` 文件

#### ④ 写日报

在 `week01-基础巩固-Java核心/` 下创建 `日报-Day01.md`：

```markdown
# Day 1 日报 —— 2026-07-20

## 今日完成
- [x] JDK 17 安装 + 环境变量配置
- [x] Trae CN 安装 + Java 扩展配置
- [x] Maven 3.9 + 阿里云镜像
- [x] Git + SSH Key + GitHub 仓库
- [x] Docker 安装 + MySQL 8.0 + Redis 7
- [x] Spring Boot 项目创建 + user 表设计
- [x] User 完整 CRUD（Entity/Mapper/Service/Controller/DTO）
- [x] 统一返回 Result + 全局异常处理
- [x] Apifox 接口测试全部通过
- [x] Git 首次规范化提交（5 次 commit）

## 踩坑记录
1. （记录你今天遇到的问题和解决方案）

## 明天计划
- Day 2：手写 MyArrayList + List 性能 Benchmark
```

```bash
# 提交日报
git add week01-基础巩固-Java核心/日报-Day01.md
git commit -m "docs: Day 1 日报 —— 环境搭建 + Spring Boot CRUD 完成"
git push origin week01-java-core
```

---

> **🎉 Day 1 完成！** 你已经成功搭建了整个开发环境并写出了第一个 CRUD 接口。
> 关键是：你的 GitHub 上已经有了 6 次规范的提交记录，这是面试官爱看的。

---

## Day 2：集合框架深入 —— 手写 ArrayList + 性能 Benchmark 📊

### 上午（3h）：手写 ArrayList

```java
/**
 * 手写简易 ArrayList
 * 核心：Object[] 数组 + size 计数器 + 扩容机制
 */
public class MyArrayList<E> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elementData;
    private int size;

    public MyArrayList() {
        this.elementData = new Object[DEFAULT_CAPACITY];
    }

    public boolean add(E e) {
        ensureCapacity(size + 1);
        elementData[size++] = e;
        return true;
    }

    public void add(int index, E e) {
        checkIndexForAdd(index);
        ensureCapacity(size + 1);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = e;
        size++;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        checkIndex(index);
        return (E) elementData[index];
    }

    public E remove(int index) {
        checkIndex(index);
        E old = (E) elementData[index];
        int moveNum = size - index - 1;
        if (moveNum > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, moveNum);
        }
        elementData[--size] = null; // 帮助 GC
        return old;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elementData.length) {
            int newCapacity = elementData.length + (elementData.length >> 1); // 1.5 倍扩容
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    // ... checkIndex / checkIndexForAdd 省略
}
```

### 下午（3h）：性能 Benchmark

```java
/**
 * 使用 JMH 做微基准测试
 * 对比 ArrayList vs LinkedList 在 头插/尾插/随机访问/中间插入 的性能
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ListBenchmark {
    private List<Integer> arrayList;
    private List<Integer> linkedList;

    @Setup
    public void setup() {
        arrayList = new ArrayList<>();
        linkedList = new LinkedList<>();
        // 预热数据
        for (int i = 0; i < 10000; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
    }

    @Benchmark
    public void arrayList_addFirst() {
        arrayList.add(0, 9999);
    }

    @Benchmark
    public void linkedList_addFirst() {
        linkedList.add(0, 9999);
    }

    @Benchmark
    public void arrayList_get() {
        arrayList.get(5000);
    }

    @Benchmark
    public void linkedList_get() {
        linkedList.get(5000);
    }
}
```

**踩坑点：** JMH 需要独立模块，不能用 `main` 方法直接跑，因为它需要生成基准测试代码。

**简易替代方案（如果 JMH 配置太复杂）：**
```java
public class SimpleBenchmark {
    public static void main(String[] args) {
        int size = 100000;
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();

        // 尾插
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) arrayList.add(i);
        long arrayEnd = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < size; i++) linkedList.add(i);
        long linkedEnd = System.nanoTime() - start;

        System.out.printf("尾插 %d 次 —— ArrayList: %dns, LinkedList: %dns%n",
            size, arrayEnd, linkedEnd);
    }
}
```

### 晚上（2h）

1. 阅读 ArrayList 源码（`java.util.ArrayList`），对比你手写的版本，找出至少 3 个不同点
2. 回答：为什么 ArrayList 扩容是 1.5 倍而不是 2 倍？
3. Trae CN AI 对话（按 `Ctrl+I`）："解释 System.arraycopy 和 Arrays.copyOf 的区别"
4. **Git 操作：**
   ```bash
   # 确认在正确的分支
   git branch
   # 应该显示 * week01-java-core

   # 提交 lab01-collections 模块
   git add lab01-collections/
   git commit -m "feat(lab01): 手写 MyArrayList + List 性能 Benchmark"

   # 推送
   git push origin week01-java-core
   ```
5. 日报 + 提交代码

---

## Day 3：HashMap 源码 + LRU Cache 🔑

### 上午（3h）：HashMap 源码精读

**在 IDEA 中打开 `java.util.HashMap`，逐方法阅读：**

#### 核心问题清单（边读边回答）

```
1. 为什么默认容量是 16？
2. 为什么加载因子是 0.75？
3. hash() 扰动函数为什么要 h ^ (h >>> 16)？
4. tableSizeFor() 如何保证容量是 2 的幂？
5. 扩容时链表如何迁移？（lo/hi 两条链）
6. 树化阈值为什么是 8？树退化阈值为什么是 6？
7. 什么时候触发扩容？什么时候触发树化？优先级？
```

#### 手写简易 HashMap（只实现数组 + 链表版本）
```java
public class MyHashMap<K, V> {
    static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private Node<K, V>[] table;
    private int size;

    public V put(K key, V value) {
        int hash = key.hashCode() ^ (key.hashCode() >>> 16);
        int index = hash & (table.length - 1);  // 核心！容量是 2 的幂时等价于取模

        if (table[index] == null) {
            table[index] = new Node<>(key, value);
        } else {
            Node<K, V> cur = table[index];
            while (true) {
                if (cur.key.equals(key)) { cur.value = value; return value; }
                if (cur.next == null) { cur.next = new Node<>(key, value); break; }
                cur = cur.next;
            }
        }
        size++;
        return value;
    }
}
```

### 下午（3h）：LRU Cache

#### 方案一：LinkedHashMap 作弊版
```java
public class LRUCacheByLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCacheByLinkedHashMap(int capacity) {
        super(capacity, 0.75f, true); // accessOrder = true
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

#### 方案二：手写版（HashMap + 双向链表）
```java
public class MyLRUCache {
    static class Node {
        int key, value;
        Node prev, next;
        Node(int k, int v) { key = k; value = v; }
    }

    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node head, tail; // 哨兵节点

    public MyLRUCache(int capacity) {
        this.capacity = capacity;
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node node = map.get(key);
        if (node == null) return -1;
        moveToHead(node);
        return node.value;
    }

    public void put(int key, int value) {
        Node node = map.get(key);
        if (node != null) {
            node.value = value;
            moveToHead(node);
        } else {
            Node newNode = new Node(key, value);
            map.put(key, newNode);
            addToHead(newNode);
            if (map.size() > capacity) {
                Node removed = removeTail();
                map.remove(removed.key);
            }
        }
    }

    // 从链表中删除节点
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // 添加到头部
    private void addToHead(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private Node removeTail() {
        Node node = tail.prev;
        removeNode(node);
        return node;
    }
}
```

### 晚上（2h）

1. LeetCode 146. LRU 缓存 —— 用你的代码直接提交，验证正确性
2. 回答：LinkedHashMap 的 `accessOrder=true` 是什么原理？
3. Trae CN AI 对话（按 `Ctrl+I`）："假设我是面试官，请解释为什么 LRU Cache 用双向链表而不是单向链表？"
4. **Git 操作：**
   ```bash
   # 提交 HashMap 分析 + LRU Cache
   git add lab01-collections/src/main/java/com/lab/HashMapAnalysis.java
   git add lab01-collections/src/main/java/com/lab/MyLRUCache.java
   git add lab01-collections/src/main/java/com/lab/LRUCacheByLinkedHashMap.java
   git commit -m "feat(lab01): HashMap 源码分析 + LRU Cache 两种实现"

   # 查看今天提交记录
   git log --oneline -3

   git push origin week01-java-core
   ```
5. 日报 + 提交代码

---

## Day 4：多线程基础 + 死锁诊断 🔒

### 上午（3h）：线程创建方式实验

```java
// 方式 1：继承 Thread
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " running");
    }
}

// 方式 2：实现 Runnable
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " running");
    }
}

// 方式 3：实现 Callable（有返回值 + 可抛异常）
class MyCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        TimeUnit.SECONDS.sleep(1);
        return "result from " + Thread.currentThread().getName();
    }
}
```

#### synchronized 实验题

```java
// 实验 1：对象锁 vs 类锁 —— 这两段代码能并发执行吗？
class LockExperiment {
    // 对象锁
    public synchronized void method1() { sleep(2); }

    // 类锁
    public static synchronized void method2() { sleep(2); }

    // 测试：两个线程分别调用 method1 和 method2，能同时执行吗？
    // 答案：能！因为 method1 锁的是 this 对象，method2 锁的是 LockExperiment.class
}

// 实验 2：ReentrantLock 公平锁 vs 非公平锁
class FairnessExperiment {
    // 非公平锁（默认）
    private final Lock unfairLock = new ReentrantLock();
    // 公平锁
    private final Lock fairLock = new ReentrantLock(true);

    // 对比：启动 10 个线程争抢同一个锁，观察执行顺序
}
```

### 下午（3h）：死锁实验

```java
public class DeadLockDemo {
    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) {
        // 线程 1：先拿 A 再拿 B
        new Thread(() -> {
            synchronized (lockA) {
                System.out.println("Thread-1 拿到 lockA");
                sleep(100); // 让线程 2 有时间拿到 lockB
                synchronized (lockB) {
                    System.out.println("Thread-1 拿到 lockB");
                }
            }
        }, "Thread-1").start();

        // 线程 2：先拿 B 再拿 A —— 和线程 1 获取顺序相反！
        new Thread(() -> {
            synchronized (lockB) {
                System.out.println("Thread-2 拿到 lockB");
                sleep(100);
                synchronized (lockA) {
                    System.out.println("Thread-2 拿到 lockA");
                }
            }
        }, "Thread-2").start();
    }
}
```

#### 死锁诊断步骤
```bash
# 1. 运行程序，观察到卡住不动

# 2. 用 jps 找到 Java 进程 PID
jps -l

# 3. 用 jstack 导出线程堆栈
jstack <pid>

# 4. 在输出中搜索 "deadlock" 或看 "Found one Java-level deadlock:"
# 你会看到类似：
# Found one Java-level deadlock:
# =============================
# "Thread-2": waiting to lock Monitor{0x000000...} which is held by "Thread-1"
# "Thread-1": waiting to lock Monitor{0x000000...} which is held by "Thread-2"

# 5. 用 Claude Code 帮你分析 jstack 输出：
# > "这是 jstack 的输出，帮我分析哪里发生了死锁"
```

### 晚上（2h）

1. 回答：`synchronized` 和 `ReentrantLock` 的区别？（至少 5 点）
2. 实验：`synchronized` 是可重入的吗？写代码验证
3. **Git 操作：**
   ```bash
   # 提交 lab02-concurrency 模块（如果还没创建就先创建）
   git add lab02-concurrency/
   git commit -m "feat(lab02): 死锁演示 + jstack 诊断 + synchronized 实验"

   git push origin week01-java-core
   ```
4. 日报 + 提交代码

---

## Day 5：JUC —— 生产者消费者 + 阻塞队列 🏭

### 上午（3h）：JUC 工具类实操

#### CountDownLatch —— 等所有人都到齐再开会
```java
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        int count = 5;
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            final int no = i + 1;
            new Thread(() -> {
                System.out.println("员工" + no + " 到达会议室");
                latch.countDown();
            }, "T" + no).start();
        }

        latch.await(); // 主线程等待
        System.out.println("所有人都到了，开始开会！");
    }
}
```

#### CyclicBarrier —— 等人齐了一起出发，可以重复使用
```java
public class CyclicBarrierDemo {
    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(3, () ->
            System.out.println("所有选手就位，比赛开始！"));

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000));
                    System.out.println(Thread.currentThread().getName() + " 已就位");
                    barrier.await(); // 等其他人
                } catch (Exception e) { e.printStackTrace(); }
            }, "选手" + (i + 1)).start();
        }
    }
}
```

#### Semaphore —— 停车场只有 3 个车位
```java
public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore parking = new Semaphore(3); // 3 个车位

        for (int i = 0; i < 10; i++) {
            final int car = i + 1;
            new Thread(() -> {
                try {
                    parking.acquire();
                    System.out.println("车" + car + " 停入，剩余车位：" + parking.availablePermits());
                    Thread.sleep(2000);
                    System.out.println("车" + car + " 驶出");
                } catch (InterruptedException e) { e.printStackTrace(); }
                finally { parking.release(); }
            }).start();
        }
    }
}
```

### 下午（3h）：手写阻塞队列

```java
/**
 * 用 wait/notify 实现阻塞队列
 */
public class MyBlockingQueue<T> {
    private final Object[] items;
    private int putIndex, takeIndex, count;

    public MyBlockingQueue(int capacity) {
        this.items = new Object[capacity];
    }

    public synchronized void put(T item) throws InterruptedException {
        while (count == items.length) {
            this.wait(); // 队列满了，等待消费者取走
        }
        items[putIndex] = item;
        if (++putIndex == items.length) putIndex = 0;
        count++;
        this.notifyAll(); // 唤醒等待的消费者
    }

    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        while (count == 0) {
            this.wait(); // 队列空了，等待生产者放入
        }
        T item = (T) items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == items.length) takeIndex = 0;
        count--;
        this.notifyAll(); // 唤醒等待的生产者
        return item;
    }
}
```

**❓ 思考题：用 `if` 判断而不是 `while` 会有什么问题？（虚假唤醒）**

#### 完整生产者-消费者模型
```java
public class ProducerConsumerDemo {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
        // 也可以用你自己的 MyBlockingQueue

        // 3 个生产者
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    try { queue.put(j); } catch (Exception e) {}
                }
            }, "Producer-" + i).start();
        }

        // 2 个消费者
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                while (true) {
                    try { System.out.println("消费: " + queue.take()); } catch (Exception e) {}
                }
            }, "Consumer-" + i).start();
        }
    }
}
```

### 晚上（2h）

1. 用 VisualVM 观察生产者-消费者运行时线程状态变化（WAITING/TIMED_WAITING/BLOCKED）
2. 回答：`wait()` 和 `sleep()` 的区别？
3. **Git 操作：**
   ```bash
   git add lab02-concurrency/src/main/java/com/lab/MyBlockingQueue.java
   git add lab02-concurrency/src/main/java/com/lab/ProducerConsumerDemo.java
   git add lab02-concurrency/src/main/java/com/lab/CountDownLatchDemo.java
   git add lab02-concurrency/src/main/java/com/lab/CyclicBarrierDemo.java
   git add lab02-concurrency/src/main/java/com/lab/SemaphoreDemo.java
   git commit -m "feat(lab02): 手写阻塞队列 + 生产者消费者 + CountDownLatch/CyclicBarrier/Semaphore 实验"

   git push origin week01-java-core
   ```
4. 日报 + 提交代码

---

## Day 6：线程池原理 + 拒绝策略实验 🏊

### 上午（3h）：线程池参数详解

```java
/**
 * ThreadPoolExecutor 7 参数实验
 *
 * corePoolSize:    核心线程数 —— 常驻线程，即使空闲也不回收（除非 allowCoreThreadTimeOut）
 * maximumPoolSize: 最大线程数 —— 核心 + 临时线程
 * keepAliveTime:   空闲存活时间 —— 临时线程超过此时间没活干就回收
 * unit:            时间单位
 * workQueue:       阻塞队列 —— 核心线程忙不过来时，任务先进队列
 * threadFactory:   线程工厂 —— 自定义线程名、优先级、守护状态
 * handler:         拒绝策略 —— 队列满了且线程数达到最大时，新任务怎么处理
 */
public class ThreadPoolLab {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                      // 核心线程数
            5,                      // 最大线程数
            60, TimeUnit.SECONDS,   // 临时线程存活 60s
            new LinkedBlockingQueue<>(10), // 队列容量 10
            new ThreadFactory() {
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "worker-" + (++count));
                }
            },
            new ThreadPoolExecutor.AbortPolicy() // 拒绝策略：抛异常
        );

        // 打印线程池状态
        printStatus(executor, "初始");

        // 提交 20 个任务，观察线程池状态变化
        for (int i = 0; i < 20; i++) {
            final int no = i + 1;
            try {
                executor.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + " 执行任务 " + no);
                    try { Thread.sleep(2000); } catch (Exception e) {}
                });
                System.out.println("任务 " + no + " 提交成功");
            } catch (RejectedExecutionException e) {
                System.out.println("❌ 任务 " + no + " 被拒绝！");
            }
        }

        executor.shutdown();
    }

    private static void printStatus(ThreadPoolExecutor executor, String tag) {
        System.out.printf("[%s] 核心=%d 最大=%d 活跃=%d 池中=%d 队列=%d 完成=%d%n",
            tag,
            executor.getCorePoolSize(),
            executor.getMaximumPoolSize(),
            executor.getActiveCount(),
            executor.getPoolSize(),
            executor.getQueue().size(),
            executor.getCompletedTaskCount());
    }
}
```

**运行结果分析（必做）：**
- 提交前 2 个任务：直接由核心线程执行
- 提交 3-12 个任务：核心线程忙，任务进入队列
- 提交 13-15 个任务：队列满了，创建临时线程
- 提交 16-20 个任务：线程池满了 + 队列满了 → 触发拒绝策略！

### 下午（3h）：四种拒绝策略

```java
public class RejectionPolicyLab {
    public static void main(String[] args) {
        // 构造一个只能容纳 2 个任务的线程池（1 核心 + 1 队列）
        // 这样提交 5 个任务时必然会触发拒绝
        testPolicy("AbortPolicy", new ThreadPoolExecutor.AbortPolicy());
        testPolicy("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy());
        testPolicy("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy());
        testPolicy("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private static void testPolicy(String name, RejectedExecutionHandler handler) {
        System.out.println("\n===== " + name + " =====");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1),
            handler
        );
        for (int i = 0; i < 5; i++) {
            final int no = i + 1;
            try {
                executor.execute(() -> {
                    System.out.println("任务" + no + " 执行");
                    try { Thread.sleep(2000); } catch (Exception e) {}
                });
                System.out.println("✅ 任务" + no + " 已提交");
            } catch (RejectedExecutionException e) {
                System.out.println("❌ 任务" + no + " 被 AbortPolicy 拒绝");
            } catch (Exception e) {
                System.out.println("⚠️ 任务" + no + " 异常：" + e.getMessage());
            }
        }
        executor.shutdown();
    }
}
```

### 晚上（2h）

1. **面试必背：线程池参数设置公式**
   - CPU 密集型：`corePoolSize = CPU 核数 + 1`
   - IO 密集型：`corePoolSize = CPU 核数 * 2`（或 `CPU 核数 / (1 - 阻塞系数)`，阻塞系数一般取 0.8~0.9）
2. Trae CN AI 对话（按 `Ctrl+I`）："为什么不推荐用 Executors 工厂方法创建线程池？"
3. **Git 操作：**
   ```bash
   git add lab02-concurrency/src/main/java/com/lab/ThreadPoolLab.java
   git add lab02-concurrency/src/main/java/com/lab/RejectionPolicyLab.java
   git commit -m "feat(lab02): 线程池 7 参数实验 + 4 种拒绝策略对比"

   git push origin week01-java-core
   ```
4. 日报 + 提交代码

---

## Day 7：算法热身 + 🎯 周总结

### 上午+下午（5h）：LeetCode Hot 100 刷 10 题

> **规则：** 每题先自己想 15 分钟，写不出来再看题解，理解后用 Claude Code 做复盘

| # | 题目 | 分类 | 核心技巧 |
|---|------|------|----------|
| 1 | [1. 两数之和](https://leetcode.cn/problems/two-sum/) | 哈希表 | 用 HashMap 一次遍历找 target - nums[i] |
| 2 | [15. 三数之和](https://leetcode.cn/problems/3sum/) | 双指针 | 排序 + 固定一个 + 双指针去重 |
| 3 | [3. 无重复字符的最长子串](https://leetcode.cn/problems/longest-substring-without-repeating-characters/) | 滑动窗口 | 窗口右移 + HashMap 记录位置 |
| 4 | [206. 反转链表](https://leetcode.cn/problems/reverse-linked-list/) | 链表 | 迭代（三个指针）和递归两种写法 |
| 5 | [20. 有效的括号](https://leetcode.cn/problems/valid-parentheses/) | 栈 | 遇到右括号检查栈顶是否匹配 |
| 6 | [21. 合并两个有序链表](https://leetcode.cn/problems/merge-two-sorted-lists/) | 链表 | 哨兵节点 + 比较插入 |
| 7 | [94. 二叉树的中序遍历](https://leetcode.cn/problems/binary-tree-inorder-traversal/) | 树 | 递归和迭代（用栈）两种方式 |
| 8 | [200. 岛屿数量](https://leetcode.cn/problems/number-of-islands/) | DFS/BFS | 遍历网格，遇到 '1' count++ 然后淹掉所有相连的陆地 |
| 9 | [46. 全排列](https://leetcode.cn/problems/permutations/) | 回溯 | 回溯模板 `for → choose → backtrack → unchoose` |
| 10 | [5. 最长回文子串](https://leetcode.cn/problems/longest-palindromic-substring/) | DP | 中心扩散法（奇/偶长度分别处理）|

**Claude Code 复盘 Prompt 模板（在 Trae CN 中按 Ctrl+I）：**
```
我做完了 LeetCode 第 X 题 [题目名]，这是我的代码：
[paste your code]
请帮我：
1. 分析时间复杂度
2. 有没有更好的解法？最优解是什么？
3. 代码中有没有可以优化的地方？
```

**算法代码 Git 提交：**
```bash
# 每做完 3-5 题提交一次
git add lab03-algorithms/
git commit -m "feat(lab03): LeetCode Hot 100 前 10 题 —— 哈希/双指针/滑动窗口/链表/栈/DFS/回溯/DP"
git push origin week01-java-core
```

### 晚上（3h）：周总结 + Git 分支合并

#### 1. Git 分支合并（本周最重要操作）

```bash
# 确保本周所有代码已提交
git status
# 应该显示 "nothing to commit, working tree clean"

# 查看本周所有提交记录
git log --oneline --graph week01-java-core

# 推送本周分支
git push origin week01-java-core

# ==== 合并到 main 分支 ====
# 切换到 main
git checkout main

# 拉取远程最新（如果有的话）
git pull origin main

# 合并本周分支
git merge week01-java-core --no-ff -m "merge: 合并 Week01 —— Java 核心（集合 + 并发 + 算法）"

# 推送到远程 main
git push origin main

# 切回本周分支（方便继续工作）
git checkout week01-java-core

# 查看合并后的分支图
git log --oneline --graph --all
# 你应该看到 week01-java-core 合并到了 main
```

#### 2. 在 GitHub 上创建 Release Tag（可选但推荐）

```bash
# 打标签
git tag -a v1.0-week01 -m "Week01: Java 核心基础 —— 集合 + JUC 并发 + LeetCode 10题"

# 推送标签到远程
git push origin v1.0-week01

# GitHub 上会自动生成一个 Release，方便面试时展示里程碑
```

#### 3. 下周分支准备

```bash
# 从 main 创建下周分支
git checkout main
git checkout -b week02-jvm-mysql
git push -u origin week02-jvm-mysql
```

#### 4. 本周代码仓库最终检查

```bash
# 检查文件结构
ls -la
# 应该看到：
# week01-springboot/    ← Day 1 的 Spring Boot 项目
# lab01-collections/    ← Day 2-3 集合框架实验
# lab02-concurrency/    ← Day 4-6 并发实验
# lab03-algorithms/     ← Day 7 算法练习
# .gitignore
# README.md（如果有的话）

# 最后确认所有改动都已提交
git status

# 查看本周贡献统计
git log --oneline --author="你的名字" --since="7 days ago"
```

#### 5. 写一篇本周总结博客（推荐用掘金/CSDN/个人博客）

**模板：**
```markdown
# Java 后端训练营 Week01 总结

## 本周做了什么
- Day 1: 搭建了完整开发环境（JDK17 + IDEA + Docker + MySQL + Redis）
- Day 2: 手写 ArrayList + List 性能 Benchmark
- Day 3: 深入 HashMap 源码 + 手写 LRU Cache
- ...

## 踩过的坑
1. JMH 配置报错：需要独立 Maven 模块，不能用 main 方法跑
2. 死锁 jstack 分析：第一次用不知道看哪一行
3. ...

## 最大的收获
1. 真正理解了 HashMap 的 hash 扰动函数和扩容机制
2. 能用 wait/notify 手写阻塞队列了
3. ...

## 下周计划
开始 JVM 和 MySQL 的深入学习...
```

#### 3. 本周检查点自检
- [ ] 环境搭建完成，能一键启动开发环境
- [ ] 能不看笔记手写 LRU Cache
- [ ] 能解释 HashMap 的 put 完整流程
- [ ] 能写正确的生产者-消费者
- [ ] 能解释线程池 7 个参数
- [ ] LeetCode 10 题完成 + 题解笔记
- [ ] GitHub 连续 7 天有提交
