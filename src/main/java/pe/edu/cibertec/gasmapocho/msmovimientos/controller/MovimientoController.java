package pe.edu.cibertec.gasmapocho.msmovimientos.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.cibertec.gasmapocho.msmovimientos.dto.CompraRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoEdicionDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.service.MovimientoService;

// ADMIN y ALMACEN pueden ver y registrar movimientos; solo ADMIN edita, confirma o anula
// (ver cuadro de permisos del informe).
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public List<MovimientoDTO> listar() {
        return movimientoService.listar();
    }

    @PostMapping
    public MovimientoDTO registrar(@Valid @RequestBody MovimientoRequestDTO request) {
        return movimientoService.registrar(request);
    }

    // Historial de compras a proveedores; opcionalmente filtrado por un proveedor.
    @GetMapping("/compras")
    public List<MovimientoDTO> listarCompras(@RequestParam(required = false) Long proveedorId) {
        return movimientoService.listarCompras(proveedorId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public MovimientoDTO editar(@PathVariable Long id, @Valid @RequestBody MovimientoEdicionDTO request) {
        return movimientoService.editar(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/confirmar")
    public MovimientoDTO confirmar(@PathVariable Long id) {
        return movimientoService.confirmar(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/anular")
    public MovimientoDTO anular(@PathVariable Long id) {
        return movimientoService.anular(id);
    }

    @PostMapping("/compras")
    public MovimientoDTO registrarCompra(@Valid @RequestBody CompraRequestDTO request) {
        return movimientoService.registrarCompra(request);
    }
}
