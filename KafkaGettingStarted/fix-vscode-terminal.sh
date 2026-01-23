#!/bin/bash

echo "🔧 VS Code Terminal Configuration Fix"
echo "====================================="
echo

# Check current shell
echo "Current shell in VS Code: $0"
echo "Current PATH: $PATH"
echo

# Add Homebrew to PATH for bash compatibility
if [[ "$PATH" != *"/opt/homebrew/bin"* ]]; then
    echo "📦 Adding Homebrew to PATH for bash compatibility..."
    export PATH="/opt/homebrew/bin:$PATH"
fi

# Test jenv availability
if command -v jenv >/dev/null 2>&1; then
    echo "✅ jenv found: $(which jenv)"
    # Initialize jenv for bash
    eval "$(jenv init -)"
    echo "✅ jenv initialized for bash"
else
    echo "❌ jenv not found in PATH"
fi

echo
echo "🎯 VS Code Terminal Solutions:"
echo "1. **Immediate Fix (Current Terminal):**"
echo "   - jenv is now working in this bash session"
echo
echo "2. **Permanent Fix (Recommended):**"
echo "   a) Close VS Code completely"
echo "   b) Open Terminal.app and run: code"
echo "   c) Or restart VS Code from Applications"
echo "   d) Open a new terminal in VS Code (should use zsh)"
echo
echo "3. **Manual Terminal Switch:**"
echo "   - Click the dropdown arrow next to '+' in terminal"
echo "   - Select 'zsh' profile"
echo
echo "4. **Force zsh in current session:**"
echo "   zsh -l"
echo
echo "💡 After switching to zsh, test with:"
echo "   source ~/.zshrc"
echo "   dev-check"
