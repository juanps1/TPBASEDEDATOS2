package com.tp.persistencia.persistencia_poliglota.config;

import com.tp.persistencia.persistencia_poliglota.model.sql.Rol;
import com.tp.persistencia.persistencia_poliglota.model.sql.Usuario;
import com.tp.persistencia.persistencia_poliglota.repository.RolRepository;
import com.tp.persistencia.persistencia_poliglota.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("🔧 Inicializando datos de usuarios y roles...");
        
        // Crear roles si no existen
        Rol adminRole = rolRepository.findByDescripcion("administrador");
        if (adminRole == null) {
            adminRole = rolRepository.save(new Rol(null, "administrador"));
            System.out.println("✅ Rol 'administrador' creado");
        }
        
        Rol userRole = rolRepository.findByDescripcion("usuario");
        if (userRole == null) {
            userRole = rolRepository.save(new Rol(null, "usuario"));
            System.out.println("✅ Rol 'usuario' creado");
        }
        
        final Rol adminRoleFinal = adminRole;
        final Rol userRoleFinal = userRole;

        // Actualizar o crear usuario admin
        Usuario admin = usuarioRepository.findByEmail("admin@admin.com").orElse(null);
        if (admin == null) {
            admin = new Usuario();
            admin.setEmail("admin@admin.com");
            System.out.println("✅ Creando nuevo usuario admin");
        } else {
            System.out.println("🔄 Actualizando usuario admin existente");
        }
        admin.setNombreCompleto("Administrador Principal");
        admin.setContrasena(passwordEncoder.encode("admin"));
        admin.setEstado("activo");
        admin.setRol(adminRoleFinal);
        usuarioRepository.save(admin);
        System.out.println("✅ Usuario admin configurado: admin@admin.com / admin");

        // Actualizar o crear usuario normal
        Usuario user = usuarioRepository.findByEmail("user@user.com").orElse(null);
        if (user == null) {
            user = new Usuario();
            user.setEmail("user@user.com");
            System.out.println("✅ Creando nuevo usuario normal");
        } else {
            System.out.println("🔄 Actualizando usuario normal existente");
        }
        user.setNombreCompleto("Usuario Normal");
        user.setContrasena(passwordEncoder.encode("user"));
        user.setEstado("activo");
        user.setRol(userRoleFinal);
        usuarioRepository.save(user);
        System.out.println("✅ Usuario normal configurado: user@user.com / user");
        
        System.out.println("✅ Inicialización de datos completada");
    }
}
