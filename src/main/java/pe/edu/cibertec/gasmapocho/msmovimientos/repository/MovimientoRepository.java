package pe.edu.cibertec.gasmapocho.msmovimientos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findAllByOrderByFechaDesc();

    List<Movimiento> findByProveedorIdIsNotNullOrderByFechaDesc();

    List<Movimiento> findByProveedorIdOrderByFechaDesc(Long proveedorId);
}
