package com.tp.persistencia.persistencia_poliglota.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.persistencia.persistencia_poliglota.model.nosql.Alerta;
import com.tp.persistencia.persistencia_poliglota.model.sql.Factura;
import com.tp.persistencia.persistencia_poliglota.model.nosql.Medicion;
import com.tp.persistencia.persistencia_poliglota.model.nosql.Sensor;
import com.tp.persistencia.persistencia_poliglota.model.sql.SolicitudProceso;
import com.tp.persistencia.persistencia_poliglota.model.sql.Usuario;
import com.tp.persistencia.persistencia_poliglota.repository.AlertaRepository;
import com.tp.persistencia.persistencia_poliglota.repository.FacturaRepository;
import com.tp.persistencia.persistencia_poliglota.repository.MedicionRepository;
import com.tp.persistencia.persistencia_poliglota.repository.SensorRepository;
import com.tp.persistencia.persistencia_poliglota.repository.SolicitudProcesoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SolicitudProcesoService {

    private final SolicitudProcesoRepository solicitudProcesoRepository;
    private final MedicionRepository medicionRepository;
    private final SensorRepository sensorRepository;
    private final AlertaRepository alertaRepository;
    @Autowired
    private GeneradorPdfService generadorPdfService;
    private final FacturaService facturaService;
    private final ObjectMapper objectMapper;

    public SolicitudProcesoService(SolicitudProcesoRepository solicitudProcesoRepository,
                                   MedicionRepository medicionRepository,
                                   SensorRepository sensorRepository,
                                   AlertaRepository alertaRepository,
                                   FacturaService facturaService) {
        this.solicitudProcesoRepository = solicitudProcesoRepository;
        this.medicionRepository = medicionRepository;
        this.sensorRepository = sensorRepository;
        this.alertaRepository = alertaRepository;
        this.facturaService = facturaService;
        this.objectMapper = new ObjectMapper();
    }

    public List<SolicitudProceso> listar() {
        return solicitudProcesoRepository.findAll();
    }

    public List<SolicitudProceso> listarTodas() {
        return solicitudProcesoRepository.findAll();
    }

    public Optional<SolicitudProceso> obtenerPorId(Long id) {
        return solicitudProcesoRepository.findById(id);
    }

    public SolicitudProceso actualizar(SolicitudProceso solicitud) {
        return solicitudProcesoRepository.save(solicitud);
    }

    public SolicitudProceso crear(SolicitudProceso solicitud) {
        return solicitudProcesoRepository.save(solicitud);
    }

    public List<SolicitudProceso> listarPorUsuario(Long usuarioId) {
        return solicitudProcesoRepository.findByUsuarioId(usuarioId);
    }

    public List<SolicitudProceso> buscarPendientes() {
        List<SolicitudProceso> pendientes = solicitudProcesoRepository.findByEstado("pendiente");
        System.out.println("🔍 Buscando solicitudes pendientes - Encontradas: " + pendientes.size());
        for (SolicitudProceso s : pendientes) {
            System.out.println("   📄 ID: " + s.getId() + ", Estado: " + s.getEstado() + ", Tipo: " + s.getTipoProceso());
        }
        return pendientes;
    }

    public List<SolicitudProceso> buscarEnProceso() {
        return solicitudProcesoRepository.findByEstado("en_proceso");
    }

    public Optional<SolicitudProceso> buscarPorId(Long id) {
        return solicitudProcesoRepository.findById(id);
    }

    public SolicitudProceso guardar(SolicitudProceso solicitud) {
        return solicitudProcesoRepository.save(solicitud);
    }

    public boolean eliminar(Long id) {
        if (solicitudProcesoRepository.existsById(id)) {
            solicitudProcesoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ejecutarProceso(SolicitudProceso solicitud) {
        System.out.println("🚀 INICIANDO ejecutarProceso para solicitud ID: " + solicitud.getId());
        
        // Recargar la solicitud desde la base de datos
        SolicitudProceso solicitudActual = solicitudProcesoRepository.findById(solicitud.getId())
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitud.getId()));
        
        System.out.println("📊 Estado actual de la solicitud: " + solicitudActual.getEstado());
        
        // Verificar que esté pendiente
        if (!"PENDIENTE".equals(solicitudActual.getEstado())) {
            System.out.println("⚠️ Solicitud " + solicitud.getId() + " ya está en estado: " + solicitudActual.getEstado());
            return;
        }
        
        try {
            // Marcar como en proceso
            solicitudActual.setEstado("EN_PROCESO");
            solicitudProcesoRepository.save(solicitudActual);

            // Parsear parámetros
            System.out.println("📋 JSON recibido: " + solicitudActual.getParametrosJson());
            Map<String, Object> parametros = parseParametros(solicitudActual.getParametrosJson());
            System.out.println("📋 Parámetros parseados: " + parametros);
            
            // VERIFICACIÓN: Buscar mediciones directamente sin filtros primero
            System.out.println("\n=== VERIFICACIÓN DE DATOS ===");
            long totalMediciones = medicionRepository.count();
            System.out.println("📊 Total de mediciones en BD: " + totalMediciones);
            
            long totalSensores = sensorRepository.count();
            System.out.println("🔧 Total de sensores en BD: " + totalSensores);
            
            // Ejecutar consulta simple según el tipo
            Map<String, Object> resultado = new HashMap<>();
            String tipoReporte = solicitudActual.getTipoProceso();
            
            if ("INFORME_MAX_MIN".equals(tipoReporte)) {
                resultado = ejecutarInformeMaxMin(parametros);
            } else if ("INFORME_PROMEDIO".equals(tipoReporte)) {
                resultado = ejecutarInformePromedio(parametros);
            } else if ("ALERTAS_RANGO".equals(tipoReporte)) {
                resultado = ejecutarAlerta(parametros, solicitudActual.getUsuario());
            } else {
                resultado.put("mensaje", "Reporte generado correctamente");
                resultado.put("tipo", tipoReporte);
            }

            System.out.println("✅ Resultado obtenido: " + resultado);

            // Guardar resultado
            solicitudActual.setResultadoJson(objectMapper.writeValueAsString(resultado));
            solicitudActual.setEstado("COMPLETADA");
            solicitudActual.setFechaFinalizacion(LocalDateTime.now());
            solicitudActual.setMensajeError(null);
            solicitudProcesoRepository.save(solicitudActual);
            
            System.out.println("✅ Proceso completado exitosamente para solicitud: " + solicitudActual.getId());

            // Generar factura
            try {
                Factura factura = facturaService.generarFacturaParaSolicitud(solicitudActual);
                if (factura != null) {
                    System.out.println("💰 Factura #" + factura.getId() + " generada - Monto: $" + factura.getMonto());
                }
            } catch (Exception facturaEx) {
                System.err.println("⚠️ Error al generar factura: " + facturaEx.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Error procesando solicitud: " + e.getMessage());
            e.printStackTrace();
            solicitudActual.setEstado("ERROR");
            solicitudActual.setMensajeError(e.getMessage());
            solicitudActual.setFechaFinalizacion(LocalDateTime.now());
            solicitudProcesoRepository.save(solicitudActual);
            throw new RuntimeException("Error procesando solicitud", e);
        }
    }

    private Map<String, Object> ejecutarInformeMaxMin(Map<String, Object> parametros) {
        List<Medicion> mediciones = obtenerMediciones(parametros);

        if (mediciones.isEmpty()) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("temperaturaMax", 0.0);
            resultado.put("temperaturaMin", 0.0);
            resultado.put("humedadMax", 0.0);
            resultado.put("humedadMin", 0.0);
            resultado.put("totalMediciones", 0);
            return resultado;
        }

        double tempMax = mediciones.stream().mapToDouble(Medicion::getTemperatura).max().orElse(0.0);
        double tempMin = mediciones.stream().mapToDouble(Medicion::getTemperatura).min().orElse(0.0);
        double humMax = mediciones.stream().mapToDouble(Medicion::getHumedad).max().orElse(0.0);
        double humMin = mediciones.stream().mapToDouble(Medicion::getHumedad).min().orElse(0.0);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("temperaturaMax", tempMax);
        resultado.put("temperaturaMin", tempMin);
        resultado.put("humedadMax", humMax);
        resultado.put("humedadMin", humMin);
        resultado.put("totalMediciones", mediciones.size());

        return resultado;
    }

    private Map<String, Object> ejecutarInformePromedio(Map<String, Object> parametros) {
        List<Medicion> mediciones = obtenerMediciones(parametros);

        if (mediciones.isEmpty()) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("temperaturaPromedio", 0.0);
            resultado.put("humedadPromedio", 0.0);
            resultado.put("totalMediciones", 0);
            return resultado;
        }

        double tempPromedio = mediciones.stream().mapToDouble(Medicion::getTemperatura).average().orElse(0.0);
        double humPromedio = mediciones.stream().mapToDouble(Medicion::getHumedad).average().orElse(0.0);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("temperaturaPromedio", tempPromedio);
        resultado.put("humedadPromedio", humPromedio);
        resultado.put("totalMediciones", mediciones.size());

        return resultado;
    }

    private Map<String, Object> ejecutarAlerta(Map<String, Object> parametros, Usuario usuario) {
        List<Medicion> mediciones = obtenerMediciones(parametros);

        // Usar parámetros personalizados o valores por defecto
        Double tempMin = getDoubleParam(parametros, "tempMin");
        if (tempMin == null) tempMin = 10.0; // Default
        
        Double tempMax = getDoubleParam(parametros, "tempMax");
        if (tempMax == null) tempMax = 30.0; // Default
        
        Double humMin = getDoubleParam(parametros, "humMin");
        if (humMin == null) humMin = 30.0; // Default
        
        Double humMax = getDoubleParam(parametros, "humMax");
        if (humMax == null) humMax = 80.0; // Default

        System.out.println("🚨 Rangos de alerta configurados:");
        System.out.println("   Temperatura: " + tempMin + "°C - " + tempMax + "°C");
        System.out.println("   Humedad: " + humMin + "% - " + humMax + "%");

        int alertasTemperatura = 0;
        int alertasHumedad = 0;

        for (Medicion medicion : mediciones) {
            // Alertas de temperatura
            if (medicion.getTemperatura() < tempMin || medicion.getTemperatura() > tempMax) {
                alertasTemperatura++;
            }
            
            // Alertas de humedad
            if (medicion.getHumedad() < humMin || medicion.getHumedad() > humMax) {
                alertasHumedad++;
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalMediciones", mediciones.size());
        resultado.put("alertasTemperatura", alertasTemperatura);
        resultado.put("alertasHumedad", alertasHumedad);
        resultado.put("totalAlertas", alertasTemperatura + alertasHumedad);
        resultado.put("rangoTempMin", tempMin);
        resultado.put("rangoTempMax", tempMax);
        resultado.put("rangoHumMin", humMin);
        resultado.put("rangoHumMax", humMax);

        System.out.println("🚨 Resultados:");
        System.out.println("   Total mediciones: " + mediciones.size());
        System.out.println("   Alertas temperatura: " + alertasTemperatura);
        System.out.println("   Alertas humedad: " + alertasHumedad);

        return resultado;
    }

    private Map<String, Object> ejecutarConsulta(Map<String, Object> parametros) {
        List<Medicion> mediciones = obtenerMediciones(parametros);

        List<Map<String, Object>> datos = mediciones.stream().map(m -> {
            Map<String, Object> item = new HashMap<>();
            item.put("sensorId", m.getSensorId());
            item.put("fechaHora", m.getFechaHora().toString());
            item.put("temperatura", m.getTemperatura());
            item.put("humedad", m.getHumedad());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalRegistros", mediciones.size());
        resultado.put("mediciones", datos);

        return resultado;
    }

    private List<Medicion> obtenerMediciones(Map<String, Object> parametros) {
        System.out.println("🔍 ========== OBTENIENDO MEDICIONES ==========");
        System.out.println("📋 Parámetros recibidos: " + parametros);
        
        // Intentar con fechaInicio/fechaFin primero, luego fechaDesde/fechaHasta
        Object fechaInicioObj = parametros.get("fechaInicio");
        if (fechaInicioObj == null) {
            fechaInicioObj = parametros.get("fechaDesde");
        }
        
        Object fechaFinObj = parametros.get("fechaFin");
        if (fechaFinObj == null) {
            fechaFinObj = parametros.get("fechaHasta");
        }
        
        LocalDateTime fechaDesde = parseDateTime(fechaInicioObj);
        LocalDateTime fechaHasta = parseDateTime(fechaFinObj);

        System.out.println("📅 Rango de fechas final: " + fechaDesde + " hasta " + fechaHasta);

        List<String> sensorIds = new ArrayList<>();

        // Filtrar por sensor específico
        if (parametros.containsKey("sensorId") && parametros.get("sensorId") != null && !parametros.get("sensorId").toString().isEmpty()) {
            sensorIds.add(parametros.get("sensorId").toString());
            System.out.println("🎯 Filtro por sensorId: " + parametros.get("sensorId"));
        } 
        // Filtrar por ciudad
        else if (parametros.containsKey("ciudad") && parametros.get("ciudad") != null && !parametros.get("ciudad").toString().isEmpty()) {
            String ciudad = parametros.get("ciudad").toString();
            
            // Buscar todos los sensores y filtrar manualmente (case insensitive)
            List<Sensor> todosSensores = sensorRepository.findAll();
            System.out.println("📍 Total de sensores en BD: " + todosSensores.size());
            
            // Mostrar primeros 5 sensores con sus ciudades
            System.out.println("   Ciudades en BD:");
            todosSensores.stream().limit(5).forEach(s -> 
                System.out.println("      - Sensor " + s.getId() + ": ciudad='" + s.getCiudad() + "'")
            );
            
            List<Sensor> sensores = todosSensores.stream()
                    .filter(s -> s.getCiudad() != null && s.getCiudad().equalsIgnoreCase(ciudad))
                    .collect(Collectors.toList());
            
            sensorIds = sensores.stream().map(Sensor::getId).collect(Collectors.toList());
            System.out.println("🏙️ Buscando ciudad: '" + ciudad + "' - Sensores encontrados: " + sensorIds.size());
            
            if (!sensores.isEmpty()) {
                System.out.println("   IDs encontrados: " + sensorIds);
            } else {
                System.out.println("   ⚠️ NO SE ENCONTRARON SENSORES PARA CIUDAD: '" + ciudad + "'");
            }
        } 
        // Filtrar por país
        else if (parametros.containsKey("pais") && parametros.get("pais") != null && !parametros.get("pais").toString().isEmpty()) {
            String pais = parametros.get("pais").toString();
            List<Sensor> sensores = sensorRepository.findByPais(pais);
            sensorIds = sensores.stream().map(Sensor::getId).collect(Collectors.toList());
            System.out.println("🌍 Filtro por país: " + pais + " - Sensores encontrados: " + sensorIds.size());
        }
        // Si no hay filtro, tomar todos los sensores
        else {
            List<Sensor> sensores = sensorRepository.findAll();
            sensorIds = sensores.stream().map(Sensor::getId).collect(Collectors.toList());
            System.out.println("🌐 Sin filtro - Todos los sensores: " + sensorIds.size());
        }

        if (sensorIds.isEmpty()) {
            System.out.println("⚠️ No se encontraron sensores con los filtros especificados");
            System.out.println("============================================");
            return new ArrayList<>();
        }

        System.out.println("🔎 Buscando mediciones para " + sensorIds.size() + " sensores en rango de fechas...");
        List<Medicion> mediciones = medicionRepository.findBySensorIdInAndFechaHoraBetween(sensorIds, fechaDesde, fechaHasta);
        System.out.println("📊 Mediciones encontradas: " + mediciones.size());
        
        if (mediciones.isEmpty()) {
            // Intentar buscar una medición de ejemplo para ver qué fechas hay
            System.out.println("🔍 Buscando mediciones de ejemplo para estos sensores (sin filtro de fecha)...");
            List<Medicion> ejemplos = medicionRepository.findBySensorIdIn(sensorIds).stream().limit(3).collect(Collectors.toList());
            if (!ejemplos.isEmpty()) {
                System.out.println("   Ejemplos de mediciones que SÍ existen:");
                ejemplos.forEach(m -> 
                    System.out.println("      - Sensor: " + m.getSensorId() + ", Fecha: " + m.getFechaHora())
                );
            } else {
                System.out.println("   ⚠️ NO HAY MEDICIONES PARA ESTOS SENSORES");
            }
        }
        
        System.out.println("============================================");
        return mediciones;
    }

    private Map<String, Object> parseParametros(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            System.out.println("⚠️ Fecha null, usando fecha por defecto");
            return LocalDateTime.now().minusYears(10); // Default muy antiguo
        }
        
        try {
            String strValue = value.toString().trim();
            System.out.println("📅 Parseando fecha: '" + strValue + "'");
            
            // Intentar varios formatos
            try {
                // Formato ISO completo (2022-01-12T00:00:00)
                return LocalDateTime.parse(strValue);
            } catch (Exception e1) {
                try {
                    // Formato fecha simple (2022-01-12) -> agregar hora
                    LocalDateTime result = LocalDateTime.parse(strValue + "T00:00:00");
                    System.out.println("✅ Fecha parseada: " + result);
                    return result;
                } catch (Exception e2) {
                    System.out.println("❌ Error parseando fecha: " + e2.getMessage());
                    return LocalDateTime.now();
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error general parseando fecha: " + e.getMessage());
            return LocalDateTime.now();
        }
    }

    private Double getDoubleParam(Map<String, Object> parametros, String key) {
        Object value = parametros.get(key);
        if (value == null) return null;
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

