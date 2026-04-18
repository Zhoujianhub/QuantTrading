# 开发指南

## 环境准备

### 必要工具

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java运行环境 |
| Maven | 3.8+ | 项目构建工具 |
| Python | 3.10+ | AI组件运行环境 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 缓存/消息队列 |
| IntelliJ IDEA | 2023+ | Java IDE |
| VS Code | - | Python IDE |

### 环境变量配置

```bash
# Java
export JAVA_HOME=/path/to/jdk-17

# Python
export PATH="/path/to/python3.10:$PATH"

# Maven (可选)
export MAVEN_HOME=/path/to/maven
export PATH="$MAVEN_HOME/bin:$PATH"
```

## 项目导入

### Java 微服务 (IntelliJ IDEA)

1. File -> Open -> 选择 `quant-trading-parent/pom.xml`
2. IDEA 会自动识别为 Maven 项目
3. 等待索引完成（约2-3分钟）

### Python AI 组件 (VS Code)

1. 打开 `quant-trading-parent/quant-analysis/executor` 目录
2. 创建虚拟环境: `python -m venv venv`
3. 安装依赖: `pip install -r requirements.txt`
4. 选择 Python 解释器为 venv 中的 Python

## 项目结构

```
quant-trading-parent/
├── quant-common/           # 公共模块（无 main 方法）
├── quant-account/          # 账户模块
├── quant-position/         # 持仓模块
├── quant-trade/            # 交易模块
├── quant-market/           # 市场模块
├── quant-monitor/         # 监控模块
├── quant-notification/     # 通知模块
├── quant-analysis/        # 分析模块（包含 Python executor）
└── Dockerfile
```

## 模块开发

### 添加新模块

1. 在 `quant-trading-parent/` 下创建新模块目录
2. 创建 `pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <parent>
        <artifactId>quant-trading-parent</artifactId>
        <groupId>com.quant</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>quant-new-module</artifactId>
</project>
```

3. 在父 `pom.xml` 的 `<modules>` 中添加新模块
4. 创建标准包结构:
```
quant-new-module/
├── src/main/java/com/quant/newmodule/
│   ├── NewModuleApplication.java
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
└── src/main/resources/
    ├── application.yml
    └── sql/
```

### 创建 Entity

```java
package com.quant.position.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("holding")
public class Holding {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountId;

    private String assetCode;

    private String assetName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdatedAt;
}
```

### 创建 Repository

```java
package com.quant.position.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quant.position.entity.Holding;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HoldingRepository extends BaseMapper<Holding> {
}
```

### 创建 Service

```java
package com.quant.position.service;

import com.quant.position.entity.Holding;
import java.util.List;

public interface HoldingService {
    List<Holding> getHoldingsByAccount(String accountId);
    Holding createHolding(Holding holding);
}
```

### 创建 Controller

```java
package com.quant.position.controller;

import com.quant.common.result.Result;
import com.quant.position.dto.request.HoldingAddRequest;
import com.quant.position.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping("/{accountId}")
    public Result<List<Holding>> getHoldings(@PathVariable String accountId) {
        return Result.success(holdingService.getHoldingsByAccount(accountId));
    }

    @PostMapping
    public Result<Holding> createHolding(@RequestBody HoldingAddRequest request) {
        return Result.success(holdingService.createHolding(request.toEntity()));
    }
}
```

## Python AI 开发

### Agent 开发

新增 Agent 只需继承 `Agent` 基类:

```python
# agents/my_analyst.py
from typing import Dict, Any

class MyAnalyst:
    def __init__(self, llm):
        self.llm = llm

    def analyze(self, state: Dict[str, Any]) -> Dict[str, Any]:
        stock_code = state.get('stock_code')
        prompt = f"分析 {stock_code} 的自定义维度..."
        result = self.llm.invoke(prompt)
        return {
            'my_report': result,
            'my_score': 85
        }
```

### 注册新 Agent

在 `agent_executor.py` 中注册:

```python
def _init_agents(self) -> Dict[str, Any]:
    return {
        # ... 其他 agents
        'my_agent': MyAnalyst(self.llm)
    }

def _my_agent_node(self, state, progress_cb=None):
    agent = self.agents['my_agent']
    result = agent.analyze(state)
    state.update(result)
    return state
```

## 数据库开发

### 创建表

在 `src/main/resources/sql/init.sql` 中添加:

```sql
CREATE TABLE IF NOT EXISTS `new_table` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 运行 SQL

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/init.sql
```

## 测试

### Java 单元测试

```bash
cd quant-trading-parent
mvn test -pl quant-position
```

### Python 单元测试

```bash
cd quant-trading-parent/quant-analysis/executor
pytest tests/ -v
```

### 集成测试

```bash
# 启动本地 MySQL 和 Redis
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8
docker run -d -p 6379:6379 redis:6

# 运行集成测试
mvn verify -P integration
```

## 代码规范

### Java

- 遵循 Java Coding Standards
- 使用 Lombok 简化代码
- 所有接口返回 `Result<T>` 包装
- 异常通过 `GlobalExceptionHandler` 统一处理

### Python

- 遵循 Python Coding Standards
- 使用类型提示 (Type Hints)
- 使用 `loguru` 进行日志记录
- 所有配置通过环境变量或 config 文件管理

### Git 提交规范

```
feat: 添加新功能
fix: 修复bug
refactor: 重构
docs: 文档更新
test: 测试相关
chore: 构建/工具变更
```

示例:
```
feat(position): 添加持仓成本价计算

添加持仓成本价自动计算功能，支持:
- 按买入均价计算
- 按定额投入计算

Closes #123
```
