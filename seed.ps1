# =================================================================
# Script COMPLETO para poblar base de datos - InterMuniFast (Windows)
# - Datos desde 6 meses atrás (Mayo 2025) hasta 6 meses adelante (Mayo 2026)
# - 3 trips URGENTES que salen en 5 minutos con 95% ocupación
# - Roles: Admin, Dispatchers (2), Drivers (5), Pasajeros (10)
# - Tickets históricos confirmados para métricas realistas
# =================================================================

$ErrorActionPreference = "Stop"

$API_BASE_URL = if ($env:API_BASE_URL) { $env:API_BASE_URL } else { "http://localhost:8080/api" }
$ADMIN_TOKEN = ""

# Obtener fecha/hora actual
$CURRENT_DATETIME = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$CURRENT_DATE = Get-Date -Format "yyyy-MM-dd"
$CURRENT_TIME = Get-Date -Format "HH:mm"
$URGENT_TIME_30MIN = (Get-Date).AddMinutes(30).ToString("HH:mm")
$URGENT_DATETIME_30MIN = (Get-Date).AddMinutes(30).ToString("yyyy-MM-ddTHH:mm:00")
$URGENT_TIME_1H = (Get-Date).AddHours(1).ToString("HH:mm")
$URGENT_DATETIME_1H = (Get-Date).AddHours(1).ToString("yyyy-MM-ddTHH:mm:00")

Write-Host "════════════════════════════════════════════════════════════"
Write-Host "🚀 INTER MUNI FAST - Seed Data Generator"
Write-Host "════════════════════════════════════════════════════════════"
Write-Host "🕒 Current DateTime: $CURRENT_DATETIME"
Write-Host "⏰ Urgent Trip 1: $URGENT_DATETIME_30MIN (in 30 minutes)"
Write-Host "⏰ Urgent Trip 2: $URGENT_DATETIME_30MIN (in 30 minutes)"
Write-Host "⏰ Urgent Trip 3: $URGENT_DATETIME_1H (in 1 hour)"
Write-Host "📡 API Base URL: $API_BASE_URL"
Write-Host "════════════════════════════════════════════════════════════"
Write-Host ""

# Función para extraer JSON
function Extract-Json {
    param($Response, $Field)
    if ($Response.PSObject.Properties.Name -contains $Field) {
        return $Response.$Field
    }
    return $null
}

# Función para realizar POST request
function Invoke-ApiPost {
    param($Url, $Body, $Token = $null)
    $headers = @{"Content-Type" = "application/json"}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    try {
        $response = Invoke-RestMethod -Uri $Url -Method Post -Headers $headers -Body ($Body | ConvertTo-Json -Depth 10) -ErrorAction Stop
        return $response
    } catch {
        Write-Host "Error: $_"
        return $null
    }
}

# Función para realizar GET request
function Invoke-ApiGet {
    param($Url, $Token = $null)
    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    try {
        $response = Invoke-RestMethod -Uri $Url -Method Get -Headers $headers -ErrorAction Stop
        return $response
    } catch {
        Write-Host "Error: $_"
        return $null
    }
}

# =================================================================
# 1. CREAR CUENTAS
# =================================================================
Write-Host "👥 Creating accounts..."

# Admin
$adminBody = @{
    name = "Admin System"
    email = "j@lojur.com"
    phone = "+573001234567"
    password = "123"
    isAdmin = $true
}
$resp = Invoke-ApiPost "$API_BASE_URL/accounts" $adminBody
$ADMIN_ID = Extract-Json $resp "id"
Write-Host "   ✅ ADMIN: j@lojur.com (ID: $ADMIN_ID)"

# Dispatchers
$DISPATCHER_IDS = @()
for ($i = 1; $i -le 2; $i++) {
    $body = @{
        name = "Dispatcher $i"
        email = "dispatcher$i@intermunifast.com"
        phone = "+57310${i}234567"
        password = "dispatch123"
        isAdmin = $false
    }
    $resp = Invoke-ApiPost "$API_BASE_URL/accounts" $body
    $id = Extract-Json $resp "id"
    $DISPATCHER_IDS += $id
    Write-Host "   ✅ DISPATCHER: dispatcher$i@intermunifast.com (ID: $id)"
}

# Drivers
$DRIVER_IDS = @()
$DRIVER_NAMES = @("José Ramírez", "Pedro González", "Luis Martínez", "Roberto Silva", "Miguel Torres")
for ($i = 1; $i -le 5; $i++) {
    $body = @{
        name = $DRIVER_NAMES[$i-1]
        email = "driver$i@intermunifast.com"
        phone = "+57320${i}234567"
        password = "driver123"
        isAdmin = $false
    }
    $resp = Invoke-ApiPost "$API_BASE_URL/accounts" $body
    $id = Extract-Json $resp "id"
    $DRIVER_IDS += $id
    Write-Host "   ✅ DRIVER: driver$i@intermunifast.com (ID: $id)"
}

# Passengers
$USER_IDS = @()
$PASSENGER_NAMES = @("María García", "Juan Pérez", "Ana Rodríguez", "Carlos Martínez", "Laura López", "Diego Hernández", "Sofía Ramírez", "Andrés Morales", "Valentina Castro", "Santiago Vargas")
$PASSENGER_EMAILS = @("maria.garcia", "juan.perez", "ana.rodriguez", "carlos.martinez", "laura.lopez", "diego.hernandez", "sofia.ramirez", "andres.morales", "valentina.castro", "santiago.vargas")
for ($i = 0; $i -le 9; $i++) {
    $body = @{
        name = $PASSENGER_NAMES[$i]
        email = "$($PASSENGER_EMAILS[$i])@example.com"
        phone = "+5730${i}9876543"
        password = "password123"
        isAdmin = $false
    }
    $resp = Invoke-ApiPost "$API_BASE_URL/accounts" $body
    $id = Extract-Json $resp "id"
    $USER_IDS += $id
    Write-Host "   ✅ PASSENGER: $($PASSENGER_EMAILS[$i])@example.com (ID: $id)"
}

Write-Host ""
Write-Host "✅ Accounts: $($DISPATCHER_IDS.Count + $DRIVER_IDS.Count + $USER_IDS.Count + 1) (1 admin + 2 dispatchers + 5 drivers + 10 passengers)"
Write-Host ""

# =================================================================
# 2. AUTENTICACIÓN
# =================================================================
Write-Host "🔐 Authenticating..."
$authBody = @{
    email = "j@lojur.com"
    password = "123"
}
$authResp = Invoke-ApiPost "$API_BASE_URL/auth/signin" $authBody
$ADMIN_TOKEN = Extract-Json $authResp "token"
if (-not $ADMIN_TOKEN) {
    Write-Host "❌ Auth failed"
    exit 1
}
Write-Host "   ✅ Token obtained ($($ADMIN_TOKEN.Length) chars)"
Write-Host ""

# =================================================================
# 3. CREAR BUSES
# =================================================================
Write-Host "🚌 Creating buses..."
$BUS_IDS = @()
$BUS_PLATES = @()
$CAPACITIES = @(40, 35, 30, 45, 25)
$PLATES = @("ABC123", "DEF456", "GHI789", "JKL012", "MNO345")

for ($i = 0; $i -le 4; $i++) {
    $body = @{
        plate = $PLATES[$i]
        capacity = $CAPACITIES[$i]
        amenities = @(
            @{name = "WiFi"; description = "Internet"},
            @{name = "AC"; description = "A/C"}
        )
    }
    $resp = Invoke-ApiPost "$API_BASE_URL/buses" $body $ADMIN_TOKEN
    $busId = Extract-Json $resp "id"
    $BUS_IDS += $busId
    $BUS_PLATES += $PLATES[$i]
    Write-Host "   ✅ Bus $($PLATES[$i]) (ID: $busId, Capacity: $($CAPACITIES[$i]))"
}
Write-Host "   Total: $($BUS_IDS.Count) buses"
Write-Host ""

# =================================================================
# 4. CREAR ASIENTOS
# =================================================================
Write-Host "💺 Creating seats..."
$TOTAL_SEATS = 0
for ($i = 0; $i -le 4; $i++) {
    $busId = $BUS_IDS[$i]
    $capacity = $CAPACITIES[$i]
    $pref = [Math]::Floor($capacity / 10)
    
    for ($seatNum = 1; $seatNum -le $capacity; $seatNum++) {
        $type = if ($seatNum -le $pref) { "PREFERENTIAL" } else { "STANDARD" }
        $body = @{
            number = $seatNum.ToString()
            type = $type
            busId = $busId
        }
        $null = Invoke-ApiPost "$API_BASE_URL/seats" $body $ADMIN_TOKEN
        $TOTAL_SEATS++
    }
    Write-Host "   ✅ Bus $($BUS_PLATES[$i]): $capacity seats ($pref preferential)"
}
Write-Host "   Total: $TOTAL_SEATS seats"
Write-Host ""

# =================================================================
# 5. CREAR RUTAS
# =================================================================
Write-Host "🛣️  Creating routes..."
$ROUTE_IDS = @()
$ROUTE_CODES = @()
$ROUTES_DATA = @(
    @{code="RT-SMT-BAQ"; name="Santa Marta - Barranquilla"; origin="Santa Marta"; destination="Barranquilla"; distanceKm=93; durationMinutes=120; pricePerKm=500},
    @{code="RT-SMT-VDU"; name="Santa Marta - Valledupar"; origin="Santa Marta"; destination="Valledupar"; distanceKm=185; durationMinutes=240; pricePerKm=450},
    @{code="RT-BAQ-VDU"; name="Barranquilla - Valledupar"; origin="Barranquilla"; destination="Valledupar"; distanceKm=272; durationMinutes=300; pricePerKm=480},
    @{code="RT-VDU-CPY"; name="Valledupar - El Copey"; origin="Valledupar"; destination="El Copey"; distanceKm=45; durationMinutes=60; pricePerKm=550}
)

foreach ($route in $ROUTES_DATA) {
    $resp = Invoke-ApiPost "$API_BASE_URL/routes" $route $ADMIN_TOKEN
    $routeId = Extract-Json $resp "id"
    $ROUTE_IDS += $routeId
    $ROUTE_CODES += $route.code
    Write-Host "   ✅ $($route.code) (ID: $routeId, $($route.distanceKm)km, ~$($route.durationMinutes)min)"
}
Write-Host "   Total: $($ROUTE_IDS.Count) routes"
Write-Host ""

# =================================================================
# 6. CREAR PARADAS
# =================================================================
Write-Host "🚏 Creating stops..."
# Route 1: SMT-BAQ
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[0]; name="Terminal Santa Marta"; sequence=1; latitude=11.2408; longitude=-74.1990} $ADMIN_TOKEN
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[0]; name="Terminal Barranquilla"; sequence=2; latitude=10.9878; longitude=-74.7889} $ADMIN_TOKEN
# Route 2: SMT-VDU
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[1]; name="Terminal Santa Marta"; sequence=1; latitude=11.2408; longitude=-74.1990} $ADMIN_TOKEN
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[1]; name="Fundación"; sequence=2; latitude=10.5214; longitude=-74.1852} $ADMIN_TOKEN
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[1]; name="Terminal Valledupar"; sequence=3; latitude=10.4631; longitude=-73.2532} $ADMIN_TOKEN
# Route 3: BAQ-VDU
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[2]; name="Terminal Barranquilla"; sequence=1; latitude=10.9878; longitude=-74.7889} $ADMIN_TOKEN
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[2]; name="Terminal Valledupar"; sequence=2; latitude=10.4631; longitude=-73.2532} $ADMIN_TOKEN
# Route 4: VDU-CPY
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[3]; name="Terminal Valledupar"; sequence=1; latitude=10.4631; longitude=-73.2532} $ADMIN_TOKEN
$null = Invoke-ApiPost "$API_BASE_URL/stops" @{routeId=$ROUTE_IDS[3]; name="El Copey"; sequence=2; latitude=10.1500; longitude=-73.9611} $ADMIN_TOKEN
Write-Host "   ✅ 8 stops created across 4 routes"
Write-Host ""

# =================================================================
# 7. REGLAS DE TARIFA
# =================================================================
Write-Host "💰 Creating fare rules..."
$BASE_PRICES = @(46500, 83250, 130560, 24750)
for ($i = 0; $i -le 3; $i++) {
    $body = @{
        routeId = $ROUTE_IDS[$i]
        basePrice = $BASE_PRICES[$i]
        dynamicPricing = $false
        childrenDiscount = 0.25
        seniorDiscount = 0.15
        studentDiscount = 0.10
    }
    $null = Invoke-ApiPost "$API_BASE_URL/operations/fare-rules" $body $ADMIN_TOKEN
    Write-Host "   ✅ $($ROUTE_CODES[$i]): `$$($BASE_PRICES[$i])"
}
Write-Host ""

# =================================================================
# 8. CONFIGURACIONES
# =================================================================
Write-Host "⚙️  Creating configs..."
$null = Invoke-ApiPost "$API_BASE_URL/config" @{key="max_seat_hold_minutes"; value="10"} $ADMIN_TOKEN
$null = Invoke-ApiPost "$API_BASE_URL/config" @{key="max_baggage_weight_kg"; value="25"} $ADMIN_TOKEN
$null = Invoke-ApiPost "$API_BASE_URL/config" @{key="baggage_fee_percentage"; value="0.03"} $ADMIN_TOKEN
Write-Host "   ✅ 3 system configs created"
Write-Host ""

# =================================================================
# 9. CREAR TRIPS - 6 MESES PASADO A 6 MESES FUTURO
# =================================================================
Write-Host "🗓️  Creating trips (May 2025 - May 2026)..."
$TRIP_COUNT = 0
$CREATED_TRIP_IDS = @()
$TIMES = @("06:00", "09:30", "13:00", "16:30", "20:00")

# Función crear trip
function Create-Trip {
    param($Date, $Time, $RouteIdx, $BusIdx)
    
    $routeId = $ROUTE_IDS[$RouteIdx]
    $busId = $BUS_IDS[$BusIdx]
    $driverId = $DRIVER_IDS[$script:TRIP_COUNT % $DRIVER_IDS.Count]
    
    $depDt = "${Date}T${Time}:00"
    
    # Calcular llegada
    $duration = switch ($RouteIdx) {
        0 { 120 }
        1 { 240 }
        2 { 300 }
        3 { 60 }
    }
    
    $arrDt = ([DateTime]::Parse($depDt)).AddMinutes($duration).ToString("yyyy-MM-ddTHH:mm:00")
    
    $body = @{
        routeId = $routeId
        busId = $busId
        departureAt = $depDt
        arrivalAt = $arrDt
    }
    
    $resp = Invoke-ApiPost "$API_BASE_URL/trips" $body $ADMIN_TOKEN
    $tripId = Extract-Json $resp "id"
    
    if ($tripId) {
        $script:CREATED_TRIP_IDS += "${tripId}:${routeId}"
        $script:TRIP_COUNT++
        
        # Asignar driver
        $assignBody = @{
            tripId = $tripId
            driverId = $driverId
            type = "DRIVER"
        }
        $null = Invoke-ApiPost "$API_BASE_URL/operations/assignments" $assignBody $ADMIN_TOKEN
        
        Write-Host "." -NoNewline
    }
}

# Mayo 2025 (pasado)
for ($day = 1; $day -le 31; $day++) {
    $date = Get-Date -Year 2025 -Month 5 -Day $day -Format "yyyy-MM-dd"
    $numTrips = Get-Random -Minimum 2 -Maximum 5
    for ($i = 1; $i -le $numTrips; $i++) {
        $timeIdx = Get-Random -Maximum 5
        $routeIdx = Get-Random -Maximum 4
        $busIdx = Get-Random -Maximum 5
        Create-Trip $date $TIMES[$timeIdx] $routeIdx $busIdx
    }
}
Write-Host ""
Write-Host "   ✅ May 2025: Complete"

# Junio-Noviembre 2025 (pasado/presente)
for ($month = 6; $month -le 11; $month++) {
    $daysInMonth = [DateTime]::DaysInMonth(2025, $month)
    
    for ($day = 1; $day -le $daysInMonth; $day++) {
        $date = Get-Date -Year 2025 -Month $month -Day $day -Format "yyyy-MM-dd"
        $numTrips = Get-Random -Minimum 2 -Maximum 5
        for ($i = 1; $i -le $numTrips; $i++) {
            $timeIdx = Get-Random -Maximum 5
            $routeIdx = Get-Random -Maximum 4
            $busIdx = Get-Random -Maximum 5
            Create-Trip $date $TIMES[$timeIdx] $routeIdx $busIdx
        }
    }
    Write-Host ""
    Write-Host "   ✅ Month 2025-$month : Complete"
}

# Diciembre 2025 - Mayo 2026 (futuro)
for ($day = 1; $day -le 31; $day++) {
    $date = Get-Date -Year 2025 -Month 12 -Day $day -Format "yyyy-MM-dd"
    $numTrips = Get-Random -Minimum 2 -Maximum 5
    for ($i = 1; $i -le $numTrips; $i++) {
        $timeIdx = Get-Random -Maximum 5
        $routeIdx = Get-Random -Maximum 4
        $busIdx = Get-Random -Maximum 5
        Create-Trip $date $TIMES[$timeIdx] $routeIdx $busIdx
    }
}
Write-Host ""
Write-Host "   ✅ Dec 2025: Complete"

for ($month = 1; $month -le 5; $month++) {
    $daysInMonth = [DateTime]::DaysInMonth(2026, $month)
    
    for ($day = 1; $day -le $daysInMonth; $day++) {
        $date = Get-Date -Year 2026 -Month $month -Day $day -Format "yyyy-MM-dd"
        $numTrips = Get-Random -Minimum 2 -Maximum 5
        for ($i = 1; $i -le $numTrips; $i++) {
            $timeIdx = Get-Random -Maximum 5
            $routeIdx = Get-Random -Maximum 4
            $busIdx = Get-Random -Maximum 5
            Create-Trip $date $TIMES[$timeIdx] $routeIdx $busIdx
        }
    }
    Write-Host ""
    Write-Host "   ✅ Month 2026-$month : Complete"
}

Write-Host ""
Write-Host "✅ Total trips created: $TRIP_COUNT"
Write-Host ""

# =================================================================
# 10. CREAR 3 TRIPS URGENTES (2 en 30min, 1 en 1h) - 97% OCUPACIÓN
# =================================================================
Write-Host "🚨 Creating 3 URGENT trips (2 departing in 30min, 1 in 1hour with 97% occupation)..."

# Trip 1: Depart in 30 minutes
$routeIdx = 0
$busIdx = 0
$busId = $BUS_IDS[$busIdx]
$routeId = $ROUTE_IDS[$routeIdx]
$capacity = $CAPACITIES[$busIdx]

$duration = 120
$arrDt = ([DateTime]::Parse($URGENT_DATETIME_30MIN)).AddMinutes($duration).ToString("yyyy-MM-ddTHH:mm:00")

$tripBody = @{
    routeId = $routeId
    busId = $busId
    departureAt = $URGENT_DATETIME_30MIN
    arrivalAt = $arrDt
}
$resp = Invoke-ApiPost "$API_BASE_URL/trips" $tripBody $ADMIN_TOKEN
$tripId = Extract-Json $resp "id"

if ($tripId) {
    Write-Host "   🚨 URGENT Trip $tripId : $($ROUTE_CODES[$routeIdx]) | Bus $($BUS_PLATES[$busIdx]) | Departs at $URGENT_TIME_30MIN (30 min)"
    
    # Asignar driver
    $assignBody = @{tripId=$tripId; driverId=$DRIVER_IDS[0]; type="DRIVER"}
    $null = Invoke-ApiPost "$API_BASE_URL/operations/assignments" $assignBody $ADMIN_TOKEN
    
    # Obtener paradas
    $stopsResp = Invoke-ApiGet "$API_BASE_URL/routes/$routeId/stops" $ADMIN_TOKEN
    
    $confirmedCount = [Math]::Floor(($capacity * 95 + 50) / 100)
    $totalTickets = $capacity
    $pendingCount = $totalTickets - $confirmedCount
    
    Write-Host "      📍 Creating $confirmedCount confirmed + $pendingCount pending (100% of $capacity seats in FULL ROUTE)"
    
    # Paso 1: Crear y confirmar 95%
    for ($seat = 1; $seat -le $confirmedCount; $seat++) {
        $passengerIdx = Get-Random -Maximum $USER_IDS.Count
        $accountId = $USER_IDS[$passengerIdx]
        
        $ticketBody = @{
            tripId = $tripId
            accountId = $accountId
            fromStopId = $null
            toStopId = $null
            seatNumber = $seat.ToString()
            passengerType = "ADULT"
            paymentMethod = "CARD"
        }
        
        $ticketResp = Invoke-ApiPost "$API_BASE_URL/tickets" $ticketBody $ADMIN_TOKEN
        $ticketId = Extract-Json $ticketResp "id"
        if ($ticketId) {
            $null = Invoke-ApiPost "$API_BASE_URL/tickets/payments/$ticketId" @{} $ADMIN_TOKEN
        }
    }
    
    # Paso 2: Crear hasta 100% (PENDING_APPROVAL)
    for ($seat = ($confirmedCount + 1); $seat -le $totalTickets; $seat++) {
        $passengerIdx = Get-Random -Maximum $USER_IDS.Count
        $accountId = $USER_IDS[$passengerIdx]
        
        $ticketBody = @{
            tripId = $tripId
            accountId = $accountId
            fromStopId = $null
            toStopId = $null
            seatNumber = $seat.ToString()
            passengerType = "ADULT"
            paymentMethod = "CARD"
        }
        
        $ticketResp = Invoke-ApiPost "$API_BASE_URL/tickets" $ticketBody $ADMIN_TOKEN
        Write-Host "." -NoNewline
    }
    
    Write-Host ""
    Write-Host "      ✅ Created $confirmedCount confirmed + $pendingCount pending approval tickets (97% of $capacity seats in FULL ROUTE segment)"
}

# Trip 2: Depart in 30 minutes
$routeIdx = 1
$busIdx = 1
$busId = $BUS_IDS[$busIdx]
$routeId = $ROUTE_IDS[$routeIdx]
$capacity = $CAPACITIES[$busIdx]

$duration = 240
$arrDt = ([DateTime]::Parse($URGENT_DATETIME_30MIN)).AddMinutes($duration).ToString("yyyy-MM-ddTHH:mm:00")

$tripBody = @{
    routeId = $routeId
    busId = $busId
    departureAt = $URGENT_DATETIME_30MIN
    arrivalAt = $arrDt
}
$resp = Invoke-ApiPost "$API_BASE_URL/trips" $tripBody $ADMIN_TOKEN
$tripId = Extract-Json $resp "id"

if ($tripId) {
    Write-Host "   🚨 URGENT Trip $tripId : $($ROUTE_CODES[$routeIdx]) | Bus $($BUS_PLATES[$busIdx]) | Departs at $URGENT_TIME_30MIN (30 min)"
    
    $assignBody = @{tripId=$tripId; driverId=$DRIVER_IDS[1]; type="DRIVER"}
    $null = Invoke-ApiPost "$API_BASE_URL/operations/assignments" $assignBody $ADMIN_TOKEN
    
    $stopsResp = Invoke-ApiGet "$API_BASE_URL/routes/$routeId/stops" $ADMIN_TOKEN
    
    $confirmedCount = [Math]::Floor(($capacity * 95 + 50) / 100)
    $totalTickets = $capacity
    $pendingCount = $totalTickets - $confirmedCount
    
    Write-Host "      📍 Creating $confirmedCount confirmed + $pendingCount pending (100% of $capacity seats in FULL ROUTE)"
    
    for ($seat = 1; $seat -le $confirmedCount; $seat++) {
        $passengerIdx = Get-Random -Maximum $USER_IDS.Count
        $accountId = $USER_IDS[$passengerIdx]
        
        $ticketBody = @{
            tripId = $tripId
            accountId = $accountId
            fromStopId = $null
            toStopId = $null
            seatNumber = $seat.ToString()
            passengerType = "ADULT"
            paymentMethod = "CARD"
        }
        
        $ticketResp = Invoke-ApiPost "$API_BASE_URL/tickets" $ticketBody $ADMIN_TOKEN
        $ticketId = Extract-Json $ticketResp "id"
        if ($ticketId) {
            $null = Invoke-ApiPost "$API_BASE_URL/tickets/payments/$ticketId" @{} $ADMIN_TOKEN
        }
    }
    
    for ($seat = ($confirmedCount + 1); $seat -le $totalTickets; $seat++) {
        $passengerIdx = Get-Random -Maximum $USER_IDS.Count
        $accountId = $USER_IDS[$passengerIdx]
        
        $ticketBody = @{
            tripId = $tripId
            accountId = $accountId
            fromStopId = $null
            toStopId = $null
            seatNumber = $seat.ToString()
            passengerType = "ADULT"
            paymentMethod = "CARD"
        }
        
        $ticketResp = Invoke-ApiPost "$API_BASE_URL/tickets" $ticketBody $ADMIN_TOKEN
        Write-Host "." -NoNewline
    }
    
    Write-Host ""
    Write-Host "      ✅ Created $confirmedCount confirmed + $pendingCount pending approval tickets (97% of $capacity seats in FULL ROUTE segment)"
}

# Trip 3: Depart in 1 hour
$routeIdx = 2
$busIdx = 2
$busId = $BUS_IDS[$busIdx]
$routeId = $ROUTE_IDS[$routeIdx]
$capacity = $CAPACITIES[$busIdx]

$duration = 300
$arrDt = ([DateTime]::Parse($URGENT_DATETIME_1H)).AddMinutes($duration).ToString("yyyy-MM-ddTHH:mm:00")

$tripBody = @{
    routeId = $routeId
    busId = $busId
    departureAt = $URGENT_DATETIME_1H
    arrivalAt = $arrDt
}
$resp = Invoke-ApiPost "$API_BASE_URL/trips" $tripBody $ADMIN_TOKEN
$tripId = Extract-Json $resp "id"

if ($tripId) {
    Write-Host "   🚨 URGENT Trip $tripId : $($ROUTE_CODES[$routeIdx]) | Bus $($BUS_PLATES[$busIdx]) | Departs at $URGENT_TIME_1H (1 hour)"
    
    $assignBody = @{tripId=$tripId; driverId=$DRIVER_IDS[2]; type="DRIVER"}
    $null = Invoke-ApiPost "$API_BASE_URL/operations/assignments" $assignBody $ADMIN_TOKEN
    
    $stopsResp = Invoke-ApiGet "$API_BASE_URL/routes/$routeId/stops" $ADMIN_TOKEN
    
    $confirmedCount = [Math]::Floor(($capacity * 95 + 50) / 100)
    $totalTickets = $capacity
    $pendingCount = $totalTickets - $confirmedCount
    
    Write-Host "      📍 Creating $confirmedCount confirmed + $pendingCount pending (100% of $capacity seats in FULL ROUTE)"
    
    for ($seat = 1; $seat -le $confirmedCount; $seat++) {
        $passengerIdx = Get-Random -Maximum $USER_IDS.Count
        $accountId = $USER_IDS[$passengerIdx]
        
        $ticketBody = @{
            tripId = $tripId
            accountId = $accountId
            fromStopId = $null
            toStopId = $null
            seatNumber = $seat.ToString()
            passengerType = "ADULT"
            paymentMethod = "CARD"
        }
        
        $ticketResp = Invoke-ApiPost "$API_BASE_URL/tickets" $ticketBody $ADMIN_TOKEN
        $ticketId = Extract-Json $ticketResp "id"
        if ($ticketId) {
            $null = Invoke-ApiPost "$API_BASE_URL/tickets/payments/$ticketId" @{} $ADMIN_TOKEN
        }
    }
    
    for ($seat = ($confirmedCount + 1); $seat -le $totalTickets; $seat++) {
        $passengerIdx = Get-Random -Maximum $USER_IDS.Count
        $accountId = $USER_IDS[$passengerIdx]
        
        $ticketBody = @{
            tripId = $tripId
            accountId = $accountId
            fromStopId = $null
            toStopId = $null
            seatNumber = $seat.ToString()
            passengerType = "ADULT"
            paymentMethod = "CARD"
        }
        
        $ticketResp = Invoke-ApiPost "$API_BASE_URL/tickets" $ticketBody $ADMIN_TOKEN
        Write-Host "." -NoNewline
    }
    
    Write-Host ""
    Write-Host "      ✅ Created $confirmedCount confirmed + $pendingCount pending approval tickets (97% of $capacity seats in FULL ROUTE segment)"
}

Write-Host ""
Write-Host "✅ SEED COMPLETED - 3 urgent trips created with segment-based 95% occupation logic"

# =================================================================
# RESUMEN FINAL
# =================================================================
Write-Host "════════════════════════════════════════════════════════════"
Write-Host "🎉 DATABASE SEEDING COMPLETE"
Write-Host "════════════════════════════════════════════════════════════"
Write-Host "📊 Summary:"
Write-Host "   • Accounts: $($DISPATCHER_IDS.Count + $DRIVER_IDS.Count + $USER_IDS.Count + 1)"
Write-Host "     - 1 Admin | 2 Dispatchers | 5 Drivers | 10 Passengers"
Write-Host "   • Buses: $($BUS_IDS.Count)"
Write-Host "   • Seats: $TOTAL_SEATS"
Write-Host "   • Routes: $($ROUTE_IDS.Count)"
Write-Host "   • Stops: 8"
Write-Host "   • Fare Rules: 4"
Write-Host "   • Configs: 3"
Write-Host "   • Trips: $TRIP_COUNT (May 2025 - May 2026)"
Write-Host "   • 🚨 URGENT Trips: 3 (2 departing in 30min, 1 in 1hour with 97% occupation)"
Write-Host ""
Write-Host "🔑 Admin Credentials:"
Write-Host "   Email: j@lojur.com"
Write-Host "   Password: 123"
Write-Host ""
Write-Host "✅ Database ready for use!"
Write-Host "════════════════════════════════════════════════════════════"
