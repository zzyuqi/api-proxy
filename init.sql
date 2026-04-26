-- API 中转系统 - 数据库初始化脚本
-- 适用于 MySQL/PostgreSQL
-- 注：使用 JPA ddl-auto=update 会自动建表，此脚本仅供手动部署时使用

CREATE DATABASE IF NOT EXISTS api_proxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE api_proxy;

-- 路由规则表
CREATE TABLE IF NOT EXISTS route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '路由名称',
    path_pattern VARCHAR(255) NOT NULL COMMENT '路径匹配模式，如 /api/proxy/gpt/**',
    target_url VARCHAR(500) NOT NULL COMMENT '目标地址，如 https://api.openai.com',
    method VARCHAR(10) DEFAULT NULL COMMENT '限制HTTP方法，NULL为不限制',
    requires_auth TINYINT(1) DEFAULT 0 COMMENT '是否需要API Key认证',
    rate_limit INT DEFAULT 0 COMMENT '每分钟最大请求数，0为不限制',
    cache_ttl INT DEFAULT 0 COMMENT '缓存时间(秒)，0为不缓存',
    allowed_models VARCHAR(500) DEFAULT NULL COMMENT '允许的模型，逗号分隔',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT 'ENABLED / DISABLED',
    user_visible TINYINT(1) DEFAULT 1 COMMENT '是否对用户可见',
    display_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_user_visible (user_visible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路由规则';

-- API密钥表
CREATE TABLE IF NOT EXISTS api_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_value VARCHAR(64) NOT NULL UNIQUE COMMENT 'API Key值',
    name VARCHAR(100) NOT NULL COMMENT '备注名称',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_key_value (key_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API密钥';

-- 请求日志表
CREATE TABLE IF NOT EXISTS request_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT DEFAULT NULL COMMENT '关联路由ID',
    api_key_id BIGINT DEFAULT NULL COMMENT '关联API Key ID',
    request_path VARCHAR(500) DEFAULT NULL COMMENT '请求路径',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    response_status INT DEFAULT NULL COMMENT '响应状态码',
    response_time_ms BIGINT DEFAULT NULL COMMENT '响应耗时(毫秒)',
    client_ip VARCHAR(45) DEFAULT NULL COMMENT '客户端IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at),
    INDEX idx_route_id (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志';

-- 模型配置表
CREATE TABLE IF NOT EXISTS model_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    provider VARCHAR(50) NOT NULL COMMENT '提供商如minimax/openai/anthropic/azure/custom',
    api_url VARCHAR(500) NOT NULL COMMENT 'API地址',
    api_key VARCHAR(255) NOT NULL COMMENT 'API密钥',
    default_model_id VARCHAR(100) DEFAULT NULL COMMENT '默认模型ID',
    models VARCHAR(500) DEFAULT NULL COMMENT '可用模型列表，逗号分隔',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    display_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置';

-- 管理员表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    role VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员';
