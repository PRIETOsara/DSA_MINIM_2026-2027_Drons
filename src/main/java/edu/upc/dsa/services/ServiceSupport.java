package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.Position;
import edu.upc.dsa.domain.repository.DroneManager;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Arrays;

final class ServiceSupport {
    private static final Logger logger = Logger.getLogger(ServiceSupport.class);

    private ServiceSupport() {
    }

    static Response error(int status, String message) {
        return Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build();
    }

    // Demo data so the API can be tried from Swagger
    static synchronized void loadDemoDataIfEmpty(DroneManager dm) {
        if (dm.numDrones() != 0) {
            return;
        }
        logger.info("loadDemoDataIfEmpty(): loading demo data");
        try {
            dm.addDrone("D1", "Halcon", "DJI", "Phantom 4");
            dm.addDrone("D2", "Aguila", "Parrot", "Anafi");
            dm.addDrone("D3", "Gaviota", "Autel", "Evo II");
            dm.addPilot("P1", "Sara", "Prieto");
            dm.addPilot("P2", "Joan", "Garcia");

            Position bcn = new Position(41.3851, 2.1734);
            Position castelldefels = new Position(41.2753, 1.9866);
            dm.createFlightPlan("D1", "P1", LocalDateTime.of(2026, 1, 10, 10, 0), 3,
                    Arrays.asList(bcn, castelldefels));
            dm.createFlightPlan("D2", "P2", LocalDateTime.of(2026, 1, 10, 10, 0), 5,
                    Arrays.asList(castelldefels, bcn));
            dm.createFlightPlan("D2", "P1", LocalDateTime.of(2026, 1, 11, 9, 0), 2,
                    Arrays.asList(bcn));
        } catch (Exception e) {
            logger.fatal("loadDemoDataIfEmpty(): could not load the demo data", e);
            throw new IllegalStateException("Could not load demo data", e);
        }
        logger.info("loadDemoDataIfEmpty(): demo data loaded");
    }
}
