package pe.edu.cibertec.gasmapocho.msmovimientos.dto;

import java.time.LocalDateTime;

import pe.edu.cibertec.gasmapocho.msmovimientos.model.EstadoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;

public record MovimientoDTO(
        Long id,
        String folio,
        TipoMovimiento tipo,
        String balonCodigo,
        String balonNombre,
        Integer cantidad,
        Double precioUnitario,
        String responsable,
        String observacion,
        LocalDateTime fecha,
        Long proveedorId,
        String proveedorNombre,
        Long clienteId,
        String clienteNombre,
        String clienteDireccion,
        EstadoMovimiento estado) {
}
