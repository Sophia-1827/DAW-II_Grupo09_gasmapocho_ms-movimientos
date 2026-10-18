package pe.edu.cibertec.gasmapocho.msmovimientos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msmovimientos.client.BalonClienteResiliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.client.ProveedorClienteResiliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.CompraRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoEdicionDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ProveedorDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoPublicador;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Cliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.EstadoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.ClienteRepository;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.MovimientoRepository;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private BalonClienteResiliente balonClienteResiliente;

    @Mock
    private ProveedorClienteResiliente proveedorClienteResiliente;

    @Mock
    private MovimientoPublicador publicador;

    @InjectMocks
    private MovimientoService movimientoService;

    @Test
    void listarDevuelveTodosLosMovimientosOrdenados() {
        Movimiento m1 = new Movimiento(1L, "MOV-00001", TipoMovimiento.ENTRADA, "BAL-5", "Balon 5 kg", 10, 14500.0,
                "Juan", null, LocalDateTime.now(), null, null, null, null, null, EstadoMovimiento.REGISTRADO);
        when(movimientoRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(m1));

        List<MovimientoDTO> resultado = movimientoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).folio()).isEqualTo("MOV-00001");
    }

    @Test
    void registrarInsertaUnaEntradaYPublicaElEvento() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 40, 10);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5")).thenReturn(balon);
        simularGuardado(7L);

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.ENTRADA, "BAL-5", 10, "Juan", null, null);
        MovimientoDTO resultado = movimientoService.registrar(request);

        assertThat(resultado.folio()).isEqualTo("MOV-00007");
        assertThat(resultado.balonNombre()).isEqualTo("Balon 5 kg");
        assertThat(resultado.precioUnitario()).isEqualTo(14500.0);
        verify(publicador).publicar(any());
    }

    @Test
    void registrarRechazaSalidaSinStockSuficiente() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 3, 10);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5")).thenReturn(balon);

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 5, "Juan", null, null);

        assertThrows(ResponseStatusException.class, () -> movimientoService.registrar(request));
    }

    @Test
    void registrarAceptaSalidaConStockSuficienteYPublicaElEvento() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 10, 2);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5")).thenReturn(balon);
        simularGuardado(3L);

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 5, "Ana", "Venta", null);
        MovimientoDTO resultado = movimientoService.registrar(request);

        assertThat(resultado.tipo()).isEqualTo(TipoMovimiento.SALIDA);
        verify(publicador).publicar(any());
    }

    @Test
    void registrarCompraCreaUnaEntradaConElProveedorYPublicaElEvento() {
        ProveedorDTO proveedor = new ProveedorDTO(2L, "Pacifico Ltda.", "77.208.915-6", "123", "Valparaiso",
                "Valparaiso", true);
        BalonDTO balon = new BalonDTO(1L, "BAL-11", "Balon 11 kg", 21000.0, 30, 8);
        when(proveedorClienteResiliente.obtener(2L)).thenReturn(proveedor);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-11")).thenReturn(balon);
        simularGuardado(9L);

        CompraRequestDTO request = new CompraRequestDTO(2L, "BAL-11", 20, 18000.0, "Ana", "Pedido semanal");
        MovimientoDTO resultado = movimientoService.registrarCompra(request);

        assertThat(resultado.tipo()).isEqualTo(TipoMovimiento.ENTRADA);
        assertThat(resultado.proveedorId()).isEqualTo(2L);
        assertThat(resultado.proveedorNombre()).isEqualTo("Pacifico Ltda.");
        assertThat(resultado.precioUnitario()).isEqualTo(18000.0);
        verify(publicador).publicar(any());
    }

    @Test
    void registrarCompraUsaElPrecioDelCatalogoSiNoSeIndicaCosto() {
        ProveedorDTO proveedor = new ProveedorDTO(2L, "Pacifico Ltda.", "77.208.915-6", "123", "Valparaiso",
                "Valparaiso", true);
        BalonDTO balon = new BalonDTO(1L, "BAL-11", "Balon 11 kg", 21000.0, 30, 8);
        when(proveedorClienteResiliente.obtener(2L)).thenReturn(proveedor);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-11")).thenReturn(balon);
        simularGuardado(10L);

        MovimientoDTO resultado = movimientoService
                .registrarCompra(new CompraRequestDTO(2L, "BAL-11", 5, null, "Ana", null));

        assertThat(resultado.precioUnitario()).isEqualTo(21000.0);
    }

    @Test
    void registrarCompraRechazaUnProveedorDesactivado() {
        ProveedorDTO inactivo = new ProveedorDTO(3L, "Sur Gas", "96.784.120-3", "123", "Coronel", "Biobio", false);
        when(proveedorClienteResiliente.obtener(3L)).thenReturn(inactivo);

        CompraRequestDTO request = new CompraRequestDTO(3L, "BAL-5", 5, null, "Ana", null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.registrarCompra(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(publicador, never()).publicar(any());
    }

    @Test
    void listarComprasFiltraPorProveedorCuandoSeIndica() {
        Movimiento compra = new Movimiento(4L, "MOV-00004", TipoMovimiento.ENTRADA, "BAL-5", "Balon 5 kg", 10,
                14000.0, "Ana", null, LocalDateTime.now(), 2L, "Pacifico Ltda.", null, null, null, EstadoMovimiento.REGISTRADO);
        when(movimientoRepository.findByProveedorIdOrderByFechaDesc(2L)).thenReturn(List.of(compra));

        List<MovimientoDTO> resultado = movimientoService.listarCompras(2L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).proveedorNombre()).isEqualTo("Pacifico Ltda.");
    }

    @Test
    void listarComprasSinFiltroDevuelveTodasLasComprasAProveedores() {
        when(movimientoRepository.findByProveedorIdIsNotNullOrderByFechaDesc()).thenReturn(List.of());

        assertThat(movimientoService.listarCompras(null)).isEmpty();
        verify(movimientoRepository).findByProveedorIdIsNotNullOrderByFechaDesc();
    }

    @Test
    void registrarVentaGuardaLosDatosDelCliente() {
        Cliente cliente = new Cliente(5L, "Maria Rojas", "12.345.678-5", "Los Aromos 123, Maipu", "+56 9 1234 5678",
                true);
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 10, 2);
        when(clienteRepository.findById(5L)).thenReturn(java.util.Optional.of(cliente));
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5")).thenReturn(balon);
        simularGuardado(11L);

        MovimientoDTO resultado = movimientoService.registrar(
                new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 2, "Ana", null, 5L));

        assertThat(resultado.clienteId()).isEqualTo(5L);
        assertThat(resultado.clienteNombre()).isEqualTo("Maria Rojas");
        assertThat(resultado.clienteDireccion()).isEqualTo("Los Aromos 123, Maipu");
    }

    @Test
    void registrarVentaRechazaUnClienteDesactivado() {
        Cliente inactivo = new Cliente(6L, "Pedro Soto", "9.876.543-2", "Calle 2", "123", false);
        when(clienteRepository.findById(6L)).thenReturn(java.util.Optional.of(inactivo));

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 1, "Ana", null, 6L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.registrar(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(publicador, never()).publicar(any());
    }

    @Test
    void registrarVentaRechazaUnClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 1, "Ana", null, 99L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.registrar(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void registrarEntradaConClienteEsInvalido() {
        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.ENTRADA, "BAL-5", 1, "Ana", null, 5L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.registrar(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Los movimientos de prueba para editar/anular: ENTRADA de 10 y SALIDA de 4 unidades de BAL-5.
    private Movimiento entrada(EstadoMovimiento estado) {
        return new Movimiento(20L, "MOV-00020", TipoMovimiento.ENTRADA, "BAL-5", "Balon 5 kg", 10, 14500.0, "Ana",
                null, LocalDateTime.now(), null, null, null, null, null, estado);
    }

    private Movimiento salida(EstadoMovimiento estado) {
        return new Movimiento(21L, "MOV-00021", TipoMovimiento.SALIDA, "BAL-5", "Balon 5 kg", 4, 14500.0, "Ana",
                null, LocalDateTime.now(), null, null, null, null, null, estado);
    }

    private org.mockito.ArgumentCaptor<pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoEvento> eventoPublicado() {
        org.mockito.ArgumentCaptor<pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoEvento> captor = org.mockito.ArgumentCaptor
                .forClass(pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoEvento.class);
        verify(publicador).publicar(captor.capture());
        return captor;
    }

    @Test
    void editarSubirCantidadDeUnaEntradaPublicaLaDiferenciaComoEntrada() {
        when(movimientoRepository.findById(20L)).thenReturn(java.util.Optional.of(entrada(EstadoMovimiento.REGISTRADO)));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoDTO resultado = movimientoService.editar(20L, new MovimientoEdicionDTO(15, "Luis", "corregido"));

        assertThat(resultado.cantidad()).isEqualTo(15);
        assertThat(resultado.responsable()).isEqualTo("Luis");
        var evento = eventoPublicado().getValue();
        assertThat(evento.tipo()).isEqualTo("ENTRADA");
        assertThat(evento.cantidad()).isEqualTo(5);
    }

    @Test
    void editarBajarCantidadDeUnaSalidaDevuelveStockConUnaEntrada() {
        when(movimientoRepository.findById(21L)).thenReturn(java.util.Optional.of(salida(EstadoMovimiento.REGISTRADO)));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        movimientoService.editar(21L, new MovimientoEdicionDTO(1, "Ana", null));

        var evento = eventoPublicado().getValue();
        assertThat(evento.tipo()).isEqualTo("ENTRADA");
        assertThat(evento.cantidad()).isEqualTo(3);
    }

    @Test
    void editarSinCambiarLaCantidadNoPublicaEvento() {
        when(movimientoRepository.findById(20L)).thenReturn(java.util.Optional.of(entrada(EstadoMovimiento.REGISTRADO)));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        movimientoService.editar(20L, new MovimientoEdicionDTO(10, "Luis", "solo el responsable"));

        verify(publicador, never()).publicar(any());
    }

    @Test
    void editarRechazaBajarUnaEntradaSiYaNoHayStockParaDevolver() {
        when(movimientoRepository.findById(20L)).thenReturn(java.util.Optional.of(entrada(EstadoMovimiento.REGISTRADO)));
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5"))
                .thenReturn(new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 2, 10));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.editar(20L, new MovimientoEdicionDTO(5, "Ana", null)));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(movimientoRepository, never()).save(any());
        verify(publicador, never()).publicar(any());
    }

    @Test
    void editarRechazaUnMovimientoQueYaNoEstaRegistrado() {
        when(movimientoRepository.findById(20L)).thenReturn(java.util.Optional.of(entrada(EstadoMovimiento.CONFIRMADO)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.editar(20L, new MovimientoEdicionDTO(12, "Ana", null)));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void confirmarPasaDeRegistradoAConfirmado() {
        when(movimientoRepository.findById(20L)).thenReturn(java.util.Optional.of(entrada(EstadoMovimiento.REGISTRADO)));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(movimientoService.confirmar(20L).estado()).isEqualTo(EstadoMovimiento.CONFIRMADO);
        verify(publicador, never()).publicar(any());
    }

    @Test
    void anularUnaSalidaDevuelveElStockConUnaEntrada() {
        when(movimientoRepository.findById(21L)).thenReturn(java.util.Optional.of(salida(EstadoMovimiento.CONFIRMADO)));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoDTO resultado = movimientoService.anular(21L);

        assertThat(resultado.estado()).isEqualTo(EstadoMovimiento.ANULADO);
        var evento = eventoPublicado().getValue();
        assertThat(evento.tipo()).isEqualTo("ENTRADA");
        assertThat(evento.cantidad()).isEqualTo(4);
    }

    @Test
    void anularUnaEntradaRestaElStockConUnaSalida() {
        when(movimientoRepository.findById(20L)).thenReturn(java.util.Optional.of(entrada(EstadoMovimiento.REGISTRADO)));
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5"))
                .thenReturn(new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 40, 10));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        movimientoService.anular(20L);

        var evento = eventoPublicado().getValue();
        assertThat(evento.tipo()).isEqualTo("SALIDA");
        assertThat(evento.cantidad()).isEqualTo(10);
    }

    @Test
    void anularUnaEntradaSinStockSuficienteEsConflicto() {
        when(movimientoRepository.findById(20L)).thenReturn(java.util.Optional.of(entrada(EstadoMovimiento.REGISTRADO)));
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5"))
                .thenReturn(new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 3, 10));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> movimientoService.anular(20L));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void anularUnMovimientoYaAnuladoEsConflicto() {
        when(movimientoRepository.findById(21L)).thenReturn(java.util.Optional.of(salida(EstadoMovimiento.ANULADO)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> movimientoService.anular(21L));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void editarLanza404SiElMovimientoNoExiste() {
        when(movimientoRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.editar(99L, new MovimientoEdicionDTO(1, "Ana", null)));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void simularGuardado(Long id) {
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(id);
            }
            return m;
        });
    }
}
