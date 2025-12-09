package com.tp.persistencia.persistencia_poliglota.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash("notificacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {
    
    @Id
    private String id;
    
    @Indexed
    private Long usuarioId;
    
    @Indexed
    private String tipo; // "alerta", "proceso_completado", "factura", "mensaje"
    
    private String titulo;
    
    private String mensaje;
    
    private String icono;
    
    private String color; // "success", "warning", "danger", "info"
    
    private LocalDateTime timestamp;
    
    private boolean leida;
    
    private String enlace; // URL para navegar al hacer click
    
    private String datos; // JSON con datos adicionales
}
