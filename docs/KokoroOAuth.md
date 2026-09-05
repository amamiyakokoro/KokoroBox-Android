# Kokoro Android OAuth integration

KokoroBox uses backend-mediated osu! OAuth with mandatory **PKCE S256**.
There is no plain-PKCE or non-PKCE fallback, including on HTTP 400 or 422.

## Fixed configuration

```text
API_BASE_URL=https://amamiyakoko.ro/api
APP_REDIRECT_URI=kokoro://oauth/callback
```

The backend allowlist must include this exact value:

```env
APP_REDIRECT_URIS=kokoro://oauth/callback
```

This shared callback remains unchanged across platforms. This repository implements
the Android client only; it does not implement desktop instance handoff or Apple
URL registration. Do not substitute App Links, Universal Links, platform-specific
schemes or loopback URLs. The client does not contain backend or osu! client secrets.

## Login and storage lifecycle

`KokoroSession` owns authentication for the UI and profile service. `KokoroOAuth`
implements PKCE and callback validation; `KokoroAuthStore` keeps the existing
Keystore-backed persistence behind an atomic storage interface.

1. Each login independently generates state and verifier from two separate 32-byte
   `SecureRandom` draws, encoded using Base64URL without padding (43 characters).
2. The challenge is Base64URL without padding of SHA-256 of the **ASCII verifier
   string**, not the original random bytes or a hexadecimal digest.
3. The session atomically saves `{state, codeVerifier, redirectUri, createdAt,
   expiresAt}` in the existing AES-GCM credential record. The encryption key stays
   in Android Keystore; SharedPreferences contains ciphertext only. I/O runs on
   `Dispatchers.IO`. Android backup is disabled for this credential store/app.
4. Exactly one pending login is retained. Starting another login replaces the old
   attempt. Pending state has a ten-minute lifetime; expired state is rejected and
   removed when the session next handles a callback or checks credentials. A clock
   rollback before creation also invalidates the attempt. No periodic polling is
   needed. An old record from before PKCE has no usable pending login, but its
   existing access/refresh tokens are preserved.
5. The UI opens a system browser using `ACTION_VIEW`, never an embedded WebView.
   If opening fails or the launch coroutine is cancelled, it removes only that
   attempt, without clearing a newer login.

The URL builder sends only:

```text
GET /api/app/auth/login
  ?redirect_uri=kokoro%3A%2F%2Foauth%2Fcallback
  &state=<random-state>
  &code_challenge=<S256-challenge>
  &code_challenge_method=S256
```

The verifier never appears in the browser URL.

## Callback handling

The existing Android manifest registers `kokoro://oauth/callback` for
`MainActivity` with `VIEW`, `DEFAULT` and `BROWSABLE`. Both `onCreate` (cold launch)
and `onNewIntent` (running app) feed the same handler. The received Intent data is
cleared before asynchronous processing; no authorization code is forwarded to
another app. Token exchange runs in the application scope so Activity recreation
does not cancel it.

Validation requires exact lowercase scheme `kokoro`, authority `oauth`, and raw
path `/callback`. Userinfo, any port (including an empty port), fragments,
alternative/encoded paths or authorities, malformed encoding, control characters
and duplicate query names (including percent-encoded duplicates) are rejected.
State is required and compared with `MessageDigest.isEqual` against an unexpired
pending login. Unknown or malformed callbacks cannot delete a legitimate pending
login. No pending login, a missing/invalid verifier or a different stored redirect
URI requires signing in again.

A matching callback atomically consumes the pending record **before** any HTTP
request. This also applies to `error=access_denied`, missing/empty code and ambiguous
code-plus-error callbacks. Replays and concurrent duplicate deliveries cannot
perform a second exchange. The original verifier is used once:

```json
{
  "grant_type": "authorization_code",
  "code": "<single-use-code>",
  "redirect_uri": "kokoro://oauth/callback",
  "code_verifier": "<original-verifier-for-this-state>"
}
```

The backend code expires after five minutes; the app exchanges it immediately.
HTTP 400/422, network failure, invalid token responses or a lost verifier require
a new login. The token HTTP client disables automatic redirects and connection
retries: it must not resend a single-use code or leak the request to another origin.
If the process dies after consuming the callback but before saving tokens, start a
new login. If it dies while the browser is open, the encrypted pending record can
be restored until its expiry.

Closing the system browser without a callback cannot be reliably distinguished
from an ongoing login. That pending attempt remains bounded by its expiry and is
replaced on the next login; an explicit error callback or logout clears it.

## Tokens and refresh

Access token, refresh token and their expiry timestamps are saved atomically in
the encrypted record. Refresh retains the existing process-wide mutex/single-flight
behavior, including one refresh and one replay after an authenticated API's 401.
Its body is unchanged and never contains a verifier:

```json
{"grant_type":"refresh_token","refresh_token":"<current-refresh-token>"}
```

Both rotated tokens are persisted before waiting requests continue. A refresh 401
clears credentials and requires login. Refresh preserves any separate pending
browser login. Explicit logout clears credentials and pending login even if revoke
fails. The storage key alias/name is intentionally unchanged to retain existing
encrypted sessions.

## Logging and security boundaries

The authentication HTTP client has no body logger, analytics or crash-reporting
interceptor. Do not add one that captures login/callback URLs, token bodies or
response bodies. Callback errors logged by `MainActivity` are generic, without the
URI or throwable. URI/JSON parsing errors are replaced with sanitized errors;
credential models redact `toString()`. No verifier, authorization code, tokens or
full callback should enter telemetry. Subscription UUIDs/external URLs remain
sensitive independently of PKCE.

Custom schemes may still be claimed by other installed apps. PKCE prevents an app
that intercepts only the authorization code from exchanging it without the original
verifier; it does not guarantee exclusive OS delivery or prevent denial of service.

## Automated verification

```sh
./gradlew :data:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

`KokoroOAuthTest` and `KokoroSessionTest` cover the
[RFC 7636 Appendix B vector](https://www.rfc-editor.org/rfc/rfc7636#appendix-B),
unpadded encoding, independent randomness, exact URL/JSON fields, forged callbacks,
missing/incorrect/duplicate/expired state, replay/concurrent delivery, denial,
missing verifier, serialized pending-login restoration, isolated consecutive
logins, token 400/422 without downgrade, response redaction and refresh rotation/
single-flight. HTTP is replaced with a recording test transport; no real credentials
or production requests are needed. The RFC vector exists only in test code.

## Required device checks

JVM tests of serialized pending-login restoration are **not** proof of Android
Keystore persistence or OS URL dispatch. Before release, verify on an Android device:

- Complete real osu! login through the system browser against the PKCE-enabled backend.
- Deliver callbacks with the app foregrounded, backgrounded and cold-started after
  process death while the browser is open; check Activity recreation during exchange.
- Cancel authorization, close the browser without a callback, and start a second login.
- Confirm pending state cannot be replayed, and expiration/lost Keystore data require login.
- Confirm a complete encrypted token rotation and subsequent profile download succeed.
- Check Logcat/crash reporting for sensitive URI/body data without copying real secrets
  into a report; verify installed-app scheme conflicts do not cause code forwarding.

No physical-device, OS callback-dispatch or live-backend OAuth verification is implied
by the automated build/test commands above.
