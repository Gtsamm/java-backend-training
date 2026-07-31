# Day 1 日报 —— 2026-07-21

## 今日完成
- [x] 环境搭建收尾（Docker MySQL/Redis 运行验证、Navicat 连接）
- [x] Spring Boot 3.2 项目创建 + pom.xml 依赖配置
- [x] user 表设计 + Navicat DDL 建表 + 测试数据插入
- [x] User 完整 CRUD（Entity/Mapper/DTO/Service/Controller）
- [x] 统一返回 Result + 全局异常处理
- [x] MyBatis-Plus 分页插件 + 自动填充 + 逻辑删除配置
- [x] 5 个 REST 接口 curl 测试全部通过
- [x] Git 规范化提交（4 次 Conventional Commits）+ 推送 GitHub
- [x] 创建 week01-java-core 分支

---

## 踩坑记录（12 个问题）

### 1. `java -version` 没反应也不报错
- **现象**：终端输入 `java -version`，没任何输出
- **排查**：`echo $env:JAVA_HOME` → 有值；`echo $env:Path` → 发现 `C:\Program Files\Common Files\Oracle\Java\javapath` 排在 `C:\Users\淋雨\.jdks\bin` 前面；用完整路径 `C:\Users\淋雨\.jdks\bin\java.exe -version` 能正常输出
- **根因**：Windows PATH 从左到右搜索，Oracle javapath 里的残留启动器在 JDK 路径之前被找到，但它已经找不到真正的 JDK，所以既不报错也无输出
- **解决**：从 PATH 中删除 Oracle javapath 条目（用 PowerShell 的 `[Environment]::SetEnvironmentVariable` 操作）
- **教训**："没反应" ≠ "没装"，先检查 PATH 顺序，再用完整路径测试来排除 PATH 干扰

### 2. `echo %JAVA_HOME%` 输出原样文字
- **现象**：终端输出 `%JAVA_HOME%` 而不是变量值
- **根因**：Trae CN 默认终端是 PowerShell（前缀 `PS`），不是 CMD。CMD 用 `%VAR%`，PowerShell 用 `$env:VAR`
- **解决**：改用 `echo $env:JAVA_HOME`
- **教训**：看终端前缀判断 Shell 类型，语法不通用

### 3. PATH 太长，GUI 编辑窗口报「2047 字符限制」
- **现象**：Windows 环境变量编辑窗口无法打开 PATH，报字符超过限制
- **排查**：PATH 有 66 条记录，含大量重复（VMware 4 次、Oracle javapath 3 次）
- **根因**：Windows 那个老旧对话框有 2047 字符硬限制，每次装软件盲目追加导致 PATH 膨胀
- **解决**：PowerShell 命令行操作绕过 GUI 限制 → `Select-Object -Unique` 去重 → 66 条缩减到 27 条
- **教训**：装软件时注意它往 PATH 里加了什么；命令行可以绕开 GUI 限制

### 4. MySQL 端口 3306 被占用
- **现象**：Docker 创建 MySQL 容器时报 `port 3306: bind: Only one usage`
- **排查**：`netstat -ano | findstr 3306` → PID 9728 在监听；`Get-Service` → MySQL80 服务在运行
- **根因**：之前装的 Windows MySQL 服务占用了 3306，Docker MySQL 也想用 3306 → 端口冲突
- **解决**：停掉 Windows MySQL80 服务（`Stop-Service MySQL80`，需管理员终端），全用 Docker 版
- **教训**：端口是独占资源，`netstat -ano | findstr 端口号` 是排查端口冲突的第一步

### 5. 停不掉 Windows MySQL 服务
- **现象**：`Stop-Service MySQL80` 报「无法打开计算机上的 MySQL80 服务」
- **根因**：普通终端没有权限管理系统服务
- **解决**：以管理员身份运行 PowerShell（Win → 搜 PowerShell → 右键 → 以管理员身份运行）
- **教训**：操作 Windows 服务需要管理员权限

### 6. Docker 容器名已存在
- **现象**：重新 `docker run --name mysql8` 报「container name already in use」
- **根因**：第一次执行虽然端口冲突失败了，但容器已经创建（`docker ps -a` 能看到 `Exited` 状态），Docker 不会自动清理失败的容器
- **解决**：`docker rm mysql8` 删除旧容器再重建
- **教训**：`docker ps` 只看运行的容器，`docker ps -a` 看全部包括已停止的

### 7. Redis 镜像拉不下来
- **现象**：`docker run redis:7-alpine` 报 `failed to do request: EOF`
- **根因**：Docker Hub 服务器在国外，国内直连超时
- **解决**：配置 registry-mirrors（`docker.1panel.live`、`hub.rat.dev`），从国内镜像站拉取
- **教训**：国内开发要配 Docker 镜像加速

### 8. Docker 重启后 MySQL 容器停了
- **现象**：配镜像加速重启 Docker Desktop 后，`docker ps` 只显示 Redis，MySQL 不见了
- **排查**：`docker ps -a` 看到 MySQL `Exited (0)`
- **根因**：Docker Desktop 重启不会自动启动之前的容器（除非创建时加了 `--restart unless-stopped`）
- **解决**：`docker start mysql8` 手动拉起
- **教训**：生产容器创建时记得加 `--restart` 参数

### 9. Spring Boot 启动报 YAML 解析错误
- **现象**：`while scanning a simple key ... could not find expected ':'`
- **根因**：`application.yml` 中 `url: jdbc:mysql://...` 的冒号被 YAML 当成了 key-value 分隔符
- **解决**：URL 用引号包起来：`url: "jdbc:mysql://..."`
- **教训**：YAML 对冒号敏感，包含冒号的字符串必须加引号

### 10. `characterEncoding=utf8mb4` 导致 `UnsupportedEncodingException`
- **现象**：POST 创建用户时报 `java.io.UnsupportedEncodingException: utf8mb4`
- **排查**：MySQL 字符集 `utf8mb4` 是数据库层面的名字，Java 层面叫 `UTF-8`
- **根因**：JDBC 驱动用 Java 的 `Charset.forName()` 来解析 `characterEncoding` 参数，Java 标准库只认 `UTF-8` 不认 `utf8mb4`
- **解决**：`characterEncoding=utf8mb4` → `characterEncoding=UTF-8`
- **教训**：数据库字符集名 ≠ Java 编码名，虽然他们指的是同一套 4 字节 Unicode

### 11. 分页查询 total=0
- **现象**：分页接口返回 `total: 0, pages: 0`，SQL 日志里也没有 `LIMIT` 和 `COUNT`
- **根因**：漏写了 `MybatisPlusConfig`——没有注入 `PaginationInnerInterceptor`
- **解决**：创建 `MybatisPlusConfig.java`，`@Bean` 注入 `PaginationInnerInterceptor(DbType.MYSQL)`
- **教训**：MyBatis-Plus 的分页插件不是自动启用的，需要手动注册拦截器

### 12. 更新用户时 `gender` 和 `status` 被 `null` 覆盖
- **现象**：更新 email 和 phone 后，gender 和 status 变成了空值
- **根因**：`BeanUtils.copyProperties(dto, user)` 是全量拷贝，DTO 里没传的字段（null）也拷进了 Entity，覆盖了原有值
- **解决**：改成逐字段判 null 赋值（`if (dto.getGender() != null) user.setGender(...)`），只更新传了的字段
- **教训**：Spring 的 `BeanUtils.copyProperties` 不区分 null 值，生产环境用 Hutool 的 `BeanUtil.copyProperties(source, target, CopyOptions.create().ignoreNullValue())` 一步搞定

---

## 学到的新知识

1. **Spring Boot 三层架构**：Controller（接客）→ Service（业务）→ Mapper（SQL），每层单一职责
2. **MyBatis-Plus 三大注解原理**：`@TableLogic` 自动拼 `deleted=0`、分页拦截器自动拼 `LIMIT`、MetaObjectHandler 自动填时间
3. **逻辑删除 vs 物理删除**：`DELETE /api/users/2` 实际执行的是 `UPDATE SET deleted=1`，数据不真删
4. **YAML 缩进和引号**：冒号是语法关键字，含冒号的值必须加引号
5. **PowerShell vs CMD**：环境变量语法不同（`$env:VAR` vs `%VAR%`）、引号规则不同（单引号不转义）
6. **Git 规范化提交**：Conventional Commits 格式 `type(scope): 描述`，小步提交 > 大杂烩

## 今日 Git 学习 —— 为什么要这样提交

### 先理解 Git 的三个区域

```
工作目录                    暂存区                      本地仓库              远程仓库
(Working Dir)    ──add──▶   (Staging)   ──commit──▶    (Local Repo)  ──push──▶  (GitHub)
    │                          │                          │
    │◀── 你在这里改文件          │◀── 决定"这次要提交哪些改动"    │◀── 你的提交历史在这里
    │                          │                          │
    └── git add = 把文件添加到"下一次提交的清单"
              git commit = 把清单里的文件拍成一张"快照"
              git push = 把本地的快照上传到 GitHub
```

> 💡 **面试类比**：`git add` = 购物车加商品，`git commit` = 结账付款（生成订单），`git push` = 快递发货。没 push 之前都在你本机。

### 为什么分了 4 次 commit，而不是一次性全提交？

一次性提交（❌ 反例）：
```
git add .
git commit -m "第一次提交"
```
这样的提交历史在 GitHub 上只显示一行，面试官看不到任何信息量。

分步提交（✅ 正例）：
```
5c0cb7f  chore(week01): Spring Boot 3.2 + MyBatis-Plus 3.5 项目初始化
1252a09  feat(week01): MySQL + MyBatis-Plus 数据源配置 + 逻辑删除 + SQL 日志
997045d  feat(week01): User 实体类 + Mapper + DTO（含参数校验）
11b2e81  feat(week01): User CRUD 完整实现 + 分页 + 自动填充 + 全局异常处理
```

好处：
1. **面试官能看出你的开发节奏**：先搭架子 → 配数据库 → 写数据层 → 写业务层，清晰的思维过程
2. **出 bug 时能精准回滚**：如果 Service 写错了，只回滚第 4 次，前 3 次还保留
3. **Code Review 时能只看某次改动**：同事审查"Service 层"时不用看 pom.xml

### 为什么有 main 和 week01-java-core 两个分支？

```
main ◀────────────────────────────────    ← 永远保持稳定、可运行的代码
  │
  └── week01-java-core ◀──── Week 1 开发    ← 在这上面写代码、犯错误、修 bug
  │
  └── week02-jvm-mysql   ◀──── Week 2 开发
  │
  └── ...
```

> 💡 **类比**：`main` 是"正式出版的书"，`week01-java-core` 是"草稿本"。你在草稿本上随便写、随便改，写好了才合到正式出版物里。没有分支的话，你在 main 上直接写代码，写到一半项目坏了没法回去。

### 提交信息格式：`type(scope): 描述`

| type | 含义 | 什么场景用 |
|------|------|-----------|
| `feat` | 新功能 | 写完一个接口/类 |
| `fix` | 修 bug | 修了一个错误 |
| `docs` | 文档 | 写日报、加注释 |
| `chore` | 杂项 | 配 pom.xml、改配置 |
| `refactor` | 重构 | 只改结构不改功能 |
| `test` | 测试 | 加测试用例 |

> 💡 **面试官看什么**：commit message 是否清晰、是否分步提交、是否有分支管理意识。这些细节就是"有团队协作经验"和"自己瞎写"的区别。

### 今天实际执行的 Git 工作流

```bash
git init                                    # 初始化仓库
git add .gitignore pom.xml                  # 第一步：基础设施
git commit -m "chore(week01): 项目初始化"
git add application.yml                     # 第二步：数据源配置
git commit -m "feat(week01): 数据源配置"
git add entity/ mapper/ dto/               # 第三步：数据层
git commit -m "feat(week01): Entity+Mapper+DTO"
git add service/ controller/ common/ config/  # 第四步：业务层
git commit -m "feat(week01): CRUD 完整实现"
git remote add origin git@github.com:...    # 关联远程
git push -u origin main                     # 推送 main
git checkout -b week01-java-core            # 创建周分支
git push -u origin week01-java-core         # 推送周分支
git checkout main                           # 回到 main
```

## AI 使用反思

- 今天用 AI 做了什么：讲解原理、排查报错、修正代码 bug
- 有没有不该用 AI 但用了的情况：日报应该自己写，但今天踩坑太多，让 AI 帮忙整理了（下次自己写）

## 明日计划
- Day 2：手写 MyArrayList + LinkedList vs ArrayList 性能 Benchmark
- 学习 ArrayList 源码（扩容机制、fail-fast）
- Git 提交 `week01-java-core` 分支
