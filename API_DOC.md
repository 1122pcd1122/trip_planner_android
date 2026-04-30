# 智能旅行助手 API 接口文档

## 基础信息

| 项目 | 值 |
|------|-----|
| 基础URL | `http://127.0.0.1:8000` |
| 启动命令 | `python main_api.py` |
| Swagger文档 | `http://127.0.0.1:8000/docs` |

---

## 接口列表

### 1. 健康检查

| 项目 | 值 |
|------|-----|
| **URL** | `/` |
| **方法** | GET |

**响应示例**：
```json
{
  "status": "ok",
  "message": "Welcome to Smart Travel Assistant API!",
  "code": "200"
}
```

---

### 2. 旅行计划汇总

| 项目 | 值 |
|------|-----|
| **URL** | `/api/plan/stream` |
| **方法** | POST |

**请求参数**：
```json
{
  "destination": "成都",
  "days": 3,
  "preferences": "和女朋友"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| destination | string | ✅ | 目的地 |
| days | int | ✅ | 游玩天数 |
| preferences | string | ❌ | 用户偏好 |

---

### 3. 天气查询

| 项目 | 值 |
|------|-----|
| **URL** | `/api/weather` |
| **方法** | POST |

**请求参数**：
```json
{"destination": "成都"}
```

**响应示例**：
```json
{
  "status": "success",
  "message": "{\"cityName\":\"成都市\",\"latitude\":30.5728,\"longitude\":104.0668,\"date\":\"2026-04-29\",\"weather\":\"多云\",\"temperature\":\"24°C\",\"tips\":\"天气晴朗多云，气温适宜，适合户外活动，注意防晒补水\"}",
  "code": "200"
}
```

**message 字段解析**：
| 字段 | 类型 | 说明 |
|------|------|------|
| cityName | string | 城市名称 |
| latitude | float | 纬度 |
| longitude | float | 经度 |
| date | string | 日期 |
| weather | string | 天气状态 |
| temperature | string | 温度 |
| tips | string | 出行建议 |

---

### 4. 景点推荐

| 项目 | 值 |
|------|-----|
| **URL** | `/api/attraction` |
| **方法** | POST |

**请求参数**：
```json
{"destination": "成都"}
```

**响应示例**：
```json
{
  "status": "success",
  "message": "{\"spotList\":[{\"name\":\"春熙路步行街\",\"latitude\":30.658,\"longitude\":104.068,\"address\":\"春熙路街道\",\"score\":\"4.9\",\"intro\":\"成都著名商业步行街，购物美食云集，夜景迷人\"}]}",
  "code": "200"
}
```

**message 字段解析**：
| 字段 | 类型 | 说明 |
|------|------|------|
| spotList | array | 景点列表 |
| └─ name | string | 景点名称 |
| └─ latitude | float | 纬度 |
| └─ longitude | float | 经度 |
| └─ address | string | 地址 |
| └─ score | string | 评分 |
| └─ intro | string | 简介 |

---

### 5. 酒店推荐

| 项目 | 值 |
|------|-----|
| **URL** | `/api/hotel` |
| **方法** | POST |

**请求参数**：
```json
{"destination": "成都"}
```

**响应示例**：
```json
{
  "status": "success",
  "message": "{\"hotelList\":[{\"name\":\"城市便捷酒店(成都西华店)\",\"latitude\":30.5728,\"longitude\":104.0668,\"address\":\"广场路北一段205号\",\"priceRange\":\"经济型\",\"feature\":\"连锁品牌，性价比高\"}]}",
  "code": "200"
}
```

**message 字段解析**：
| 字段 | 类型 | 说明 |
|------|------|------|
| hotelList | array | 酒店列表 |
| └─ name | string | 酒店名称 |
| └─ latitude | float | 纬度 |
| └─ longitude | float | 经度 |
| └─ address | string | 地址 |
| └─ priceRange | string | 价格区间 |
| └─ feature | string | 特色 |

---

### 6. 餐饮推荐

| 项目 | 值 |
|------|-----|
| **URL** | `/api/restaurant` |
| **方法** | POST |

**请求参数**：
```json
{"destination": "成都"}
```

**响应示例**：
```json
{
  "status": "success",
  "message": "{\"foodList\":[{\"name\":\"小龙坎火锅\",\"latitude\":30.658,\"longitude\":104.068,\"address\":\"春熙路附近\",\"featureDish\":\"麻辣火锅\",\"score\":\"4.8\"}]}",
  "code": "200"
}
```

**message 字段解析**：
| 字段 | 类型 | 说明 |
|------|------|------|
| foodList | array | 餐饮列表 |
| └─ name | string | 餐厅名称 |
| └─ latitude | float | 纬度 |
| └─ longitude | float | 经度 |
| └─ address | string | 地址 |
| └─ featureDish | string | 招牌菜 |
| └─ score | string | 评分 |

---

## 统一响应格式

```json
{
  "status": "success | error",
  "message": "返回内容(JSON字符串)",
  "code": "200 | 500"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| status | string | 状态：`success` 或 `error` |
| message | string | 成功时返回JSON字符串，失败时返回错误信息 |
| code | string | 状态码：`200` 成功，`500` 错误 |

---

## 使用示例

### Python requests

```python
import requests
import json

BASE_URL = "http://127.0.0.1:8000"

# 天气查询
response = requests.post(f"{BASE_URL}/api/weather", json={"destination": "成都"})
data = response.json()
result = json.loads(data["message"])  # 解析内嵌JSON
print(result)
```

### cURL

```bash
# 天气查询
curl -X POST http://127.0.0.1:8000/api/weather \
  -H "Content-Type: application/json" \
  -d '{"destination": "成都"}'
```
