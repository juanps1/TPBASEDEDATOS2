package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.SolicitudProceso;
import com.tp.persistencia.persistencia_poliglota.service.SolicitudProcesoService;
import com.tp.persistencia.persistencia_poliglota.service.ProcesoService;
import com.tp.persistencia.persistencia_poliglota.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudProcesoWebController {

    private final SolicitudProcesoService solicitudProcesoService;
    private final ProcesoService procesoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarSolicitudes(@RequestParam(value = "estado", required = false) String estado,
                                   Model model) {
        List<SolicitudProceso> solicitudes = solicitudProcesoService.listarTodas();
        
        if (estado != null && !estado.isEmpty()) {
            solicitudes = solicitudes.stream()
                    .filter(s -> s.getEstado().equalsIgnoreCase(estado))
                    .toList();
        }
        
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("titulo", "Solicitudes de Proceso");
        model.addAttribute("filtroEstado", estado);
        return "solicitudes/lista";
    }

    @GetMapping("/{id}")
    public String verDetalles(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        SolicitudProceso solicitud = solicitudProcesoService.obtenerPorId(id).orElse(null);
        
        if (solicitud == null) {
            redirectAttributes.addFlashAttribute("error", "Solicitud no encontrada");
            return "redirect:/solicitudes";
        }
        
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("titulo", "Detalles de Solicitud");
        return "solicitudes/detalles";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("solicitud", new SolicitudProceso());
        model.addAttribute("procesos", procesoService.listar());
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        model.addAttribute("titulo", "Nueva Solicitud");
        model.addAttribute("accion", "crear");
        return "solicitudes/formulario";
    }

    @PostMapping("/crear")
    public String crearSolicitud(@ModelAttribute SolicitudProceso solicitud,
                                RedirectAttributes redirectAttributes) {
        try {
            solicitud.setEstado("pendiente");
            solicitudProcesoService.guardar(solicitud);
            redirectAttributes.addFlashAttribute("success", "Solicitud creada correctamente");
            return "redirect:/solicitudes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/solicitudes/nueva";
        }
    }

    @PostMapping("/{id}/aprobar")
    public String aprobarSolicitud(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SolicitudProceso solicitud = solicitudProcesoService.obtenerPorId(id).orElse(null);
            if (solicitud != null) {
                solicitud.setEstado("aprobada");
                solicitudProcesoService.actualizar(solicitud);
                redirectAttributes.addFlashAttribute("success", "Solicitud aprobada");
            }
            return "redirect:/solicitudes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/solicitudes/" + id;
        }
    }

    @PostMapping("/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SolicitudProceso solicitud = solicitudProcesoService.obtenerPorId(id).orElse(null);
            if (solicitud != null) {
                solicitud.setEstado("rechazada");
                solicitudProcesoService.actualizar(solicitud);
                redirectAttributes.addFlashAttribute("success", "Solicitud rechazada");
            }
            return "redirect:/solicitudes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/solicitudes/" + id;
        }
    }
}
