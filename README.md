# API Relay System (API中转代理系统)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)

---

## Languages / 语言

[English](#english-version) | [中文](#中文版本)

---

## English Version

### Features

- **API Proxy Forwarding**: Forward requests to any target API
- **Multi-Model Support**: Configure multiple AI providers (OpenAI, Anthropic, Azure, Minimax, etc.)
- **API Key Management**: Create, enable/disable API keys
- **Request Rate Limiting**: Route-based or API Key-based rate limiting
- **User Rate Limiting**: Per-user concurrent and hourly request limits
- **Request Logging**: Complete request logging and query functionality
- **User Management**: Registration, login, account expiration (30 days), idle timeout (1 hour auto-logout)
- **Login Security**: Account locked for 5 minutes after 5 failed login attempts
- **Auto Cleanup**: Scheduled task to delete expired user accounts
- **Admin Dashboard**: Vue.js-based web management interface
- **Swagger Documentation**: Built-in API documentation

### System Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────▶│   Nginx     │────▶│  Backend    │
│             │     │  (Frontend) │     │  (Spring)   │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │    MySQL    │
                                        └─────────────┘
```

### Tech Stack

- **Backend**: Java 21 + Spring Boot 3.4 + Spring Data JPA + Spring Security
- **Frontend**: Vue 3 + Vite
- **Database**: MySQL 8.0
- **Container**: Docker + Docker Compose

### Quick Start

#### Prerequisites

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0 (if not using Docker)

#### 1. Create Local Database

**Option 1: H2 In-Memory Database (default, no config needed)**

```bash
mvn spring-boot:run
```

**Option 2: MySQL Local Database**

```sql
CREATE DATABASE api_proxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update `src/main/resources/application.properties`:

```properties
spring.profiles.active=dev
spring.datasource.url=jdbc:mysql://localhost:3306/api_proxy?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

#### 2. Build and Run

```bash
mvn clean package -DskipTests
java -jar target/api-proxy-1.0.0.jar
```

#### 3. Access Services

| Service | URL |
|---------|-----|
| Admin Dashboard | http://localhost:8080/api/admin |
| Swagger Docs | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

> **First Use**: Call `POST /api/admin/init` to create the admin account after first startup.

### Docker Deployment

```bash
cd deploy
docker-compose up -d
docker-compose ps
```

| Container | Port | Description |
|-----------|------|-------------|
| api-proxy-mysql | 3311 | MySQL Database |
| api-proxy | 1100 | Backend API |
| api-proxy-web | 1101 | Frontend |

### API Endpoints

#### Admin APIs

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/admin/init | Initialize admin account |
| GET | /api/admin/stats | Get statistics |
| POST | /api/routes | Create route |
| GET | /api/routes | List routes |
| PUT | /api/routes/{id} | Update route |
| DELETE | /api/routes/{id} | Delete route |
| POST | /api/keys | Create API Key |
| GET | /api/keys | List API Keys |
| DELETE | /api/keys/{id} | Delete API Key |
| GET | /api/logs | Query logs |

#### Proxy APIs

| Method | Path | Description |
|--------|------|-------------|
| * | /api/proxy/** | Proxy requests |

#### Auth APIs

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register | User registration |
| POST | /api/auth/login | User login |
| GET | /api/auth/me | Get current user info |

> **Token Lifetime**: User tokens are valid for 1 hour. Accounts expire after 30 days of inactivity.

### Security Notes

1. Change default passwords in production
2. Protect API keys - don't expose sensitive config
3. Backup database regularly
4. Use HTTPS in production
5. Configure rate limits appropriately
6. User quotas can be set per user
7. Login locked after 5 failed attempts

### Project Structure

```
api-proxy/
├── src/main/java/com/zhongzhuan/proxy/
│   ├── controller/    # Controllers
│   ├── service/       # Business services
│   ├── repository/    # Data access
│   ├── model/         # Entities
│   ├── dto/           # Data transfer objects
│   ├── config/        # Configuration
│   └── filter/        # Filters
├── admin-ui/          # Vue frontend
├── deploy/            # Docker deployment
├── init.sql           # Database init script
└── pom.xml
```

---

## 中文版本

### 功能特性

- **API代理转发**: 支持将请求中转到任意目标API
- **多模型支持**: 支持配置多个AI模型提供商（OpenAI、Anthropic、Azure、Minimax等）
- **API Key管理**: 提供API Key的创建、启用/禁用功能
- **请求限流**: 支持按路由或API Key进行限流
- **用户限流**: 支持每个用户独立的并发限制和每小时请求限制
- **请求日志**: 完整的请求日志记录和查询功能
- **用户管理**: 支持用户注册、登录、账号过期（30天）、空闲超时（1小时自动登出）
- **登录安全**: 登录失败5次后锁定5分钟，防止暴力破解
- **自动清理**: 定时清理过期用户账号
- **管理后台**: Vue.js构建的Web管理界面
- **Swagger文档**: 内置API文档界面

### 系统架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   客户端     │────▶│   Nginx     │────▶│  后端服务    │
│             │     │  (前端)      │     │  (Spring)   │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │    MySQL    │
                                        └─────────────┘
```

### 技术栈

- **后端**: Java 21 + Spring Boot 3.4 + Spring Data JPA + Spring Security
- **前端**: Vue 3 + Vite
- **数据库**: MySQL 8.0
- **容器**: Docker + Docker Compose

### 快速开始

#### 环境要求

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0 (如不使用Docker)

#### 1. 创建本地数据库

**方式一：使用H2内存数据库（默认，无需配置）**

```bash
mvn spring-boot:run
```

**方式二：使用MySQL本地数据库**

```sql
CREATE DATABASE api_proxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.properties`：

```properties
spring.profiles.active=dev
spring.datasource.url=jdbc:mysql://localhost:3306/api_proxy?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

#### 2. 构建并运行

```bash
mvn clean package -DskipTests
java -jar target/api-proxy-1.0.0.jar
```

#### 3. 访问服务

| 服务 | 地址 |
|------|------|
| 管理后台 | http://localhost:8080/api/admin |
| Swagger文档 | http://localhost:8080/swagger-ui.html |
| H2控制台 | http://localhost:8080/h2-console |

> **首次使用**：首次启动后，需要调用 `POST /api/admin/init` 接口创建管理员账号。

### Docker部署

```bash
cd deploy
docker-compose up -d
docker-compose ps
```

| 容器名 | 端口 | 说明 |
|--------|------|------|
| api-proxy-mysql | 3311 | MySQL数据库 |
| api-proxy | 1100 | 后端API服务 |
| api-proxy-web | 1101 | 前端管理界面 |

### API接口说明

#### 管理接口

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

#### 代理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| * | /api/proxy/** | 代理请求 |

#### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/me | 获取当前用户信息 |

> **用户Token有效期**: 用户登录后Token有效期为1小时，超时需重新登录。账号30天不使用将自动过期删除。

### 安全注意事项

1. **修改默认密码**: 生产环境请务必修改默认管理员密码
2. **API Key保护**: 不要在前端暴露敏感配置
3. **数据库备份**: 定期备份MySQL数据
4. **HTTPS**: 生产环境建议配置HTTPS
5. **限流配置**: 根据实际需求合理配置限流规则
6. **用户配额**: 可为用户设置请求配额，超出后需管理员分配
7. **登录锁定**: 连续5次登录失败后账号锁定5分钟

### 项目结构

```
api-proxy/
├── src/main/java/com/zhongzhuan/proxy/
│   ├── controller/    # 控制器
│   ├── service/       # 业务服务
│   ├── repository/    # 数据访问
│   ├── model/        # 实体类
│   ├── dto/          # 数据传输对象
│   ├── config/       # 配置类
│   └── filter/       # 过滤器
├── admin-ui/          # Vue前端
├── deploy/            # Docker部署文件
├── init.sql           # 数据库初始化脚本
└── pom.xml
```

---

## License

MIT
