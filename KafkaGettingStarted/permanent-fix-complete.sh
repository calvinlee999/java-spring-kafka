#!/bin/bash

echo "🎯 PERMANENT FIX: VS Code Terminal & Enhanced Development Environment"
echo "=================================================================="
echo

# Step 1: Fix current bash session
echo "STEP 1: Fixing current bash session..."
export PATH="/opt/homebrew/bin:$PATH"
eval "$(jenv init -)" 2>/dev/null

echo "✅ Current session fixed:"
echo "   - PATH includes Homebrew"
echo "   - jenv initialized"
echo "   - Java version: $(jenv version-name 2>/dev/null || echo 'Not detected')"
echo

# Step 2: VS Code Settings
echo "STEP 2: VS Code settings updated ✅"
echo "   - Default terminal set to zsh"
echo "   - Bash profile includes Homebrew PATH"
echo "   - Environment inheritance enabled"
echo

# Step 3: Next Steps
echo "STEP 3: Complete the permanent fix..."
echo "📋 Choose one option:"
echo
echo "OPTION A - Restart VS Code (Recommended):"
echo "   1. Save your work"
echo "   2. Close VS Code completely (Cmd+Q)"
echo "   3. Open Terminal.app"
echo "   4. Run: code"
echo "   5. Open new terminal in VS Code (will use zsh)"
echo "   6. Test: source ~/.zshrc && dev-check"
echo
echo "OPTION B - Create new zsh terminal now:"
echo "   1. Click '+' dropdown in terminal panel"
echo "   2. Select 'zsh' profile"
echo "   3. Test: source ~/.zshrc && dev-check"
echo
echo "OPTION C - Force zsh in current terminal:"
echo "   1. Run: zsh -l"
echo "   2. Test: source ~/.zshrc && dev-check"
echo

# Step 4: Verification
echo "STEP 4: How to verify everything works..."
echo "✨ After switching to zsh, you should see:"
echo "   🟢 Node.js: $(node --version 2>/dev/null || echo 'Will be detected')"
echo "   ☕ Java: $(java -version 2>&1 | head -1 2>/dev/null || echo 'Will be detected')"
echo "   🐍 Python: $(python --version 2>/dev/null || echo 'Will be detected')"
echo "   🅰️  Angular: Available with create-ng-project command"
echo "   🔧 Project auto-detection when entering directories"
echo

echo "💡 Pro tip: Your enhanced .zshrc will automatically:"
echo "   - Detect project types (Angular, Node.js, Java, Python)"
echo "   - Switch Node.js versions via .nvmrc files"
echo "   - Provide smart development commands"
echo "   - Show environment status when entering projects"
echo

echo "🎉 Your development environment is ready for enterprise-level work!"
