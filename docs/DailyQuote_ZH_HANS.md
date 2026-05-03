# 每日一言 API 与用户自定义一言格式说明

**简体中文** | [English](DailyQuote.md)

本文说明 YumeBox MD3 ACG 首页「每日一言」功能中，自定义一言 API 与用户自定义一言 JSON 的数据格式要求，并提供可直接参考的示例。

## 1. 每日一言 API

在设置中填写「每日一言 API URL」后，应用会在需要刷新一言时向该地址发起一次 HTTP 请求，并从返回的 JSON 中解析一言文本与作者/来源。

默认 API：

```text docs/DailyQuote_ZH_HANS.md
https://v1.hitokoto.cn/?c=a&c=b&c=c
```

### 1.1 请求方式

- 请求方法：`GET`
- 请求头：
  - `Accept: application/json`
  - `User-Agent: YumeBox-MaterialDesign`
- 超时：单次请求约 1 秒，失败时会进行少量重试。
- URL：必须是可直接访问的完整 URL，例如 `https://example.com/quote`。

### 1.2 响应要求

API 应返回一个 JSON 对象，且 HTTP 状态码应为 2xx。若响应头 `Content-Type` 不为空，建议包含 `json`，例如：

```http docs/DailyQuote_ZH_HANS.md
Content-Type: application/json; charset=utf-8
```

返回 JSON 中必须包含一个可识别的「一言文本」字段；作者/来源字段可选。

#### 一言文本字段

应用会按以下顺序尝试读取，取第一个非空字符串：

1. `hitokoto`
2. `text`
3. `quote`
4. `content`
5. `sentence`

#### 作者/来源字段

应用会按以下顺序尝试读取，取第一个非空且不为字符串 `"null"` 的值：

1. `from_who`
2. `author`
3. `from`
4. `source`

如果没有作者/来源字段，应用会只显示一言文本。

### 1.3 API 返回示例

兼容 Hitokoto 风格：

```json docs/DailyQuote_ZH_HANS.md
{
  "hitokoto": "如果奇迹有颜色，那一定是橙色。",
  "from_who": "宫园薰",
  "from": "四月是你的谎言"
}
```

使用通用字段：

```json docs/DailyQuote_ZH_HANS.md
{
  "text": "愿你历尽千帆，归来仍是少年。",
  "author": "自定义 API"
}
```

使用 `quote` 与 `source` 字段：

```json docs/DailyQuote_ZH_HANS.md
{
  "quote": "在安静的线路上，等一朵云完成漫游。",
  "source": "YumeBox"
}
```

### 1.4 不符合要求的示例

以下返回不会被识别，因为缺少可用的一言文本字段：

```json docs/DailyQuote_ZH_HANS.md
{
  "message": "hello",
  "name": "YumeBox"
}
```

以下返回不会被识别，因为顶层不是 JSON 对象：

```json docs/DailyQuote_ZH_HANS.md
[
  {
    "text": "这是一句话。",
    "author": "示例"
  }
]
```

> 注意：API 返回必须是单条一言的 JSON 对象；如果想维护多条一言列表，请使用「用户自定义一言」JSON。

## 2. 用户自定义一言 JSON

「用户自定义一言」用于在本地维护一组一言。启用后，应用会从列表中随机选择一条。若同时启用 API 与自定义列表，应用会在 API 与本地列表之间混合选择；当 API 请求失败时，本地列表也会作为回退来源。

### 2.1 顶层格式

自定义一言 JSON 的顶层必须是数组：

```json docs/DailyQuote_ZH_HANS.md
[
  {
    "text": "第一句话。",
    "author": "作者 A"
  },
  {
    "text": "第二句话。",
    "author": "作者 B"
  }
]
```

数组中的每一项可以是：

1. 字符串：只提供一言文本，没有作者/来源。
2. 对象：使用与 API 返回相同的字段规则解析文本与作者/来源。

### 2.2 字符串项示例

```json docs/DailyQuote_ZH_HANS.md
[
  "愿风指引你的道路。",
  "今天也要好好生活。",
  "在安静的线路上，等一朵云完成漫游。"
]
```

### 2.3 对象项示例

```json docs/DailyQuote_ZH_HANS.md
[
  {
    "text": "时间一分一秒流逝而去，终结一步一步迎面而来。",
    "author": "恋文"
  },
  {
    "hitokoto": "如果奇迹有颜色，那一定是橙色。",
    "from_who": "宫园薰",
    "from": "四月是你的谎言"
  },
  {
    "quote": "愿你历尽千帆，归来仍是少年。",
    "source": "自定义"
  }
]
```

### 2.4 混合写法示例

```json docs/DailyQuote_ZH_HANS.md
[
  "没有作者的纯文本一言。",
  {
    "text": "所谓的成长，就是越来越能接受自己本来的样子。",
    "author": "某角色"
  },
  {
    "content": "愿你的连接稳定，愿你的旅途顺利。",
    "source": "YumeBox"
  }
]
```

### 2.5 注释支持

自定义一言 JSON 支持整行 `//` 注释。应用会在解析前忽略以 `//` 开头的行：

```jsonc docs/DailyQuote_ZH_HANS.md
[
  // 纯文本一言
  "今天也要好好生活。",

  // 带作者的一言
  {
    "text": "愿你历尽千帆，归来仍是少年。",
    "author": "自定义"
  }
]
```

注意事项：

- 只支持整行注释，不建议在 JSON 值后面追加行尾注释。
- 注释去除后，剩余内容仍必须是合法 JSON 数组。

## 3. 字段兼容表

| 用途 | 支持字段 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| 一言文本 | `hitokoto`, `text`, `quote`, `content`, `sentence` | 是 | 按顺序读取第一个非空字符串。 |
| 作者/来源 | `from_who`, `author`, `from`, `source` | 否 | 按顺序读取第一个非空且不为 `"null"` 的字符串。 |

## 4. 推荐配置

如果你自己搭建 API，推荐直接返回以下格式，最简单也最清晰：

```json docs/DailyQuote_ZH_HANS.md
{
  "text": "这里是一言内容。",
  "author": "这里是作者或来源"
}
```

如果你维护本地自定义列表，推荐使用以下格式：

```json docs/DailyQuote_ZH_HANS.md
[
  {
    "text": "这里是第一条一言。",
    "author": "作者或来源"
  },
  {
    "text": "这里是第二条一言。",
    "author": "作者或来源"
  }
]
```

## 5. 排查建议

如果一言没有刷新或没有显示，可以检查：

1. API 是否可以正常访问，且返回 HTTP 2xx。
2. API 响应是否为 JSON 对象，而不是数组、纯文本或 HTML。
3. API 响应头 `Content-Type` 是否包含 `json`。
4. JSON 中是否包含 `hitokoto`、`text`、`quote`、`content` 或 `sentence` 中至少一个非空字段。
5. 自定义一言 JSON 顶层是否为数组。
6. 自定义一言数组中的对象是否包含可识别的一言文本字段。
7. JSON 中是否存在多余逗号、未闭合引号等语法错误。
