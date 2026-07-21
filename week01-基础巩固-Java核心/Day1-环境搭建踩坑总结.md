# Day 1 环境搭建踩坑总结

> 日期：2026-07-20  
> 目标：搭建 JDK 17 + Trae CN + Maven + Git + Docker + MySQL + Redis 完整开发环境  
> 核心理念：**每个问题不是「怎么修」，而是「怎么发现 → 为什么这样修」**

---

## 踩坑全景

| # | 问题 | 发现方式 | 根因 | 类型 |
|---|------|----------|------|------|
| 1 | `java -version` 没反应 | 直接测试 | PATH 里 Oracle javapath 残留拦截 | 环境配置 |
| 2 | `echo %JAVA_HOME%` 输出原样文字 | 看到输出不对 | PowerShell 语法与 CMD 不同 | 工具使用 |
| 3 | JDK 不在默认路径 | `dir` 找不到目录 | IDE 自动安装到 `C:\Users\淋雨\.jdks` | 经验不足 |
| 4 | PATH 太长 GUI 编辑不了 | 改环境变量时弹窗 | 66 条记录超过 2047 字符限制 | Windows 限制 |
| 5 | Docker 未安装 | `docker --version` 报错 | 没有装过 | 正常流程 |
| 6 | WSL2 未安装 | `wsl --status` 报错 | Docker 依赖 WSL2 | 前置依赖 |
| 7 | SSH Key 未生成 | `dir ~\.ssh\` 找不到 | 新电脑没配过 | 正常流程 |
| 8 | MySQL 端口 3306 被占用 | `docker run` 报错 | 之前装的 Windows MySQL 服务在跑 | 端口冲突 |
| 9 | 旧 MySQL 服务停不掉 | `Stop-Service` 权限不足 | 不是管理员终端 | 权限问题 |
| 10 | 容器名已存在 | `docker run` 报错 | 第一次失败后容器没有自动删除 | Docker 机制 |
| 11 | Redis 镜像拉不下来 | `docker run` 超时 | Docker Hub 国内访问慢 | 网络问题 |
| 12 | MySQL 容器重启后停了 | `docker ps` 只看到 Redis | Docker Desktop 重启不会自动启容器 | Docker 机制 |

---

## 逐问题详细分析

### 问题 1：`java -version` 没反应

**现象：** 终端输入 `java -version`，没有报错，也没有输出。

**排查过程：**
1. `echo $env:JAVA_HOME` → 输出 `C:\Users\淋雨\.jdks` ✓ 环境变量配了
2. `echo $env:Path` → 看到一大串路径，其中有：
   - `C:\Program Files\Common Files\Oracle\Java\javapath` ← 排在最前面
   - `C:\Users\淋雨\.jdks\bin` ← 排在后面
3. `C:\Users\淋雨\.jdks\bin\java.exe -version` → 直接用完整路径调，能输出 17.0.12 ✓

**根因分析：**
```
Windows 找 java.exe 的顺序：
  ① C:\Program Files\Common Files\Oracle\Java\javapath  ← 先找到这里的 java.exe
  ② C:\Users\淋雨\.jdks\bin                                ← 永远不会走到这里
  
Oracle javapath 里的 java.exe 是一个"残留的启动器"——
之前装过 Oracle JDK，卸载后这个文件没删干净，但它已经找不到真正的 JDK 了。
所以它不报错（文件存在），也没有输出（找不到 JDK 干活）。
```

**解决方案：** 从 PATH 中删除 `C:\Program Files\Common Files\Oracle\Java\javapath`

**学到的：**
- PATH 搜索是从左到右的，谁排前面谁执行
- "没反应" ≠ "没安装"，可能是被前面的拦截了
- 卸载软件后 PATH 里可能留垃圾

---

### 问题 2：`echo %JAVA_HOME%` 输出 `%JAVA_HOME%`

**现象：** 明明是查环境变量，终端却原样输出 `%JAVA_HOME%`

**排查过程：** 看到终端前缀是 `PS`（PowerShell），不是 `cmd`

**根因分析：**
```
CMD 语法：   echo %JAVA_HOME%     → 解析环境变量
PowerShell： echo %JAVA_HOME%     → 当成纯文本打印出来

正确写法：
  PowerShell： echo $env:JAVA_HOME
  CMD：        echo %JAVA_HOME%
```

**学到的：** 看终端前缀判断是什么 Shell，语法不一样。Trae CN 默认是 PowerShell（前缀 `PS`）

---

### 问题 4：PATH 太长，GUI 编辑不了

**现象：** 打开环境变量编辑窗口，提示「此环境变量太大。此对话框允许将值设置最长 2047 个字符」

**根因分析：**
- 用了 `echo $env:Path | Select-Object Length`（间接得知）PATH 有 66 条记录
- VMware 出现 4 次、Oracle javapath 出现 3 次——**装软件时重复追加**
- Windows 那个老旧的编辑框有 2047 字符限制，超过就卡死

**解决方案：** 用 PowerShell 命令行操作，不受 GUI 限制
```powershell
$path = [Environment]::GetEnvironmentVariable("Path", "User")
$entries = $path -split ";"
$clean = $entries | Where-Object { 过滤条件 }  # 删 Oracle javapath 和 %JAVA_HOME%\bin
$clean = $clean | Select-Object -Unique          # 去重，66→27 条
[Environment]::SetEnvironmentVariable("Path", ($clean -join ";"), "User")
```

**学到的：**
- 66→27 条，去掉了 39 条重复/无效数据
- Windows GUI 有字符限制时，用命令行绕过去
- 定期清理 PATH 是个好习惯

---

### 问题 8：MySQL 端口 3306 被占用

**现象：** `docker run mysql` 报错 `port 3306: bind: Only one usage of each socket address is normally permitted`

**排查过程：**
```powershell
netstat -ano | findstr 3306
# 输出：TCP  0.0.0.0:3306  LISTENING  9728
```
→ 有个进程（PID 9728）在监听 3306

```powershell
Get-Service | Where-Object { $_.DisplayName -like "*MySQL*" }
# 输出：Running  MySQL80
```
→ 是你之前安装的 Windows MySQL 服务

**根因分析：** 端口就像电话分机号。Windows MySQL 已经占用了 3306，Docker MySQL 也想用 3306 → 冲突。

**决策：**
- 方案 A：关掉旧 MySQL，全用 Docker（✅ 选了这）
- 方案 B：Docker 用 3307 端口（两个 MySQL 共存）
- 选择理由：统一用 Docker 管理，干净不乱

**解决方案：** 管理员终端停掉旧服务 → Docker 启动 MySQL

---

### 问题 9：停不掉旧 MySQL 服务

**现象：** `Stop-Service MySQL80` 报错「无法打开计算机"."上的 MySQL80 服务」

**根因分析：** 普通终端没有权限停系统服务。PowerShell 前缀可以看到是普通用户。

**解决方案：** `Win` → 搜 PowerShell → 右键 → **以管理员身份运行**

---

### 问题 10：容器名已存在

**现象：** 第二次 `docker run --name mysql8` 报错「container name "/mysql8" is already in use」

**根因分析：** 第一次执行虽然端口冲突失败了，但容器已经创建了（`docker ps -a` 能看到 `Exited` 状态）。Docker 不会自动清理失败的容器。

**解决方案：**
```powershell
docker rm mysql8       # 删掉旧容器
docker run -d --name mysql8 ... # 重新创建
```

**学到的：** `docker ps` 只看运行的容器，`docker ps -a` 看全部（包括停止的）

---

### 问题 11：Redis 镜像拉不下来

**现象：** `docker run redis:7-alpine` 报错 `failed to do request: EOF`

**根因分析：** Docker Hub 服务器在国外，国内直连经常超时

**解决方案：** 配置镜像加速器（registry-mirrors），让 Docker 从国内镜像站下载
```json
"registry-mirrors": [
  "https://docker.1panel.live",
  "https://hub.rat.dev"
]
```

---

### 问题 12：Docker 重启后 MySQL 容器停了

**现象：** 配镜像加速重启 Docker 后，`docker ps` 只显示 Redis，MySQL 不见了

**排查过程：** `docker ps -a` 看到 MySQL `Exited (0) 2 minutes ago`

**根因分析：** Docker Desktop 重启时不会自动启动之前运行的容器，除非加了 `--restart` 参数

**解决方案：**
```powershell
docker start mysql8    # 手动拉起来
```

**后续优化（可选）：** 创建容器时加 `--restart unless-stopped`，以后 Docker 重启会自动拉起

---

## 🔀 附录：切回非 Docker 版 MySQL 的操作

因为你选了方案 A（关掉旧 MySQL），以下是需要切回去时的操作步骤：

### 切换到 Windows 版 MySQL

```powershell
# Step 1：关掉 Docker MySQL
docker stop mysql8

# Step 2：启动 Windows MySQL 服务（需要管理员终端）
Start-Service MySQL80

# Step 3：验证
mysql -uroot -p -P 3306
```

### 切回 Docker 版 MySQL

```powershell
# Step 1：停掉 Windows MySQL 服务（需要管理员终端）
Stop-Service MySQL80

# Step 2：启动 Docker MySQL
docker start mysql8

# Step 3：验证
docker exec -it mysql8 mysql -uroot -proot123
```

### 重要提醒
- 两个 MySQL 的数据是**独立**的——在 Windows MySQL 里建的表，Docker MySQL 里看不到
- `spring.datasource.url` 里的端口都是 `3306`，连接哪个取决于哪个在跑
- 旧 MySQL 的 `root` 密码是你之前自己设的，Docker 版是 `root123`

---

## 📊 排查能力总结

| 技能 | 使用次数 | 关键命令 |
|------|----------|----------|
| 看 PATH | 3 次 | `echo $env:Path` |
| 找文件位置 | 2 次 | `dir 路径` |
| 看端口占用 | 1 次 | `netstat -ano \| findstr 端口` |
| 看服务状态 | 2 次 | `Get-Service`, `docker ps -a` |
| 命令行改系统配置 | 2 次 | `[Environment]::SetEnvironmentVariable` |
| 判断 Shell 类型 | 2 次 | 看前缀是 `PS` 还是 `C:\` |

> **最重要的收获：** "没反应"不等于"没装"，排查顺序 = 确认安装位置 → 确认 PATH 顺序 → 用完整路径测试 → 排除拦截项
