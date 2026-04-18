# 部署指南

## 环境要求

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4核 | 8核+ |
| 内存 | 8GB | 16GB+ |
| 磁盘 | 100GB | 200GB+ SSD |
| MySQL | 8.0 | 8.0+ |
| Redis | 6.0 | 6.0+ |

## Docker 部署

### 构建镜像

```bash
cd quant-trading-parent

# 构建 Java 微服务镜像
mvn clean package -DskipTests
docker build -t quant-trading:1.0.0 .

# 或使用构建脚本
./build.sh
```

### 启动服务

```bash
# 启动基础组件 (MySQL, Redis)
docker-compose up -d mysql redis

# 启动应用
docker-compose up -d
```

### docker-compose.yml 示例

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: quant_trading
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:6
    ports:
      - "6379:6379"

  quant-position:
    image: quant-trading:1.0.0
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/quant_trading
      SPRING_REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

## Kubernetes 部署

### Deployment 示例

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quant-position
spec:
  replicas: 2
  selector:
    matchLabels:
      app: quant-position
  template:
    metadata:
      labels:
        app: quant-position
    spec:
      containers:
        - name: quant-position
          image: quant-trading:1.0.0
          ports:
            - containerPort: 8082
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                secretKeyRef:
                  name: quant-secrets
                  key: database-url
```

## 环境配置

### 生产环境配置

创建 `application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/quant_trading?useSSL=true
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: ${REDIS_HOST}
    port: 6379
    password: ${REDIS_PASSWORD}

server:
  port: 8082

logging:
  level:
    root: INFO
    com.quant: DEBUG
```

### 外部 API 配置

```yaml
# 新浪行情
sina:
  quote:
    base-url: https://hq.sinajs.cn

# 东方财富
eastmoney:
  base-url: https://push2.eastmoney.com
```

## 监控配置

### 监控端点

确保以下端点可用:

- `/actuator/health` - 健康检查
- `/actuator/prometheus` - Prometheus 指标
- `/swagger-ui.html` - API 文档

### XXL-Job 调度器配置

```yaml
xxl:
  job:
    admin:
      addresses: http://xxl-job-admin:8080/xxl-job-admin
    executor:
      app-name: quant-position
      port: 9999
      log-path: /tmp/xxl-job/jobhandler
```

## AI 服务部署

### Python 环境

```bash
# 创建虚拟环境
python3.10 -m venv /opt/quant-venv

# 激活环境
source /opt/quant-venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 启动 AI Executor

```bash
# 设置环境变量
export MOCK_MODE=false
export LLM_PROVIDER=minimax
export API_KEY=your_api_key

# 启动服务
python server.py
```

### Supervisor 配置示例

```ini
[program:quant-executor]
command=/opt/quant-venv/bin/python /opt/quant-executor/server.py
directory=/opt/quant-executor
user=quant
autostart=true
autorestart=true
stdout_logfile=/var/log/quant-executor.log
stderr_logfile=/var/log/quant-executor-error.log
```

## 数据库初始化

### 执行初始化脚本

```bash
# 登录 MySQL
mysql -h ${DB_HOST} -u root -p

# 选择数据库
USE quant_trading;

# 执行初始化脚本
SOURCE src/main/resources/sql/init.sql;
```

### 数据迁移

```bash
# 使用 Maven Flyway 插件
mvn flyway:migrate -pl quant-position
```

## 健康检查

### API 健康检查

```bash
curl http://localhost:8082/actuator/health
```

响应示例:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

### AI Executor 健康检查

```bash
curl http://localhost:5000/health
```

## 日志管理

### 日志配置

```yaml
logging:
  file:
    name: /var/log/quant/quant-position.log
    max-size: 100MB
    max-history: 30
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 日志聚合

推荐使用 ELK Stack:
- Elasticsearch - 日志存储
- Logstash - 日志收集
- Kibana - 日志可视化

## 备份策略

### 数据库备份

```bash
# 全量备份
mysqldump -h ${DB_HOST} -u root -p quant_trading > backup_$(date +%Y%m%d).sql

# 增量备份 (使用 binlog)
mysqlbinlog --raw --read-from-remote-server --host=${DB_HOST} --result-file=binlog_ backup.000001
```

### Redis 备份

```bash
# RDB 持久化备份
redis-cli BGSAVE
redis-cli -r 1 COPYRARE /var/lib/redis/dump.rdb
```

## 故障排查

### 常见问题

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 服务启动失败 | 端口占用 | 检查端口: `netstat -tlnp \| grep 8082` |
| 数据库连接失败 | 密码错误/网络不通 | 检查配置和网络连通性 |
| AI 分析超时 | LLM API 限流 | 增加超时时间或切换 API |
| 内存溢出 | 堆内存不足 | 调整 JVM 参数: `-Xmx4g` |

### 查看日志

```bash
# Java 服务日志
tail -f /var/log/quant/quant-position.log

# Python 服务日志
tail -f /var/log/quant-executor.log

# Docker 日志
docker logs -f quant-position
```
