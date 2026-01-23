#!/bin/bash

# PostgreSQL Connection Test Script
echo "🧪 PostgreSQL Connection Test"
echo "============================="

# Test basic connection
echo "1. Testing basic connection..."
docker exec postgres16 psql -U cnldev -d mydata -c "SELECT 'Connection successful!' as status;" 2>/dev/null

# Check database size
echo ""
echo "2. Database size information..."
docker exec postgres16 psql -U cnldev -d mydata -c "SELECT pg_database.datname, pg_size_pretty(pg_database_size(pg_database.datname)) AS size FROM pg_database;" 2>/dev/null

# Check active connections
echo ""
echo "3. Active connections..."
docker exec postgres16 psql -U cnldev -d mydata -c "SELECT count(*) as active_connections FROM pg_stat_activity WHERE state = 'active';" 2>/dev/null

# Check PostgreSQL configuration
echo ""
echo "4. Key PostgreSQL settings..."
docker exec postgres16 psql -U cnldev -d mydata -c "SELECT name, setting FROM pg_settings WHERE name IN ('max_connections', 'shared_buffers', 'effective_cache_size');" 2>/dev/null

echo ""
echo "✅ PostgreSQL Connection Test Complete!"
echo ""
echo "📋 Connection Details:"
echo "Host: localhost"
echo "Port: 5432"
echo "Database: mydata"
echo "Username: cnldev"
echo "Password: cnldev_123"
echo ""
echo "🌐 pgAdmin URL: http://localhost:5050"
echo "Email: admin@local.dev"
echo "Password: cnladmin_123"
