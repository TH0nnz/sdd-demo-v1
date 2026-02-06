#!/bin/bash

# 工時管理系統 - 環境檢查腳本
# 檢查所有必要的工具是否已安裝並符合版本需求

# Remove set -e to continue on errors
# set -e

# 顏色設定
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 計數器
PASSED=0
FAILED=0
WARNING=0

# 輸出函數
print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
    ((PASSED++))
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    ((FAILED++))
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
    ((WARNING++))
}

# 版本比較函數
version_ge() {
    # 比較版本號 $1 >= $2
    [ "$(printf '%s\n' "$1" "$2" | sort -V | head -n1)" = "$2" ]
}

# 開始檢查
print_header "工時管理系統 - 環境需求檢查"

# 1. Java 檢查
echo "檢查 Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
    JAVA_FULL_VERSION=$(java -version 2>&1 | head -n 1)
    
    if [ "$JAVA_VERSION" -ge 21 ] 2>/dev/null; then
        print_success "Java $JAVA_VERSION 已安裝（需求：Java 21+）"
        echo "   版本: $JAVA_FULL_VERSION"
    else
        print_error "Java 版本過低: $JAVA_VERSION（需求：Java 21+）"
        echo "   安裝指南: https://adoptium.net/"
    fi
else
    print_error "Java 未安裝（需求：Java 21+）"
    echo "   安裝指南: https://adoptium.net/"
fi
echo ""

# 2. Maven 檢查
echo "檢查 Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn --version 2>&1 | head -n 1 | awk '{print $3}')
    
    if version_ge "$MVN_VERSION" "3.6.0"; then
        print_success "Maven $MVN_VERSION 已安裝（需求：Maven 3.6+）"
    else
        print_error "Maven 版本過低: $MVN_VERSION（需求：Maven 3.6+）"
    fi
else
    print_error "Maven 未安裝（需求：Maven 3.6+）"
    echo "   安裝指南: https://maven.apache.org/install.html"
fi
echo ""

# 3. Node.js 檢查
echo "檢查 Node.js..."
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version | sed 's/v//')
    NODE_MAJOR=$(echo $NODE_VERSION | awk -F '.' '{print $1}')
    
    if [ "$NODE_MAJOR" -ge 18 ] 2>/dev/null; then
        print_success "Node.js $NODE_VERSION 已安裝（需求：Node.js 18+）"
    else
        print_error "Node.js 版本過低: $NODE_VERSION（需求：Node.js 18+）"
        echo "   安裝指南: https://nodejs.org/"
    fi
else
    print_error "Node.js 未安裝（需求：Node.js 18+）"
    echo "   安裝指南: https://nodejs.org/"
fi
echo ""

# 4. npm 檢查
echo "檢查 npm..."
if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm --version)
    NPM_MAJOR=$(echo $NPM_VERSION | awk -F '.' '{print $1}')
    
    if [ "$NPM_MAJOR" -ge 8 ] 2>/dev/null; then
        print_success "npm $NPM_VERSION 已安裝（需求：npm 8+）"
    else
        print_warning "npm 版本過低: $NPM_VERSION（需求：npm 8+）"
    fi
else
    print_error "npm 未安裝（需求：npm 8+）"
fi
echo ""

# 5. pnpm 檢查（選用）
echo "檢查 pnpm（選用）..."
if command -v pnpm &> /dev/null; then
    PNPM_VERSION=$(pnpm --version)
    print_success "pnpm $PNPM_VERSION 已安裝（推薦但非必需）"
else
    print_warning "pnpm 未安裝（推薦安裝以獲得更好的效能）"
    echo "   安裝命令: npm install -g pnpm"
fi
echo ""

# 6. Docker 檢查
echo "檢查 Docker..."
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
    print_success "Docker $DOCKER_VERSION 已安裝"
    
    # 檢查 Docker 是否運行
    if docker ps &> /dev/null; then
        print_success "Docker daemon 正在運行"
    else
        print_error "Docker daemon 未運行"
        echo "   請啟動 Docker Desktop 或 Docker 服務"
    fi
else
    print_error "Docker 未安裝"
    echo "   安裝指南: https://docs.docker.com/get-docker/"
fi
echo ""

# 7. Docker Compose 檢查
echo "檢查 Docker Compose..."
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version | awk '{print $4}' | sed 's/,//')
    print_success "Docker Compose $COMPOSE_VERSION 已安裝"
elif docker compose version &> /dev/null 2>&1; then
    COMPOSE_VERSION=$(docker compose version | awk '{print $4}')
    print_success "Docker Compose $COMPOSE_VERSION 已安裝（內建於 Docker）"
else
    print_error "Docker Compose 未安裝"
    echo "   安裝指南: https://docs.docker.com/compose/install/"
fi
echo ""

# 8. PostgreSQL 檢查（選用）
echo "檢查 PostgreSQL 客戶端（選用）..."
if command -v psql &> /dev/null; then
    PSQL_VERSION=$(psql --version | awk '{print $3}')
    print_success "PostgreSQL 客戶端 $PSQL_VERSION 已安裝（用於資料庫管理）"
else
    print_warning "PostgreSQL 客戶端未安裝（選用，Docker 容器已包含）"
    echo "   安裝指南: https://www.postgresql.org/download/"
fi
echo ""

# 9. Git 檢查
echo "檢查 Git..."
if command -v git &> /dev/null; then
    GIT_VERSION=$(git --version | awk '{print $3}')
    print_success "Git $GIT_VERSION 已安裝"
else
    print_warning "Git 未安裝（版本控制工具）"
fi
echo ""

# 10. 檢查專案檔案
print_header "專案檔案檢查"

if [ -f "docker-compose.yml" ]; then
    print_success "docker-compose.yml 存在"
else
    print_error "docker-compose.yml 不存在"
fi

if [ -f "backend/pom.xml" ]; then
    print_success "backend/pom.xml 存在"
else
    print_error "backend/pom.xml 不存在"
fi

if [ -f "backend/Dockerfile" ]; then
    print_success "backend/Dockerfile 存在"
else
    print_warning "backend/Dockerfile 不存在（Docker 部署需要）"
fi

if [ -f "frontend/package.json" ]; then
    print_success "frontend/package.json 存在"
else
    print_error "frontend/package.json 不存在"
fi

if [ -f "frontend/Dockerfile" ]; then
    print_success "frontend/Dockerfile 存在"
else
    print_warning "frontend/Dockerfile 不存在（Docker 部署需要）"
fi

echo ""

# 11. 端口檢查
print_header "端口可用性檢查"

check_port() {
    local port=$1
    local service=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t &> /dev/null || netstat -an 2>/dev/null | grep -q ":$port.*LISTEN"; then
        print_warning "端口 $port 已被佔用（$service）"
        echo "   請先關閉佔用該端口的程式"
    else
        print_success "端口 $port 可用（$service）"
    fi
}

check_port 5432 "PostgreSQL"
check_port 8080 "Backend API"
check_port 5173 "Frontend Dev"
check_port 80 "Frontend Prod (Nginx)"

echo ""

# 總結
print_header "檢查總結"

echo "通過: $PASSED 項"
echo "警告: $WARNING 項"
echo "失敗: $FAILED 項"
echo ""

if [ $FAILED -eq 0 ]; then
    if [ $WARNING -eq 0 ]; then
        echo -e "${GREEN}🎉 所有檢查通過！環境已就緒。${NC}"
        echo ""
        echo "下一步："
        echo "  1. 使用 Docker Compose 啟動: docker-compose up -d"
        echo "  2. 或手動啟動各服務，參考 ENVIRONMENT.md"
    else
        echo -e "${YELLOW}⚠️  環境基本就緒，但有 $WARNING 個警告。${NC}"
        echo "建議查看上述警告並進行相應調整。"
    fi
    exit 0
else
    echo -e "${RED}❌ 環境檢查失敗！請解決上述 $FAILED 個問題。${NC}"
    echo ""
    echo "詳細資訊請參考 ENVIRONMENT.md"
    exit 1
fi
