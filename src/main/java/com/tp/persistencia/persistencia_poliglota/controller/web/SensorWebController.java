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
 * 📡 CONTROLADOR WEB PARA SENSORES IoT
 * Maneja la interfaz web (HTML) del sistema de sensores
 * Sensores almacenados en MongoDB
 */
@Controller
@RequestMapping("/sensores")
@RequiredArgsConstructor
public class SensorWebController {

    private final SensorService sensorService;
    private final MedicionService medicionService;

    // ========== LISTADO DE SENSORES ==========
    @GetMapping
    public String listarSensores(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "estado", required = false) String estado,
            Model model) {
        
        List<Sensor> sensores = sensorService.listarSensores();
        
        // Filtrar por tipo si se especifica
        if (tipo != null && !tipo.isEmpty()) {
            sensores = sensores.stream()
                    .filter(s -> s.getTipo().equalsIgnoreCase(tipo))
                    .toList();
        }
        
        // Filtrar por estado si se especifica
        if (estado != null && !estado.isEmpty()) {
            sensores = sensores.stream()
                    .filter(s -> s.getEstado().equalsIgnoreCase(estado))
                    .toList();
        }
        
        model.addAttribute("sensores", sensores);
        model.addAttribute("titulo", "Gestión de Sensores IoT");
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("filtroEstado", estado);
        
        return "sensores/lista";
    }

    // ========== VER DETALLES DE UN SENSOR ==========
    @GetMapping("/{id}")
    public String verDetalles(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Sensor sensor = sensorService.buscarPorId(id);
        
        if (sensor == null) {
            redirectAttributes.addFlashAttribute("error", "Sensor no encontrado");
            return "redirect:/sensores";
        }
        
        // Cargar mediciones del sensor
        List<Medicion> mediciones = medicionService.listarPorSensor(id);
        
        model.addAttribute("sensor", sensor);
        model.addAttribute("mediciones", mediciones);
        model.addAttribute("titulo", "Detalles del Sensor");
        
        return "sensores/detalles";
    }

    // ========== MOSTRAR FORMULARIO PARA CREAR NUEVO SENSOR ==========
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("sensor", new Sensor());
        model.addAttribute("titulo", "Crear Nuevo Sensor");
        model.addAttribute("accion", "crear");
        
        return "sensores/formulario";
    }

    // ========== CREAR NUEVO SENSOR ==========
    @PostMapping("/crear")
    public String crearSensor(@ModelAttribute Sensor sensor, RedirectAttributes redirectAttributes) {
        try {
            // Validar que el nombre no exista
            if (sensorService.existeNombre(sensor.getNombre())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un sensor con ese nombre");
                return "redirect:/sensores/nuevo";
            }
            
            // Establecer fecha de inicio
            sensor.setFechaInicioEmision(LocalDateTime.now());
            
            // Establecer estado por defecto si no viene
            if (sensor.getEstado() == null || sensor.getEstado().isEmpty()) {
                sensor.setEstado("activo");
            }
            
            sensorService.guardarSensor(sensor);
            redirectAttributes.addFlashAttribute("success", "Sensor creado correctamente");
            
            return "redirect:/sensores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el sensor: " + e.getMessage());
            return "redirect:/sensores/nuevo";
        }
    }

    // ========== MOSTRAR FORMULARIO PARA EDITAR SENSOR ==========
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Sensor sensor = sensorService.buscarPorId(id);
        
        if (sensor == null) {
            redirectAttributes.addFlashAttribute("error", "Sensor no encontrado");
            return "redirect:/sensores";
        }
        
        model.addAttribute("sensor", sensor);
        model.addAttribute("titulo", "Editar Sensor");
        model.addAttribute("accion", "editar");
        
        return "sensores/formulario";
    }

    // ========== ACTUALIZAR SENSOR ==========
    @PostMapping("/{id}/actualizar")
    public String actualizarSensor(@PathVariable String id, @ModelAttribute Sensor sensor, 
                                   RedirectAttributes redirectAttributes) {
        try {
            Sensor sensorExistente = sensorService.buscarPorId(id);
            
            if (sensorExistente == null) {
                redirectAttributes.addFlashAttribute("error", "Sensor no encontrado");
                return "redirect:/sensores";
            }
            
            // Mantener el ID y fecha de inicio original
            sensor.setId(id);
            sensor.setFechaInicioEmision(sensorExistente.getFechaInicioEmision());
            
            sensorService.actualizar(sensor);
            redirectAttributes.addFlashAttribute("success", "Sensor actualizado correctamente");
            
            return "redirect:/sensores/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el sensor: " + e.getMessage());
            return "redirect:/sensores/" + id + "/editar";
        }
    }

    // ========== ELIMINAR SENSOR ==========
    @PostMapping("/{id}/eliminar")
    public String eliminarSensor(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Sensor sensor = sensorService.buscarPorId(id);
            
            if (sensor == null) {
                redirectAttributes.addFlashAttribute("error", "Sensor no encontrado");
                return "redirect:/sensores";
            }
            
            sensorService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Sensor eliminado correctamente");
            
            return "redirect:/sensores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el sensor: " + e.getMessage());
            return "redirect:/sensores";
        }
    }

    // ========== CAMBIAR ESTADO DEL SENSOR ==========
    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable String id, 
                               @RequestParam String nuevoEstado,
                               RedirectAttributes redirectAttributes) {
        try {
            Sensor sensor = sensorService.buscarPorId(id);
            
            if (sensor == null) {
                redirectAttributes.addFlashAttribute("error", "Sensor no encontrado");
                return "redirect:/sensores";
            }
            
            sensor.setEstado(nuevoEstado);
            sensorService.actualizar(sensor);
            
            redirectAttributes.addFlashAttribute("success", "Estado actualizado a: " + nuevoEstado);
            
            return "redirect:/sensores/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
            return "redirect:/sensores/" + id;
        }
    }
    
    // ========== CREAR NUEVA MEDICIÓN ==========
    @PostMapping("/{id}/mediciones/crear")
    public String crearMedicion(@PathVariable String id,
                               @RequestParam(required = false, defaultValue = "0.0") Double temperatura,
                               @RequestParam(required = false, defaultValue = "0.0") Double humedad,
                               RedirectAttributes redirectAttributes) {
        try {
            Sensor sensor = sensorService.buscarPorId(id);
            
            if (sensor == null) {
                redirectAttributes.addFlashAttribute("error", "Sensor no encontrado");
                return "redirect:/sensores";
            }
            
            Medicion medicion = new Medicion();
            medicion.setSensorId(id);
            medicion.setFechaHora(LocalDateTime.now());
            medicion.setTemperatura(temperatura);
            medicion.setHumedad(humedad);
            
            medicionService.guardarMedicion(medicion);
            redirectAttributes.addFlashAttribute("success", "Medición agregada correctamente");
            
            return "redirect:/sensores/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear medición: " + e.getMessage());
            return "redirect:/sensores/" + id;
        }
    }
}
