package pe.edu.cibertec.gasmapocho.msmovimientos.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ClienteDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ClienteRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Cliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteDTO> listar() {
        return clienteRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<ClienteDTO> listarActivos() {
        return clienteRepository.findByActivoTrue().stream().map(this::toDTO).toList();
    }

    public ClienteDTO obtener(Long id) {
        return toDTO(buscarOLanzar(id));
    }

    public ClienteDTO crear(ClienteRequestDTO request) {
        if (clienteRepository.existsByRut(normalizarRut(request.rut()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese RUT");
        }
        Cliente cliente = new Cliente();
        aplicar(cliente, request);
        return toDTO(clienteRepository.save(cliente));
    }

    public ClienteDTO actualizar(Long id, ClienteRequestDTO request) {
        Cliente cliente = buscarOLanzar(id);
        if (clienteRepository.existsByRutAndIdNot(normalizarRut(request.rut()), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro cliente con ese RUT");
        }
        aplicar(cliente, request);
        return toDTO(clienteRepository.save(cliente));
    }

    // Alterna activo/inactivo en vez de borrar, para no perder el historial de ventas.
    public ClienteDTO cambiarEstado(Long id) {
        Cliente cliente = buscarOLanzar(id);
        cliente.setActivo(!cliente.getActivo());
        return toDTO(clienteRepository.save(cliente));
    }

    private void aplicar(Cliente cliente, ClienteRequestDTO request) {
        cliente.setNombre(request.nombre().trim());
        cliente.setRut(normalizarRut(request.rut()));
        cliente.setDireccion(request.direccion().trim());
        cliente.setTelefono(request.telefono().trim());
    }

    private String normalizarRut(String rut) {
        return rut.trim().toUpperCase();
    }

    private Cliente buscarOLanzar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private ClienteDTO toDTO(Cliente cliente) {
        return new ClienteDTO(cliente.getId(), cliente.getNombre(), cliente.getRut(), cliente.getDireccion(),
                cliente.getTelefono(), cliente.getActivo());
    }
}
