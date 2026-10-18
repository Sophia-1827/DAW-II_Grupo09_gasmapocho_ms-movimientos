package pe.edu.cibertec.gasmapocho.msmovimientos.dto;

public record ProveedorDTO(Long id, String nombre, String rut, String telefono, String direccion, String region,
        Boolean activo) {
}
