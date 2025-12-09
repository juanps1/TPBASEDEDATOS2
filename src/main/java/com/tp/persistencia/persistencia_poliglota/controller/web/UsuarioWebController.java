package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.Usuario;
import com.tp.persistencia.persistencia_poliglota.model.sql.Rol;
import com.tp.persistencia.persistencia_poliglota.service.UsuarioService;
import com.tp.persistencia.persistencia_poliglota.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    @GetMapping
    public String listarUsuarios(@RequestParam(value = "estado", required = false) String estado, Model model) {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        
        if (estado != null && !estado.isEmpty()) {
            usuarios = usuarios.stream()
                    .filter(u -> u.getEstado().equalsIgnoreCase(estado))
                    .toList();
        }
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("titulo", "Gestión de Usuarios");
        model.addAttribute("filtroEstado", estado);
        return "usuarios/lista";
    }

    @GetMapping("/{id}")
    public String verDetalles(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.listarUsuarios().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("titulo", "Detalles del Usuario");
        return "usuarios/detalles";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", roles);
        model.addAttribute("titulo", "Crear Nuevo Usuario");
        model.addAttribute("accion", "crear");
        return "usuarios/formulario";
    }

    @PostMapping("/crear")
    public String crearUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            if (usuarioService.existeEmail(usuario.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con ese email");
                return "redirect:/usuarios/nuevo";
            }
            
            usuario.setEstado("activo");
            usuarioService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario creado correctamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear usuario: " + e.getMessage());
            return "redirect:/usuarios/nuevo";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.listarUsuarios().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", roles);
        model.addAttribute("titulo", "Editar Usuario");
        model.addAttribute("accion", "editar");
        return "usuarios/formulario";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizarUsuario(@PathVariable Long id, @ModelAttribute Usuario usuario, 
                                   RedirectAttributes redirectAttributes) {
        try {
            usuario.setId(id);
            usuarioService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado correctamente");
            return "redirect:/usuarios/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/usuarios/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam String nuevoEstado,
                               RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.listarUsuarios().stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/usuarios";
            }
            
            usuario.setEstado(nuevoEstado);
            usuarioService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Estado actualizado a: " + nuevoEstado);
            return "redirect:/usuarios/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/usuarios/" + id;
        }
    }
}
