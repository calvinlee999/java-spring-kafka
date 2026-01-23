# 🚀 macOS Development Environment - The Golden Path

> **A comprehensive `.zshrc` configuration for modern full-stack development on macOS**

[![macOS](https://img.shields.io/badge/macOS-Big%20Sur%2B-blue?style=flat-square&logo=apple)](https://www.apple.com/macos/)
[![Zsh](https://img.shields.io/badge/Shell-Zsh-green?style=flat-square&logo=gnu-bash)](https://zsh.sourceforge.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

This repository contains a meticulously crafted `.zshrc` configuration that provides the **golden path** for setting up a complete development environment on macOS. It supports modern development workflows across multiple technologies with intelligent auto-detection, performance optimizations, and productivity enhancements.

## 🎯 **Supported Technologies**

| Technology | Status | Features |
|------------|--------|----------|
| **🅰️ Angular** | ✅ Production Ready | Auto-detection, CLI integration, project templates |
| **🧩 .NET** | ✅ Production Ready | SDK switching, global.json support, version management |
| **☕ Java** | ✅ Production Ready | jenv integration, Maven/Gradle support, version switching |
| **🐍 Python** | ✅ Production Ready | Conda environments, dependency management |
| **🤖 ML/AI** | ✅ Production Ready | Auto-environment switching, library detection, Jupyter integration |
| **🌍 Terraform** | ✅ Production Ready | Project detection, security validation, CLI shortcuts |
| **☁️ AWS CLI** | ✅ Production Ready | Command completion, configuration management |
| **🐳 Docker** | ✅ Production Ready | Container management, compose shortcuts, cleanup utilities |
| **⚓ Kubernetes** | ✅ Production Ready | kubectl shortcuts, context management, namespace switching |

## 🚀 **Quick Start**

### Prerequisites

- macOS Big Sur or later
- [Homebrew](https://brew.sh/) package manager
- Zsh (default on macOS Catalina+)

### Installation

```bash
# Clone the repository
git clone https://github.com/calvinlee999/dotfiles.git ~/.dotfiles

# Backup your existing .zshrc (if any)
cp ~/.zshrc ~/.zshrc.backup 2>/dev/null || true

# Install the configuration
cp ~/.dotfiles/.zshrc ~/.zshrc

# Reload your shell
source ~/.zshrc
```

### Initial Setup

```bash
# Install essential development tools
install-dev-tools

# Install modern CLI replacements (optional but recommended)
install-modern-tools

# Validate your environment
dev-check
```

## 🌟 **Key Features**

### 🔄 **Intelligent Auto-Detection**

The configuration automatically detects and configures environments when you enter project directories:

- **Angular Projects**: Detects `angular.json`, validates CLI, checks dependencies
- **.NET Projects**: Recognizes `*.csproj`/`*.sln`, manages SDK versions via `global.json`
- **Java Projects**: Identifies Maven/Gradle projects, manages versions with jenv
- **Python/ML Projects**: Detects ML libraries, auto-switches to appropriate conda environments
- **Terraform Projects**: Validates configurations, checks for security issues

### ⚡ **Performance Optimized**

- **Lazy Loading**: Heavy operations only run when needed
- **Smart Caching**: Minimizes redundant environment checks
- **Background Processing**: ML library detection runs asynchronously
- **Fast Startup**: Optimized initialization sequence

### 🛠️ **Rich Development Utilities**

#### Project Creation
```bash
create-ng-project myapp          # Angular project with best practices
create-nx-workspace myworkspace  # Nx monorepo setup
ml-new-project ml-analysis       # Complete ML project structure
quick-start                      # Interactive project creator
```

#### Environment Management
```bash
dev-check                        # Complete environment validation
setup-full-env                   # Comprehensive project setup
project-type-quick              # Fast project type detection
project-health                  # Dependency and git status analysis
```

#### Technology-Specific Commands
```bash
# .NET Development
dotnet-switch 8.0.100           # Switch SDK versions
dotnet-globaljson               # Create global.json with current SDK

# Java Development  
java-global 17                  # Set global Java version via jenv
java-versions                   # List available Java versions

# Machine Learning
ml-setup                        # Initialize ML environment
ml-check                        # Verify ML libraries
ml-env                          # Activate ML conda environment

# Container & Cloud
dps                             # Pretty Docker container list
k get pods                      # Kubernetes shortcuts
tf-plan                         # Terraform planning
```

## 📋 **Comprehensive Alias Reference**

### Angular & Node.js
```bash
ng-serve, ng-build, ng-test     # Angular CLI shortcuts
npm-clean, yarn-clean           # Dependency cleanup
node-versions, node-latest      # Node version management
nx-graph, nx-affected          # Nx monorepo utilities
```

### .NET Development
```bash
dotnet-info, dotnet-sdk         # SDK information
dotnet-run, dotnet-test         # Build and test shortcuts
dotnet-new, dotnet-new-web      # Project templates
```

### Java Development
```bash
java-versions, java-current     # Version management
mvn-clean, mvn-test, mvn-run    # Maven shortcuts
java-setup                      # Project environment setup
```

### Python & ML
```bash
conda-envs, conda-current       # Environment management
ml-env, ml-libs, ml-versions    # Machine learning utilities
jupyter-start, jupyter-lab      # Jupyter notebook launchers
```

### DevOps & Cloud
```bash
tf-init, tf-plan, tf-apply      # Terraform workflow
dps, dc, dcu, dcd              # Docker & Docker Compose
k, kgp, kgs, klogs             # Kubernetes shortcuts
```

### Modern CLI Tools
```bash
ll, la, l                      # Enhanced ls with exa
cat                            # Better cat with bat
find                           # Modern find with fd
grep                           # Faster grep with ripgrep
```

## 🎛️ **Advanced Configuration**

### Environment Variables

The configuration sets up optimal paths and environment variables:

```bash
# Node.js & Angular
export NVM_DIR="$HOME/.nvm"
export PATH="$YARN_GLOBAL_BIN:$PATH"

# .NET Development
export DOTNET_ROOT="/usr/local/share/dotnet"

# Java Development
export JAVA_HOME="$(jenv javahome)"

# Python & ML
export PATH="$HOME/Library/Python/3.12/bin:$PATH"

# Cloud & DevOps
export AWS_PAGER=""
export KUBECONFIG="$HOME/.kube/config"
```

### Intelligent Project Detection

The `chpwd` hooks automatically run environment setup when entering directories:

```bash
# Auto-detects and configures based on project files:
# package.json → Node.js/Angular setup
# *.csproj/*.sln → .NET environment
# pom.xml/build.gradle → Java configuration  
# requirements.txt/*.ipynb → Python/ML setup
# *.tf → Terraform validation
```

## 🔧 **Customization**

### Personal Overrides

Create additional configuration files for personal customization:

```bash
# Machine-specific configuration
~/.zshrc.local

# Work-specific settings  
~/.work.zsh
```

### Performance Tuning

For even faster startup, you can disable specific features:

```bash
# Disable expensive ML detection
export SKIP_ML_DETECTION=true

# Disable automatic project setup
export SKIP_AUTO_SETUP=true
```

## 🧪 **Validation & Testing**

### Environment Health Check

```bash
dev-check
```

**Expected Output:**
```
🔍 Development Environment Check
================================

💻 System: Darwin 24.5.0
🖥️  Architecture: arm64

☕ Java Environment:
   ✅ Java: openjdk version "17.0.5"
   ✅ jenv: 17.0.5 (set by /Users/username/.jenv/version)

🐍 Python Environment:
   ✅ Python: Python 3.12.2
   ✅ Conda env: base

🟢 Node.js Environment:
   ✅ Node.js: v18.17.0
   ✅ npm: 9.6.7
   ✅ Angular CLI: 16.2.0

🛠️  Development Tools:
   ✅ Git: git version 2.41.0
   ✅ Docker: Docker version 24.0.2
   ✅ VS Code: 1.81.0
```

### Project Detection Test

```bash
project-type-quick
```

Test in different project directories to verify auto-detection works correctly.

## 🚨 **Troubleshooting**

### Common Issues

**Issue**: `command not found: ng`
```bash
# Solution: Install Angular CLI globally
npm install -g @angular/cli
```

**Issue**: Java version not switching
```bash
# Solution: Ensure jenv is properly initialized
jenv enable-plugin export
```

**Issue**: Conda environment not activating
```bash
# Solution: Reinitialize conda
conda init zsh
```

**Issue**: Docker commands not working
```bash
# Solution: Ensure Docker Desktop is running
open /Applications/Docker.app
```

### Performance Issues

If shell startup is slow:

```bash
# Profile startup time
time zsh -i -c exit

# Disable heavy features temporarily
export SKIP_ML_DETECTION=true
export SKIP_AUTO_SETUP=true
```

## 📚 **Learning Resources**

### Technology-Specific Guides

- **Angular**: [Angular CLI Documentation](https://angular.io/cli)
- **.NET**: [.NET CLI Documentation](https://docs.microsoft.com/en-us/dotnet/core/tools/)
- **Java**: [jenv Documentation](https://github.com/jenv/jenv)
- **Python**: [Conda Documentation](https://docs.conda.io/)
- **Docker**: [Docker CLI Reference](https://docs.docker.com/engine/reference/commandline/cli/)
- **Kubernetes**: [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)

### Best Practices

1. **Version Management**: Use version managers (nvm, jenv, conda) rather than system packages
2. **Project Isolation**: Each project should have its own environment/dependencies
3. **Security**: Never commit credentials; use environment variables and AWS profiles
4. **Performance**: Regularly clean up unused containers, images, and dependencies

## 🤝 **Contributing**

Contributions are welcome! Please:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Test** your changes thoroughly
4. **Commit** your changes (`git commit -m 'Add amazing feature'`)
5. **Push** to the branch (`git push origin feature/amazing-feature`)
6. **Open** a Pull Request

### Development Guidelines

- **Performance**: Ensure additions don't slow down shell startup
- **Compatibility**: Test on multiple macOS versions
- **Documentation**: Update README for new features
- **Testing**: Validate with `dev-check` and project detection

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 **Acknowledgments**

- **Oh My Zsh** community for shell enhancement inspiration
- **Homebrew** team for excellent package management
- **Modern CLI tools** authors (bat, fd, ripgrep, etc.)
- **Technology communities** for best practices and patterns

---

## ⭐ **Star History**

If this configuration helps your development workflow, please consider giving it a star! ⭐

**Happy coding!** 🚀

---

*Last updated: August 2025*
