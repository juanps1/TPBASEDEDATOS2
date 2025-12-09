package com.tp.persistencia.persistencia_poliglota.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash("metricas_tiempo_real")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaTiempoReal {
    
    @Id
    private String id;
    
    @Indexed
    private String tipo; // "temperatura", "humedad", "usuarios_activos", "alertas_pendientes"
    
    @Indexed
    private String sensorId;
    
    private Double valor;
    
    private String unidad;
    
    private LocalDateTime timestamp;
    
    private String ciudad;
    
    private String estado; // "normal", "alerta", "critico"
}
