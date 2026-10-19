
# Gas Mapocho - Microservicio de Movimientos

Este repositorio contiene el microservicio de movimientos de inventario del proyecto **Gas Mapocho**, que estamos desarrollando para el curso de Desarrollo de Aplicaciones Web II de Cibertec.

Este módulo está pensado para llevar el control de los movimientos de los balones de gas, como las entradas, salidas y ventas. La idea es poder registrar lo que ocurre con el inventario y tener un mejor seguimiento de las cantidades que se manejan.

## Tecnologías utilizadas

Para este microservicio estamos trabajando con:

- Java 25
- Spring Boot 4.1.1
- Spring Data JPA
- Spring Security
- Spring Cloud Config
- Netflix Eureka Client
- OpenFeign
- Resilience4j
- RabbitMQ
- JWT
- H2 y PostgreSQL
- Maven
- Docker

## ¿Qué hace este microservicio?

Este servicio se encarga de manejar los movimientos que se realizan dentro del inventario de Gas Mapocho.

Entre las funciones que se buscan cubrir están:

- Registrar las entradas de balones al almacén.
- Registrar las salidas de balones.
- Llevar un registro de los movimientos relacionados con las ventas.
- Consultar el historial de movimientos.
- Mantener un seguimiento de los cambios en el inventario.
- Comunicarse con otros microservicios cuando sea necesario.

Por ejemplo, cuando ingresan nuevos balones al almacén, se puede registrar ese movimiento como una entrada. De la misma manera, cuando se retiran o venden balones, se registra la salida correspondiente.

Esto nos permite tener un historial de lo que sucede con el inventario.

## ¿Cómo funciona?

El proyecto utiliza Spring Boot para manejar las peticiones y Spring Data JPA para trabajar con la información almacenada en la base de datos.

También cuenta con varias dependencias para comunicarse con los otros módulos del sistema.

- **OpenFeign:** permite realizar peticiones HTTP a otros microservicios.
- **RabbitMQ:** se utiliza para trabajar con mensajes entre servicios.
- **Eureka Client:** ayuda a localizar los microservicios registrados.
- **Config Client:** permite obtener las configuraciones desde el Config Server.
- **Resilience4j:** ayuda a manejar posibles fallos cuando se realizan llamadas entre servicios.

Además, se incluyen Spring Security y JWT para trabajar con la autenticación y seguridad de las peticiones.

## Requisitos

Para ejecutar el proyecto necesitamos:

- JDK 25
- Git
- Maven o el Maven Wrapper incluido
- Docker (opcional)

También debemos tener configurados los servicios externos que se utilicen, como Eureka, Config Server, RabbitMQ o la base de datos.

## Instalación

Primero clonamos el repositorio:

```bash
git clone https://github.com/Sophia-1827/DAW-II_Grupo09_gasmapocho_ms-movimientos.git
```

Entramos a la carpeta del proyecto:

```bash
cd DAW-II_Grupo09_gasmapocho_ms-movimientos
```

Luego compilamos:

```bash
.\mvnw.cmd clean install
```

Finalmente, iniciamos el microservicio:

```bash
.\mvnw.cmd spring-boot:run
```

Estos comandos son para Windows. En Linux o macOS podemos utilizar `./mvnw`.

## Base de datos

El proyecto tiene dependencias para trabajar con H2 y PostgreSQL.

Durante el desarrollo podemos utilizar H2 para hacer pruebas sin necesidad de configurar una base de datos externa.

Por otro lado, PostgreSQL permite guardar la información de manera persistente.

La base de datos que se utilice dependerá de las propiedades configuradas para el entorno.

## Comunicación con otros microservicios

Una de las partes importantes de este módulo es que necesita trabajar con la información de otros servicios.

Por ejemplo, el microservicio de balones administra el catálogo y los productos, mientras que el de movimientos se encarga de registrar lo que ocurre cuando esos productos ingresan o salen del inventario.

Para la comunicación entre servicios contamos con OpenFeign y RabbitMQ.

También se incluye Resilience4j para manejar los casos en los que otro servicio no se encuentre disponible o falle durante una petición.

## Ejecución con Docker

El proyecto incluye un Dockerfile para poder ejecutarlo dentro de un contenedor.

Primero construimos la imagen:

```bash
docker build -t gasmapocho-ms-movimientos .
```

Después ejecutamos el contenedor:

```bash
docker run -p 8083:8083 gasmapocho-ms-movimientos
```

El Dockerfile expone el puerto **8083**, por lo que debemos asegurarnos de que coincida con el puerto configurado en la aplicación.

Si utilizamos RabbitMQ, Eureka o una base de datos externa, también tendremos que configurar sus conexiones dentro del entorno de Docker.

## Relación con Gas Mapocho

Este microservicio forma parte del sistema general de Gas Mapocho y se complementa con los demás módulos.

Principalmente se relaciona con:

- **Microservicio de Balones:** maneja la información de los productos del catálogo.
- **Microservicio de Usuarios:** administra los usuarios y sus permisos.
- **API Gateway:** permite acceder a los servicios desde un punto de entrada.
- **Eureka Server:** facilita el descubrimiento de los microservicios.
- **Config Server:** centraliza las configuraciones del proyecto.

Al mantener los movimientos de inventario en un microservicio separado, podemos trabajar en esta parte del sistema sin mezclarla directamente con la gestión de usuarios o el catálogo de balones.

## Pruebas

El proyecto cuenta con dependencias para realizar pruebas con Spring Boot y Spring Security.

Podemos ejecutarlas con:

```bash
.\mvnw.cmd test
```

Esto permite revisar las pruebas implementadas y comprobar los cambios antes de integrarlos con el resto del sistema.

## Curso

**Desarrollo de Aplicaciones Web II**

**Instituto:** Cibertec

**Grupo:** 09

**Proyecto:** Gas Mapocho
