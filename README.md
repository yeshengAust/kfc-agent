# 肯德基券文案修改系统

基于 Langchain4j 和千文模型的肯德基兑换券文案自动修改系统，支持自然语言操作指令。

## 功能特性

1. **自然语言操作指令**
   - 支持自然语言描述操作，无需固定格式
   - 例如："删除黄金脆皮鸡"、"将黄金脆皮鸡替换为避风塘黄金脆皮鸡"

2. **智能理解操作**
   - 自动识别删除和替换操作
   - 自动识别产品名称和相关规则
   - 自动更新替换规则中的产品名称

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- 千文模型API密钥

### 2. 配置

编辑 `src/main/resources/application.properties`：

```properties
# 千文模型配置
qwen.api.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1
qwen.api.key=your-api-key-here
qwen.api.model-name=qwen-plus
qwen.api.temperature=0.1
```

### 3. 运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/kfc-aigc-0.0.1-SNAPSHOT.jar
```

### 4. API使用

#### 删除操作示例

**请求：**
```bash
curl -X POST http://localhost:8080/api/coupon/modify \
  -H "Content-Type: application/json" \
  -d '{
    "originalText": "原始券文案...",
    "operation": "删除黄金脆皮鸡"
  }'
```

**响应：**
```json
{
  "success": true,
  "couponText": "修改后的券文案...",
  "scene": "删除黄金脆皮鸡",
  "subScene": null
}
```

#### 替换操作示例

**请求：**
```bash
curl -X POST http://localhost:8080/api/coupon/modify \
  -H "Content-Type: application/json" \
  -d '{
    "originalText": "原始券文案...",
    "operation": "将黄金脆皮鸡替换为避风塘黄金脆皮鸡"
  }'
```

**响应：**
```json
{
  "success": true,
  "couponText": "修改后的券文案...",
  "scene": "将黄金脆皮鸡替换为避风塘黄金脆皮鸡",
  "subScene": null
}
```

#### 健康检查

```bash
curl http://localhost:8080/api/coupon/health
```

## 请求参数说明

### CouponGenerationRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| originalText | String | 是 | 原始券文案 |
| operation | String | 是 | 自然语言操作指令，例如："删除黄金脆皮鸡"、"将黄金脆皮鸡替换为避风塘黄金脆皮鸡" |

## 操作指令示例

### 删除操作
- "删除黄金脆皮鸡"
- "移除黄金脆皮鸡（1块装）"
- "删除黄金脆皮鸡及其相关规则"

### 替换操作
- "将黄金脆皮鸡替换为避风塘黄金脆皮鸡"
- "黄金脆皮鸡改为避风塘黄金脆皮鸡"
- "把黄金脆皮鸡改成避风塘黄金脆皮鸡"

## 项目结构

```
kfc-aigc/
├── src/
│   ├── main/
│   │   ├── java/com/yes/kfcaigc/
│   │   │   ├── config/
│   │   │   │   └── QwenModelConfig.java          # 千文模型配置
│   │   │   ├── controller/
│   │   │   │   └── CouponGenerationController.java  # REST API控制器
│   │   │   ├── model/
│   │   │   │   ├── CouponGenerationRequest.java  # 请求模型
│   │   │   │   └── CouponGenerationResponse.java # 响应模型
│   │   │   ├── service/
│   │   │   │   └── CouponModificationService.java  # 文案修改服务
│   │   │   └── KfcAigcApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/yes/kfcaigc/service/
│           └── CouponModificationServiceTest.java
└── pom.xml
```

## 核心组件说明

### 1. CouponModificationService

核心文案修改服务：
- 接收原始券文案和自然语言操作指令
- 使用千文模型理解操作意图
- 执行删除或替换操作
- 返回修改后的券文案

### 2. QwenModelConfig

千文模型配置：
- 配置API端点和密钥
- 设置模型参数（temperature等）

## 使用示例

### 示例1：删除黄金脆皮鸡

输入原始券文案包含：
```
产品包含：...黄金脆皮鸡（1块装）...
常规：在不售卖黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡...
```

操作指令：`"删除黄金脆皮鸡"`

输出：
```
产品包含：...（已删除黄金脆皮鸡（1块装））...
（已删除黄金脆皮鸡相关替换规则）
```

### 示例2：替换为避风塘黄金脆皮鸡

输入原始券文案包含：
```
产品包含：...黄金脆皮鸡（1块装）...
常规：在不售卖黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡...
```

操作指令：`"将黄金脆皮鸡替换为避风塘黄金脆皮鸡"`

输出：
```
产品包含：...避风塘黄金脆皮鸡（1块装）...
常规：在不售卖避风塘黄金脆皮鸡的餐厅，产品将替换为吮指原味鸡...
```

## 注意事项

1. 确保千文模型API密钥正确配置
2. 操作指令使用自然语言，系统会自动理解操作意图
3. 删除操作会同时删除产品选项和相关替换规则
4. 替换操作会替换文案中所有出现的目标内容，包括替换规则
5. 系统会保持原始文案的格式和结构不变

## 依赖说明

- `langchain4j`: Langchain4j核心库
- `langchain4j-open-ai`: OpenAI兼容接口（适配千文模型）
- `spring-boot-starter-web`: Spring Boot Web支持

## 许可证

本项目为内部使用项目。
