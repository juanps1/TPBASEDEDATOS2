package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.Proceso;
import com.tp.persistencia.persistencia_poliglota.service.ProcesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/procesos")
@RequiredArgsConstructor
public class ProcesoWebController {

    private final ProcesoService procesoService;

    @GetMapping
    public String listarProcesos(Model model) {
        List<Proceso> procesos = procesoService.listar();
        model.addAttribute("procesos", procesos);
        model.addAttribute("titulo", "Gestión de Procesos");
        return "procesos/lista";
    }

    @GetMapping("/{id}")
    public String verDetalles(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Proceso proceso = procesoService.listar().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (proceso == null) {
            redirectAttributes.addFlashAttribute("error", "Proceso no encontrado");
            return "redirect:/procesos";
        }
        
        model.addAttribute("proceso", proceso);
        model.addAttribute("titulo", "Detalles del Proceso");
        return "procesos/detalles";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("proceso", new Proceso());
        model.addAttribute("titulo", "Crear Nuevo Proceso");
        model.addAttribute("accion", "crear");
        return "procesos/formulario";
    }

    @PostMapping("/crear")
    public String crearProceso(@ModelAttribute Proceso proceso, RedirectAttributes redirectAttributes) {
        try {
            procesoService.guardar(proceso);
            redirectAttributes.addFlashAttribute("success", "Proceso creado correctamente");
            return "redirect:/procesos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear proceso: " + e.getMessage());
            return "redirect:/procesos/nuevo";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Proceso proceso = procesoService.listar().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (proceso == null) {
            redirectAttributes.addFlashAttribute("error", "Proceso no encontrado");
            return "redirect:/procesos";
        }
        
        model.addAttribute("proceso", proceso);
        model.addAttribute("titulo", "Editar Proceso");
        model.addAttribute("accion", "editar");
        return "procesos/formulario";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizarProceso(@PathVariable Long id, @ModelAttribute Proceso proceso,
                                   RedirectAttributes redirectAttributes) {
        try {
            proceso.setId(id);
            procesoService.guardar(proceso);
            redirectAttributes.addFlashAttribute("success", "Proceso actualizado correctamente");
            return "redirect:/procesos/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/procesos/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarProceso(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            procesoService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Proceso eliminado correctamente");
            return "redirect:/procesos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/procesos";
        }
    }
}
