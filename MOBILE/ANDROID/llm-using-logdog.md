# LLM (client project) — using Logdog (Android-friendly)

Этот файл — короткая инструкция для проекта‑клиента (в т.ч. **Android**), который отправляет логи в Logdog, чтобы Cursor/агенты могли быстро подтягивать отладку.

## 0) Preconditions (перед стартом)

- Logdog ingest **запущен** на машине в локалке и доступен по сети.
- Выбери `LOGDOG_HOST`:
  - если проект и Logdog на одной машине: `localhost`
  - если с другой машины в LAN: IP хоста Logdog (например `192.168.1.10`)
- Для **Android**:
  - **Эмулятор Android Studio**: используй `10.0.2.2` (это “localhost” хост‑машины из эмулятора).
  - **Genymotion**: обычно `10.0.3.2`.
  - **Реальный телефон**: используй IP твоего ПК в Wi‑Fi/LAN (например `192.168.1.10`). ПК и телефон должны быть в одной сети.
  - (Опционально) **ADB reverse**: можно прокинуть порт `3000` на телефон и тогда ходить на `127.0.0.1:3000`.
- Если Windows Firewall блокирует порт: открой входящие на `TCP 3000`.

## 1) Куда писать логи (HTTP)

Logdog ingest слушает:

- `POST http://<LOGDOG_HOST>:3000/logs`
- `Content-Type: application/json`

Минимальный JSON:

```json
{
  "level": "debug",
  "app": "my-app",
  "message": "something happened"
}
```

Рекомендуемые поля:

- `ts`: epoch ms (если не указать — проставит сервер)
- `traceId`: строка корреляции (одна на запрос/операцию/пайплайн)
- `fields`: объект с полезным контекстом (ids, параметры, метрики)

Ограничения:

- размер одной записи: до **256KB**
- уровни: `debug|info|warn|error`

Ожидаемые ответы:

- `201 Created`: вернёт JSON с `id` и нормализованным `ts`
- `400`: плохой JSON
- `413`: payload слишком большой
- `422`: не прошла валидация полей

## 2) Как договориться об идентификации (важно)

Чтобы запросы в MCP были точными, в проекте **зафиксируй**:

- `app`: стабильное имя сервиса/приложения (например `billing-api`, `desktop-ui`)
- `traceId`: формат (например UUID) и где он берётся/прокидывается
- Для Android‑приложения удобно:
  - `app`: `synapse-android`
  - `traceId`: UUID на “операцию” (кнопка/сценарий/экран) или на BLE‑команду/пайплайн

## 3) Как Cursor будет читать логи (MCP)

Cursor подключается к MCP через **stdio**. В каждом проекте-клиенте добавь `.cursor/mcp.json`
чтобы Cursor мог запускать MCP-процесс.

Варианты подключения:

- **Per-project**: `.cursor/mcp.json` в каждом проекте (самый предсказуемый).
- **Global**: `~/.cursor/mcp.json` (один раз на всю машину) — удобно, если Logdog стоит всегда в одном месте.

Пример (Windows, если Logdog находится в `D:\\Git\\Logdog` и venv уже создан):

```json
{
  "mcpServers": {
    "logdog": {
      "command": "D:\\\\Git\\\\Logdog\\\\.venv\\\\Scripts\\\\python.exe",
      "args": ["-m", "logdog.mcp_server"],
      "env": { "LOGDOG_DB_PATH": "D:\\\\Git\\\\Logdog\\\\data\\\\logdog.db" }
    }
  }
}
```

После изменения `mcp.json` **перезапусти Cursor полностью**.

## 4) Как просить LLM/агента искать нужное

Используй MCP tools:

- `recent(limit, app?, level?)` — последние записи
- `query(app?, level?, since?, until?, contains?, traceId?, limit)` — поиск по фильтрам

Шаблоны запросов:

- “Найди ошибки за последние 10 минут по `app=my-app` (level=error)”
- “Покажи лог-цепочку по `traceId=<...>`”
- “Найди записи, где `message` содержит `timeout` после момента \(ts=...\)”

Пример формулировки прямо в чате Cursor:

- `Вызови MCP tool logdog.recent с аргументами {"limit":5,"app":"my-app"}`
- `Вызови MCP tool logdog.query с аргументами {"traceId":"<id>","limit":200}`

## 5) Быстрая проверка отправки (PowerShell)

```powershell
$body = @{ level="debug"; app="my-app"; message="hello from client" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://<LOGDOG_HOST>:3000/logs" -ContentType "application/json" -Body $body
```

## 6) Android: как отправлять логи (Kotlin + OkHttp)

Минимально (наглядно, без ретраев/батчинга):

```kotlin
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

data class LogdogConfig(
    val host: String,          // "10.0.2.2" (emulator) | "192.168.1.10" (phone)
    val port: Int = 3000,
    val app: String = "synapse-android",
)

fun sendLogdog(
    client: OkHttpClient,
    cfg: LogdogConfig,
    level: String,
    message: String,
    traceId: String = UUID.randomUUID().toString(),
    fields: Map<String, Any?> = emptyMap(),
) {
    val url = "http://${cfg.host}:${cfg.port}/logs"
    val bodyJson = JSONObject().apply {
        put("level", level)
        put("app", cfg.app)
        put("message", message)
        put("traceId", traceId)
        put("fields", JSONObject(fields))
        // ts не обязателен: сервер проставит сам
    }.toString()

    val req = Request.Builder()
        .url(url)
        .post(bodyJson.toRequestBody(jsonMediaType))
        .build()

    // Важно: не блокируй main thread. Используй enqueue / корутины / Dispatchers.IO.
    client.newCall(req).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) { response.close() }
    })
}
```

### Android нюансы (важно)

- Разрешение сети в `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

- HTTP (cleartext) может быть запрещён на Android 9+. Варианты:
  - **на debug**: включить `android:usesCleartextTraffic="true"` в `<application ...>`
  - или настроить `networkSecurityConfig` (если нужно точечно)

### Быстрый sanity‑check (Android)

- Если эмулятор: `LOGDOG_HOST=10.0.2.2`
- Если телефон: `LOGDOG_HOST=<IP ПК>` и ПК/телефон в одной сети
- В Logdog должны появиться записи с `app=synapse-android`

### Troubleshooting (быстро)

- `failed to connect` / `ECONNREFUSED`:
  - проверь `LOGDOG_HOST` (эмулятор: `10.0.2.2`)
  - проверь Firewall: входящий `TCP 3000`
  - Logdog ingest должен слушать на интерфейсе LAN (не только `127.0.0.1`)
- `CLEARTEXT communication not permitted`:
  - включи cleartext на debug (`usesCleartextTraffic`) или настрой `networkSecurityConfig`
- `413 Payload Too Large`:
  - урежь сообщение/поля (лимит 256KB на запись)

