package pe.edu.cibertec.gasmapocho.msmovimientos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import pe.edu.cibertec.gasmapocho.msmovimientos.dto.BalonDTO;

@FeignClient(name = "ms-balones", configuration = FeignAuthConfig.class)
public interface BalonClient {

    @GetMapping("/api/balones/codigo/{codigo}")
    BalonDTO obtenerPorCodigo(@PathVariable("codigo") String codigo);
}
