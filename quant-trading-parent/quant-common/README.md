# quant-common - 公共组件模块

公共组件模块，为所有微服务提供基础架构支持。

## 模块职责

- 统一响应格式 (`Result<T>`)
- 全局异常处理
- 公共常量定义
- HTTP 工具类

## 核心类

| 类 | 说明 |
|----|------|
| `Result<T>` | 统一 API 响应包装类 |
| `ResultCode` | 响应码枚举 |
| `GlobalExceptionHandler` | Spring MVC 全局异常处理器 |
| `BusinessException` | 业务异常类 |
| `DateUtils` | 日期工具类 |
| `HttpUtils` | HTTP 请求工具类 |

## 使用方式

### 统一响应

```java
@RestController
public class ExampleController {

    @GetMapping("/example")
    public Result<String> example() {
        return Result.success("data");
    }

    @GetMapping("/error")
    public Result<Void> error() {
        return Result.fail(ResultCode.PARAM_INVALID);
    }
}
```

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

## 依赖

无外部依赖，仅使用 JDK 标准库和 Spring Framework。
