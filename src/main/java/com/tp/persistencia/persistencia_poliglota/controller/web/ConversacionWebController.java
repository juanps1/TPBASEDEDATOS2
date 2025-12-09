package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.Conversacion;
import com.tp.persistencia.persistencia_poliglota.model.sql.Mensaje;
import com.tp.persistencia.persistencia_poliglota.service.ConversacionService;
import com.tp.persistencia.persistencia_poliglota.service.MensajeService;
import com.tp.persistencia.persistencia_poliglota.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/conversaciones")
@RequiredArgsConstructor
public class ConversacionWebController {

    private final ConversacionService conversacionService;
    private final MensajeService mensajeService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarConversaciones(@RequestParam(value = "usuarioId", required = false) Long usuarioId,
                                      Model model) {
        List<Conversacion> conversaciones;
        
        if (usuarioId != null) {
            conversaciones = conversacionService.obtenerConversacionesUsuario(usuarioId);
        } else {
            conversaciones = conversacionService.listarTodas();
        }
        
        model.addAttribute("conversaciones", conversaciones);
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        model.addAttribute("titulo", "Conversaciones");
        return "conversaciones/lista";
    }

    @GetMapping("/{id}")
    public String verConversacion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Conversacion conversacion = conversacionService.obtenerPorId(id).orElse(null);
        
        if (conversacion == null) {
            redirectAttributes.addFlashAttribute("error", "Conversación no encontrada");
            return "redirect:/conversaciones";
        }
        
        List<Mensaje> mensajes = mensajeService.obtenerMensajesDeConversacion(id);
        
        model.addAttribute("conversacion", conversacion);
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("titulo", conversacion.getNombre() != null ? conversacion.getNombre() : "Conversación");
        return "conversaciones/chat";
    }

    @PostMapping("/{id}/enviar-mensaje")
    public String enviarMensaje(@PathVariable Long id,
                               @RequestParam Long remitenteId,
                               @RequestParam String contenido,
                               RedirectAttributes redirectAttributes) {
        try {
            mensajeService.enviarMensaje(id, remitenteId, contenido);
            redirectAttributes.addFlashAttribute("success", "Mensaje enviado");
            return "redirect:/conversaciones/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/conversaciones/" + id;
        }
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        model.addAttribute("titulo", "Nueva Conversación");
        return "conversaciones/formulario";
    }

    @PostMapping("/crear")
    public String crearConversacion(@RequestParam String titulo,
                                   @RequestParam Long creadorId,
                                   @RequestParam List<Long> participantesIds,
                                   RedirectAttributes redirectAttributes) {
        try {
            Conversacion conversacion = conversacionService.crearConversacionGrupal(titulo, creadorId, participantesIds);
            redirectAttributes.addFlashAttribute("success", "Conversación creada correctamente");
            return "redirect:/conversaciones/" + conversacion.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/conversaciones/nueva";
        }
    }
}
