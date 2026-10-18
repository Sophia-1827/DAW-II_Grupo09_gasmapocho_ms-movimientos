package pe.edu.cibertec.gasmapocho.msmovimientos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Lo unico editable de un movimiento REGISTRADO; el balon, el tipo y el cliente/proveedor no cambian.
public record MovimientoEdicionDTO(
        @NotNull @Min(1) Integer cantidad,
        @NotBlank String responsable,
        String observacion) {
}
