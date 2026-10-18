package pe.edu.cibertec.gasmapocho.msmovimientos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompraRequestDTO(
        @NotNull Long proveedorId,
        @NotBlank String balonCodigo,
        @NotNull @Min(1) Integer cantidad,
        @Positive Double precioUnitario,
        @NotBlank String responsable,
        String observacion) {
}
