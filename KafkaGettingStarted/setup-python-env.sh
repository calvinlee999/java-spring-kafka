#!/bin/bash
# PYTHON PROJECT ENVIRONMENT SETUP
# Use this for all Python/ML/Data Science projects

echo "🐍 Setting up Python development environment with conda..."

# Ensure conda is in PATH
export PATH="/opt/anaconda3/bin:/opt/anaconda3/condabin:$PATH"

# Initialize conda
if command -v conda &> /dev/null; then
    eval "$(conda shell.bash hook)"
    echo "✅ Conda environment configured:"
    echo "   Conda version: $(conda --version)"
    echo "   Active environment: $(conda env list | grep '*' | awk '{print $1}')"
    echo "   Python version: $(python --version 2>&1)"
else
    echo "❌ conda not found. Please install Anaconda or Miniconda first."
    exit 1
fi

echo "🚀 Python development environment ready!"
echo ""
echo "Available commands:"
echo "  conda env list                    - List all environments"
echo "  conda create -n myenv python=3.12 - Create new environment"
echo "  conda activate myenv              - Activate environment"
echo "  conda deactivate                  - Deactivate environment"
echo "  pip install package              - Install Python packages"
