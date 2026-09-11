# 智能创新创业项目管理系统 — 后端说明文档（SpringBoot-project）

> 本文档描述后端服务的技术栈、目录结构、启动方式、配置、认证机制、API 接口、数据库设计、请求/响应示例与部署方式，供开发、联调与部署参考。

---

## 目录

- [一、项目简介](#一项目简介)
- [二、技术栈](#二技术栈)
- [三、环境要求](#三环境要求)
- [四、目录结构](#四目录结构)
- [五、快速开始](#五快速开始)
- [六、配置说明](#六配置说明applicationproperties)
- [七、统一响应格式](#七统一响应格式)
- [八、认证与鉴权（JWT）](#八认证与鉴权jwt)
- [九、静态资源与文件上传](#九静态资源与文件上传)
- [十、API 接口文档](#十api-接口文档)
- [十一、请求 / 响应示例](#十一请求--响应示例)
- [十二、数据库设计](#十二数据库设计)
- [十三、业务状态流转](#十三业务状态流转)
- [十四、全局异常处理](#十四全局异常处理)
- [十五、前后端联调说明](#十五前后端联调说明)
- [十六、部署（打包 jar + Nginx）](#十六部署打包-jar--nginx)
- [十七、Postman / Apifox 导入片段](#十七postman--apifox-导入片段)
- [十八、常见问题（FAQ）](#十八常见问题faq)

---

## 一、项目简介

`SpringBoot-project` 是「智能创新创业项目管理系统」的后端服务，基于 **Spring Boot** 构建，采用经典的 **Controller → Service → Mapper(MyBatis) → MySQL** 分层架构，对外提供 RESTful API，供前端 `vue-project` 调用。

核心业务模块：

- 用户与角色管理（注册、登录、审批、头像上传）
- 项目申报与审批（含项目成员、指导老师、计划书文件）
- 获奖记录管理与审批（含获奖证明图片）
- 公告发布与管理
- 站内消息通知（审批结果、公告发布等自动推送）

---

## 二、技术栈

| 分类 | 技术 / 版本 |
| --- | --- |
| 语言 | Java 21 |
| 框架 | Spring Boot 4.0.0（Spring Framework 7.0.1） |
| Web | spring-boot-starter-webmvc（内嵌 Tomcat 11） |
| 持久层 | MyBatis（mybatis-spring-boot-starter 4.0.0） |
| 分页 | PageHelper（pagehelper-spring-boot-starter 1.4.7） |
| 数据库 | MySQL（mysql-connector-j 9.5.0） |
| 连接池 | HikariCP |
| 认证 | JWT（jjwt 0.11.5 + java-jwt 3.18.2） |
| 工具 | Lombok 1.18.42 |
| 构建 | Maven（含 `mvnw` / `mvnw.cmd` 包装器） |

---

## 三、环境要求

- **JDK 21**（Oracle OpenJDK 21 已验证）
- **Maven 3.9+**（可使用项目自带 `mvnw.cmd`）
- **MySQL 8.x**，需提前创建数据库 `bsgl`
- 默认服务端口：**8080**（未在配置中显式指定 `server.port`）

---

## 四、目录结构

```
SpringBoot-project/
├── src/main/java/org/example/springbootproject/
│   ├── SpringBootProjectApplication.java   # 启动类（@MapperScan 扫描 mapper 包）
│   ├── config/                             # 配置类
│   │   ├── WebConfig.java                  # 跨域、拦截器、静态资源映射
│   │   ├── JwtInterceptor.java             # JWT 登录拦截器
│   │   └── GlobalExceptionHandler.java     # 全局异常处理
│   ├── controller/                         # REST 接口层
│   │   ├── AuthController.java             # 登录 / 忘记密码
│   │   ├── UserController.java             # 用户管理 / 头像上传
│   │   ├── RoleController.java             # 角色管理
│   │   ├── ProjectController.java          # 项目申报 / 审批 / 计划书
│   │   ├── AwardController.java            # 获奖记录 / 审批 / 证明上传
│   │   ├── NoticeController.java           # 公告管理
│   │   └── NotificationController.java     # 站内通知
│   ├── service/ (含 impl/)                 # 业务逻辑层
│   ├── mapper/                             # MyBatis Mapper 接口
│   ├── pojo/                               # 实体类 / DTO / 统一响应
│   └── util/                               # JwtUtil、Md5Util
├── src/main/resources/
│   ├── application.properties              # 主配置文件
│   ├── mapper/*.xml                        # MyBatis SQL 映射
│   └── *.sql                               # 建表与迁移脚本
├── uploads/                                # 上传文件存放目录（运行时生成）
│   ├── avatars/                            # 用户头像
│   ├── project-plans/                      # 项目计划书
│   └── award-certificates/                 # 获奖证明图片
└── pom.xml
```

---

## 五、快速开始

### 1. 准备数据库

```sql
CREATE DATABASE IF NOT EXISTS bsgl DEFAULT CHARACTER SET utf8mb4;
```

依次执行 `src/main/resources/` 下的建表与迁移脚本：

- `project_apply_schema.sql`（project / project_member / award / notice）
- `project_plan_schema.sql`（project_plan）
- `notification_schema.sql`（notification）
- `alter_project_advisor_table.sql`（project_advisor）
- `alter_project_member_add_gender.sql`、`alter_project_table.sql`
- `alter_award_table.sql`、`alter_award_add_reject_reason.sql`
- `alter_add_project_plan_table.sql`、`alter_user_add_avatar.sql`

> 说明：`user` / `role` 表为系统基础表，需保证已存在（用户注册依赖 `role` 表中的角色数据）。

### 2. 修改配置

编辑 `src/main/resources/application.properties`，将数据库账号密码改为本地实际值（见「六、配置说明」）。

### 3. 启动服务

- **IDE 启动**：运行 `SpringBootProjectApplication` 的 `main` 方法。
- **命令行启动**：

```bash
# Windows PowerShell
cd SpringBoot-project
./mvnw.cmd spring-boot:run

# Linux / macOS
cd SpringBoot-project
./mvnw spring-boot:run
```

启动成功后，服务监听 `http://localhost:8080`。

> ⚠️ **工作目录提示**：文件上传与静态资源访问均基于 `System.getProperty("user.dir")`（JVM 启动工作目录）下的 `uploads/` 目录。请确保以 `SpringBoot-project` 作为工作目录启动，使上传目录与已有文件位置一致。

---

## 六、配置说明（application.properties）

```properties
spring.application.name=SpringBoot-project

# 数据源配置
spring.datasource.url=jdbc:mysql://localhost:3306/bsgl?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# MyBatis 配置
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl   # 打印 SQL 日志
mybatis.configuration.map-underscore-to-camel-case=true                      # 下划线转驼峰
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=org.example.springbootproject.pojo

# PageHelper 分页插件
pagehelper.helper-dialect=mysql
pagehelper.reasonable=true
pagehelper.support-methods-arguments=true
pagehelper.params=count=countSql

# 文件上传限制
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
spring.servlet.multipart.enabled=true
```

> 生产环境建议：将数据库密码、JWT 密钥等敏感信息改为环境变量或配置中心管理。

---

## 七、统一响应格式

所有接口返回统一结构 `Result<T>`：

```json
{
  "code": 1,
  "msg": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | Integer | 状态码：`1` 成功，`0` 失败 |
| msg | String | 提示信息 |
| data | T | 业务数据（可为对象、数组或 null） |

分页数据统一使用 `PageBean<T>`：

```json
{
  "total": 100,
  "list": []
}
```

日期时间字段统一格式化为 `yyyy-MM-dd HH:mm:ss`。

---

## 八、认证与鉴权（JWT）

### 认证流程

1. 客户端调用 `POST /login`，携带 `username` + `password`（密码后端使用 **MD5** 校验）。
2. 仅 **状态为「已审批」** 的用户可登录成功。
3. 登录成功返回 JWT `token`（有效期 **7 天**），其中包含 `userId`、`username`、`role`。
4. 后续请求需在请求头携带：`Authorization: Bearer <token>`（兼容旧方式 `token: <token>`）。

### 拦截器（JwtInterceptor）

- 拦截所有请求 `/**`，校验 Token 有效性；无效或缺失返回 **401**。
- 校验通过后，将 `userId`、`username`、`role` 写入 `request` 属性，供 Controller 使用。
- **放行路径**（无需 Token）：

```
/login                 # 登录
/users/register        # 注册
/roles                 # 角色列表（注册时需要）
/auth/send-code        # 发送验证码
/auth/reset-password   # 重置密码
/error                 # 错误页
/uploads/**            # 静态资源（头像、文件等）
```

### 跨域（CORS）

`WebConfig` 中放开跨域：允许所有来源（`allowedOriginPatterns("*")`）、方法 `GET/POST/PUT/DELETE/OPTIONS`、所有请求头，允许携带凭证，预检缓存 3600 秒。

---

## 九、静态资源与文件上传

上传文件统一存放于 `uploads/` 目录，并通过 URL 路径 `/uploads/**` 对外访问：

| 业务 | 存放目录 | 访问路径前缀 | 允许格式 |
| --- | --- | --- | --- |
| 用户头像 | `uploads/avatars/` | `/uploads/avatars/` | jpg / jpeg / png / gif |
| 项目计划书 | `uploads/project-plans/` | `/uploads/project-plans/` | doc / docx / pdf |
| 获奖证明 | `uploads/award-certificates/` | `/uploads/award-certificates/` | jpg / jpeg / png / gif |

- 文件保存时以 **UUID** 重命名，避免重名覆盖。
- 单文件最大 **100MB**（超出由 `GlobalExceptionHandler` 统一返回友好提示）。
- 前端通过 Vite 代理访问：图片/文件地址形如 `/api/uploads/avatars/xxx.jpg`，代理去掉 `/api` 前缀后转发至后端 `/uploads/avatars/xxx.jpg`。

> 🔧 **排查提示（头像/文件 404）**：静态资源映射 `WebConfig.addResourceHandlers` 使用相对路径 `file:uploads/...`，其解析依赖启动工作目录。若出现资源 404，请确认工作目录为 `SpringBoot-project`；更稳妥的做法是将映射位置改为基于 `System.getProperty("user.dir")` 拼接的**绝对路径** `file:` URL（与各上传接口保持一致）。

---

## 十、API 接口文档

> 基础地址：`http://localhost:8080`；除标注「放行」外，均需携带 `Authorization: Bearer <token>`。

### 10.1 认证模块（AuthController）

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/login` | 用户登录，返回 token 与用户基本信息 | 放行 |
| POST | `/auth/send-code` | 发送找回密码验证码（开发阶段验证码打印到控制台） | 放行 |
| POST | `/auth/reset-password` | 校验验证码并重置密码 | 放行 |

### 10.2 用户模块（UserController，前缀 `/users`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/users` | 分页查询用户（参数：page、pageSize、name、phone、role、college、status） |
| GET | `/users/{id}` | 根据 ID 查询用户 |
| POST | `/users` | 新增用户（按角色名匹配 roleId，校验学号唯一） |
| POST | `/users/register` | 用户注册（**放行**，校验 roleId 与账号唯一） |
| POST | `/users/changePassword` | 修改密码（userId、oldPassword、newPassword） |
| PUT | `/users/{id}` | 更新用户 |
| DELETE | `/users/{id}` | 删除用户 |
| POST | `/users/upload-avatar` | 上传头像（表单参数：`file`、`userId`），返回头像访问路径 |

### 10.3 角色模块（RoleController，前缀 `/roles`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/roles` | 分页查询角色（**放行**；参数：page、pageSize、searchText） |
| GET | `/roles/{id}` | 根据 ID 查询角色 |
| POST | `/roles` | 新增角色 |
| PUT | `/roles/{id}` | 修改角色 |
| DELETE | `/roles/{id}` | 删除角色 |

### 10.4 项目模块（ProjectController，前缀 `/projects`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/projects` | 分页查询项目（projectName、projectType、status、projectSpace、applicantId） |
| GET | `/projects/{id}` | 项目详情（含成员、指导老师、获奖、计划书） |
| POST | `/projects` | 新增项目（自动填充申请人信息） |
| PUT | `/projects/{id}` | 更新项目 |
| DELETE | `/projects/{id}` | 删除项目 |
| POST | `/projects/{id}/submit` | 提交项目申报 |
| POST | `/projects/{id}/approve` | 审批通过（并向申请人推送通知） |
| POST | `/projects/{id}/reject` | 驳回（请求体 `{"reason":"..."}`，并推送通知） |
| POST | `/projects/upload-plan` | 上传计划书（多文件，表单参数：`files[]`、`projectId`） |
| DELETE | `/projects/plan/{id}` | 删除计划书（同时删文件与记录） |
| GET | `/projects/download-template` | 获取计划书模板路径 |
| GET | `/projects/my-projects` | 获取当前用户创建的项目（下拉选择用） |

### 10.5 获奖模块（AwardController，前缀 `/awards`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/awards` | 分页查询获奖记录（projectName、competitionName、competitionLevel、awardLevel、applicantId） |
| GET | `/awards/project/{projectId}` | 按项目 ID 查询获奖记录 |
| GET | `/awards/{id}` | 获奖记录详情 |
| POST | `/awards` | 新增获奖记录 |
| PUT | `/awards/{id}` | 更新获奖记录 |
| DELETE | `/awards/{id}` | 删除获奖记录 |
| POST | `/awards/{id}/approve` | 审批通过（推送通知） |
| POST | `/awards/{id}/reject` | 审批未通过（请求体 `{"reason":"..."}`，推送通知） |
| POST | `/awards/upload-certificate` | 上传获奖证明图片（表单参数：`file`） |

### 10.6 公告模块（NoticeController，前缀 `/notices`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/notices` | 分页查询公告（参数：title） |
| GET | `/notices/{id}` | 公告详情 |
| POST | `/notices` | 发布公告（**项目负责人不可发布**；发布后向其他用户推送通知） |
| DELETE | `/notices/{id}` | 删除公告（管理员可删全部，其他角色仅可删自己发布的） |

### 10.7 通知模块（NotificationController，前缀 `/notifications`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/notifications` | 当前用户通知列表（分页） |
| GET | `/notifications/unread-count` | 当前用户未读通知数量 |
| PUT | `/notifications/{id}/read` | 标记单条通知为已读 |
| PUT | `/notifications/read-all` | 标记当前用户全部通知为已读 |

通知类型 `type`：`project_approve`、`project_reject`、`award_approve`、`award_reject`、`notice_publish`。

---

## 十一、请求 / 响应示例

### 11.1 登录

**请求**

```http
POST /login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "username": "2021001",
  "password": "123456"
}
```

**成功响应**

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjMwLCJ1c2VybmFtZSI6IjIwMjEwMDEiLCJyb2xlIjoi5a6e5oi36LSf55CG5ZGYIn0.abc123",
    "id": 30,
    "username": "2021001",
    "name": "张三",
    "role": "项目负责人",
    "status": "已审批",
    "avatar": "/uploads/avatars/720e9aef-2c9d-409e-b8f7-96e7e5d4ca87.jpg"
  }
}
```

**失败响应（状态未审批）**

```json
{ "code": 0, "msg": "当前状态未审批，无法登录", "data": null }
```

### 11.2 分页查询项目列表

**请求**

```http
GET /projects?page=1&pageSize=10&status=approved HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 2,
    "list": [
      {
        "id": 1,
        "projectType": "创新训练",
        "projectName": "校园二手交易平台",
        "status": "approved",
        "applicantId": 30,
        "applicantName": "张三",
        "auditorName": "李管理员",
        "createTime": "2026-09-01 10:20:30",
        "submitTime": "2026-09-02 09:00:00"
      }
    ]
  }
}
```

### 11.3 新增项目

**请求**

```http
POST /projects HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
Content-Type: application/json

{
  "projectType": "创业训练",
  "projectName": "智能浇花系统",
  "projectIntro": "基于物联网的自动浇花方案",
  "innovationPoints": "低功耗 + AI 湿度预测",
  "members": [
    { "studentId": "2021002", "name": "李四", "gender": "男", "phone": "13800000000", "college": "信息工程学院", "className": "计科2101" }
  ],
  "advisors": [
    { "name": "王老师", "gender": "女", "phone": "13900000000", "college": "信息工程学院", "introduction": "研究方向：物联网" }
  ]
}
```

**响应**

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 5,
    "projectName": "智能浇花系统",
    "status": "draft",
    "applicantId": 30,
    "applicantName": "张三"
  }
}
```

### 11.4 上传头像

**请求（multipart/form-data）**

```http
POST /users/upload-avatar HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="userId"

30
------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="avatar.jpg"
Content-Type: image/jpeg

<binary>
------WebKitFormBoundary--
```

**响应**

```json
{
  "code": 1,
  "msg": "success",
  "data": "/uploads/avatars/9f1c2a3b-0000-1111-2222-333344445555.jpg"
}
```

> 前端展示时将该路径拼为 `/api/uploads/avatars/xxx.jpg`，经 Vite 代理转发至后端。

### 11.5 驳回项目（带原因）

**请求**

```http
POST /projects/5/reject HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
Content-Type: application/json

{ "reason": "计划书缺少市场调研数据" }
```

**响应**

```json
{ "code": 1, "msg": "success", "data": null }
```

---

## 十二、数据库设计

数据库：`bsgl`，字符集：`utf8mb4`，引擎：`InnoDB`。

| 表名 | 说明 | 关键字段 |
| --- | --- | --- |
| `user` | 用户表 | id、student_id、name、phone、role_id、password、role、college、avatar、status、create_time、login_time |
| `role` | 角色表 | id、role_id、role_name、description |
| `project` | 项目表 | id、project_type、project_name、status、applicant_id、auditor_name、reject_reason、plan_file_path、submit_time 等 |
| `project_member` | 项目成员表 | id、project_id、student_id、name、gender、phone、college、class_name |
| `project_advisor` | 项目指导老师表 | id、project_id、name、gender、phone、college、introduction |
| `project_plan` | 计划书表 | id、project_id、file_path、file_name、file_size |
| `award` | 获奖记录表 | id、project_id、project_name、competition_name、competition_level、award_level、award_certificate、status、reject_reason |
| `notice` | 公告表 | id、title、content、publisher_id、publisher_name、publisher_role、is_top、status |
| `notification` | 站内通知表 | id、user_id、title、content、type、related_id、is_read、create_time |

### 关键状态字典

- **用户状态 `user.status`**：`待审批` / `已审批` / `已停用`（仅「已审批」可登录）。
- **项目状态 `project.status`**：`draft`(草稿) / `submitted`(已申报) / `approved`(已审批) / `rejected`(已拒绝) / `settled`(入驻)。
- **获奖状态 `award.status`**：`pending`(未审批) / `approved`(通过) / `rejected`(未通过)。
- **公告状态 `notice.status`**：`normal`(正常)。
- **通知已读 `notification.is_read`**：`0`(未读) / `1`(已读)。

> 外键约束：`project_member`、`project_advisor`、`project_plan`、`award` 均以 `project_id` 关联 `project(id)`，并设置 `ON DELETE CASCADE`（删除项目时级联删除关联数据）。

---

## 十三、业务状态流转

```
项目：draft（新建/草稿）
        │ submit
        ▼
     submitted（已申报）
        │ approve                │ reject(reason)
        ▼                        ▼
    approved（已审批）        rejected（已拒绝）
        │
        ▼
     settled（入驻，可选）

获奖：pending ──approve──▶ approved
         └────reject(reason)──▶ rejected

用户：待审批 ──管理员审批──▶ 已审批（可登录） ──▶ 已停用
```

审批动作（项目 / 获奖）与公告发布均会通过 `NotificationService` 自动向相关用户推送站内通知。

---

## 十四、全局异常处理

`GlobalExceptionHandler`（`@ControllerAdvice`）统一处理异常，当前重点处理文件上传超限：

- `MaxUploadSizeExceededException` → 返回 `Result.error("文件大小超过限制，单个文件最大支持100MB，请压缩文件后重新上传")`。

---

## 十五、前后端联调说明

前端 `vue-project` 通过 Vite 代理访问后端：

```
前端请求  /api/xxx        （baseURL = /api）
   │  Vite proxy rewrite：去掉 /api 前缀
   ▼
后端接收  /xxx            （http://localhost:8080）
```

因此后端接口路径**不含** `/api` 前缀；静态资源访问路径为 `/api/uploads/...` → 实际后端 `/uploads/...`。

---

## 十六、部署（打包 jar + Nginx）

### 1. 打包

```bash
cd SpringBoot-project
./mvnw.cmd clean package -DskipTests     # Windows
# ./mvnw clean package -DskipTests       # Linux / macOS
```

打包产物位于 `target/SpringBoot-project-0.0.1-SNAPSHOT.jar`（可执行 fat jar）。

### 2. 运行 jar

```bash
java -jar target/SpringBoot-project-0.0.1-SNAPSHOT.jar \
  --spring.datasource.password=生产库密码 \
  --server.port=8080
```

> ⚠️ **上传目录**：jar 运行时 `System.getProperty("user.dir")` 为**启动 jar 时所在目录**，`uploads/` 会在该目录下创建/读取。建议使用固定目录启动，并将历史 `uploads/` 一并放置于该目录，避免头像/文件 404。可通过 `nohup` 或 systemd 守护：

```bash
# systemd 示例：/etc/systemd/system/bsgl.service
[Unit]
Description=创新创业项目管理系统后端
After=network.target mysql.service

[Service]
WorkingDirectory=/opt/bsgl          # user.dir，uploads 将位于 /opt/bsgl/uploads
ExecStart=/usr/bin/java -jar /opt/bsgl/SpringBoot-project-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

### 3. Nginx 反向代理（前后端合并部署）

将前端 `vue-project` 构建产物（`dist/`）与后端一并部署：

```nginx
server {
    listen       80;
    server_name  your-domain.com;

    # 前端静态资源
    location / {
        root   /var/www/vue-dist;
        index  index.html;
        try_files $uri $uri/ /index.html;   # 支持 Vue Router history 模式
    }

    # 后端 API：/api 前缀转发并去掉前缀
    location /api/ {
        proxy_pass         http://127.0.0.1:8080/;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        client_max_body_size 100m;          # 与后端上传限制保持一致
    }
}
```

> 说明：`proxy_pass` 结尾的 `/` 会去掉 `/api` 前缀，等价于前端 Vite 代理的 `rewrite` 行为；因此前端生产构建仍可使用 `/api` 作为 baseURL。

---

## 十七、Postman / Apifox 导入片段

将以下 JSON 保存为 `bsgl.postman_collection.json`，在 Postman（Import）或 Apifox（导入 → Postman）中打开即可。集合已配置 `baseUrl` 与 `token` 变量，并在集合级启用 Bearer 鉴权。

```json
{
  "info": {
    "name": "创新创业项目管理系统-后端API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "auth": {
    "type": "bearer",
    "bearer": [{ "key": "token", "value": "{{token}}", "type": "string" }]
  },
  "variable": [
    { "key": "baseUrl", "value": "http://localhost:8080" },
    { "key": "token", "value": "" }
  ],
  "item": [
    {
      "name": "登录",
      "event": [{
        "listen": "test",
        "script": { "exec": [
          "const r = pm.response.json();",
          "if (r.code === 1 && r.data && r.data.token) {",
          "  pm.collectionVariables.set('token', r.data.token);",
          "}"
        ] }
      }],
      "request": {
        "auth": { "type": "noauth" },
        "method": "POST",
        "header": [{ "key": "Content-Type", "value": "application/json" }],
        "body": { "mode": "raw", "raw": "{\n  \"username\": \"2021001\",\n  \"password\": \"123456\"\n}" },
        "url": { "raw": "{{baseUrl}}/login", "host": ["{{baseUrl}}"], "path": ["login"] }
      }
    },
    {
      "name": "项目-分页列表",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{baseUrl}}/projects?page=1&pageSize=10",
          "host": ["{{baseUrl}}"], "path": ["projects"],
          "query": [{ "key": "page", "value": "1" }, { "key": "pageSize", "value": "10" }]
        }
      }
    },
    {
      "name": "项目-详情",
      "request": {
        "method": "GET",
        "url": { "raw": "{{baseUrl}}/projects/1", "host": ["{{baseUrl}}"], "path": ["projects", "1"] }
      }
    },
    {
      "name": "项目-审批通过",
      "request": {
        "method": "POST",
        "url": { "raw": "{{baseUrl}}/projects/1/approve", "host": ["{{baseUrl}}"], "path": ["projects", "1", "approve"] }
      }
    },
    {
      "name": "用户-上传头像",
      "request": {
        "method": "POST",
        "body": {
          "mode": "formdata",
          "formdata": [
            { "key": "userId", "value": "30", "type": "text" },
            { "key": "file", "type": "file", "src": [] }
          ]
        },
        "url": { "raw": "{{baseUrl}}/users/upload-avatar", "host": ["{{baseUrl}}"], "path": ["users", "upload-avatar"] }
      }
    },
    {
      "name": "通知-未读数量",
      "request": {
        "method": "GET",
        "url": { "raw": "{{baseUrl}}/notifications/unread-count", "host": ["{{baseUrl}}"], "path": ["notifications", "unread-count"] }
      }
    }
  ]
}
```

> 使用步骤：先运行「登录」请求（测试脚本会自动把返回的 `token` 写入集合变量），随后其他需要鉴权的接口即可直接调用。可在此基础上按需补齐其余接口。

---

## 十八、常见问题（FAQ）

**Q1：上传的头像 / 计划书 / 获奖证明在页面无法显示（404）？**
- 确认后端启动的**工作目录**为 `SpringBoot-project`（或 jar 启动目录下存在 `uploads/`）；
- 确认 `WebConfig.addResourceHandlers` 的资源映射路径能被正确解析（建议使用基于 `user.dir` 的**绝对路径** `file:` URL）；
- 确认前端访问路径带 `/api` 前缀并经代理转发到后端。

**Q2：登录返回「当前状态未审批，无法登录」？**
- 该用户 `status` 不是「已审批」，需由管理员在用户管理中审批通过后再登录。

**Q3：接口返回 401？**
- Token 缺失、格式错误（需 `Bearer <token>`）或已过期（有效期 7 天），请重新登录获取。

**Q4：注册报「角色不存在」？**
- `role` 表中无对应 `roleId`，请先初始化角色数据。

**Q5：控制台看不到 SQL？**
- 已开启 `mybatis.configuration.log-impl=StdOutImpl`，SQL 会打印到标准输出；如未看到请确认日志级别与启动方式。

**Q6：上传大文件报「文件大小超过限制」？**
- 单文件上限 100MB，由 `spring.servlet.multipart.max-file-size` 控制；经 Nginx 部署时还需同步调整 `client_max_body_size`。

---

_文档结束。如接口或表结构发生变更，请同步更新本文件。_
