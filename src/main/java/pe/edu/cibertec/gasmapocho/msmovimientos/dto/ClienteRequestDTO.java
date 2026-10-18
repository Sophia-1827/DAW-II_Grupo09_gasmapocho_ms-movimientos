package pe.edu.cibertec.gasmapocho.msmovimientos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 15) String rut,
        @NotBlank @Size(max = 150) String direccion,
        @NotBlank @Size(max = 20) String telefono) {
}
