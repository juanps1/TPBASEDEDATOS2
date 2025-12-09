package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.nosql.Medicion;
import com.tp.persistencia.persistencia_poliglota.model.nosql.Sensor;
import com.tp.persistencia.persistencia_poliglota.service.MedicionService;
import com.tp.persistencia.persistencia_poliglota.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📊 CONTROLADOR WEB PARA MEDICIONES IoT
 * Maneja la interfaz web (HTML) del sistema de mediciones de sensores
 * Mediciones almacenadas en MongoDB
 */
@Controller
@RequestMapping("/mediciones")
@RequiredArgsConstructor
public class MedicionWebController {

    private final MedicionService medicionService;
    private final SensorService sensorService;

    // ========== LISTADO DE MEDICIONES ==========
    @GetMapping
    public String listarMediciones(
            @RequestParam(value = "sensorId", required = false) String sensorId,
            Model model) {
        
        List<Medicion> mediciones;
        
        if (sensorId != null && !sensorId.isEmpty()) {
            mediciones = medicionService.listarPorSensor(sensorId);
            Sensor sensor = sensorService.buscarPorId(sensorId);
            model.addAttribute("sensorSeleccionado", sensor);
        } else {
            mediciones = medicionService.listarTodas();
        }
        
        List<Sensor> sensores = sensorService.listarSensores();
        
        model.addAttribute("mediciones", mediciones);
        model.addAttribute("sensores", sensores);
        model.addAttribute("titulo", "Mediciones de Sensores");
        model.addAttribute("sensorIdFiltro", sensorId);
        
        return "mediciones/lista";
    }

    // ========== VER DETALLES DE UNA MEDICIÓN ==========
    @GetMapping("/{id}")
    public String verDetalles(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Medicion medicion = medicionService.buscarPorId(id);
        
        if (medicion == null) {
            redirectAttributes.addFlashAttribute("error", "Medición no encontrada");
            return "redirect:/mediciones";
        }
        
        Sensor sensor = sensorService.buscarPorId(medicion.getSensorId());
        
        model.addAttribute("medicion", medicion);
        model.addAttribute("sensor", sensor);
        model.addAttribute("titulo", "Detalles de la Medición");
        
        return "mediciones/detalles";
    }

    // ========== MOSTRAR FORMULARIO PARA CREAR NUEVA MEDICIÓN ==========
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        List<Sensor> sensores = sensorService.listarSensores();
        
        model.addAttribute("medicion", new Medicion());
        model.addAttribute("sensores", sensores);
        model.addAttribute("titulo", "Registrar Nueva Medición");
        model.addAttribute("accion", "crear");
        
        return "mediciones/formulario";
    }

    // ========== CREAR NUEVA MEDICIÓN ==========
    @PostMapping("/crear")
    public String crearMedicion(@ModelAttribute Medicion medicion, RedirectAttributes redirectAttributes) {
        try {
            // Validar que el sensor existe
            Sensor sensor = sensorService.buscarPorId(medicion.getSensorId());
            if (sensor == null) {
                redirectAttributes.addFlashAttribute("error", "El sensor especificado no existe");
                return "redirect:/mediciones/nueva";
            }
            
            // Establecer fecha/hora actual
            medicion.setFechaHora(LocalDateTime.now());
            
            medicionService.guardarMedicion(medicion);
            redirectAttributes.addFlashAttribute("success", "Medición registrada correctamente");
            
            return "redirect:/mediciones";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar la medición: " + e.getMessage());
            return "redirect:/mediciones/nueva";
        }
    }

    // ========== ELIMINAR MEDICIÓN ==========
    @PostMapping("/{id}/eliminar")
    public String eliminarMedicion(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Medicion medicion = medicionService.buscarPorId(id);
            
            if (medicion == null) {
                redirectAttributes.addFlashAttribute("error", "Medición no encontrada");
                return "redirect:/mediciones";
            }
            
            medicionService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Medición eliminada correctamente");
            
            return "redirect:/mediciones";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la medición: " + e.getMessage());
            return "redirect:/mediciones";
        }
    }

    // ========== ELIMINAR TODAS LAS MEDICIONES DE UN SENSOR ==========
    @PostMapping("/sensor/{sensorId}/eliminar-todas")
    public String eliminarMedicionesSensor(@PathVariable String sensorId, RedirectAttributes redirectAttributes) {
        try {
            long cantidadEliminada = medicionService.eliminarPorSensor(sensorId);
            redirectAttributes.addFlashAttribute("success", 
                "Se eliminaron " + cantidadEliminada + " mediciones del sensor");
            
            return "redirect:/mediciones?sensorId=" + sensorId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar mediciones: " + e.getMessage());
            return "redirect:/mediciones?sensorId=" + sensorId;
        }
    }
}
