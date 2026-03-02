#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="${1:-.env}"
PROFILES="${2:-oauth2}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Env file not found: ${ENV_FILE}" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

./gradlew :core-app:movie-core-app:bootRun --args="--spring.profiles.active=${PROFILES}"
