#!/bin/bash

# =================================================================
# Script COMPLETO para poblar base de datos - InterMuniFast
# - Datos desde 6 meses atrás (Mayo 2025) hasta 6 meses adelante (Mayo 2026)
# - 3 trips URGENTES que salen en 5 minutos con 95% ocupación
# - Roles: Admin, Dispatchers (2), Drivers (5), Pasajeros (10)
# - Tickets históricos confirmados para métricas realistas
# =================================================================

set -e

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api}"
ADMIN_TOKEN=""

# Obtener fecha/hora actual
CURRENT_DATETIME=$(date +"%Y-%m-%d %H:%M:%S")
CURRENT_DATE=$(date +"%Y-%m-%d")
CURRENT_TIME=$(date +"%H:%M")
URGENT_TIME_30MIN=$(date -d "+30 minutes" +"%H:%M")
URGENT_DATETIME_30MIN=$(date -d "+30 minutes" +"%Y-%m-%dT%H:%M:00")
URGENT_TIME_1H=$(date -d "+1 hour" +"%H:%M")
URGENT_DATETIME_1H=$(date -d "+1 hour" +"%Y-%m-%dT%H:%M:00")

echo "════════════════════════════════════════════════════════════"
echo "🚀 INTER MUNI FAST - Seed Data Generator"
echo "════════════════════════════════════════════════════════════"
echo "🕒 Current DateTime: $CURRENT_DATETIME"
echo "⏰ Urgent Trip 1: $URGENT_DATETIME_30MIN (in 30 minutes)"
echo "⏰ Urgent Trip 2: $URGENT_DATETIME_30MIN (in 30 minutes)"
echo "⏰ Urgent Trip 3: $URGENT_DATETIME_1H (in 1 hour)"
echo "📡 API Base URL: $API_BASE_URL"
echo "════════════════════════════════════════════════════════════"
echo ""

# Función para extraer JSON
extract_json() {
  echo "$1" | grep -o "\"$2\":[^,}]*" | head -1 | sed 's/.*://g' | tr -d '"' | sed 's/^ *//g' | sed 's/ *$//g'
}

# =================================================================
# 1. CREAR CUENTAS
# =================================================================
echo "👥 Creating accounts..."

# Admin
RESP=$(curl -s -X POST "$API_BASE_URL/accounts" -H "Content-Type: application/json" -d '{"name":"Admin System","email":"j@lojur.com","phone":"+573001234567","password":"123","isAdmin":true}')
ADMIN_ID=$(extract_json "$RESP" "id")
echo "   ✅ ADMIN: j@lojur.com (ID: $ADMIN_ID)"

# Dispatchers
declare -a DISPATCHER_IDS
for i in 1 2; do
  RESP=$(curl -s -X POST "$API_BASE_URL/accounts" -H "Content-Type: application/json" -d "{\"name\":\"Dispatcher $i\",\"email\":\"dispatcher$i@intermunifast.com\",\"phone\":\"+57310${i}234567\",\"password\":\"dispatch123\",\"isAdmin\":false}")
  ID=$(extract_json "$RESP" "id")
  DISPATCHER_IDS+=("$ID")
  echo "   ✅ DISPATCHER: dispatcher$i@intermunifast.com (ID: $ID)"
done

# Drivers
declare -a DRIVER_IDS
DRIVER_NAMES=("José Ramírez" "Pedro González" "Luis Martínez" "Roberto Silva" "Miguel Torres")
for i in {1..5}; do
  RESP=$(curl -s -X POST "$API_BASE_URL/accounts" -H "Content-Type: application/json" -d "{\"name\":\"${DRIVER_NAMES[$i-1]}\",\"email\":\"driver$i@intermunifast.com\",\"phone\":\"+57320${i}234567\",\"password\":\"driver123\",\"isAdmin\":false}")
  ID=$(extract_json "$RESP" "id")
  DRIVER_IDS+=("$ID")
  echo "   ✅ DRIVER: driver$i@intermunifast.com (ID: $ID)"
done

# Passengers
declare -a USER_IDS
PASSENGER_NAMES=("María García" "Juan Pérez" "Ana Rodríguez" "Carlos Martínez" "Laura López" "Diego Hernández" "Sofía Ramírez" "Andrés Morales" "Valentina Castro" "Santiago Vargas")
PASSENGER_EMAILS=("maria.garcia" "juan.perez" "ana.rodriguez" "carlos.martinez" "laura.lopez" "diego.hernandez" "sofia.ramirez" "andres.morales" "valentina.castro" "santiago.vargas")
for i in {0..9}; do
  RESP=$(curl -s -X POST "$API_BASE_URL/accounts" -H "Content-Type: application/json" -d "{\"name\":\"${PASSENGER_NAMES[$i]}\",\"email\":\"${PASSENGER_EMAILS[$i]}@example.com\",\"phone\":\"+5730${i}9876543\",\"password\":\"password123\",\"isAdmin\":false}")
  ID=$(extract_json "$RESP" "id")
  USER_IDS+=("$ID")
  echo "   ✅ PASSENGER: ${PASSENGER_EMAILS[$i]}@example.com (ID: $ID)"
done

echo ""
echo "✅ Accounts: $((1 + ${#DISPATCHER_IDS[@]} + ${#DRIVER_IDS[@]} + ${#USER_IDS[@]})) (1 admin + 2 dispatchers + 5 drivers + 10 passengers)"
echo ""

# =================================================================
# 2. AUTENTICACIÓN
# =================================================================
echo "🔐 Authenticating..."
AUTH_RESP=$(curl -s -X POST "$API_BASE_URL/auth/signin" -H "Content-Type: application/json" -d '{"email":"j@lojur.com","password":"123"}')
ADMIN_TOKEN=$(extract_json "$AUTH_RESP" "token")
if [ -z "$ADMIN_TOKEN" ]; then echo "❌ Auth failed"; exit 1; fi
echo "   ✅ Token obtained (${#ADMIN_TOKEN} chars)"
echo ""

# =================================================================
# 3. CREAR BUSES
# =================================================================
echo "🚌 Creating buses..."
declare -a BUS_IDS BUS_PLATES
CAPACITIES=(40 35 30 45 25)
PLATES=("ABC123" "DEF456" "GHI789" "JKL012" "MNO345")
for i in {0..4}; do
  RESP=$(curl -s -X POST "$API_BASE_URL/buses" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"plate\":\"${PLATES[$i]}\",\"capacity\":${CAPACITIES[$i]},\"amenities\":[{\"name\":\"WiFi\",\"description\":\"Internet\"},{\"name\":\"AC\",\"description\":\"A/C\"}]}")
  BUS_ID=$(extract_json "$RESP" "id")
  BUS_IDS+=("$BUS_ID")
  BUS_PLATES+=("${PLATES[$i]}")
  echo "   ✅ Bus ${PLATES[$i]} (ID: $BUS_ID, Capacity: ${CAPACITIES[$i]})"
done
echo "   Total: ${#BUS_IDS[@]} buses"
echo ""

# =================================================================
# 4. CREAR ASIENTOS
# =================================================================
echo "💺 Creating seats..."
TOTAL_SEATS=0
for i in {0..4}; do
  BUS_ID=${BUS_IDS[$i]}
  CAPACITY=${CAPACITIES[$i]}
  PREF=$((CAPACITY / 10)) # 10% preferential
  for SEAT_NUM in $(seq 1 $CAPACITY); do
    TYPE=$( [ $SEAT_NUM -le $PREF ] && echo "PREFERENTIAL" || echo "STANDARD" )
    curl -s -X POST "$API_BASE_URL/seats" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"number\":\"$SEAT_NUM\",\"type\":\"$TYPE\",\"busId\":$BUS_ID}" > /dev/null
    TOTAL_SEATS=$((TOTAL_SEATS + 1))
  done
  echo "   ✅ Bus ${BUS_PLATES[$i]}: $CAPACITY seats ($PREF preferential)"
done
echo "   Total: $TOTAL_SEATS seats"
echo ""

# =================================================================
# 5. CREAR RUTAS
# =================================================================
echo "🛣️  Creating routes..."
declare -a ROUTE_IDS ROUTE_CODES
ROUTES_DATA=(
  "RT-SMT-BAQ|Santa Marta - Barranquilla|Santa Marta|Barranquilla|93|120|500"
  "RT-SMT-VDU|Santa Marta - Valledupar|Santa Marta|Valledupar|185|240|450"
  "RT-BAQ-VDU|Barranquilla - Valledupar|Barranquilla|Valledupar|272|300|480"
  "RT-VDU-CPY|Valledupar - El Copey|Valledupar|El Copey|45|60|550"
)

for ROUTE_DATA in "${ROUTES_DATA[@]}"; do
  IFS='|' read -r CODE NAME ORIGIN DEST DIST DUR PRICE <<< "$ROUTE_DATA"
  RESP=$(curl -s -X POST "$API_BASE_URL/routes" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"code\":\"$CODE\",\"name\":\"$NAME\",\"origin\":\"$ORIGIN\",\"destination\":\"$DEST\",\"distanceKm\":$DIST,\"durationMinutes\":$DUR,\"pricePerKm\":$PRICE}")
  ROUTE_ID=$(extract_json "$RESP" "id")
  ROUTE_IDS+=("$ROUTE_ID")
  ROUTE_CODES+=("$CODE")
  echo "   ✅ $CODE (ID: $ROUTE_ID, ${DIST}km, ~${DUR}min)"
done
echo "   Total: ${#ROUTE_IDS[@]} routes"
echo ""

# =================================================================
# 6. CREAR PARADAS
# =================================================================
echo "🚏 Creating stops..."
# Route 1: SMT-BAQ
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[0]},\"name\":\"Terminal Santa Marta\",\"sequence\":1,\"latitude\":11.2408,\"longitude\":-74.1990}" > /dev/null
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[0]},\"name\":\"Terminal Barranquilla\",\"sequence\":2,\"latitude\":10.9878,\"longitude\":-74.7889}" > /dev/null
# Route 2: SMT-VDU
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[1]},\"name\":\"Terminal Santa Marta\",\"sequence\":1,\"latitude\":11.2408,\"longitude\":-74.1990}" > /dev/null
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[1]},\"name\":\"Fundación\",\"sequence\":2,\"latitude\":10.5214,\"longitude\":-74.1852}" > /dev/null
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[1]},\"name\":\"Terminal Valledupar\",\"sequence\":3,\"latitude\":10.4631,\"longitude\":-73.2532}" > /dev/null
# Route 3: BAQ-VDU
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[2]},\"name\":\"Terminal Barranquilla\",\"sequence\":1,\"latitude\":10.9878,\"longitude\":-74.7889}" > /dev/null
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[2]},\"name\":\"Terminal Valledupar\",\"sequence\":2,\"latitude\":10.4631,\"longitude\":-73.2532}" > /dev/null
# Route 4: VDU-CPY
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[3]},\"name\":\"Terminal Valledupar\",\"sequence\":1,\"latitude\":10.4631,\"longitude\":-73.2532}" > /dev/null
curl -s -X POST "$API_BASE_URL/stops" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[3]},\"name\":\"El Copey\",\"sequence\":2,\"latitude\":10.1500,\"longitude\":-73.9611}" > /dev/null
echo "   ✅ 8 stops created across 4 routes"
echo ""

# =================================================================
# 7. REGLAS DE TARIFA
# =================================================================
echo "💰 Creating fare rules..."
BASE_PRICES=(46500 83250 130560 24750)
for i in {0..3}; do
  curl -s -X POST "$API_BASE_URL/operations/fare-rules" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":${ROUTE_IDS[$i]},\"basePrice\":${BASE_PRICES[$i]},\"dynamicPricing\":false,\"childrenDiscount\":0.25,\"seniorDiscount\":0.15,\"studentDiscount\":0.10}" > /dev/null
  echo "   ✅ ${ROUTE_CODES[$i]}: \$${BASE_PRICES[$i]}"
done
echo ""

# =================================================================
# 8. CONFIGURACIONES
# =================================================================
echo "⚙️  Creating configs..."
curl -s -X POST "$API_BASE_URL/config" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d '{"key":"max_seat_hold_minutes","value":"10"}' > /dev/null
curl -s -X POST "$API_BASE_URL/config" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d '{"key":"max_baggage_weight_kg","value":"25"}' > /dev/null
curl -s -X POST "$API_BASE_URL/config" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d '{"key":"baggage_fee_percentage","value":"0.03"}' > /dev/null
echo "   ✅ 3 system configs created"
echo ""

# =================================================================
# 9. CREAR TRIPS - 6 MESES PASADO A 6 MESES FUTURO
# =================================================================
echo "🗓️  Creating trips (May 2025 - May 2026)..."
TRIP_COUNT=0
declare -a CREATED_TRIP_IDS
TIMES=("06:00" "09:30" "13:00" "16:30" "20:00")

# Función crear trip
create_trip() {
  local DATE=$1
  local TIME=$2
  local ROUTE_IDX=$3
  local BUS_IDX=$4
  
  ROUTE_ID=${ROUTE_IDS[$ROUTE_IDX]}
  BUS_ID=${BUS_IDS[$BUS_IDX]}
  DRIVER_ID=${DRIVER_IDS[$((TRIP_COUNT % ${#DRIVER_IDS[@]}))]}
  
  DEP_DT="${DATE}T${TIME}:00"
  
  # Calcular llegada
  case $ROUTE_IDX in
    0) DUR=120 ;;
    1) DUR=240 ;;
    2) DUR=300 ;;
    3) DUR=60 ;;
  esac
  
  ARR_DT=$(date -d "$DEP_DT +${DUR} minutes" +"%Y-%m-%dT%H:%M:00" 2>/dev/null || echo "${DATE}T23:59:00")
  
  RESP=$(curl -s -X POST "$API_BASE_URL/trips" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$DEP_DT\",\"arrivalAt\":\"$ARR_DT\"}")
  TRIP_ID=$(extract_json "$RESP" "id")
  
  if [ -n "$TRIP_ID" ]; then
    CREATED_TRIP_IDS+=("$TRIP_ID:$ROUTE_ID")
    TRIP_COUNT=$((TRIP_COUNT + 1))
    
    # Asignar driver
    curl -s -X POST "$API_BASE_URL/operations/assignments" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"driverId\":$DRIVER_ID,\"type\":\"DRIVER\"}" > /dev/null 2>&1
    
    echo "."
  fi
}

# Mayo 2025 (pasado)
for DAY in {1..31}; do
  DATE=$(printf "2025-05-%02d" $DAY)
  NUM_TRIPS=$((2 + RANDOM % 3))
  for i in $(seq 1 $NUM_TRIPS); do
    TIME_IDX=$((RANDOM % 5))
    ROUTE_IDX=$((RANDOM % 4))
    BUS_IDX=$((RANDOM % 5))
    create_trip "$DATE" "${TIMES[$TIME_IDX]}" $ROUTE_IDX $BUS_IDX
  done
done
echo "   ✅ May 2025: Complete"

# Junio-Noviembre 2025 (pasado/presente)
for MONTH in {6..11}; do
  DAYS=30
  [ $MONTH -eq 7 ] && DAYS=31
  [ $MONTH -eq 8 ] && DAYS=31
  [ $MONTH -eq 10 ] && DAYS=31
  
  for DAY in $(seq 1 $DAYS); do
    DATE=$(printf "2025-%02d-%02d" $MONTH $DAY)
    NUM_TRIPS=$((2 + RANDOM % 3))
    for i in $(seq 1 $NUM_TRIPS); do
      TIME_IDX=$((RANDOM % 5))
      ROUTE_IDX=$((RANDOM % 4))
      BUS_IDX=$((RANDOM % 5))
      create_trip "$DATE" "${TIMES[$TIME_IDX]}" $ROUTE_IDX $BUS_IDX
    done
  done
  echo "   ✅ Month 2025-$MONTH: Complete"
done

# Diciembre 2025 - Mayo 2026 (futuro)
for DAY in {1..31}; do
  DATE=$(printf "2025-12-%02d" $DAY)
  NUM_TRIPS=$((2 + RANDOM % 3))
  for i in $(seq 1 $NUM_TRIPS); do
    TIME_IDX=$((RANDOM % 5))
    ROUTE_IDX=$((RANDOM % 4))
    BUS_IDX=$((RANDOM % 5))
    create_trip "$DATE" "${TIMES[$TIME_IDX]}" $ROUTE_IDX $BUS_IDX
  done
done
echo "   ✅ Dec 2025: Complete"

for MONTH in {1..5}; do
  DAYS=31
  [ $MONTH -eq 2 ] && DAYS=28
  [ $MONTH -eq 4 ] && DAYS=30
  
  for DAY in $(seq 1 $DAYS); do
    DATE=$(printf "2026-%02d-%02d" $MONTH $DAY)
    NUM_TRIPS=$((2 + RANDOM % 3))
    for i in $(seq 1 $NUM_TRIPS); do
      TIME_IDX=$((RANDOM % 5))
      ROUTE_IDX=$((RANDOM % 4))
      BUS_IDX=$((RANDOM % 5))
      create_trip "$DATE" "${TIMES[$TIME_IDX]}" $ROUTE_IDX $BUS_IDX
    done
  done
  echo "   ✅ Month 2026-$MONTH: Complete"
done

echo ""
echo "✅ Total trips created: $TRIP_COUNT"
echo ""

# =================================================================
# 10. CREAR 3 TRIPS URGENTES (2 en 30min, 1 en 1h) - 97% OCUPACIÓN
# =================================================================
echo "🚨 Creating 3 URGENT trips (2 departing in 30min, 1 in 1hour with 97% occupation)..."

# Trip 1: Depart in 30 minutes
ROUTE_IDX=0
BUS_IDX=0
BUS_ID=${BUS_IDS[$BUS_IDX]}
ROUTE_ID=${ROUTE_IDS[$ROUTE_IDX]}
CAPACITY=${CAPACITIES[$BUS_IDX]}

# Calcular llegada
DUR=120
ARR_DT=$(date -d "$URGENT_DATETIME_30MIN +${DUR} minutes" +"%Y-%m-%dT%H:%M:00" 2>/dev/null || echo "${CURRENT_DATE}T23:59:00")

RESP=$(curl -s -X POST "$API_BASE_URL/trips" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$URGENT_DATETIME_30MIN\",\"arrivalAt\":\"$ARR_DT\"}")
TRIP_ID=$(extract_json "$RESP" "id")

if [ -n "$TRIP_ID" ]; then
  echo "   🚨 URGENT Trip $TRIP_ID: ${ROUTE_CODES[$ROUTE_IDX]} | Bus ${BUS_PLATES[$BUS_IDX]} | Departs at $URGENT_TIME_30MIN (30 min)"
  
  # Asignar driver
  DRIVER_ID=${DRIVER_IDS[0]}
  curl -s -X POST "$API_BASE_URL/operations/assignments" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"driverId\":$DRIVER_ID,\"type\":\"DRIVER\"}" > /dev/null 2>&1
  
  # Obtener paradas
  STOPS_RESP=$(curl -s -X GET "$API_BASE_URL/routes/$ROUTE_ID/stops" -H "Authorization: Bearer $ADMIN_TOKEN")
  FIRST_STOP=$(echo "$STOPS_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  LAST_STOP=$(echo "$STOPS_RESP" | grep -o '"id":[0-9]*' | tail -1 | cut -d':' -f2)
  
  # Si no hay stops, usar null (full route)
  FROM_STOP_JSON=$([ -n "$FIRST_STOP" ] && echo "$FIRST_STOP" || echo "null")
  TO_STOP_JSON=$([ -n "$LAST_STOP" ] && echo "$LAST_STOP" || echo "null")
  
  # Estrategia: Crear todos los tickets en el MISMO SEGMENTO (full route: origin -> destination)
  # Esto garantiza que la ocupación del segmento llegue al 95% y los últimos tickets sean PENDING_APPROVAL
  
  CONFIRMED_COUNT=$(( (CAPACITY * 95 + 50) / 100 ))  # Redondeo: 30*95=2850, +50=2900, /100=29
  TOTAL_TICKETS=$CAPACITY  # 100% de capacidad para garantizar tickets PENDING_APPROVAL
  PENDING_COUNT=$((TOTAL_TICKETS - CONFIRMED_COUNT))
  
  echo "      📍 Creating $CONFIRMED_COUNT confirmed + $PENDING_COUNT pending (100% of $CAPACITY seats in FULL ROUTE)"
  
  # Paso 1: Crear 95% de tickets en el mismo segmento (full route) y confirmar pagos
  for SEAT in $(seq 1 $CONFIRMED_COUNT); do
    PASSENGER_IDX=$((RANDOM % ${#USER_IDS[@]}))
    ACCOUNT_ID=${USER_IDS[$PASSENGER_IDX]}
    
    # Todos los tickets usan fromStop=null, toStop=null (ruta completa)
    TICKET_RESP=$(curl -s -X POST "$API_BASE_URL/tickets" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"accountId\":$ACCOUNT_ID,\"fromStopId\":null,\"toStopId\":null,\"seatNumber\":\"$SEAT\",\"passengerType\":\"ADULT\",\"paymentMethod\":\"CARD\"}")
    
    TICKET_ID=$(extract_json "$TICKET_RESP" "id")
    if [ -n "$TICKET_ID" ]; then
      # Confirmar pago -> status=CONFIRMED, paymentStatus=COMPLETED
      curl -s -X POST "$API_BASE_URL/tickets/payments/$TICKET_ID" -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null 2>&1
    fi
  done
  
  # Paso 2: Crear hasta 100% en el MISMO SEGMENTO (full route)
  # Backend detecta: ocupación en segmento full route >= 95% -> status=PENDING_APPROVAL
  for SEAT in $(seq $((CONFIRMED_COUNT + 1)) $TOTAL_TICKETS); do
    PASSENGER_IDX=$((RANDOM % ${#USER_IDS[@]}))
    ACCOUNT_ID=${USER_IDS[$PASSENGER_IDX]}
    
    # Mismo segmento (full route) -> Backend ve 95% ocupado -> PENDING_APPROVAL
    TICKET_RESP=$(curl -s -X POST "$API_BASE_URL/tickets" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"accountId\":$ACCOUNT_ID,\"fromStopId\":null,\"toStopId\":null,\"seatNumber\":\"$SEAT\",\"passengerType\":\"ADULT\",\"paymentMethod\":\"CARD\"}")
    
    TICKET_ID=$(extract_json "$TICKET_RESP" "id")
    echo -n "."  # Progress indicator
  done
  
  echo ""
  echo "      ✅ Created $CONFIRMED_COUNT confirmed + $PENDING_COUNT pending approval tickets (97% of $CAPACITY seats in FULL ROUTE segment)"
fi

# Trip 2: Depart in 30 minutes
ROUTE_IDX=1
BUS_IDX=1
BUS_ID=${BUS_IDS[$BUS_IDX]}
ROUTE_ID=${ROUTE_IDS[$ROUTE_IDX]}
CAPACITY=${CAPACITIES[$BUS_IDX]}

DUR=240
ARR_DT=$(date -d "$URGENT_DATETIME_30MIN +${DUR} minutes" +"%Y-%m-%dT%H:%M:00" 2>/dev/null || echo "${CURRENT_DATE}T23:59:00")

RESP=$(curl -s -X POST "$API_BASE_URL/trips" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$URGENT_DATETIME_30MIN\",\"arrivalAt\":\"$ARR_DT\"}")
TRIP_ID=$(extract_json "$RESP" "id")

if [ -n "$TRIP_ID" ]; then
  echo "   🚨 URGENT Trip $TRIP_ID: ${ROUTE_CODES[$ROUTE_IDX]} | Bus ${BUS_PLATES[$BUS_IDX]} | Departs at $URGENT_TIME_30MIN (30 min)"
  
  DRIVER_ID=${DRIVER_IDS[1]}
  curl -s -X POST "$API_BASE_URL/operations/assignments" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"driverId\":$DRIVER_ID,\"type\":\"DRIVER\"}" > /dev/null 2>&1
  
  STOPS_RESP=$(curl -s -X GET "$API_BASE_URL/routes/$ROUTE_ID/stops" -H "Authorization: Bearer $ADMIN_TOKEN")
  FIRST_STOP=$(echo "$STOPS_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  LAST_STOP=$(echo "$STOPS_RESP" | grep -o '"id":[0-9]*' | tail -1 | cut -d':' -f2)
  
  FROM_STOP_JSON=$([ -n "$FIRST_STOP" ] && echo "$FIRST_STOP" || echo "null")
  TO_STOP_JSON=$([ -n "$LAST_STOP" ] && echo "$LAST_STOP" || echo "null")
  
  CONFIRMED_COUNT=$(( (CAPACITY * 95 + 50) / 100 ))
  TOTAL_TICKETS=$CAPACITY
  PENDING_COUNT=$((TOTAL_TICKETS - CONFIRMED_COUNT))
  
  echo "      📍 Creating $CONFIRMED_COUNT confirmed + $PENDING_COUNT pending (100% of $CAPACITY seats in FULL ROUTE)"
  
  # Paso 1: Crear y confirmar 95%
  for SEAT in $(seq 1 $CONFIRMED_COUNT); do
    PASSENGER_IDX=$((RANDOM % ${#USER_IDS[@]}))
    ACCOUNT_ID=${USER_IDS[$PASSENGER_IDX]}
    
    TICKET_RESP=$(curl -s -X POST "$API_BASE_URL/tickets" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"accountId\":$ACCOUNT_ID,\"fromStopId\":null,\"toStopId\":null,\"seatNumber\":\"$SEAT\",\"passengerType\":\"ADULT\",\"paymentMethod\":\"CARD\"}")
    
    TICKET_ID=$(extract_json "$TICKET_RESP" "id")
    if [ -n "$TICKET_ID" ]; then
      curl -s -X POST "$API_BASE_URL/tickets/payments/$TICKET_ID" -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null 2>&1
    fi
  done
  
  # Paso 2: Crear 2% más (PENDING_APPROVAL)
  for SEAT in $(seq $((CONFIRMED_COUNT + 1)) $TOTAL_TICKETS); do
    PASSENGER_IDX=$((RANDOM % ${#USER_IDS[@]}))
    ACCOUNT_ID=${USER_IDS[$PASSENGER_IDX]}
    
    TICKET_RESP=$(curl -s -X POST "$API_BASE_URL/tickets" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"accountId\":$ACCOUNT_ID,\"fromStopId\":null,\"toStopId\":null,\"seatNumber\":\"$SEAT\",\"passengerType\":\"ADULT\",\"paymentMethod\":\"CARD\"}")
    
    TICKET_ID=$(extract_json "$TICKET_RESP" "id")
    echo -n "."
  done
  
  echo ""
  echo "      ✅ Created $CONFIRMED_COUNT confirmed + $PENDING_COUNT pending approval tickets (97% of $CAPACITY seats in FULL ROUTE segment)"
fi

# Trip 3: Depart in 1 hour
ROUTE_IDX=2
BUS_IDX=2
BUS_ID=${BUS_IDS[$BUS_IDX]}
ROUTE_ID=${ROUTE_IDS[$ROUTE_IDX]}
CAPACITY=${CAPACITIES[$BUS_IDX]}

DUR=300
ARR_DT=$(date -d "$URGENT_DATETIME_1H +${DUR} minutes" +"%Y-%m-%dT%H:%M:00" 2>/dev/null || echo "${CURRENT_DATE}T23:59:00")

RESP=$(curl -s -X POST "$API_BASE_URL/trips" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"routeId\":$ROUTE_ID,\"busId\":$BUS_ID,\"departureAt\":\"$URGENT_DATETIME_1H\",\"arrivalAt\":\"$ARR_DT\"}")
TRIP_ID=$(extract_json "$RESP" "id")

if [ -n "$TRIP_ID" ]; then
  echo "   🚨 URGENT Trip $TRIP_ID: ${ROUTE_CODES[$ROUTE_IDX]} | Bus ${BUS_PLATES[$BUS_IDX]} | Departs at $URGENT_TIME_1H (1 hour)"
  
  DRIVER_ID=${DRIVER_IDS[2]}
  curl -s -X POST "$API_BASE_URL/operations/assignments" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"driverId\":$DRIVER_ID,\"type\":\"DRIVER\"}" > /dev/null 2>&1
  
  STOPS_RESP=$(curl -s -X GET "$API_BASE_URL/routes/$ROUTE_ID/stops" -H "Authorization: Bearer $ADMIN_TOKEN")
  FIRST_STOP=$(echo "$STOPS_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  LAST_STOP=$(echo "$STOPS_RESP" | grep -o '"id":[0-9]*' | tail -1 | cut -d':' -f2)
  
  FROM_STOP_JSON=$([ -n "$FIRST_STOP" ] && echo "$FIRST_STOP" || echo "null")
  TO_STOP_JSON=$([ -n "$LAST_STOP" ] && echo "$LAST_STOP" || echo "null")
  
  CONFIRMED_COUNT=$(( (CAPACITY * 95 + 50) / 100 ))
  TOTAL_TICKETS=$CAPACITY
  PENDING_COUNT=$((TOTAL_TICKETS - CONFIRMED_COUNT))
  
  echo "      📍 Creating $CONFIRMED_COUNT confirmed + $PENDING_COUNT pending (100% of $CAPACITY seats in FULL ROUTE)"
  
  # Paso 1: Crear y confirmar 95%
  for SEAT in $(seq 1 $CONFIRMED_COUNT); do
    PASSENGER_IDX=$((RANDOM % ${#USER_IDS[@]}))
    ACCOUNT_ID=${USER_IDS[$PASSENGER_IDX]}
    
    TICKET_RESP=$(curl -s -X POST "$API_BASE_URL/tickets" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"accountId\":$ACCOUNT_ID,\"fromStopId\":null,\"toStopId\":null,\"seatNumber\":\"$SEAT\",\"passengerType\":\"ADULT\",\"paymentMethod\":\"CARD\"}")
    
    TICKET_ID=$(extract_json "$TICKET_RESP" "id")
    if [ -n "$TICKET_ID" ]; then
      curl -s -X POST "$API_BASE_URL/tickets/payments/$TICKET_ID" -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null 2>&1
    fi
  done
  
  # Paso 2: Crear 2% más (PENDING_APPROVAL)
  for SEAT in $(seq $((CONFIRMED_COUNT + 1)) $TOTAL_TICKETS); do
    PASSENGER_IDX=$((RANDOM % ${#USER_IDS[@]}))
    ACCOUNT_ID=${USER_IDS[$PASSENGER_IDX]}
    
    TICKET_RESP=$(curl -s -X POST "$API_BASE_URL/tickets" -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" -d "{\"tripId\":$TRIP_ID,\"accountId\":$ACCOUNT_ID,\"fromStopId\":null,\"toStopId\":null,\"seatNumber\":\"$SEAT\",\"passengerType\":\"ADULT\",\"paymentMethod\":\"CARD\"}")
    
    TICKET_ID=$(extract_json "$TICKET_RESP" "id")
    echo -n "."
  done
  
  echo ""
  echo "      ✅ Created $CONFIRMED_COUNT confirmed + $PENDING_COUNT pending approval tickets (97% of $CAPACITY seats in FULL ROUTE segment)"
fi

echo ""
echo "✅ SEED COMPLETED - 3 urgent trips created with segment-based 95% occupation logic"

# =================================================================
# RESUMEN FINAL
# =================================================================
echo "════════════════════════════════════════════════════════════"
echo "🎉 DATABASE SEEDING COMPLETE"
echo "════════════════════════════════════════════════════════════"
echo "📊 Summary:"
echo "   • Accounts: $((1 + ${#DISPATCHER_IDS[@]} + ${#DRIVER_IDS[@]} + ${#USER_IDS[@]}))"
echo "     - 1 Admin | 2 Dispatchers | 5 Drivers | 10 Passengers"
echo "   • Buses: ${#BUS_IDS[@]}"
echo "   • Seats: $TOTAL_SEATS"
echo "   • Routes: ${#ROUTE_IDS[@]}"
echo "   • Stops: 8"
echo "   • Fare Rules: 4"
echo "   • Configs: 3"
echo "   • Trips: $TRIP_COUNT (May 2025 - May 2026)"
echo "   • 🚨 URGENT Trips: 3 (2 departing in 30min, 1 in 1hour with 97% occupation)"
echo ""
echo "🔑 Admin Credentials:"
echo "   Email: j@lojur.com"
echo "   Password: 123"
echo ""
echo "✅ Database ready for use!"
echo "════════════════════════════════════════════════════════════"
