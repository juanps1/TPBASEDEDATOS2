package com.tp.persistencia.persistencia_poliglota.service;

import com.tp.persistencia.persistencia_poliglota.model.sql.SolicitudProceso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcesoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ProcesoScheduler.class);
    
    private final SolicitudProcesoService solicitudProcesoService;

    public ProcesoScheduler(SolicitudProcesoService solicitudProcesoService) {
        this.solicitudProcesoService = solicitudProcesoService;
        logger.info("🚀 ProcesoScheduler inicializado - Worker de procesos asíncronos ACTIVO");
    }

    // ⏰ Ejecuta cada 10 segundos para procesamiento más rápido
    @Scheduled(fixedDelay = 10000)
    public void procesarSolicitudesPendientes() {
        try {
            logger.debug("🔍 Scheduler ejecutándose - Buscando solicitudes pendientes...");
            List<SolicitudProceso> pendientes = solicitudProcesoService.buscarPendientes();
            
            if (!pendientes.isEmpty()) {
                logger.info("📋 Worker encontró {} solicitudes pendientes para procesar", pendientes.size());
                
                for (SolicitudProceso solicitud : pendientes) {
                    try {
                        logger.info("⚡ Iniciando procesamiento - ID: {}, Tipo: {}, Usuario: {}, Estado actual: {}", 
                                   solicitud.getId(), 
                                   solicitud.getTipoProceso(), 
                                   solicitud.getUsuario().getEmail(),
                                   solicitud.getEstado());
                        
                        // El método ejecutarProceso maneja internamente el cambio de estado
                        solicitudProcesoService.ejecutarProceso(solicitud);
                        
                        logger.info("✅ Solicitud {} procesada exitosamente", solicitud.getId());
                        
                    } catch (Exception e) {
                        logger.error("❌ Error al procesar solicitud {}: {}", solicitud.getId(), e.getMessage());
                        logger.error("❌ Stack trace completo:", e);
                        // El método ejecutarProceso ya maneja el cambio a estado "error"
                    }
                }
            } else {
                // Log cada 30 segundos aprox (3 * 10 segundos = 30 segundos)
                if (LocalDateTime.now().getSecond() % 30 < 10) {
                    logger.debug("⏳ Scheduler activo - No hay solicitudes pendientes en este momento");
                }
            }
            
        } catch (Exception e) {
            logger.error("💥 Error crítico en el worker de procesos: {}", e.getMessage(), e);
        }
    }
    
    // 🔄 Método adicional para limpiar procesos abandonados (cada 5 minutos)
    @Scheduled(fixedDelay = 300000)
    public void limpiarProcesosAbandonados() {
        try {
            List<SolicitudProceso> procesosEnProceso = solicitudProcesoService.buscarEnProceso();
            
            for (SolicitudProceso proceso : procesosEnProceso) {
                // Si lleva más de 10 minutos "en_proceso", marcarlo como error
                if (proceso.getFechaSolicitud().plusMinutes(10).isBefore(LocalDateTime.now())) {
                    logger.warn("🚨 Proceso abandonado detectado - ID: {} - Marcando como error", proceso.getId());
                    
                    proceso.setEstado("error");
                    proceso.setMensajeError("Proceso abandonado - Timeout después de 10 minutos");
                    proceso.setFechaFinalizacion(LocalDateTime.now());
                    solicitudProcesoService.guardar(proceso);
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Error en limpieza de procesos abandonados: {}", e.getMessage());
        }
    }
}
