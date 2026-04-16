#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ASSET_DIR="$ROOT_DIR/app/src/main/assets/native-curl"

mkdir -p "$ASSET_DIR"
curl -L --fail -o "$ASSET_DIR/cacert.pem" https://curl.se/ca/cacert.pem
sha256sum "$ASSET_DIR/cacert.pem" | awk '{print $1}' > "$ASSET_DIR/cacert.pem.sha256"

