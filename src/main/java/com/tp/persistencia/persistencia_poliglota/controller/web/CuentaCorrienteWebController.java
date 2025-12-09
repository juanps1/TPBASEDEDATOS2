package com.tp.persistencia.persistencia_poliglota.controller.web;

import com.tp.persistencia.persistencia_poliglota.model.sql.CuentaCorriente;
import com.tp.persistencia.persistencia_poliglota.service.CuentaCorrienteService;
import com.tp.persistencia.persistencia_poliglota.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaCorrienteWebController {

    private final CuentaCorrienteService cuentaCorrienteService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarCuentas(Model model) {
        List<CuentaCorriente> cuentas = cuentaCorrienteService.listarTodas();
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("titulo", "Cuentas Corrientes");
        return "cuentas/lista";
    }

    @GetMapping("/{id}")
    public String verDetalles(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        CuentaCorriente cuenta = cuentaCorrienteService.obtenerPorId(id).orElse(null);
        
        if (cuenta == null) {
            redirectAttributes.addFlashAttribute("error", "Cuenta no encontrada");
            return "redirect:/cuentas";
        }
        
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("movimientos", cuenta.getMovimientos());
        model.addAttribute("titulo", "Detalles de Cuenta Corriente");
        return "cuentas/detalles";
    }

    @PostMapping("/{id}/debito")
    public String registrarDebito(@PathVariable Long id,
                                 @RequestParam Double monto,
                                 @RequestParam String descripcion,
                                 RedirectAttributes redirectAttributes) {
        try {
            cuentaCorrienteService.registrarDebito(id, monto, descripcion, null);
            redirectAttributes.addFlashAttribute("success", "Débito registrado correctamente");
            return "redirect:/cuentas/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/cuentas/" + id;
        }
    }

    @PostMapping("/{id}/credito")
    public String registrarCredito(@PathVariable Long id,
                                  @RequestParam Double monto,
                                  @RequestParam String descripcion,
                                  RedirectAttributes redirectAttributes) {
        try {
            cuentaCorrienteService.registrarCredito(id, monto, descripcion, null);
            redirectAttributes.addFlashAttribute("success", "Crédito registrado correctamente");
            return "redirect:/cuentas/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/cuentas/" + id;
        }
    }
}
