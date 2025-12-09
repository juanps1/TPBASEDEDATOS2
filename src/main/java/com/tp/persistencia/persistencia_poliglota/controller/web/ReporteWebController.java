package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.persistencia.persistencia_poliglota.model.nosql.Sensor;
import com.tp.persistencia.persistencia_poliglota.model.sql.Proceso;
import com.tp.persistencia.persistencia_poliglota.model.sql.SolicitudProceso;
import com.tp.persistencia.persistencia_poliglota.model.sql.Usuario;
import com.tp.persistencia.persistencia_poliglota.repository.SensorRepository;
import com.tp.persistencia.persistencia_poliglota.service.ProcesoService;
import com.tp.persistencia.persistencia_poliglota.service.SolicitudProcesoService;
import com.tp.persistencia.persistencia_poliglota.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📊 CONTROLADOR WEB PARA REPORTES
 * Permite a los usuarios solicitar reportes personalizados que generan facturas
 */
@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteWebController {

    private final ProcesoService procesoService;
    private final SolicitudProcesoService solicitudProcesoService;
    private final UsuarioService usuarioService;
    private final SensorRepository sensorRepository;

    @GetMapping
    public String listarReportes(Model model) {
        // Obtener sensores disponibles
        List<Sensor> sensores = sensorRepository.findAll();
        
        // Obtener ciudades únicas
        List<String> ciudades = sensores.stream()
            .map(Sensor::getCiudad)
            .filter(c -> c != null && !c.isEmpty())
            .distinct()
            .sorted()
            .toList();
        
        model.addAttribute("sensores", sensores);
        model.addAttribute("ciudades", ciudades);
        // Obtener procesos de tipo reporte
        List<Proceso> procesosReporte = procesoService.listar().stream()
                .filter(p -> p.getTipo() != null && p.getTipo().contains("REPORTE"))
                .toList();
        
        model.addAttribute("procesos", procesosReporte);
        model.addAttribute("titulo", "Solicitar Reportes");
        
        return "reportes/solicitar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioReporte(Model model) {
        model.addAttribute("titulo", "Solicitar Nuevo Reporte");
        return "reportes/formulario";
    }

    @PostMapping("/solicitar")
    public String solicitarReporte(
            @RequestParam String tipoReporte,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String zona,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) String agrupacion,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String sensorId,
            @RequestParam(required = false) Double tempMin,
            @RequestParam(required = false) Double tempMax,
            @RequestParam(required = false) Double humMin,
            @RequestParam(required = false) Double humMax,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Obtener usuario autenticado
            String username = authentication.getName();
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            // Si no existe en BD (usuarios en memoria), usar el primer usuario disponible
            if (usuario == null) {
                List<Usuario> usuarios = usuarioService.listarUsuarios();
                if (usuarios.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No hay usuarios en el sistema");
                    return "redirect:/reportes";
                }
                usuario = usuarios.get(0);
            }

            // Buscar o crear el proceso de reporte
            Proceso proceso = procesoService.buscarPorNombre("Reporte de Análisis");
            if (proceso == null) {
                // Crear proceso de reporte con costo
                proceso = new Proceso();
                proceso.setNombre("Reporte de Análisis");
                proceso.setDescripcion("Generación de reportes personalizados de datos IoT");
                proceso.setTipo("REPORTE_ANALISIS");
                proceso.setCosto(250.00); // Costo del reporte
                proceso = procesoService.guardar(proceso);
            }

            // Crear solicitud de proceso
            SolicitudProceso solicitud = new SolicitudProceso();
            solicitud.setUsuario(usuario);
            solicitud.setProceso(proceso);
            solicitud.setTipoProceso(tipoReporte);
            solicitud.setEstado("PENDIENTE");
            solicitud.setFechaSolicitud(LocalDateTime.now());

            // Construir parámetros del reporte
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("tipoReporte", tipoReporte);
            if (ciudad != null && !ciudad.isEmpty()) {
                parametros.put("ciudad", ciudad);
            }
            if (zona != null && !zona.isEmpty()) {
                parametros.put("zona", zona);
            }
            if (pais != null && !pais.isEmpty()) {
                parametros.put("pais", pais);
            }
            if (agrupacion != null && !agrupacion.isEmpty()) {
                parametros.put("agrupacion", agrupacion);
            }
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                parametros.put("fechaInicio", fechaInicio);
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                parametros.put("fechaFin", fechaFin);
            }
            if (sensorId != null && !sensorId.isEmpty()) {
                parametros.put("sensorId", sensorId);
            }
            
            // Parámetros de alertas personalizados
            if ("ALERTAS_RANGO".equals(tipoReporte)) {
                if (tempMin != null) {
                    parametros.put("tempMin", tempMin);
                }
                if (tempMax != null) {
                    parametros.put("tempMax", tempMax);
                }
                if (humMin != null) {
                    parametros.put("humMin", humMin);
                }
                if (humMax != null) {
                    parametros.put("humMax", humMax);
                }
            }
            
            // Convertir a JSON correctamente
            ObjectMapper mapper = new ObjectMapper();
            String jsonParametros = mapper.writeValueAsString(parametros);
            solicitud.setParametrosJson(jsonParametros);

            // Guardar solicitud
            SolicitudProceso solicitudGuardada = solicitudProcesoService.crear(solicitud);

            // Procesar automáticamente la solicitud
            try {
                solicitudProcesoService.ejecutarProceso(solicitudGuardada);
                redirectAttributes.addFlashAttribute("success", 
                    "Reporte generado correctamente. Se ha creado una factura de $" + proceso.getCosto() + 
                    ". Solicitud #" + solicitudGuardada.getId());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("warning", 
                    "Solicitud creada (#" + solicitudGuardada.getId() + ") pero hubo un error al procesar: " + e.getMessage());
            }
            
            return "redirect:/reportes/solicitud/" + solicitudGuardada.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al solicitar reporte: " + e.getMessage());
            return "redirect:/reportes";
        }
    }

    @GetMapping("/mis-solicitudes")
    public String misSolicitudes(Authentication authentication, Model model) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username);
        
        // Si no existe en BD (usuarios en memoria), usar el primer usuario disponible
        if (usuario == null) {
            List<Usuario> usuarios = usuarioService.listarUsuarios();
            if (usuarios.isEmpty()) {
                model.addAttribute("error", "No hay usuarios en el sistema");
                return "redirect:/login";
            }
            usuario = usuarios.get(0);
        }

        List<SolicitudProceso> solicitudes = solicitudProcesoService.listarPorUsuario(usuario.getId());
        
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("titulo", "Mis Solicitudes de Reportes");
        
        return "reportes/mis-solicitudes";
    }

    @GetMapping("/solicitud/{id}")
    public String verSolicitud(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        SolicitudProceso solicitud = solicitudProcesoService.obtenerPorId(id).orElse(null);
        
        if (solicitud == null) {
            redirectAttributes.addFlashAttribute("error", "Solicitud no encontrada");
            return "redirect:/reportes/mis-solicitudes";
        }

        // Verificar que la solicitud pertenece al usuario
        String username = authentication.getName();
        Usuario usuario = usuarioService.buscarPorUsername(username);
        
        // Si no existe en BD (usuarios en memoria), usar el primer usuario disponible
        if (usuario == null) {
            List<Usuario> usuarios = usuarioService.listarUsuarios();
            if (usuarios.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No hay usuarios en el sistema");
                return "redirect:/reportes/mis-solicitudes";
            }
            usuario = usuarios.get(0);
        }
        
        if (!solicitud.getUsuario().getId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permisos para ver esta solicitud");
            return "redirect:/reportes/mis-solicitudes";
        }

        model.addAttribute("solicitud", solicitud);
        model.addAttribute("titulo", "Detalle de Solicitud");
        
        return "reportes/detalle-solicitud";
    }

    @PostMapping("/procesar/{id}")
    public String procesarSolicitud(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            SolicitudProceso solicitud = solicitudProcesoService.obtenerPorId(id).orElse(null);
            
            if (solicitud == null) {
                redirectAttributes.addFlashAttribute("error", "Solicitud no encontrada");
                return "redirect:/reportes/mis-solicitudes";
            }

            // Verificar que la solicitud pertenece al usuario
            String username = authentication.getName();
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario == null) {
                List<Usuario> usuarios = usuarioService.listarUsuarios();
                if (usuarios.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No hay usuarios en el sistema");
                    return "redirect:/reportes/mis-solicitudes";
                }
                usuario = usuarios.get(0);
            }
            
            if (!solicitud.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tiene permisos para procesar esta solicitud");
                return "redirect:/reportes/mis-solicitudes";
            }

            // Verificar que está pendiente
            if (!"PENDIENTE".equals(solicitud.getEstado())) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden procesar solicitudes pendientes");
                return "redirect:/reportes/solicitud/" + id;
            }

            // Ejecutar el proceso
            solicitudProcesoService.ejecutarProceso(solicitud);
            
            redirectAttributes.addFlashAttribute("success", "Solicitud procesada correctamente. Se ha generado la factura.");
            return "redirect:/reportes/solicitud/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/reportes/solicitud/" + id;
        }
    }
}

