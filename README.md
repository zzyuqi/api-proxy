# API Relay System (API中转代理系统)

一个通用的API中转代理系统，支持多模型配置、API Key管理、请求限流、日志记录等功能。

## 功能特性

- **API代理转发**: 支持将请求中转到任意目标API
- **多模型支持**: 支持配置多个AI模型提供商（OpenAI、Anthropic、Azure、Minimax等）
- **API Key管理**: 提供API Key的创建、启用/禁用功能
- **请求限流**: 支持按路由或API Key进行限流
- **请求日志**: 完整的请求日志记录和查询功能
- **管理后台**: Vue.js构建的Web管理界面
- **Swagger文档**: 内置API文档界面

## 系统架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────▶│   Nginx     │────▶│  Backend    │
│             │     │  (Frontend) │     │  (Spring)   │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │    MySQL    │
                                        │  (Storage)  │
                                        └─────────────┘
```

## 技术栈

- **后端**: Java 21 + Spring Boot 3.4 + Spring Data JPA + Spring Security
- **前端**: Vue 3 + Vite
- **数据库**: MySQL 8.0
- **容器**: Docker + Docker Compose

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0 (如不使用Docker)

### 本地开发运行

#### 1. 创建本地数据库

**方式一：使用H2内存数据库（默认，无需配置）**

直接运行即可使用H2内存数据库，适合开发测试。

```bash
# 使用Maven运行
mvn spring-boot:run
```

**方式二：使用MySQL本地数据库**

1. 创建MySQL数据库：

```sql
CREATE DATABASE api_proxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `src/main/resources/application.properties`：

```properties
spring.profiles.active=dev
spring.datasource.url=jdbc:mysql://localhost:3306/api_proxy?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

3. 初始化数据库表结构（可选，JPA会自动创建）：

```bash
mysql -u root -p api_proxy < init.sql
```

#### 2. 构建并运行

```bash
# 编译项目
mvn clean package -DskipTests

# 运行
java -jar target/api-proxy-1.0.0.jar
```

或使用快捷脚本：

```bash
start.bat
```

#### 3. 访问服务

| 服务 | 地址 |
|------|------|
| 管理后台 | http://localhost:8080/api/admin |
| Swagger文档 | http://localhost:8080/swagger-ui.html |
| H2控制台 | http://localhost:8080/h2-console |

> **首次使用**：首次启动后，需要调用 `POST /api/admin/init` 接口创建管理员账号。

---

## Docker部署

### 使用Docker Compose一键部署

```bash
# 进入deploy目录
cd deploy

# 构建并启动所有服务
docker-compose up -d

# 查看运行状态
docker-compose ps
```

这将启动以下服务：

| 容器名 | 端口 | 说明 |
|--------|------|------|
| api-proxy-mysql | 3311 | MySQL数据库 |
| api-proxy | 1100 | 后端API服务 |
| api-proxy-web | 1101 | 前端管理界面 |

### 访问地址

| 服务 | 地址 |
|------|------|
| 管理后台 | http://localhost:1101 |
| 后端API | http://localhost:1100 |
| Swagger文档 | http://localhost:1100/swagger-ui.html |
| MySQL | localhost:3311 |

---

## 数据库配置说明

### MySQL数据库创建与修改

#### 1. 手动创建数据库

```sql
-- 连接MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE api_proxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE api_proxy;

-- 创建表（可选，JPA会自动创建）
-- source init.sql
```

#### 2. 修改数据库连接

**Docker部署环境变量修改**

编辑 `deploy/docker-compose.yml` 中的环境变量：

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/api_proxy?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
  - SPRING_DATASOURCE_USERNAME=root
  - SPRING_DATASOURCE_PASSWORD=your_new_password
```

修改后重启服务：

```bash
docker-compose down
docker-compose up -d
```

**本地开发环境修改**

编辑 `src/main/resources/application.properties`：

```properties
# 开发环境配置
spring.profiles.active=dev
spring.datasource.url=jdbc:mysql://localhost:3306/api_proxy?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password

# 生产环境配置
spring.profiles.active=prod
spring.datasource.url=jdbc:mysql://mysql:3306/api_proxy?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

#### 3. 数据库表结构

系统使用以下主要表：

| 表名 | 说明 |
|------|------|
| route | 路由规则表 |
| api_key | API密钥表 |
| request_log | 请求日志表 |
| model_config | 模型配置表 |
| admin_user | 管理员用户表 |
| proxy_user | 代理用户表 |
| token_record | Token记录表 |

详细表结构请参考 `init.sql` 文件。

#### 4. 常见数据库操作

```sql
-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESC route;

-- 查看请求日志
SELECT * FROM request_log ORDER BY created_at DESC LIMIT 100;

-- 查看API Keys
SELECT * FROM api_key;

-- 重置管理员密码
UPDATE admin_user SET password='$2a$10$...' WHERE username='admin';
```

---

## 环境变量说明

### 后端环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | 激活的配置文件 | prod |
| SERVER_PORT | 服务端口 | 1100 |
| SPRING_DATASOURCE_URL | 数据库连接URL | - |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | root |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | 123456 |
| PROXY_ADMIN_USERNAME | 管理员用户名 | admin |
| PROXY_ADMIN_PASSWORD | 管理员密码 | admin123 |

---

## API接口说明

### 管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/admin/init | 初始化管理员账号 |
| GET | /api/admin/stats | 获取统计信息 |
| POST | /api/routes | 创建路由规则 |
| GET | /api/routes | 获取路由列表 |
| PUT | /api/routes/{id} | 更新路由规则 |
| DELETE | /api/routes/{id} | 删除路由规则 |
| POST | /api/keys | 创建API Key |
| GET | /api/keys | 获取API Key列表 |
| DELETE | /api/keys/{id} | 删除API Key |
| GET | /api/logs | 获取请求日志 |
| POST | /api/models | 创建模型配置 |
| GET | /api/models | 获取模型列表 |

### 代理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| * | /api/proxy/** | 代理请求 |

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/refresh | 刷新Token |

---

## 前端管理界面

### 构建前端

```bash
cd admin-ui
npm install
npm run build
```

构建产物在 `admin-ui/dist` 目录。

### Nginx配置说明

前端默认通过Nginx提供服务，反向代理到后端API：

```nginx
location /api/ {
    proxy_pass http://api-proxy:1100/api/;
}
```

---

## 安全注意事项

1. **修改默认密码**: 生产环境请务必修改默认管理员密码
2. **API Key保护**: 不要在前端暴露敏感配置
3. **数据库备份**: 定期备份MySQL数据
4. **HTTPS**: 生产环境建议配置HTTPS
5. **限流配置**: 根据实际需求合理配置限流规则

---

## 常见问题

### Q: 启动失败，提示数据库连接错误？

检查MySQL是否正常运行，数据库是否创建，连接凭据是否正确。

### Q: 前端页面无法访问？

检查Nginx容器是否正常运行，确认 `admin-ui/dist` 目录存在且有内容。

### Q: Swagger文档无法加载？

检查后端服务是否正常运行，确认 `springdoc.openapi.enabled=true`。

### Q: 如何查看日志？

```bash
# Docker环境
docker logs api-proxy

# 本地环境
tail -f app.log
```

---

## 项目结构

```
api-proxy/
├── src/
│   └── main/
│       ├── java/com/zhongzhuan/proxy/
│       │   ├── controller/    # 控制器
│       │   ├── service/       # 业务服务
│       │   ├── repository/    # 数据访问
│       │   ├── model/        # 实体类
│       │   ├── dto/          # 数据传输对象
│       │   ├── config/       # 配置类
│       │   └── filter/       # 过滤器
│       └── resources/
│           └── application.properties
├── admin-ui/                 # Vue前端
├── deploy/                   # Docker部署文件
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── nginx.conf
├── init.sql                  # 数据库初始化脚本
├── pom.xml                   # Maven配置
└── README.md
```

---

## License

MIT
