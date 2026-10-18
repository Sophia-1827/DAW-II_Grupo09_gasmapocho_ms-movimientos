package pe.edu.cibertec.gasmapocho.msmovimientos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;

public record MovimientoRequestDTO(
        @NotNull TipoMovimiento tipo,
        @NotBlank String balonCodigo,
        @NotNull @Min(1) Integer cantidad,
        @NotBlank String responsable,
        String observacion,
        Long clienteId) {
}
