package pe.edu.cibertec.gasmapocho.msmovimientos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.cibertec.gasmapocho.msmovimientos.model.Cliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.EstadoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.ClienteRepository;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.MovimientoRepository;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    void noSiembraNadaSiYaHayMovimientos() {
        when(movimientoRepository.count()).thenReturn(3L);

        dataSeeder.run();

        verify(movimientoRepository, never()).save(any());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void siembraClientesYMovimientosCoherentesEnUnaBaseVacia() {
        when(movimientoRepository.count()).thenReturn(0L);
        when(clienteRepository.count()).thenReturn(0L);
        long[] siguienteId = { 1 };
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(siguienteId[0]++);
            return c;
        });
        List<Movimiento> guardados = new ArrayList<>();
        long[] idMovimiento = { 1 };
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(idMovimiento[0]++);
                guardados.add(m);
            }
            return m;
        });

        dataSeeder.run();

        verify(clienteRepository, times(3)).save(any(Cliente.class));
        assertThat(guardados).hasSize(12);
        assertThat(guardados).extracting(Movimiento::getFolio).doesNotHaveDuplicates()
                .allMatch(f -> f.matches("MOV-\\d{5}"));
        // Las compras llevan proveedor; las ventas, cliente o nada, pero nunca proveedor.
        assertThat(guardados).filteredOn(m -> m.getTipo() == TipoMovimiento.ENTRADA)
                .allMatch(m -> m.getProveedorId() != null && m.getClienteId() == null);
        assertThat(guardados).filteredOn(m -> m.getTipo() == TipoMovimiento.SALIDA)
                .allMatch(m -> m.getProveedorId() == null);
        // Hay un ejemplo de cada estado y ninguna fecha en el futuro.
        assertThat(guardados).extracting(Movimiento::getEstado).contains(EstadoMovimiento.REGISTRADO,
                EstadoMovimiento.CONFIRMADO, EstadoMovimiento.ANULADO);
        assertThat(guardados).allMatch(m -> !m.getFecha().isAfter(java.time.LocalDateTime.now()));

        ArgumentCaptor<Cliente> clientes = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository, times(3)).save(clientes.capture());
        assertThat(clientes.getAllValues()).extracting(Cliente::getRut).doesNotHaveDuplicates();
    }
}
