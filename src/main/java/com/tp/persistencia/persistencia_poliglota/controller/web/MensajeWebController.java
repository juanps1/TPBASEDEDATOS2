package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.Conversacion;
import com.tp.persistencia.persistencia_poliglota.model.sql.Mensaje;
import com.tp.persistencia.persistencia_poliglota.model.sql.Usuario;
import com.tp.persistencia.persistencia_poliglota.service.ConversacionService;
import com.tp.persistencia.persistencia_poliglota.service.MensajeService;
import com.tp.persistencia.persistencia_poliglota.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mensajes")
@RequiredArgsConstructor
public class MensajeWebController {

    private final ConversacionService conversacionService;
    private final MensajeService mensajeService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarConversaciones(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario == null) {
                List<Usuario> usuarios = usuarioService.listarUsuarios();
                if (!usuarios.isEmpty()) {
                    usuario = usuarios.get(0);
                }
            }
            
            List<Conversacion> conversaciones = conversacionService.listarPorUsuario(usuario.getId());
            List<Usuario> todosUsuarios = usuarioService.listarUsuarios();
            
            model.addAttribute("conversaciones", conversaciones);
            model.addAttribute("usuarios", todosUsuarios);
            model.addAttribute("usuarioActual", usuario);
            model.addAttribute("titulo", "Mis Conversaciones");
            
            return "mensajes/lista";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar conversaciones: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/{id}")
    public String verConversacion(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario == null) {
                List<Usuario> usuarios = usuarioService.listarUsuarios();
                if (!usuarios.isEmpty()) {
                    usuario = usuarios.get(0);
                }
            }
            
            Conversacion conversacion = conversacionService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));
            
            List<Mensaje> mensajes = mensajeService.obtenerMensajesPorConversacion(id);
            
            String titulo = conversacion.getNombre() != null ? conversacion.getNombre() : "Conversación #" + id;
            
            model.addAttribute("conversacion", conversacion);
            model.addAttribute("mensajes", mensajes);
            model.addAttribute("usuarioActual", usuario);
            model.addAttribute("titulo", "Conversación: " + titulo);
            
            return "mensajes/chat";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar conversación: " + e.getMessage());
            return "error/500";
        }
    }

    @PostMapping("/crear")
    public String crearConversacion(
            @RequestParam String titulo,
            @RequestParam(required = false) Long destinatarioId,
            @RequestParam(required = false) List<Long> participantesIds,
            @RequestParam(required = false) String primerMensaje,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            String username = authentication.getName();
            Usuario creador = usuarioService.buscarPorUsername(username);
            
            if (creador == null) {
                List<Usuario> usuarios = usuarioService.listarUsuarios();
                if (!usuarios.isEmpty()) {
                    creador = usuarios.get(0);
                }
            }
            
            Conversacion conversacion;
            
            // Conversación individual
            if (destinatarioId != null) {
                Usuario destinatario = usuarioService.obtenerPorId(destinatarioId);
                conversacion = conversacionService.crearConversacionIndividual(creador.getId(), destinatario.getId(), titulo);
            } 
            // Conversación grupal
            else if (participantesIds != null && !participantesIds.isEmpty()) {
                conversacion = conversacionService.crearConversacionGrupal(creador.getId(), participantesIds, titulo);
            }
            else {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un participante");
                return "redirect:/mensajes";
            }
            
            // Enviar primer mensaje si existe
            if (primerMensaje != null && !primerMensaje.trim().isEmpty()) {
                mensajeService.enviarMensaje(conversacion.getId(), creador.getId(), primerMensaje);
            }
            
            redirectAttributes.addFlashAttribute("success", "Conversación creada exitosamente");
            return "redirect:/mensajes/" + conversacion.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear conversación: " + e.getMessage());
            return "redirect:/mensajes";
        }
    }

    @PostMapping("/{id}/enviar")
    public String enviarMensaje(
            @PathVariable Long id,
            @RequestParam String contenido,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            String username = authentication.getName();
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario == null) {
                List<Usuario> usuarios = usuarioService.listarUsuarios();
                if (!usuarios.isEmpty()) {
                    usuario = usuarios.get(0);
                }
            }
            
            mensajeService.crearMensaje(id, usuario.getId(), contenido);
            
            return "redirect:/mensajes/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar mensaje: " + e.getMessage());
            return "redirect:/mensajes/" + id;
        }
    }
}
