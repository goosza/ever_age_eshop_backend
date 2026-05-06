#!/bin/bash

set -e

echo "🔐 Setting up Docker Credential Helper..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}📦 Installing dependencies...${NC}"
if [ "$EUID" -eq 0 ]; then
    # Running as root
    apt-get update -qq
    apt-get install -y pass gnupg2 wget jq
else
    # Running as regular user
    sudo apt-get update -qq
    sudo apt-get install -y pass gnupg2 wget jq
fi

# Check if GPG key exists
echo -e "${YELLOW}🔑 Checking for GPG key...${NC}"
if ! gpg --list-secret-keys | grep -q "sec"; then
    echo -e "${YELLOW}No GPG key found. Creating one...${NC}"
    echo -e "${YELLOW}Please enter your name and email when prompted.${NC}"
    echo -e "${YELLOW}You can use default values for other options (just press Enter).${NC}"
    
    # Generate GPG key non-interactively
    cat >gpg-batch <<EOF
%no-protection
Key-Type: RSA
Key-Length: 2048
Subkey-Type: RSA
Subkey-Length: 2048
Name-Real: Docker Credentials
Name-Email: docker@localhost
Expire-Date: 0
EOF
    
    gpg --batch --generate-key gpg-batch
    rm gpg-batch
    echo -e "${GREEN}✅ GPG key created${NC}"
else
    echo -e "${GREEN}✅ GPG key already exists${NC}"
fi

# Get GPG key ID
GPG_KEY_ID=$(gpg --list-secret-keys --keyid-format LONG | grep sec | head -n 1 | awk '{print $2}' | cut -d'/' -f2)
echo -e "${GREEN}Using GPG key: $GPG_KEY_ID${NC}"

# Initialize pass
echo -e "${YELLOW}🔐 Initializing pass...${NC}"
if [ ! -d "$HOME/.password-store" ]; then
    pass init "$GPG_KEY_ID"
    echo -e "${GREEN}✅ Pass initialized${NC}"
else
    echo -e "${GREEN}✅ Pass already initialized${NC}"
fi

# Download and install docker-credential-pass
echo -e "${YELLOW}📥 Installing docker-credential-pass...${NC}"
CREDENTIAL_HELPER_VERSION="v0.8.0"
CREDENTIAL_HELPER_URL="https://github.com/docker/docker-credential-helpers/releases/download/${CREDENTIAL_HELPER_VERSION}/docker-credential-pass-${CREDENTIAL_HELPER_VERSION}.linux-amd64"

if [ ! -f "/usr/local/bin/docker-credential-pass" ]; then
    wget -q "$CREDENTIAL_HELPER_URL" -O docker-credential-pass
    chmod +x docker-credential-pass
    if [ "$EUID" -eq 0 ]; then
        mv docker-credential-pass /usr/local/bin/
    else
        sudo mv docker-credential-pass /usr/local/bin/
    fi
    echo -e "${GREEN}✅ docker-credential-pass installed${NC}"
else
    echo -e "${GREEN}✅ docker-credential-pass already installed${NC}"
fi

# Verify installation
if ! command -v docker-credential-pass &> /dev/null; then
    echo -e "${RED}❌ docker-credential-pass installation failed${NC}"
    exit 1
fi

# Backup existing Docker config
echo -e "${YELLOW}💾 Backing up Docker config...${NC}"
if [ -f "$HOME/.docker/config.json" ]; then
    cp "$HOME/.docker/config.json" "$HOME/.docker/config.json.backup.$(date +%Y%m%d_%H%M%S)"
    echo -e "${GREEN}✅ Backup created${NC}"
fi

# Configure Docker to use credential helper
echo -e "${YELLOW}⚙️  Configuring Docker...${NC}"
mkdir -p "$HOME/.docker"

# Read existing config or create new one
if [ -f "$HOME/.docker/config.json" ]; then
    # Update existing config
    jq '. + {"credsStore": "pass"}' "$HOME/.docker/config.json" > "$HOME/.docker/config.json.tmp"
    mv "$HOME/.docker/config.json.tmp" "$HOME/.docker/config.json"
else
    # Create new config
    echo '{"credsStore": "pass"}' > "$HOME/.docker/config.json"
fi

echo -e "${GREEN}✅ Docker configured to use credential helper${NC}"

# Test the setup
echo -e "${YELLOW}🧪 Testing credential helper...${NC}"
if echo "test" | docker-credential-pass store <<< '{"ServerURL": "test.example.com", "Username": "test", "Secret": "test"}' 2>/dev/null; then
    docker-credential-pass erase <<< "test.example.com" 2>/dev/null
    echo -e "${GREEN}✅ Credential helper is working${NC}"
else
    echo -e "${RED}❌ Credential helper test failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Docker Credential Helper setup complete!${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Re-login to your Docker registries:"
echo "   docker login ghcr.io"
echo ""
echo "2. Your credentials will now be stored securely using GPG encryption"
echo ""
echo -e "${YELLOW}Note:${NC} Old credentials backup saved in ~/.docker/config.json.backup.*"
