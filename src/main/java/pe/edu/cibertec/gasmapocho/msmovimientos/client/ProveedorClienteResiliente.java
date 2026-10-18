package pe.edu.cibertec.gasmapocho.msmovimientos.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ProveedorDTO;

// Mismo patron que BalonClienteResiliente: la anotacion vive en un bean aparte para que el AOP funcione,
// y el 404 (proveedor inexistente) se ignora en application.properties para no abrir el circuito.
@Service
public class ProveedorClienteResiliente {

    private final ProveedorClient proveedorClient;

    public ProveedorClienteResiliente(ProveedorClient proveedorClient) {
        this.proveedorClient = proveedorClient;
    }

    @CircuitBreaker(name = "proveedorClient", fallbackMethod = "fallbackObtener")
    public ProveedorDTO obtener(Long id) {
        return proveedorClient.obtener(id);
    }

    // El 404 llega igual al fallback aunque este ignorado por el circuito: se relanza para responder 404.
    private ProveedorDTO fallbackObtener(Long id, Throwable t) {
        if (t instanceof FeignException.NotFound notFound) {
            throw notFound;
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "ms-balones no esta disponible en este momento, intenta de nuevo en unos segundos");
    }
}
