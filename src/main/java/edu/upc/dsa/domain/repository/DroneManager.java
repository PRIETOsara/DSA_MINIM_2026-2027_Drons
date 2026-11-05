package edu.upc.dsa.domain.repository;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;
import edu.upc.dsa.domain.models.Position;
import edu.upc.dsa.domain.exceptions.DroneNotFoundException;
import edu.upc.dsa.domain.exceptions.DroneNotOperativeException;
import edu.upc.dsa.domain.exceptions.DuplicateEntityException;
import edu.upc.dsa.domain.exceptions.FlightPlanConflictException;
import edu.upc.dsa.domain.exceptions.PilotNotFoundException;
import edu.upc.dsa.domain.exceptions.AlmacenVacioException;

import java.time.LocalDateTime;
import java.util.List;

public interface DroneManager {

    Drone addDrone(String id, String name, String manufacturer, String model)
            throws DuplicateEntityException;

    Pilot addPilot(String id, String name, String surname) throws DuplicateEntityException;

    List<Drone> getDronesByFlightHours();

    List<Pilot> getPilotsByFlightHours();

    // Almacen = LIFO last in first otu
    void storeDroneForMaintenance(String droneId)
            throws DroneNotFoundException, DroneNotOperativeException;

    Drone repairDrone() throws AlmacenVacioException;

    // Adds the duration to the flight hours of the drone and of the pilot
    FlightPlan createFlightPlan(String droneId, String pilotId, LocalDateTime startDate,
                                double durationHours, List<Position> positions)
            throws DroneNotFoundException, PilotNotFoundException, DroneNotOperativeException,
            FlightPlanConflictException;

    List<FlightPlan> getFlightPlansByPilot(String pilotId) throws PilotNotFoundException;

    List<FlightPlan> getFlightPlansByDrone(String droneId) throws DroneNotFoundException;

    Drone getDrone(String id);

    Pilot getPilot(String id);

    int numDrones();

    void clear();
}
