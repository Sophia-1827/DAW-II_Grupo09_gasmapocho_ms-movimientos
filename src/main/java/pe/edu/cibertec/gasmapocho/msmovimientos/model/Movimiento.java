package pe.edu.cibertec.gasmapocho.msmovimientos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String folio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(name = "balon_codigo", nullable = false, length = 20)
    private String balonCodigo;

    @Column(name = "balon_nombre", nullable = false, length = 100)
    private String balonNombre;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(nullable = false, length = 100)
    private String responsable;

    @Column(length = 255)
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // Solo las compras a proveedor los llenan; las ventas y ajustes quedan en null.
    @Column(name = "proveedor_id")
    private Long proveedorId;

    @Column(name = "proveedor_nombre", length = 100)
    private String proveedorNombre;

    // Solo las ventas (SALIDA) con cliente los llenan. Se copian al registrar para que el historial
    // conserve lo que se vendio aunque luego se edite o desactive el cliente.
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nombre", length = 100)
    private String clienteNombre;

    @Column(name = "cliente_direccion", length = 150)
    private String clienteDireccion;

    // El default de columna permite agregar el campo a una tabla que ya tiene movimientos.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15, columnDefinition = "varchar(15) default 'REGISTRADO'")
    private EstadoMovimiento estado = EstadoMovimiento.REGISTRADO;
}
