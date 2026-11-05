# DSA - Mínimo 1. 2026-2027


## Cosas hechas

- **Componente (Parte II):** interfaz DroneManager y fachada DroneManagerImpl (Singleton). Con las operaciones del enunciado: añadir drones y pilotos, listados por horas de vuelo, guardar un dron en el almacén y repararlo, crear planes de vuelo y listar los planes de un piloto o de un dron. Logs con log4j en cada método: parámetros al inicio y resultado al final (INFO), errores con ERROR / FATAL.

- **Tests (Parte II):** 19 tests de DroneManagerTest.

- **Servicio REST (Parte III):** /drones, /pilots y /flightplans, con objetos de transferència (DroneTO, PilotTO, FlightPlanTO...). Las pruebas en el Swagger en http://localhost:8080/swagger/.

- **Página web:** http://localhost:8080/index.html, con los drones ordenados por horas de vuelo.
