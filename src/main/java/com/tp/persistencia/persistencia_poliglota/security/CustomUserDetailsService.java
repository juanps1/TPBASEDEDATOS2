package com.tp.persistencia.persistencia_poliglota.security;

import com.tp.persistencia.persistencia_poliglota.model.sql.Usuario;
import com.tp.persistencia.persistencia_poliglota.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Intentando autenticar usuario: " + email);
        
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario no encontrado: " + email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        System.out.println("✅ Usuario encontrado: " + usuario.getEmail());
        System.out.println("   - Nombre: " + usuario.getNombreCompleto());
        System.out.println("   - Rol: " + (usuario.getRol() != null ? usuario.getRol().getDescripcion() : "SIN ROL"));
        System.out.println("   - Password hash: " + usuario.getContrasena().substring(0, Math.min(20, usuario.getContrasena().length())) + "...");
        
        String rol = usuario.getRol() != null ? usuario.getRol().getDescripcion() : "usuario";
        
        UserDetails userDetails = User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasena())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(rol.toUpperCase())))
                .build();
        
        System.out.println("✅ UserDetails creado con rol: " + rol.toUpperCase());
        return userDetails;
    }
}
