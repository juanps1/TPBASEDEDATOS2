package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.redis.MetricaTiempoReal;
import com.tp.persistencia.persistencia_poliglota.repository.MetricaTiempoRealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class DashboardWebController {

    private final MetricaTiempoRealRepository metricaRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        try {
            // Datos básicos del dashboard
            model.addAttribute("totalSensores", 12);
            model.addAttribute("sensoresActivos", 8);
            model.addAttribute("alertasPendientes", 3);
            model.addAttribute("usuariosRegistrados", 25);
            
            // Métricas en tiempo real desde Redis (si existen)
            try {
                List<MetricaTiempoReal> metricas = (List<MetricaTiempoReal>) metricaRepository.findAll();
                model.addAttribute("metricas", metricas);
            } catch (Exception e) {
                model.addAttribute("metricas", java.util.Collections.emptyList());
            }
            
            return "dashboard/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "dashboard/index";
        }
    }

    @GetMapping("/sensores")
    public String sensores(Model model) {
        try {
            // TODO: Implementar cuando se conozcan los métodos correctos del SensorService
            model.addAttribute("sensores", "Lista de sensores - Próximamente");
            return "sensores/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar sensores: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/alertas")
    public String alertas(Model model) {
        try {
            // TODO: Implementar cuando se conozcan los métodos correctos del AlertaService
            model.addAttribute("alertas", "Lista de alertas - Próximamente");
            return "alertas/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar alertas: " + e.getMessage());
            return "error/500";
        }
    }
}

