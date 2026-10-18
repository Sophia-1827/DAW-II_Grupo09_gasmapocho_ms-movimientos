package pe.edu.cibertec.gasmapocho.msmovimientos;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pe.edu.cibertec.gasmapocho.msmovimientos.model.Cliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.EstadoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.ClienteRepository;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.MovimientoRepository;

// Datos de ejemplo para que la demo no empiece en cero. Los movimientos se insertan directo (sin eventos de
// RabbitMQ): el stock sembrado en ms-balones ya es el stock "actual" y estos movimientos son su historial reciente.
// Los proveedores 1 a 3 son los que siembra ms-balones; aqui solo se guardan su id y nombre.
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String RESPONSABLE = "Personal de Almacen";

    private final MovimientoRepository movimientoRepository;
    private final ClienteRepository clienteRepository;

    public DataSeeder(MovimientoRepository movimientoRepository, ClienteRepository clienteRepository) {
        this.movimientoRepository = movimientoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void run(String... args) {
        if (movimientoRepository.count() > 0 || clienteRepository.count() > 0) {
            return;
        }
        Cliente rojas = clienteRepository.save(new Cliente(null, "Maria Rojas Pizarro", "12.345.678-5",
                "Los Aromos 1234, Maipu", "+56 9 8123 4567", true));
        Cliente soto = clienteRepository.save(new Cliente(null, "Restaurante El Fogon Ltda.", "76.123.456-K",
                "Av. Independencia 2580, Independencia", "+56 2 2735 1020", true));
        Cliente munoz = clienteRepository.save(new Cliente(null, "Pedro Munoz Araya", "9.876.543-2",
                "Pasaje Los Olmos 45, La Florida", "+56 9 7765 4321", true));

        compra(9, "BAL-5", "Balon de gas 5 kg", 30, 11600.0, 1L, "Gas Andes Distribuciones SpA", "Reposicion mensual",
                EstadoMovimiento.CONFIRMADO);
        compra(9, "BAL-11", "Balon de gas 11 kg", 20, 16800.0, 1L, "Gas Andes Distribuciones SpA",
                "Reposicion mensual", EstadoMovimiento.CONFIRMADO);
        compra(7, "BAL-15", "Balon de gas 15 kg", 15, 23100.0, 2L, "Combustibles del Pacifico Ltda.", null,
                EstadoMovimiento.CONFIRMADO);
        venta(6, "BAL-5", "Balon de gas 5 kg", 4, 14500.0, rojas, null, EstadoMovimiento.CONFIRMADO);
        venta(5, "BAL-11", "Balon de gas 11 kg", 3, 21000.0, soto, "Pedido semanal", EstadoMovimiento.CONFIRMADO);
        compra(4, "BAL-45", "Balon de gas 45 kg", 6, 23900.0, 3L, "Envasadora Sur Gas S.A.", null,
                EstadoMovimiento.CONFIRMADO);
        venta(3, "BAL-15", "Balon de gas 15 kg", 2, 28900.0, munoz, null, EstadoMovimiento.CONFIRMADO);
        venta(2, "BAL-5", "Balon de gas 5 kg", 6, 14500.0, rojas, null, EstadoMovimiento.REGISTRADO);
        venta(1, "BAL-45", "Balon de gas 45 kg", 1, 29900.0, soto, null, EstadoMovimiento.REGISTRADO);
        venta(1, "BAL-11", "Balon de gas 11 kg", 2, 21000.0, null, "Anulada: se registro con el cliente equivocado",
                EstadoMovimiento.ANULADO);
        compra(0, "BAL-5", "Balon de gas 5 kg", 12, 11600.0, 1L, "Gas Andes Distribuciones SpA", null,
                EstadoMovimiento.REGISTRADO);
        venta(0, "BAL-11", "Balon de gas 11 kg", 1, 21000.0, munoz, null, EstadoMovimiento.REGISTRADO);
    }

    private void compra(int diasAtras, String codigo, String nombre, int cantidad, double costo, Long proveedorId,
            String proveedorNombre, String observacion, EstadoMovimiento estado) {
        Movimiento m = base(diasAtras, TipoMovimiento.ENTRADA, codigo, nombre, cantidad, costo, observacion, estado);
        m.setProveedorId(proveedorId);
        m.setProveedorNombre(proveedorNombre);
        guardar(m);
    }

    private void venta(int diasAtras, String codigo, String nombre, int cantidad, double precio, Cliente cliente,
            String observacion, EstadoMovimiento estado) {
        Movimiento m = base(diasAtras, TipoMovimiento.SALIDA, codigo, nombre, cantidad, precio, observacion, estado);
        if (cliente != null) {
            m.setClienteId(cliente.getId());
            m.setClienteNombre(cliente.getNombre());
            m.setClienteDireccion(cliente.getDireccion());
        }
        guardar(m);
    }

    private Movimiento base(int diasAtras, TipoMovimiento tipo, String codigo, String nombre, int cantidad,
            double precio, String observacion, EstadoMovimiento estado) {
        Movimiento m = new Movimiento();
        m.setTipo(tipo);
        m.setBalonCodigo(codigo);
        m.setBalonNombre(nombre);
        m.setCantidad(cantidad);
        m.setPrecioUnitario(precio);
        m.setResponsable(RESPONSABLE);
        m.setObservacion(observacion);
        m.setEstado(estado);
        // Los de hoy se fechan 30 minutos atras (nunca en el futuro); los anteriores, a las 11:00 de ese dia.
        m.setFecha(diasAtras == 0 ? LocalDateTime.now().minusMinutes(30)
                : LocalDateTime.now().minusDays(diasAtras).withHour(11).withMinute(0).withSecond(0).withNano(0));
        return m;
    }

    // Mismo folio que genera MovimientoService: MOV-<id con 5 digitos>.
    private void guardar(Movimiento m) {
        m.setFolio("TEMP-" + System.nanoTime());
        m = movimientoRepository.save(m);
        m.setFolio("MOV-" + String.format("%05d", m.getId()));
        movimientoRepository.save(m);
    }
}
