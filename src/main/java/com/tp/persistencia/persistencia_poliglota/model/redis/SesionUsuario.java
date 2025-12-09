package com.tp.persistencia.persistencia_poliglota.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;

@RedisHash("sesion_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesionUsuario {
    
    @Id
    private String sessionId;
    
    @Indexed
    private String email;
    
    @Indexed
    private Long usuarioId;
    
    private String nombreCompleto;
    
    private String rol;
    
    private LocalDateTime inicioSesion;
    
    private LocalDateTime ultimaActividad;
    
    private String ipAddress;
    
    private String userAgent;
    
    private Map<String, Object> preferencias;
    
    private boolean activa;
}
