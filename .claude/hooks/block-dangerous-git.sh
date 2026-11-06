#!/bin/bash

INPUT=$(cat)
if command -v jq >/dev/null 2>&1; then
  COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')
else
  COMMAND=$(echo "$INPUT" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$COMMAND" ]; then
    COMMAND="$INPUT"
  fi
fi

DANGEROUS_PATTERNS=(
  "git[[:space:]]+push"
  "git[[:space:]]+reset[[:space:]]+--hard"
  "git[[:space:]]+clean[[:space:]]+-f"
  "git[[:space:]]+clean[[:space:]]+-fd"
  "git[[:space:]]+branch[[:space:]]+-D"
  "git[[:space:]]+checkout[[:space:]]+\\."
  "git[[:space:]]+restore[[:space:]]+\\."
  "push[[:space:]]+--force"
  "reset[[:space:]]+--hard"
)

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -qE "$pattern"; then
    echo "BLOCKED: '$COMMAND' matches dangerous pattern '$pattern'. The user has prevented you from doing this." >&2
    exit 2
  fi
done

exit 0
