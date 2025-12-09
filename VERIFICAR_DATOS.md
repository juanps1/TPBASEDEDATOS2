# VERIFICACIÓN DE DATOS

Para verificar que hay datos en MongoDB, ejecuta estos comandos en Mongo Shell o Compass:

## 1. Verificar Sensores
```javascript
use persistencia_iot
db.sensores.countDocuments()
db.sensores.find().limit(5).pretty()
```

## 2. Verificar Mediciones
```javascript
db.mediciones.countDocuments()
db.mediciones.find().limit(5).pretty()
```

## 3. Verificar Mediciones por Sensor
```javascript
db.mediciones.find({"sensorId": "sensor-001"}).limit(5).pretty()
```

## 4. Verificar Mediciones con Rango de Fechas
```javascript
db.mediciones.find({
  "fechaHora": {
    $gte: ISODate("2022-01-12T00:00:00Z"),
    $lte: ISODate("2022-04-12T00:00:00Z")
  }
}).limit(5).pretty()
```

## Logs que deberías ver en la consola Java:

Cuando ejecutes un reporte, deberías ver:
```
🚀 INICIANDO ejecutarProceso para solicitud ID: XX
📊 Estado actual de la solicitud: PENDIENTE
=== VERIFICACIÓN DE DATOS ===
📊 Total de mediciones en BD: 19992
🔧 Total de sensores en BD: 12
📋 JSON recibido: {fechaInicio=2022-01-12, ciudad=Buenos Aires, tipoReporte=INFORME_MAX_MIN, fechaFin=2022-04-12, sensorId=sensor-001}
📋 Parámetros parseados: {fechaInicio=2022-01-12, ciudad=Buenos Aires, tipoReporte=INFORME_MAX_MIN, fechaFin=2022-04-12, sensorId=sensor-001}
🔍 ========== OBTENIENDO MEDICIONES ==========
📋 Parámetros recibidos: {fechaInicio=2022-01-12, ciudad=Buenos Aires, tipoReporte=INFORME_MAX_MIN, fechaFin=2022-04-12, sensorId=sensor-001}
📅 Parseando fecha: '2022-01-12'
✅ Fecha parseada: 2022-01-12T00:00
📅 Parseando fecha: '2022-04-12'
✅ Fecha parseada: 2022-04-12T00:00
📅 Rango de fechas final: 2022-01-12T00:00 hasta 2022-04-12T00:00
🎯 Filtro por sensorId: sensor-001
📊 Mediciones encontradas: XXX
```

Si NO ves estos logs, el servidor no se está ejecutando correctamente.
