#!/bin/bash

# =================================================================
# Script mejorado para poblar la base de datos usando la API REST
# - Muestra mensajes detallados con información real de la API
# - Crea trips para todo el mes de diciembre con horarios variados
# =================================================================

set -e  # Exit on error

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api}"
ADMIN_TOKEN=""

# Función para extraer valores JSON
extract_json_value() {
  echo "$1" | grep -o "\"$2\":[^,}]*" | head -1 | sed 's/.*://g' | tr -d '"' | sed 's/^ *//g' | sed 's/ *$//g'
}

echo "🚀 Poblando base de datos usando API REST..."
echo "📡 API Base URL: $API_BASE_URL"
echo ""

# =================================================================
# 1. CREAR CUENTAS
# =================================================================
echo "👤 Creando cuentas..."

# Admin
RESPONSE=$(curl -s -X POST "$API_BASE_URL/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin System",
    "email": "j@lojur.com",
    "phone": "+573001234567",
    "password": "123",
    "isAdmin": true
  }')
ADMIN_ID=$(extract_json_value "$RESPONSE" "id")
ADMIN_EMAIL=$(extract_json_value "$RESPONSE" "email")
echo "   ✅ Admin: $ADMIN_EMAIL (ID: $ADMIN_ID)"

# Usuarios regulares
declare -a USER_IDS

RESPONSE=$(curl -s -X POST "$API_BASE_URL/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "María García",
    "email": "maria.garcia@example.com",
    "phone": "+573009876543",
    "password": "password123",
    "isAdmin": false
  }')
USER_ID=$(extract_json_value "$RESPONSE" "id")
USER_IDS+=("$USER_ID")
echo "   ✅ Usuario: maría.garcia@example.com (ID: $USER_ID)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan.perez@example.com",
    "phone": "+573012345678",
    "password": "password123",
    "isAdmin": false
  }')
USER_ID=$(extract_json_value "$RESPONSE" "id")
USER_IDS+=("$USER_ID")
echo "   ✅ Usuario: juan.perez@example.com (ID: $USER_ID)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ana Rodríguez",
    "email": "ana.rodriguez@example.com",
    "phone": "+573023456789",
    "password": "password123",
    "isAdmin": false
  }')
USER_ID=$(extract_json_value "$RESPONSE" "id")
USER_IDS+=("$USER_ID")
echo "   ✅ Usuario: ana.rodriguez@example.com (ID: $USER_ID)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Carlos Martínez",
    "email": "carlos.martinez@example.com",
    "phone": "+573034567890",
    "password": "password123",
    "isAdmin": false
  }')
USER_ID=$(extract_json_value "$RESPONSE" "id")
USER_IDS+=("$USER_ID")
echo "   ✅ Usuario: carlos.martinez@example.com (ID: $USER_ID)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laura López",
    "email": "laura.lopez@example.com",
    "phone": "+573045678901",
    "password": "password123",
    "isAdmin": false
  }')
USER_ID=$(extract_json_value "$RESPONSE" "id")
USER_IDS+=("$USER_ID")
echo "   ✅ Usuario: laura.lopez@example.com (ID: $USER_ID)"

echo ""
echo "✅ Total cuentas creadas: 6 (1 admin + 5 usuarios)"
echo ""

# =================================================================
# 2. AUTENTICACIÓN
# =================================================================
echo "🔐 Autenticando como admin..."

SIGNIN_RESPONSE=$(curl -s -X POST "$API_BASE_URL/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "j@lojur.com",
    "password": "123"
  }')

ADMIN_TOKEN=$(extract_json_value "$SIGNIN_RESPONSE" "token")

if [ -z "$ADMIN_TOKEN" ]; then
  echo "❌ Error: No se pudo obtener token de autenticación"
  echo "Respuesta: $SIGNIN_RESPONSE"
  exit 1
fi

echo "   ✅ Token obtenido (${#ADMIN_TOKEN} caracteres)"
echo ""

# =================================================================
# 3. CREAR BUSES
# =================================================================
echo "🚌 Creando buses..."

declare -a BUS_IDS
declare -a BUS_PLATES

RESPONSE=$(curl -s -X POST "$API_BASE_URL/buses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "plate": "ABC123",
    "capacity": 40,
    "amenities": [
      {"name": "WiFi", "description": "Internet inalámbrico"},
      {"name": "AC", "description": "Aire acondicionado"}
    ]
  }')
BUS_ID=$(extract_json_value "$RESPONSE" "id")
BUS_PLATE=$(extract_json_value "$RESPONSE" "plate")
BUS_IDS+=("$BUS_ID")
BUS_PLATES+=("$BUS_PLATE")
echo "   ✅ Bus $BUS_PLATE (ID: $BUS_ID, Capacidad: 40)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/buses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "plate": "DEF456",
    "capacity": 35,
    "amenities": [
      {"name": "WiFi", "description": "Internet inalámbrico"},
      {"name": "TV", "description": "Televisión"}
    ]
  }')
BUS_ID=$(extract_json_value "$RESPONSE" "id")
BUS_PLATE=$(extract_json_value "$RESPONSE" "plate")
BUS_IDS+=("$BUS_ID")
BUS_PLATES+=("$BUS_PLATE")
echo "   ✅ Bus $BUS_PLATE (ID: $BUS_ID, Capacidad: 35)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/buses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "plate": "GHI789",
    "capacity": 30,
    "amenities": [
      {"name": "AC", "description": "Aire acondicionado"}
    ]
  }')
BUS_ID=$(extract_json_value "$RESPONSE" "id")
BUS_PLATE=$(extract_json_value "$RESPONSE" "plate")
BUS_IDS+=("$BUS_ID")
BUS_PLATES+=("$BUS_PLATE")
echo "   ✅ Bus $BUS_PLATE (ID: $BUS_ID, Capacidad: 30)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/buses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "plate": "JKL012",
    "capacity": 45,
    "amenities": [
      {"name": "WiFi", "description": "Internet inalámbrico"},
      {"name": "AC", "description": "Aire acondicionado"},
      {"name": "USB", "description": "Puertos de carga USB"}
    ]
  }')
BUS_ID=$(extract_json_value "$RESPONSE" "id")
BUS_PLATE=$(extract_json_value "$RESPONSE" "plate")
BUS_IDS+=("$BUS_ID")
BUS_PLATES+=("$BUS_PLATE")
echo "   ✅ Bus $BUS_PLATE (ID: $BUS_ID, Capacidad: 45)"

RESPONSE=$(curl -s -X POST "$API_BASE_URL/buses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "plate": "MNO345",
    "capacity": 25,
    "amenities": [
      {"name": "AC", "description": "Aire acondicionado"}
    ]
  }')
BUS_ID=$(extract_json_value "$RESPONSE" "id")
BUS_PLATE=$(extract_json_value "$RESPONSE" "plate")
BUS_IDS+=("$BUS_ID")
BUS_PLATES+=("$BUS_PLATE")
echo "   ✅ Bus $BUS_PLATE (ID: $BUS_ID, Capacidad: 25)"

echo ""
echo "✅ Total buses creados: ${#BUS_IDS[@]}"
echo "   IDs de buses: ${BUS_IDS[*]}"
echo "   Placas: ${BUS_PLATES[*]}"
echo ""

# =================================================================
# 4. CREAR ASIENTOS PARA LOS BUSES
# =================================================================
echo "💺 Creando asientos para los buses..."

TOTAL_SEATS_CREATED=0

# Bus 1: ABC123 - Capacidad 40 (asientos 1-40, primeros 4 preferenciales)
BUS_ID=${BUS_IDS[0]}
for SEAT_NUM in {1..40}; do
  if [ $SEAT_NUM -le 4 ]; then
    SEAT_TYPE="PREFERENTIAL"
  else
    SEAT_TYPE="STANDARD"
  fi
  
  curl -s -X POST "$API_BASE_URL/seats" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"number\": \"$SEAT_NUM\",
      \"type\": \"$SEAT_TYPE\",
      \"busId\": $BUS_ID
    }" > /dev/null
  TOTAL_SEATS_CREATED=$((TOTAL_SEATS_CREATED + 1))
done
echo "   ✅ Bus ${BUS_PLATES[0]}: 40 asientos creados (4 preferenciales, 36 estándar)"

# Bus 2: DEF456 - Capacidad 35 (asientos 1-35, primeros 3 preferenciales)
BUS_ID=${BUS_IDS[1]}
for SEAT_NUM in {1..35}; do
  if [ $SEAT_NUM -le 3 ]; then
    SEAT_TYPE="PREFERENTIAL"
  else
    SEAT_TYPE="STANDARD"
  fi
  
  curl -s -X POST "$API_BASE_URL/seats" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"number\": \"$SEAT_NUM\",
      \"type\": \"$SEAT_TYPE\",
      \"busId\": $BUS_ID
    }" > /dev/null
  TOTAL_SEATS_CREATED=$((TOTAL_SEATS_CREATED + 1))
done
echo "   ✅ Bus ${BUS_PLATES[1]}: 35 asientos creados (3 preferenciales, 32 estándar)"

# Bus 3: GHI789 - Capacidad 30 (asientos 1-30, primeros 3 preferenciales)
BUS_ID=${BUS_IDS[2]}
for SEAT_NUM in {1..30}; do
  if [ $SEAT_NUM -le 3 ]; then
    SEAT_TYPE="PREFERENTIAL"
  else
    SEAT_TYPE="STANDARD"
  fi
  
  curl -s -X POST "$API_BASE_URL/seats" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"number\": \"$SEAT_NUM\",
      \"type\": \"$SEAT_TYPE\",
      \"busId\": $BUS_ID
    }" > /dev/null
  TOTAL_SEATS_CREATED=$((TOTAL_SEATS_CREATED + 1))
done
echo "   ✅ Bus ${BUS_PLATES[2]}: 30 asientos creados (3 preferenciales, 27 estándar)"

# Bus 4: JKL012 - Capacidad 45 (asientos 1-45, primeros 5 preferenciales)
BUS_ID=${BUS_IDS[3]}
for SEAT_NUM in {1..45}; do
  if [ $SEAT_NUM -le 5 ]; then
    SEAT_TYPE="PREFERENTIAL"
  else
    SEAT_TYPE="STANDARD"
  fi
  
  curl -s -X POST "$API_BASE_URL/seats" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"number\": \"$SEAT_NUM\",
      \"type\": \"$SEAT_TYPE\",
      \"busId\": $BUS_ID
    }" > /dev/null
  TOTAL_SEATS_CREATED=$((TOTAL_SEATS_CREATED + 1))
done
echo "   ✅ Bus ${BUS_PLATES[3]}: 45 asientos creados (5 preferenciales, 40 estándar)"

# Bus 5: MNO345 - Capacidad 25 (asientos 1-25, primeros 2 preferenciales)
BUS_ID=${BUS_IDS[4]}
for SEAT_NUM in {1..25}; do
  if [ $SEAT_NUM -le 2 ]; then
    SEAT_TYPE="PREFERENTIAL"
  else
    SEAT_TYPE="STANDARD"
  fi
  
  curl -s -X POST "$API_BASE_URL/seats" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"number\": \"$SEAT_NUM\",
      \"type\": \"$SEAT_TYPE\",
      \"busId\": $BUS_ID
    }" > /dev/null
  TOTAL_SEATS_CREATED=$((TOTAL_SEATS_CREATED + 1))
done
echo "   ✅ Bus ${BUS_PLATES[4]}: 25 asientos creados (2 preferenciales, 23 estándar)"

echo ""
echo "✅ Total asientos creados: $TOTAL_SEATS_CREATED"
echo ""

# =================================================================
# 5. CREAR RUTAS
# =================================================================
echo "🛣️  Creando rutas..."

declare -a ROUTE_IDS
declare -a ROUTE_CODES

RESPONSE=$(curl -s -X POST "$API_BASE_URL/routes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "RT-SMT-BAQ",
    "name": "Santa Marta - Barranquilla",
    "origin": "Santa Marta",
    "destination": "Barranquilla",
    "distanceKm": 93,
    "durationMinutes": 120,
    "pricePerKm": 500
  }')
ROUTE_ID=$(extract_json_value "$RESPONSE" "id")
ROUTE_CODE=$(extract_json_value "$RESPONSE" "code")
ROUTE_NAME=$(extract_json_value "$RESPONSE" "name")
if [ -n "$ROUTE_ID" ]; then
  ROUTE_IDS+=("$ROUTE_ID")
  ROUTE_CODES+=("$ROUTE_CODE")
  echo "   ✅ $ROUTE_CODE: $ROUTE_NAME (ID: $ROUTE_ID, 93km, ~2h)"
else
  echo "   ⚠️  Error creando ruta RT-SMT-BAQ"
  echo "   📄 Response: $RESPONSE"
fi

RESPONSE=$(curl -s -X POST "$API_BASE_URL/routes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "RT-SMT-VDU",
    "name": "Santa Marta - Valledupar",
    "origin": "Santa Marta",
    "destination": "Valledupar",
    "distanceKm": 185,
    "durationMinutes": 240,
    "pricePerKm": 450
  }')
ROUTE_ID=$(extract_json_value "$RESPONSE" "id")
ROUTE_CODE=$(extract_json_value "$RESPONSE" "code")
ROUTE_NAME=$(extract_json_value "$RESPONSE" "name")
if [ -n "$ROUTE_ID" ]; then
  ROUTE_IDS+=("$ROUTE_ID")
  ROUTE_CODES+=("$ROUTE_CODE")
  echo "   ✅ $ROUTE_CODE: $ROUTE_NAME (ID: $ROUTE_ID, 185km, ~4h)"
else
  echo "   ⚠️  Error creando ruta RT-SMT-VDU"
  echo "   📄 Response: $RESPONSE"
fi

RESPONSE=$(curl -s -X POST "$API_BASE_URL/routes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "RT-BAQ-VDU",
    "name": "Barranquilla - Valledupar",
    "origin": "Barranquilla",
    "destination": "Valledupar",
    "distanceKm": 272,
    "durationMinutes": 300,
    "pricePerKm": 480
  }')
ROUTE_ID=$(extract_json_value "$RESPONSE" "id")
ROUTE_CODE=$(extract_json_value "$RESPONSE" "code")
ROUTE_NAME=$(extract_json_value "$RESPONSE" "name")
if [ -n "$ROUTE_ID" ]; then
  ROUTE_IDS+=("$ROUTE_ID")
  ROUTE_CODES+=("$ROUTE_CODE")
  echo "   ✅ $ROUTE_CODE: $ROUTE_NAME (ID: $ROUTE_ID, 272km, ~5h)"
else
  echo "   ⚠️  Error creando ruta RT-BAQ-VDU"
  echo "   📄 Response: $RESPONSE"
fi

RESPONSE=$(curl -s -X POST "$API_BASE_URL/routes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "code": "RT-VDU-CPY",
    "name": "Valledupar - El Copey",
    "origin": "Valledupar",
    "destination": "El Copey",
    "distanceKm": 45,
    "durationMinutes": 60,
    "pricePerKm": 550
  }')
ROUTE_ID=$(extract_json_value "$RESPONSE" "id")
ROUTE_CODE=$(extract_json_value "$RESPONSE" "code")
ROUTE_NAME=$(extract_json_value "$RESPONSE" "name")
if [ -n "$ROUTE_ID" ]; then
  ROUTE_IDS+=("$ROUTE_ID")
  ROUTE_CODES+=("$ROUTE_CODE")
  echo "   ✅ $ROUTE_CODE: $ROUTE_NAME (ID: $ROUTE_ID, 45km, ~1h)"
else
  echo "   ⚠️  Error creando ruta RT-VDU-CPY"
  echo "   📄 Response: $RESPONSE"
fi

echo ""
echo "✅ Total rutas creadas: ${#ROUTE_IDS[@]}"
echo "   IDs de rutas: ${ROUTE_IDS[*]}"
echo "   Códigos: ${ROUTE_CODES[*]}"
echo ""

# =================================================================
# 6. CREAR PARADAS
# =================================================================
echo "🚏 Creando paradas..."

# Paradas para ruta Santa Marta - Barranquilla
curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[0]},
    \"name\": \"Terminal Santa Marta\",
    \"sequence\": 1,
    \"latitude\": 11.2408,
    \"longitude\": -74.1990
  }" > /dev/null
echo "   ✅ Parada: Terminal Santa Marta (Ruta: ${ROUTE_CODES[0]})"

curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[0]},
    \"name\": \"Terminal Barranquilla\",
    \"sequence\": 2,
    \"latitude\": 10.9878,
    \"longitude\": -74.7889
  }" > /dev/null
echo "   ✅ Parada: Terminal Barranquilla (Ruta: ${ROUTE_CODES[0]})"

# Paradas para ruta Santa Marta - Valledupar
curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[1]},
    \"name\": \"Terminal Santa Marta\",
    \"sequence\": 1,
    \"latitude\": 11.2408,
    \"longitude\": -74.1990
  }" > /dev/null
echo "   ✅ Parada: Terminal Santa Marta (Ruta: ${ROUTE_CODES[1]})"

curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[1]},
    \"name\": \"Fundación\",
    \"sequence\": 2,
    \"latitude\": 10.5214,
    \"longitude\": -74.1852
  }" > /dev/null
echo "   ✅ Parada: Fundación (Ruta: ${ROUTE_CODES[1]})"

curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[1]},
    \"name\": \"Terminal Valledupar\",
    \"sequence\": 3,
    \"latitude\": 10.4631,
    \"longitude\": -73.2532
  }" > /dev/null
echo "   ✅ Parada: Terminal Valledupar (Ruta: ${ROUTE_CODES[1]})"

# Paradas para ruta Barranquilla - Valledupar
curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[2]},
    \"name\": \"Terminal Barranquilla\",
    \"sequence\": 1,
    \"latitude\": 10.9878,
    \"longitude\": -74.7889
  }" > /dev/null
echo "   ✅ Parada: Terminal Barranquilla (Ruta: ${ROUTE_CODES[2]})"

curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[2]},
    \"name\": \"Terminal Valledupar\",
    \"sequence\": 2,
    \"latitude\": 10.4631,
    \"longitude\": -73.2532
  }" > /dev/null
echo "   ✅ Parada: Terminal Valledupar (Ruta: ${ROUTE_CODES[2]})"

# Paradas para ruta Valledupar - El Copey
curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[3]},
    \"name\": \"Terminal Valledupar\",
    \"sequence\": 1,
    \"latitude\": 10.4631,
    \"longitude\": -73.2532
  }" > /dev/null
echo "   ✅ Parada: Terminal Valledupar (Ruta: ${ROUTE_CODES[3]})"

curl -s -X POST "$API_BASE_URL/stops" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[3]},
    \"name\": \"El Copey\",
    \"sequence\": 2,
    \"latitude\": 10.1500,
    \"longitude\": -73.9611
  }" > /dev/null
echo "   ✅ Parada: El Copey (Ruta: ${ROUTE_CODES[3]})"

echo ""
echo "✅ Total paradas creadas: 8"
echo ""

# =================================================================
# 7. CREAR REGLAS DE TARIFA
# =================================================================
echo "💰 Creando reglas de tarifa..."

# Regla para ruta 1 (Santa Marta - Barranquilla)
# Precio base: 93 km * 500 = 46,500
curl -s -X POST "$API_BASE_URL/operations/fare-rules" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[0]},
    \"basePrice\": 46500,
    \"dynamicPricing\": false,
    \"childrenDiscount\": 0.25,
    \"seniorDiscount\": 0.15,
    \"studentDiscount\": 0.10
  }" > /dev/null
echo "   ✅ Regla de tarifa para ${ROUTE_CODES[0]} (Base: \$46,500, Children: 25%, Senior: 15%, Student: 10%)"

# Regla para ruta 2 (Santa Marta - Valledupar)
# Precio base: 185 km * 450 = 83,250
curl -s -X POST "$API_BASE_URL/operations/fare-rules" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[1]},
    \"basePrice\": 83250,
    \"dynamicPricing\": false,
    \"childrenDiscount\": 0.20,
    \"seniorDiscount\": 0.10,
    \"studentDiscount\": 0.05
  }" > /dev/null
echo "   ✅ Regla de tarifa para ${ROUTE_CODES[1]} (Base: \$83,250, Children: 20%, Senior: 10%, Student: 5%)"

# Regla para ruta 3 (Barranquilla - Valledupar)
# Precio base: 272 km * 480 = 130,560
curl -s -X POST "$API_BASE_URL/operations/fare-rules" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[2]},
    \"basePrice\": 130560,
    \"dynamicPricing\": true,
    \"childrenDiscount\": 0.30,
    \"seniorDiscount\": 0.20,
    \"studentDiscount\": 0.15
  }" > /dev/null
echo "   ✅ Regla de tarifa para ${ROUTE_CODES[2]} (Base: \$130,560, Dynamic Pricing, Children: 30%, Senior: 20%, Student: 15%)"

# Regla para ruta 4 (Valledupar - El Copey)
# Precio base: 45 km * 550 = 24,750
curl -s -X POST "$API_BASE_URL/operations/fare-rules" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"routeId\": ${ROUTE_IDS[3]},
    \"basePrice\": 24750,
    \"dynamicPricing\": false,
    \"childrenDiscount\": 0.25,
    \"seniorDiscount\": 0.15,
    \"studentDiscount\": 0.10
  }" > /dev/null
echo "   ✅ Regla de tarifa para ${ROUTE_CODES[3]} (Base: \$24,750, Children: 25%, Senior: 15%, Student: 10%)"

echo ""
echo "✅ Total reglas de tarifa creadas: 4 (1 por ruta)"
echo ""

# =================================================================
# 8. CREAR CONFIGURACIONES
# =================================================================
echo "⚙️  Creando configuraciones del sistema..."

curl -s -X POST "$API_BASE_URL/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "key": "max_seat_hold_minutes",
    "value": "10"
  }' > /dev/null
echo "   ✅ Config: max_seat_hold_minutes = 10"

curl -s -X POST "$API_BASE_URL/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "key": "max_baggage_weight_kg",
    "value": "25"
  }' > /dev/null
echo "   ✅ Config: max_baggage_weight_kg = 25"

curl -s -X POST "$API_BASE_URL/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "key": "baggage_fee_percentage",
    "value": "0.03"
  }' > /dev/null
echo "   ✅ Config: baggage_fee_percentage = 0.03"

echo ""
echo "✅ Total configuraciones creadas: 3"
echo ""

# =================================================================
# 9. CREAR TRIPS PARA NOVIEMBRE-DICIEMBRE 2025
# =================================================================
echo "🗓️  Creando trips para Noviembre-Diciembre 2025..."
echo ""

TRIP_COUNT=0
CREATED_TRIP_IDS=()

# Verificar que tenemos buses y rutas
if [ ${#BUS_IDS[@]} -eq 0 ]; then
  echo "⚠️  No hay buses disponibles. Obteniendo buses existentes..."
  BUSES_RESPONSE=$(curl -s -X GET "$API_BASE_URL/buses/all" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
  
  # Extraer IDs de buses del JSON (asumiendo array de objetos)
  BUS_IDS=($(echo "$BUSES_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -5))
  BUS_PLATES=($(echo "$BUSES_RESPONSE" | grep -o '"plate":"[^"]*"' | cut -d'"' -f4 | head -5))
  echo "   ✅ Encontrados ${#BUS_IDS[@]} buses: ${BUS_IDS[*]}"
fi

if [ ${#ROUTE_IDS[@]} -eq 0 ]; then
  echo "⚠️  No hay rutas disponibles. Obteniendo rutas existentes..."
  ROUTES_RESPONSE=$(curl -s -X GET "$API_BASE_URL/routes/all" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
  
  # Extraer IDs de rutas del JSON
  ROUTE_IDS=($(echo "$ROUTES_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -4))
  ROUTE_CODES=($(echo "$ROUTES_RESPONSE" | grep -o '"code":"[^"]*"' | cut -d'"' -f4 | head -4))
  echo "   ✅ Encontradas ${#ROUTE_IDS[@]} rutas: ${ROUTE_IDS[*]}"
fi

# Validar que tenemos datos
if [ ${#BUS_IDS[@]} -eq 0 ] || [ ${#ROUTE_IDS[@]} -eq 0 ]; then
  echo "❌ Error: No se pueden crear trips sin buses y rutas"
  exit 1
fi

echo ""
echo "📋 Usando para trips:"
echo "   Buses (${#BUS_IDS[@]}): ${BUS_IDS[*]}"
echo "   Rutas (${#ROUTE_IDS[@]}): ${ROUTE_IDS[*]}"
echo ""

TRIP_COUNT=0

# Horarios disponibles (hora militar)
TIMES=("06:00" "09:30" "13:00" "16:30" "20:00")

# Crear trips para resto de Noviembre (18-30) y todo Diciembre (1-31)
# Noviembre 2025: del 18 al 30
for DAY in {18..30}; do
  DAY_FORMATTED=$(printf "%02d" $DAY)
  DATE="2025-11-$DAY_FORMATTED"
  
  # Determinar cuántos trips crear para este día (2-4 trips aleatorios)
  NUM_TRIPS=$((2 + RANDOM % 3))
  
  echo "📅 $DATE - Creando $NUM_TRIPS trips..."
  
  for i in $(seq 1 $NUM_TRIPS); do
    # Seleccionar ruta rotativamente
    ROUTE_INDEX=$((TRIP_COUNT % ${#ROUTE_IDS[@]}))
    ROUTE_ID=${ROUTE_IDS[$ROUTE_INDEX]}
    ROUTE_CODE=${ROUTE_CODES[$ROUTE_INDEX]}
    
    # Seleccionar bus rotativamente
    BUS_INDEX=$((TRIP_COUNT % ${#BUS_IDS[@]}))
    BUS_ID=${BUS_IDS[$BUS_INDEX]}
    BUS_PLATE=${BUS_PLATES[$BUS_INDEX]}
    
    # Seleccionar hora aleatoria
    TIME_INDEX=$((RANDOM % ${#TIMES[@]}))
    TIME=${TIMES[$TIME_INDEX]}
    
    # Construir fecha y hora de salida
    DEPARTURE_DATETIME="${DATE}T${TIME}:00"
    
    # Calcular hora de llegada (agregar 2-5 horas a la salida)
    TRIP_DURATION=$((2 + RANDOM % 4))
    
    # Extraer hora y minuto
    HOUR=$(echo $TIME | cut -d':' -f1)
    MINUTE=$(echo $TIME | cut -d':' -f2)
    
    # Calcular nueva hora (puede pasar de 24)
    ARRIVAL_HOUR=$((10#$HOUR + TRIP_DURATION))
    ARRIVAL_MINUTE=$MINUTE
    
    # Ajustar día si la hora pasa de 24
    ARRIVAL_DAY=$DAY
    if [ $ARRIVAL_HOUR -ge 24 ]; then
      ARRIVAL_HOUR=$((ARRIVAL_HOUR - 24))
      ARRIVAL_DAY=$((DAY + 1))
    fi
    
    # Formatear hora y día de llegada
    ARRIVAL_HOUR_FORMATTED=$(printf "%02d" $ARRIVAL_HOUR)
    ARRIVAL_DAY_FORMATTED=$(printf "%02d" $ARRIVAL_DAY)
    ARRIVAL_MINUTE_FORMATTED=$(printf "%02d" $ARRIVAL_MINUTE)
    ARRIVAL_DATE="2025-11-$ARRIVAL_DAY_FORMATTED"
    ARRIVAL_DATETIME="${ARRIVAL_DATE}T${ARRIVAL_HOUR_FORMATTED}:${ARRIVAL_MINUTE_FORMATTED}:00"
    
    # Crear el trip (formato exacto del frontend)
    RESPONSE=$(curl -s -X POST "$API_BASE_URL/trips" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d "{\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$DEPARTURE_DATETIME\",\"arrivalAt\":\"$ARRIVAL_DATETIME\"}")
    
    TRIP_ID=$(extract_json_value "$RESPONSE" "id")
    
    if [ -n "$TRIP_ID" ]; then
      echo "   ✅ Trip $TRIP_ID: $ROUTE_CODE | Bus $BUS_PLATE | $TIME → ${ARRIVAL_HOUR_FORMATTED}:${ARRIVAL_MINUTE_FORMATTED}"
      TRIP_COUNT=$((TRIP_COUNT + 1))
      CREATED_TRIP_IDS+=("$TRIP_ID:$ROUTE_ID") # Store trip ID with its route ID
    else
      echo "   ⚠️  Error creando trip para $DATE $TIME"
      echo "   📄 Response: $RESPONSE"
      echo "   📄 Request: {\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$DEPARTURE_DATETIME\",\"arrivalAt\":\"$ARRIVAL_DATETIME\"}"
    fi
  done
  
  echo ""
done

# Diciembre 2025: del 1 al 31
for DAY in {1..31}; do
  # Formatear día con ceros a la izquierda
  DAY_FORMATTED=$(printf "%02d" $DAY)
  DATE="2025-12-$DAY_FORMATTED"
  
  # Determinar cuántos trips crear para este día (2-4 trips aleatorios)
  NUM_TRIPS=$((2 + RANDOM % 3))
  
  echo "📅 $DATE - Creando $NUM_TRIPS trips..."
  
  for i in $(seq 1 $NUM_TRIPS); do
    # Seleccionar ruta rotativamente
    ROUTE_INDEX=$((TRIP_COUNT % ${#ROUTE_IDS[@]}))
    ROUTE_ID=${ROUTE_IDS[$ROUTE_INDEX]}
    ROUTE_CODE=${ROUTE_CODES[$ROUTE_INDEX]}
    
    # Seleccionar bus rotativamente
    BUS_INDEX=$((TRIP_COUNT % ${#BUS_IDS[@]}))
    BUS_ID=${BUS_IDS[$BUS_INDEX]}
    BUS_PLATE=${BUS_PLATES[$BUS_INDEX]}
    
    # Seleccionar hora aleatoria
    TIME_INDEX=$((RANDOM % ${#TIMES[@]}))
    TIME=${TIMES[$TIME_INDEX]}
    
    # Determinar duración según la ruta
    case $ROUTE_INDEX in
      0) DURATION=120 ;;  # Santa Marta - Barranquilla (2h)
      1) DURATION=240 ;;  # Santa Marta - Valledupar (4h)
      2) DURATION=300 ;;  # Barranquilla - Valledupar (5h)
      3) DURATION=60 ;;   # Valledupar - El Copey (1h)
    esac
    
    # Calcular hora de llegada
    DEPARTURE_DATETIME="${DATE}T${TIME}:00"
    
    # Calcular hora de llegada (simple: sumar minutos)
    HOUR=$(echo $TIME | cut -d: -f1)
    MINUTE=$(echo $TIME | cut -d: -f2)
    TOTAL_MINUTES=$((10#$HOUR * 60 + 10#$MINUTE + DURATION))
    ARRIVAL_HOUR=$((TOTAL_MINUTES / 60))
    ARRIVAL_MINUTE=$((TOTAL_MINUTES % 60))
    
    # Manejar cambio de día si la llegada pasa de medianoche
    if [ $ARRIVAL_HOUR -ge 24 ]; then
      ARRIVAL_HOUR=$((ARRIVAL_HOUR - 24))
      NEXT_DAY=$((DAY + 1))
      NEXT_DAY_FORMATTED=$(printf "%02d" $NEXT_DAY)
      ARRIVAL_DATE="2025-12-$NEXT_DAY_FORMATTED"
    else
      ARRIVAL_DATE=$DATE
    fi
    
    ARRIVAL_HOUR_FORMATTED=$(printf "%02d" $ARRIVAL_HOUR)
    ARRIVAL_MINUTE_FORMATTED=$(printf "%02d" $ARRIVAL_MINUTE)
    ARRIVAL_DATETIME="${ARRIVAL_DATE}T${ARRIVAL_HOUR_FORMATTED}:${ARRIVAL_MINUTE_FORMATTED}:00"
    
    # Validar que tengamos los IDs necesarios
    if [ -z "$BUS_ID" ] || [ -z "$ROUTE_ID" ]; then
      echo "   ⚠️  Error: Bus ID o Route ID vacío (Bus: $BUS_ID, Route: $ROUTE_ID)"
      echo "   📊 Debug - ROUTE_INDEX: $ROUTE_INDEX, BUS_INDEX: $BUS_INDEX"
      echo "   📊 Debug - Total rutas: ${#ROUTE_IDS[@]}, Total buses: ${#BUS_IDS[@]}"
      echo "   📊 Debug - ROUTE_IDS: ${ROUTE_IDS[*]}"
      echo "   📊 Debug - BUS_IDS: ${BUS_IDS[*]}"
      continue
    fi
    
    # Crear el trip (formato exacto del frontend)
    RESPONSE=$(curl -s -X POST "$API_BASE_URL/trips" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d "{\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$DEPARTURE_DATETIME\",\"arrivalAt\":\"$ARRIVAL_DATETIME\"}")
    
    TRIP_ID=$(extract_json_value "$RESPONSE" "id")
    
    if [ -n "$TRIP_ID" ]; then
      echo "   ✅ Trip $TRIP_ID: $ROUTE_CODE | Bus $BUS_PLATE | $TIME → ${ARRIVAL_HOUR_FORMATTED}:${ARRIVAL_MINUTE_FORMATTED}"
      TRIP_COUNT=$((TRIP_COUNT + 1))
      CREATED_TRIP_IDS+=("$TRIP_ID:$ROUTE_ID") # Store trip ID with its route ID
    else
      echo "   ⚠️  Error creando trip para $DATE $TIME"
      echo "   📄 Response: $RESPONSE"
      echo "   📄 Request: {\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$DEPARTURE_DATETIME\",\"arrivalAt\":\"$ARRIVAL_DATETIME\"}"
    fi
  done
  
  echo ""
done

echo ""
echo "✅ Total trips creados: $TRIP_COUNT"
echo ""

# =================================================================
# 10. CREAR TICKETS DE PASAJEROS PARA SHOWCASE/TESTING
# =================================================================
echo "🎫 Creando tickets de pasajeros para showcase..."
echo ""

if [ ${#CREATED_TRIP_IDS[@]} -eq 0 ]; then
  echo "⚠️  No se encontraron trips para crear tickets"
else
  echo "📋 Creando tickets para ${#CREATED_TRIP_IDS[@]} trips..."
  
  TICKET_COUNT=0
  
  # Use passenger accounts, if empty use all accounts
  if [ ${#ACCOUNT_IDS[@]} -gt 1 ]; then
    PASSENGER_ACCOUNTS=("${ACCOUNT_IDS[@]:1}") # Skip admin (index 0), use passengers
  else
    PASSENGER_ACCOUNTS=("${ACCOUNT_IDS[@]}") # Use all accounts including admin if needed
  fi
  
  # Verify we have passenger accounts
  if [ ${#PASSENGER_ACCOUNTS[@]} -eq 0 ]; then
    echo "⚠️  No hay cuentas de pasajeros disponibles para crear tickets"
    PASSENGER_ACCOUNTS=("1") # Use default account ID 1 as fallback
  fi
  
  echo "   Usando ${#PASSENGER_ACCOUNTS[@]} cuentas de pasajeros: ${PASSENGER_ACCOUNTS[*]}"
  
  # Take first 15 trips or all if less than 15
  MAX_TRIPS=$((${#CREATED_TRIP_IDS[@]} < 15 ? ${#CREATED_TRIP_IDS[@]} : 15))
  
  for i in $(seq 0 $((MAX_TRIPS - 1))); do
    TRIP_INFO="${CREATED_TRIP_IDS[$i]}"
    TRIP_ID=$(echo "$TRIP_INFO" | cut -d':' -f1)
    TRIP_ROUTE_ID=$(echo "$TRIP_INFO" | cut -d':' -f2)
    
    # Get route stops
    ROUTE_STOPS=$(curl -s -X GET "$API_BASE_URL/routes/$TRIP_ROUTE_ID/stops" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    # Extract first and last stop IDs
    FIRST_STOP_ID=$(echo "$ROUTE_STOPS" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    LAST_STOP_ID=$(echo "$ROUTE_STOPS" | grep -o '"id":[0-9]*' | tail -1 | cut -d':' -f2)
    
    if [ -n "$FIRST_STOP_ID" ] && [ -n "$LAST_STOP_ID" ]; then
      # Create 3-8 random tickets per trip
      NUM_TICKETS=$((3 + RANDOM % 6))
      
      for j in $(seq 1 $NUM_TICKETS); do
        # Random passenger
        PASSENGER_INDEX=$((RANDOM % ${#PASSENGER_ACCOUNTS[@]}))
        ACCOUNT_ID=${PASSENGER_ACCOUNTS[$PASSENGER_INDEX]}
        
        # Random seat (A1-E10)
        SEAT_ROW=$((1 + RANDOM % 10))
        SEAT_LETTERS=("A" "B" "C" "D" "E")
        SEAT_LETTER=${SEAT_LETTERS[$((RANDOM % 5))]}
        SEAT_NUMBER="${SEAT_LETTER}${SEAT_ROW}"
        
        # Random passenger type for discounts
        PASSENGER_TYPES=("ADULT" "STUDENT" "SENIOR" "CHILDREN")
        PASSENGER_TYPE=${PASSENGER_TYPES[$((RANDOM % 4))]}
        
        # Create ticket
        TICKET_RESPONSE=$(curl -s -X POST "$API_BASE_URL/tickets" \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer $ADMIN_TOKEN" \
          -d "{
            \"tripId\": $TRIP_ID,
            \"accountId\": $ACCOUNT_ID,
            \"fromStopId\": $FIRST_STOP_ID,
            \"toStopId\": $LAST_STOP_ID,
            \"seatNumber\": \"$SEAT_NUMBER\",
            \"passengerType\": \"$PASSENGER_TYPE\",
            \"paymentMethod\": \"CARD\"
          }")
        
        TICKET_ID=$(extract_json_value "$TICKET_RESPONSE" "id")
        
        if [ -n "$TICKET_ID" ]; then
          # Mark ticket as paid (confirm it)
          curl -s -X POST "$API_BASE_URL/tickets/payments/$TICKET_ID" \
            -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null 2>&1
          
          TICKET_COUNT=$((TICKET_COUNT + 1))
        fi
      done
      
      echo "   ✅ Trip $TRIP_ID: $NUM_TICKETS tickets creados"
    else
      echo "   ⚠️  No se pudieron obtener paradas para trip $TRIP_ID (Route $TRIP_ROUTE_ID)"
    fi
  done
  
  echo ""
  echo "✅ Total tickets creados: $TICKET_COUNT"
fi

echo ""

# =================================================================
# RESUMEN FINAL
# =================================================================
echo "════════════════════════════════════════════════════════════"
echo "🎉 POBLADO DE BASE DE DATOS COMPLETADO"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "📊 Resumen de datos creados:"
echo "   • Cuentas: 6 (1 admin + 5 usuarios)"
echo "   • Buses: ${#BUS_IDS[@]}"
echo "   • Asientos: $TOTAL_SEATS_CREATED (40+35+30+45+25)"
echo "   • Rutas: ${#ROUTE_IDS[@]}"
echo "   • Paradas: 8"
echo "   • Reglas de tarifa: 4 (1 por ruta)"
echo "   • Configuraciones: 3"
echo "   • Trips (Nov 18-30 + Dic 1-31): $TRIP_COUNT"
echo "   • Tickets vendidos: $TICKET_COUNT"
echo ""
echo "🔑 Credenciales de Admin:"
echo "   Email: j@lojur.com"
echo "   Password: 123"
echo ""
echo "✅ La base de datos está lista para usar"
echo "════════════════════════════════════════════════════════════"
