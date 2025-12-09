package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.Factura;
import com.tp.persistencia.persistencia_poliglota.model.sql.Usuario;
import com.tp.persistencia.persistencia_poliglota.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 💰 CONTROLADOR WEB PARA FACTURAS
 * Maneja la interfaz web (HTML) del sistema de facturación
 */
@Controller
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class FacturaWebController {

    private final FacturaService facturaService;
    private final com.tp.persistencia.persistencia_poliglota.service.UsuarioService usuarioService;

    @GetMapping
    public String listarFacturas(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "usuarioId", required = false) Long usuarioId,
            Authentication authentication,
            Model model) {
        
        // Obtener usuario autenticado
        String username = authentication.getName();
        Usuario usuarioActual = usuarioService.buscarPorUsername(username);
        
        // Si no encuentra el usuario en BD, usar el primer usuario disponible
        if (usuarioActual == null) {
            List<Usuario> usuarios = usuarioService.listarUsuarios();
            if (!usuarios.isEmpty()) {
                usuarioActual = usuarios.get(0);
            } else {
                model.addAttribute("error", "No se pudo identificar al usuario");
                model.addAttribute("facturas", List.of());
                model.addAttribute("titulo", "Mis Facturas");
                return "facturas/lista";
            }
        }
        
        List<Factura> facturas;
        
        // Solo admin puede ver todas las facturas o filtrar por usuario
        if (usuarioActual.getRol() != null && 
            "administrador".equalsIgnoreCase(usuarioActual.getRol().getDescripcion())) {
            if (usuarioId != null) {
                facturas = facturaService.listarPorUsuario(usuarioId);
            } else {
                facturas = facturaService.listarTodas();
            }
        } else {
            // Usuario normal solo ve sus propias facturas
            facturas = facturaService.listarPorUsuario(usuarioActual.getId());
        }
        
        if (estado != null && !estado.isEmpty()) {
            facturas = facturas.stream()
                    .filter(f -> f.getEstado().equalsIgnoreCase(estado))
                    .toList();
        }
        
        model.addAttribute("facturas", facturas);
        model.addAttribute("titulo", "Mis Facturas");
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroUsuarioId", usuarioId);
        model.addAttribute("usuarioActual", usuarioActual);
        
        return "facturas/lista";
    }

    @GetMapping("/{id}")
    public String verFactura(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Factura factura = facturaService.obtenerPorId(id).orElse(null);
        
        if (factura == null) {
            redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
            return "redirect:/facturas";
        }
        
        model.addAttribute("factura", factura);
        model.addAttribute("titulo", "Detalles de Factura");
        
        return "facturas/detalles";
    }

    @PostMapping("/{id}/marcar-pagada")
    public String marcarComoPagada(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facturaService.marcarComoPagada(id);
            redirectAttributes.addFlashAttribute("success", "Factura marcada como pagada");
            return "redirect:/facturas/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/facturas/" + id;
        }
    }

    @PostMapping("/{id}/anular")
    public String anularFactura(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Factura factura = facturaService.obtenerPorId(id).orElse(null);
            if (factura == null) {
                redirectAttributes.addFlashAttribute("error", "Factura no encontrada");
                return "redirect:/facturas";
            }
            
            factura.setEstado("anulada");
            facturaService.guardar(factura);
            
            redirectAttributes.addFlashAttribute("success", "Factura anulada correctamente");
            return "redirect:/facturas/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/facturas/" + id;
        }
    }
}
