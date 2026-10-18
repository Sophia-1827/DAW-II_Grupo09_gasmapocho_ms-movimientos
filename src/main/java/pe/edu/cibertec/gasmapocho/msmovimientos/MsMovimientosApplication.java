package pe.edu.cibertec.gasmapocho.msmovimientos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsMovimientosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsMovimientosApplication.class, args);
    }

}
