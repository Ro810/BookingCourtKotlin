#!/bin/bash

echo "📦 Installing Git hooks..."

HOOK_DIR=".git/hooks"
SOURCE_DIR="hooks"

if [ ! -d "$HOOK_DIR" ]; then
  echo "❌ Error: .git/hooks directory not found. Are you in the project root?"
  exit 1
fi

if [ ! -d "$SOURCE_DIR" ]; then
  echo "❌ Error: hooks directory not found."
  exit 1
fi

cp "$SOURCE_DIR/pre-commit" "$HOOK_DIR/pre-commit"
chmod +x "$HOOK_DIR/pre-commit"

echo "✅ Git hooks installed successfully!"
echo ""
echo "Pre-commit hook will now automatically format your Kotlin code before each commit."
echo ""
echo "To bypass the hook (not recommended), use: git commit --no-verify"
