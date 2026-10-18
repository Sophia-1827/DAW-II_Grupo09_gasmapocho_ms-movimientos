package pe.edu.cibertec.gasmapocho.msmovimientos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ClienteDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ClienteRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Cliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void listarActivosDevuelveSoloLosActivos() {
        Cliente c1 = new Cliente(1L, "Maria Rojas", "12.345.678-5", "Los Aromos 123", "123", true);
        when(clienteRepository.findByActivoTrue()).thenReturn(List.of(c1));

        List<ClienteDTO> resultado = clienteService.listarActivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("Maria Rojas");
    }

    @Test
    void crearInsertaClienteActivoConRutNormalizado() {
        ClienteRequestDTO request = new ClienteRequestDTO("Nuevo Cliente", " 11.222.333-k ", "Calle 1", "+56 9 1111 2222");
        when(clienteRepository.existsByRut("11.222.333-K")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        ClienteDTO resultado = clienteService.crear(request);

        assertThat(resultado.id()).isEqualTo(10L);
        assertThat(resultado.rut()).isEqualTo("11.222.333-K");
        assertThat(resultado.activo()).isTrue();
    }

    @Test
    void crearRechazaRutDuplicado() {
        ClienteRequestDTO request = new ClienteRequestDTO("Otro", "12.345.678-5", "Calle", "123");
        when(clienteRepository.existsByRut("12.345.678-5")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> clienteService.crear(request));
    }

    @Test
    void actualizarRechazaRutDeOtroCliente() {
        Cliente existente = new Cliente(3L, "Pedro Soto", "9.876.543-2", "Calle 2", "123", true);
        when(clienteRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByRutAndIdNot("12.345.678-5", 3L)).thenReturn(true);

        ClienteRequestDTO request = new ClienteRequestDTO("Pedro Soto", "12.345.678-5", "Calle 2", "123");

        assertThrows(ResponseStatusException.class, () -> clienteService.actualizar(3L, request));
    }

    @Test
    void actualizarModificaLosDatosDelCliente() {
        Cliente existente = new Cliente(2L, "Maria Rojas", "12.345.678-5", "Los Aromos 123", "111", true);
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByRutAndIdNot("12.345.678-5", 2L)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteDTO resultado = clienteService.actualizar(2L,
                new ClienteRequestDTO("Maria Rojas P.", "12.345.678-5", "Los Aromos 456", "222"));

        assertThat(resultado.nombre()).isEqualTo("Maria Rojas P.");
        assertThat(resultado.direccion()).isEqualTo("Los Aromos 456");
    }

    @Test
    void cambiarEstadoAlternaActivoSinBorrar() {
        Cliente existente = new Cliente(4L, "Maria Rojas", "12.345.678-5", "Los Aromos 123", "111", true);
        when(clienteRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteDTO resultado = clienteService.cambiarEstado(4L);

        assertThat(resultado.activo()).isFalse();
        verify(clienteRepository).save(existente);
    }

    @Test
    void cambiarEstadoLanza404SiNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> clienteService.cambiarEstado(99L));
    }
}
