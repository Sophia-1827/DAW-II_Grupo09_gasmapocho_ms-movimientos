package pe.edu.cibertec.gasmapocho.msmovimientos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ProveedorDTO;

@FeignClient(name = "ms-balones", contextId = "proveedorClient", configuration = FeignAuthConfig.class)
public interface ProveedorClient {

    @GetMapping("/api/proveedores/{id}")
    ProveedorDTO obtener(@PathVariable("id") Long id);
}
