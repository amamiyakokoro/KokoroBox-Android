# Kokoro Custom Rules

KokoroBox manages server-side Custom Rules through `https://amamiyakoko.ro/api/app/custom-rules`.
Every request uses the existing Kokoro App bearer session; no user ID, subscription UUID, or backend
secret is accepted from the UI.

## Client behavior

- The editor loads both `/app/custom-rules/options` and `/app/custom-rules` when opened.
- Rule types, targets, domain rule providers, and limits come from `options`; they are not hard-coded.
- Rules remain ordered in memory and are saved with one atomic `PUT /sets/{set_id}/rules` request.
- The client sends the revision read with the selected rule set as `expected_revision`.
- Create, rename, and delete operations use the same authenticated session and update the local state
  only after the server confirms success.
- The `default` set cannot be renamed or deleted.

The bearer allowlist accepts only the fixed Custom Rules endpoints and positive numeric set IDs on the
canonical HTTPS host. Access tokens, Authorization headers, callback URLs, rule payloads, and response
bodies are not logged.

## Conflict and failure handling

An HTTP `409` is never retried automatically. KokoroBox reloads the selected remote set and asks the
user to either use the remote rules or keep the local draft. Keeping the draft adopts the newly read
revision, but still requires a separate explicit Save action before anything is overwritten.

Before each save, the client reloads `options` and validates the complete draft. A `422` refreshes
capabilities and leaves the draft intact. A `404` reloads all sets. A `429` is surfaced without an
automatic write retry.

If a write times out, the client first performs a fresh GET. An identical ordered rule list is treated as
a successful save; otherwise the outcome is shown as unknown and enters the same user-mediated conflict
flow. The client never blindly replays an uncertain or stale write.

## Rule validation

The client mirrors the server's structural checks before submission:

- non-`MATCH` rules require a payload;
- `MATCH` has no payload, cannot target `REJECT`, appears at most once, and must be last;
- targets must be present in the latest options response;
- `RULE-SET` payloads must name a current provider whose behavior is `domain`;
- payloads and targets reject leading/trailing whitespace, commas, control characters, and excessive
  length;
- the latest server-provided per-set rule limit is enforced.

The backend remains authoritative and can reject a request even after local validation.
