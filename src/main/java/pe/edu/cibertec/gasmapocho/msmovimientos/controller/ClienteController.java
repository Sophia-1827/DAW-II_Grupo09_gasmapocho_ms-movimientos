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
import org.springframework.web.bind.annotation.RestController;

import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ClienteDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ClienteRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.service.ClienteService;

// ALMACEN lee y da de alta clientes al registrar una venta; solo ADMIN edita y activa/desactiva.
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteDTO> listar() {
        return clienteService.listar();
    }

    @GetMapping("/activos")
    public List<ClienteDTO> listarActivos() {
        return clienteService.listarActivos();
    }

    @GetMapping("/{id}")
    public ClienteDTO obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACEN')")
    @PostMapping
    public ClienteDTO crear(@Valid @RequestBody ClienteRequestDTO request) {
        return clienteService.crear(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ClienteDTO actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO request) {
        return clienteService.actualizar(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ClienteDTO cambiarEstado(@PathVariable Long id) {
        return clienteService.cambiarEstado(id);
    }
}
