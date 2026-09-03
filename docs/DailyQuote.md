# Daily Quote API and Custom Quote JSON Format

**English** | [简体中文](DailyQuote_ZH_HANS.md)

This document describes the format requirements for the KokoroBox ACG home "Daily Quote" feature, including custom quote APIs and user-defined quote JSON lists, with examples you can copy and adapt.

## 1. Daily Quote API

After you set a "Daily Quote API URL" in settings, the app sends an HTTP request to that URL when it needs to refresh the quote, then parses the quote text and author/source from the returned JSON.

Default API:

```text docs/DailyQuote.md
https://v1.hitokoto.cn/?c=a&c=b&c=c
```

### 1.1 Request

- Method: `GET`
- Request headers:
  - `Accept: application/json`
  - `User-Agent: KokoroBox`
- Timeout: about 1 second per attempt, with a small number of retries on failure.
- URL: must be a directly accessible full URL, for example `https://example.com/quote`.

### 1.2 Response requirements

The API should return a JSON object with a 2xx HTTP status code. If the `Content-Type` response header is not empty, it should contain `json`, for example:

```http docs/DailyQuote.md
Content-Type: application/json; charset=utf-8
```

The returned JSON must contain at least one recognizable quote text field. The author/source field is optional.

#### Quote text fields

The app tries the following fields in order and uses the first non-empty string:

1. `hitokoto`
2. `text`
3. `quote`
4. `content`
5. `sentence`

#### Author/source fields

The app tries the following fields in order and uses the first non-empty value that is not the string `"null"`:

1. `from_who`
2. `author`
3. `from`
4. `source`

If no author/source field is available, the app displays only the quote text.

### 1.3 API response examples

Hitokoto-compatible style:

```json docs/DailyQuote.md
{
  "hitokoto": "If miracles have a color, it must be orange.",
  "from_who": "Kaori Miyazono",
  "from": "Your Lie in April"
}
```

Generic fields:

```json docs/DailyQuote.md
{
  "text": "May you return as a young soul after crossing a thousand sails.",
  "author": "Custom API"
}
```

Using `quote` and `source`:

```json docs/DailyQuote.md
{
  "quote": "Wait for a cloud to finish roaming on a quiet route.",
  "source": "YumeBox"
}
```

### 1.4 Invalid examples

The following response will not be recognized because it does not contain any supported quote text field:

```json docs/DailyQuote.md
{
  "message": "hello",
  "name": "YumeBox"
}
```

The following response will not be recognized because the top level is not a JSON object:

```json docs/DailyQuote.md
[
  {
    "text": "This is a quote.",
    "author": "Example"
  }
]
```

> Note: the API must return a JSON object for a single quote. If you want to maintain multiple quotes, use the user-defined quote JSON list instead.

## 2. User-defined quote JSON

The user-defined quote JSON is used to maintain a local quote list. When enabled, the app randomly selects one quote from the list. If both the API and the custom list are enabled, the app mixes API and local quote selection; the local list can also be used as a fallback when the API request fails.

### 2.1 Top-level format

The top level of the custom quote JSON must be an array:

```json docs/DailyQuote.md
[
  {
    "text": "First quote.",
    "author": "Author A"
  },
  {
    "text": "Second quote.",
    "author": "Author B"
  }
]
```

Each array item can be:

1. A string: quote text only, without author/source.
2. An object: parsed with the same field rules as the API response.

### 2.2 String item example

```json docs/DailyQuote.md
[
  "May the wind guide your path.",
  "Live well today, too.",
  "Wait for a cloud to finish roaming on a quiet route."
]
```

### 2.3 Object item example

```json docs/DailyQuote.md
[
  {
    "text": "Time slips away second by second, and the end approaches step by step.",
    "author": "Koibumi"
  },
  {
    "hitokoto": "If miracles have a color, it must be orange.",
    "from_who": "Kaori Miyazono",
    "from": "Your Lie in April"
  },
  {
    "quote": "May you return as a young soul after crossing a thousand sails.",
    "source": "Custom"
  }
]
```

### 2.4 Mixed example

```json docs/DailyQuote.md
[
  "A plain quote without author.",
  {
    "text": "Growing up means becoming more able to accept who you really are.",
    "author": "Some character"
  },
  {
    "content": "May your connection be stable and your journey be smooth.",
    "source": "YumeBox"
  }
]
```

### 2.5 Comment support

User-defined quote JSON supports whole-line `//` comments. Lines starting with `//` are ignored before parsing:

```jsonc docs/DailyQuote.md
[
  // Plain quote
  "Live well today, too.",

  // Quote with author
  {
    "text": "May you return as a young soul after crossing a thousand sails.",
    "author": "Custom"
  }
]
```

Notes:

- Only whole-line comments are supported. Avoid adding inline comments after JSON values.
- After comments are removed, the remaining content must still be a valid JSON array.

## 3. Field compatibility table

| Purpose | Supported fields | Required | Description |
| --- | --- | --- | --- |
| Quote text | `hitokoto`, `text`, `quote`, `content`, `sentence` | Yes | The first non-empty string is used. |
| Author/source | `from_who`, `author`, `from`, `source` | No | The first non-empty string that is not `"null"` is used. |

## 4. Recommended configuration

If you host your own API, the simplest recommended format is:

```json docs/DailyQuote.md
{
  "text": "Quote content here.",
  "author": "Author or source here"
}
```

If you maintain a local custom list, the recommended format is:

```json docs/DailyQuote.md
[
  {
    "text": "First quote here.",
    "author": "Author or source"
  },
  {
    "text": "Second quote here.",
    "author": "Author or source"
  }
]
```

## 5. Troubleshooting

If the quote does not refresh or display, check the following:

1. The API is reachable and returns an HTTP 2xx status code.
2. The API response is a JSON object, not an array, plain text, or HTML.
3. The API response `Content-Type` contains `json` when it is present.
4. The JSON contains at least one non-empty field among `hitokoto`, `text`, `quote`, `content`, or `sentence`.
5. The custom quote JSON top level is an array.
6. Objects in the custom quote array contain a recognizable quote text field.
7. The JSON does not contain syntax errors such as trailing commas or unclosed quotes.
