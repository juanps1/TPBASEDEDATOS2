package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.Pago;
import com.tp.persistencia.persistencia_poliglota.model.sql.Factura;
import com.tp.persistencia.persistencia_poliglota.service.PagoService;
import com.tp.persistencia.persistencia_poliglota.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoWebController {

    private final PagoService pagoService;
    private final FacturaService facturaService;

    @GetMapping
    public String listarPagos(Model model) {
        List<Pago> pagos = pagoService.listarTodos();
        model.addAttribute("pagos", pagos);
        model.addAttribute("titulo", "Gestión de Pagos");
        return "pagos/lista";
    }

    @GetMapping("/{id}")
    public String verDetalles(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Pago pago = pagoService.obtenerPorId(id).orElse(null);
        
        if (pago == null) {
            redirectAttributes.addFlashAttribute("error", "Pago no encontrado");
            return "redirect:/pagos";
        }
        
        model.addAttribute("pago", pago);
        model.addAttribute("titulo", "Detalles del Pago");
        return "pagos/detalles";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        List<Factura> facturasPendientes = facturaService.listarTodas().stream()
                .filter(f -> "pendiente".equalsIgnoreCase(f.getEstado()))
                .toList();
        
        model.addAttribute("pago", new Pago());
        model.addAttribute("facturas", facturasPendientes);
        model.addAttribute("titulo", "Registrar Nuevo Pago");
        model.addAttribute("accion", "crear");
        return "pagos/formulario";
    }

    @PostMapping("/crear")
    public String crearPago(@RequestParam Long facturaId,
                           @RequestParam Double monto,
                           @RequestParam String metodoPago,
                           @RequestParam(required = false) String observaciones,
                           RedirectAttributes redirectAttributes) {
        try {
            Pago pago = new Pago();
            pago.setFactura(facturaService.obtenerPorId(facturaId).orElse(null));
            pago.setMontoPagado(monto);
            pago.setMetodoPago(metodoPago);
            pago.setFechaPago(LocalDateTime.now());
            
            pagoService.registrarPago(facturaId, monto, metodoPago);
            redirectAttributes.addFlashAttribute("success", "Pago registrado correctamente");
            return "redirect:/pagos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar pago: " + e.getMessage());
            return "redirect:/pagos/nuevo";
        }
    }
}
