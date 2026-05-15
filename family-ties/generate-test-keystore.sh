#!/bin/bash
# Generate SSL keystore for system tests if it doesn't exist
# Uses SSL_KEYSTORE_PASSWORD environment variable

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_SYSTEM="$SCRIPT_DIR/app/src/testSystem/resources/ssl/keystore.p12"
KEYSTORE_E2E="$SCRIPT_DIR/app/src/testE2E/resources/ssl/keystore.p12"

# Check if SSL_KEYSTORE_PASSWORD is set
if [ -z "$SSL_KEYSTORE_PASSWORD" ]; then
    echo "❌ Error: SSL_KEYSTORE_PASSWORD environment variable is not set"
    echo ""
    echo "Please set it using one of these methods:"
    echo "  1. Export in your shell: export SSL_KEYSTORE_PASSWORD='your-password'"
    echo "  2. Run setup-test-env.sh to create a .env file"
    echo "  3. Set it in your IDE's test configuration"
    exit 1
fi

generate_keystore() {
    local keystore_path=$1
    local keystore_dir=$(dirname "$keystore_path")
    
    # Create directory if it doesn't exist
    mkdir -p "$keystore_dir"
    
    if [ -f "$keystore_path" ]; then
        echo "✅ Keystore already exists: $keystore_path"
        return 0
    fi
    
    echo "🔐 Generating keystore: $keystore_path"
    
    keytool -genkeypair \
        -alias familyties \
        -keyalg RSA \
        -keysize 2048 \
        -storetype PKCS12 \
        -keystore "$keystore_path" \
        -storepass "$SSL_KEYSTORE_PASSWORD" \
        -keypass "$SSL_KEYSTORE_PASSWORD" \
        -validity 3650 \
        -dname "CN=localhost, OU=Family Ties, O=Arc-E-Tect, L=Amsterdam, S=NH, C=NL" \
        -ext "SAN=dns:localhost,ip:127.0.0.1"
    
    echo "✅ Keystore generated successfully"
}

echo "Checking for SSL keystores..."
echo ""

# Generate system test keystore
generate_keystore "$KEYSTORE_SYSTEM"

# Generate E2E test keystore  
generate_keystore "$KEYSTORE_E2E"

echo ""
echo "✅ All keystores ready"
