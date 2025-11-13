#!/bin/bash#!/bin/bash



# Test Suite Runner for ReservaBus# Script para ejecutar los tests con Docker

# This script helps run different categories of tests# Este script:

# 1. Levanta la base de datos PostgreSQL en Docker

echo "======================================"# 2. Espera a que esté saludable

echo "  ReservaBus Test Suite Runner"# 3. Ejecuta los tests

echo "======================================"# 4. Opcionalmente para los contenedores

echo ""

set -e

# Colors for output

GREEN='\033[0;32m'# Colores para output

RED='\033[0;31m'RED='\033[0;31m'

YELLOW='\033[1;33m'GREEN='\033[0;32m'

NC='\033[0m' # No ColorYELLOW='\033[1;33m'

NC='\033[0m' # No Color

function print_success {

    echo -e "${GREEN}✓ $1${NC}"echo -e "${YELLOW}🚀 Iniciando suite de tests...${NC}\n"

}

# Directorio del proyecto

function print_error {PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

    echo -e "${RED}✗ $1${NC}"cd "$PROJECT_DIR"

}

# Función para limpiar al salir

function print_warning {cleanup() {

    echo -e "${YELLOW}⚠ $1${NC}"    if [ "$KEEP_CONTAINERS" != "true" ]; then

}        echo -e "\n${YELLOW}🧹 Limpiando contenedores...${NC}"

        docker-compose -f docker-compose.test.yml down -v

# Function to run a specific test    else

function run_test {        echo -e "\n${GREEN}✅ Contenedores dejados corriendo (usa 'docker-compose -f docker-compose.test.yml down' para pararlos)${NC}"

    local test_name=$1    fi

    echo ""}

    echo "Running: $test_name"

    echo "--------------------------------------"# Registrar cleanup al salir

    trap cleanup EXIT

    if ./mvnw test -Dtest="$test_name" > /dev/null 2>&1; then

        print_success "$test_name PASSED"# Verificar si Docker está corriendo

        return 0if ! docker info > /dev/null 2>&1; then

    else    echo -e "${RED}❌ Error: Docker no está corriendo${NC}"

        print_error "$test_name FAILED"    exit 1

        return 1fi

    fi

}# Levantar contenedores

echo -e "${GREEN}📦 Levantando contenedores de Docker...${NC}"

# Main menudocker-compose -f docker-compose.test.yml up -d

echo "Select test category to run:"

echo ""# Esperar a que PostgreSQL esté listo

echo "1) Run AccountRepositoryIT (working)"echo -e "${YELLOW}⏳ Esperando a que PostgreSQL esté listo...${NC}"

echo "2) Run all Repository tests"MAX_RETRIES=30

echo "3) Run all Service tests"RETRY_COUNT=0

echo "4) Run ALL tests"

echo "5) Check test compilation"while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do

echo "6) Exit"    if docker-compose -f docker-compose.test.yml exec -T postgres-test pg_isready -U test_user -d reservabus_test > /dev/null 2>&1; then

echo ""        echo -e "${GREEN}✅ PostgreSQL está listo!${NC}\n"

read -p "Enter choice [1-6]: " choice        break

    fi

case $choice in    

    1)    RETRY_COUNT=$((RETRY_COUNT + 1))

        echo ""    echo -e "${YELLOW}Intento $RETRY_COUNT/$MAX_RETRIES...${NC}"

        print_warning "Running AccountRepositoryIT..."    sleep 1

        ./mvnw test -Dtest=AccountRepositoryITdone

        ;;

    2)if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then

        echo ""    echo -e "${RED}❌ Error: PostgreSQL no respondió a tiempo${NC}"

        print_warning "Running all Repository Integration Tests..."    exit 1

        fi

        # List of repository tests

        tests=(# Ejecutar tests

            "AccountRepositoryIT"echo -e "${GREEN}🧪 Ejecutando tests...${NC}\n"

            "AssignmentRepositoryIT"

            "BusRepositoryIT"# Parsear argumentos

            "RouteRepositoryIT"TEST_CLASS=""

            "TripRepositoryIT"TEST_METHOD=""

            "TicketRepositoryIT"KEEP_CONTAINERS=false

        )

        while [[ $# -gt 0 ]]; do

        passed=0    case $1 in

        failed=0        --class)

                    TEST_CLASS="$2"

        for test in "${tests[@]}"; do            shift 2

            if run_test "$test"; then            ;;

                ((passed++))        --method)

            else            TEST_METHOD="$2"

                ((failed++))            shift 2

            fi            ;;

        done        --keep)

                    KEEP_CONTAINERS=true

        echo ""            shift

        echo "======================================"            ;;

        echo "Summary:"        *)

        print_success "Passed: $passed"            shift

        print_error "Failed: $failed"            ;;

        echo "======================================"    esac

        ;;done

    3)

        echo ""# Construir comando Maven

        print_warning "Running all Service Unit Tests..."MVN_CMD="./mvnw test -Dspring.profiles.active=test"

        ./mvnw test -Dtest=*ServiceTest

        ;;if [ -n "$TEST_CLASS" ]; then

    4)    MVN_CMD="$MVN_CMD -Dtest=$TEST_CLASS"

        echo ""    if [ -n "$TEST_METHOD" ]; then

        print_warning "Running ALL tests..."        MVN_CMD="$MVN_CMD#$TEST_METHOD"

        ./mvnw test    fi

        ;;fi

    5)

        echo ""# Ejecutar tests

        print_warning "Checking test compilation..."if eval "$MVN_CMD"; then

        ./mvnw clean test-compile    echo -e "\n${GREEN}✅ Tests completados exitosamente!${NC}"

        ;;    exit 0

    6)else

        echo "Exiting..."    echo -e "\n${RED}❌ Tests fallaron${NC}"

        exit 0    exit 1

        ;;fi

    *)
        print_error "Invalid choice!"
        exit 1
        ;;
esac

echo ""
echo "Done!"
