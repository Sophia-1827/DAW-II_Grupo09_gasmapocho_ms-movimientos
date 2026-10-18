package pe.edu.cibertec.gasmapocho.msmovimientos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.cibertec.gasmapocho.msmovimientos.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByRut(String rut);

    boolean existsByRutAndIdNot(String rut, Long id);

    List<Cliente> findByActivoTrue();
}
