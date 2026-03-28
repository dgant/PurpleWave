Repo Notes For Codex

- Preserve existing line endings on touched files. In this repo, default to Windows `CRLF` unless the file clearly uses something else already.
- Do not make whitespace-only edits unless the user explicitly asks for them.
- Do not make line-ending-only changes unless the user explicitly asks for them.
- Keep diffs minimal. Avoid reflowing, reindenting, or rewriting unchanged lines when making a local fix.
- Before finalizing edits, sanity-check that the diff does not contain no-op churn that makes review harder.
