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
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DroneManagerImpl implements DroneManager {
    private static final Logger logger = Logger.getLogger(DroneManagerImpl.class);

    private static DroneManagerImpl instance;

    private final Map<String, Drone> drones;
    private final Map<String, Pilot> pilots;
    private final Deque<Drone> almacen;
    private final Map<String, List<FlightPlan>> plansByDrone;
    private final Map<String, List<FlightPlan>> plansByPilot;

    private DroneManagerImpl() {
        this.drones = new HashMap<>();
        this.pilots = new HashMap<>();
        this.almacen = new ArrayDeque<>();
        this.plansByDrone = new HashMap<>();
        this.plansByPilot = new HashMap<>();
    }

    public static synchronized DroneManager getInstance() {
        logger.info("getInstance()");
        if (instance == null) {
            instance = new DroneManagerImpl();
            logger.info("getInstance(): new DroneManagerImpl created");
        }
        logger.info("getInstance(): returning singleton instance");
        return instance;
    }

    @Override
    public synchronized Drone addDrone(String id, String name, String manufacturer, String model)
            throws DuplicateEntityException {
        logger.info("addDrone(id=" + id + ", name=" + name + ", manufacturer=" + manufacturer
                + ", model=" + model + ")");

        if (isBlank(id) || isBlank(name)) {
            logger.warn("addDrone: id and name are mandatory");
            throw new IllegalArgumentException("Drone id and name are mandatory");
        }
        if (drones.containsKey(id)) {
            logger.error("addDrone: drone already exists, id=" + id);
            throw new DuplicateEntityException("Drone already exists: " + id);
        }

        Drone drone = new Drone(id, name, manufacturer, model);
        drones.put(id, drone);

        logger.info("addDrone: added " + drone + ", total drones=" + drones.size());
        return drone;
    }

    @Override
    public synchronized Pilot addPilot(String id, String name, String surname)
            throws DuplicateEntityException {
        logger.info("addPilot(id=" + id + ", name=" + name + ", surname=" + surname + ")");

        if (isBlank(id) || isBlank(name)) {
            logger.warn("addPilot: id and name are mandatory");
            throw new IllegalArgumentException("Pilot id and name are mandatory");
        }
        if (pilots.containsKey(id)) {
            logger.error("addPilot: pilot already exists, id=" + id);
            throw new DuplicateEntityException("Pilot already exists: " + id);
        }

        Pilot pilot = new Pilot(id, name, surname);
        pilots.put(id, pilot);

        logger.info("addPilot: added " + pilot + ", total pilots=" + pilots.size());
        return pilot;
    }

    @Override
    public synchronized List<Drone> getDronesByFlightHours() {
        logger.info("getDronesByFlightHours()");

        List<Drone> result = new ArrayList<>(drones.values());
        Collections.sort(result, Comparator.comparingDouble(Drone::getFlightHours).reversed()
                .thenComparing(Drone::getId));

        logger.info("getDronesByFlightHours: " + result.size() + " drones " + result);
        return result;
    }

    @Override
    public synchronized List<Pilot> getPilotsByFlightHours() {
        logger.info("getPilotsByFlightHours()");

        List<Pilot> result = new ArrayList<>(pilots.values());
        Collections.sort(result, Comparator.comparingDouble(Pilot::getFlightHours).reversed()
                .thenComparing(Pilot::getId));

        logger.info("getPilotsByFlightHours: " + result.size() + " pilots " + result);
        return result;
    }

    @Override
    public synchronized void storeDroneForMaintenance(String droneId)
            throws DroneNotFoundException, DroneNotOperativeException {
        logger.info("storeDroneForMaintenance(droneId=" + droneId + ")");

        Drone drone = drones.get(droneId);
        if (drone == null) {
            logger.error("storeDroneForMaintenance: drone not found, id=" + droneId);
            throw new DroneNotFoundException("Drone not found: " + droneId);
        }
        if (drone.isInMaintenance()) {
            logger.error("storeDroneForMaintenance: drone already in the almacen, id=" + droneId);
            throw new DroneNotOperativeException("Drone already in the almacen: " + droneId);
        }

        drone.setInMaintenance(true);
        almacen.push(drone);

        logger.info("storeDroneForMaintenance: " + droneId + " stored, drones in almacen="
                + almacen.size() + ", closest to the door=" + almacen.peek().getId());
    }

    @Override
    public synchronized Drone repairDrone() throws AlmacenVacioException {
        logger.info("repairDrone()");

        if (almacen.isEmpty()) {
            logger.error("repairDrone: the almacen is empty");
            throw new AlmacenVacioException("There are no drones in the almacen");
        }

        Drone drone = almacen.pop();
        if (drones.get(drone.getId()) != drone || !drone.isInMaintenance()) {
            // Should never happen
            logger.fatal("repairDrone: inconsistent state, drone " + drone
                    + " is in the almacen but is not registered / not flagged in maintenance");
            throw new IllegalStateException("Inconsistent almacen state for drone " + drone.getId());
        }
        drone.setInMaintenance(false);

        logger.info("repairDrone: repaired " + drone + ", drones left in almacen="
                + almacen.size());
        return drone;
    }

    @Override
    public synchronized FlightPlan createFlightPlan(String droneId, String pilotId,
                                                    LocalDateTime startDate, double durationHours,
                                                    List<Position> positions)
            throws DroneNotFoundException, PilotNotFoundException, DroneNotOperativeException,
            FlightPlanConflictException {
        logger.info("createFlightPlan(droneId=" + droneId + ", pilotId=" + pilotId
                + ", startDate=" + startDate + ", durationHours=" + durationHours
                + ", positions=" + positions + ")");

        if (isBlank(droneId) || isBlank(pilotId) || startDate == null
                || Double.isNaN(durationHours) || durationHours <= 0
                || positions == null || positions.isEmpty()) {
            logger.warn("createFlightPlan: invalid arguments");
            throw new IllegalArgumentException("droneId, pilotId, startDate, a duration > 0 "
                    + "and at least one position are mandatory");
        }

        Drone drone = drones.get(droneId);
        if (drone == null) {
            logger.error("createFlightPlan: drone not found, id=" + droneId);
            throw new DroneNotFoundException("Drone not found: " + droneId);
        }
        Pilot pilot = pilots.get(pilotId);
        if (pilot == null) {
            logger.error("createFlightPlan: pilot not found, id=" + pilotId);
            throw new PilotNotFoundException("Pilot not found: " + pilotId);
        }
        if (drone.isInMaintenance()) {
            logger.error("createFlightPlan: drone " + droneId + " is in the almacen (maintenance)");
            throw new DroneNotOperativeException("Drone is in maintenance: " + droneId);
        }

        FlightPlan plan = new FlightPlan(UUID.randomUUID().toString(), droneId, pilotId, startDate,
                durationHours, new ArrayList<>(positions));
        LocalDateTime endDate = plan.getEndDate();

        if (hasOverlap(plansByDrone.get(droneId), startDate, endDate)) {
            logger.error("createFlightPlan: drone " + droneId + " already has a plan overlapping ["
                    + startDate + ", " + endDate + ")");
            throw new FlightPlanConflictException("Drone " + droneId
                    + " already has a flight plan overlapping [" + startDate + ", " + endDate + ")");
        }
        if (hasOverlap(plansByPilot.get(pilotId), startDate, endDate)) {
            logger.error("createFlightPlan: pilot " + pilotId + " already has a plan overlapping ["
                    + startDate + ", " + endDate + ")");
            throw new FlightPlanConflictException("Pilot " + pilotId
                    + " already has a flight plan overlapping [" + startDate + ", " + endDate + ")");
        }

        addToIndex(plansByDrone, droneId, plan);
        addToIndex(plansByPilot, pilotId, plan);
        drone.addFlightHours(durationHours);
        pilot.addFlightHours(durationHours);

        logger.info("createFlightPlan: created " + plan + ", drone hours=" + drone.getFlightHours()
                + ", pilot hours=" + pilot.getFlightHours());
        return plan;
    }

    @Override
    public synchronized List<FlightPlan> getFlightPlansByPilot(String pilotId)
            throws PilotNotFoundException {
        logger.info("getFlightPlansByPilot(pilotId=" + pilotId + ")");

        if (!pilots.containsKey(pilotId)) {
            logger.error("getFlightPlansByPilot: pilot not found, id=" + pilotId);
            throw new PilotNotFoundException("Pilot not found: " + pilotId);
        }

        List<FlightPlan> result = sortedCopy(plansByPilot.get(pilotId));

        logger.info("getFlightPlansByPilot: pilot " + pilotId + " has " + result.size() + " plans");
        return result;
    }

    @Override
    public synchronized List<FlightPlan> getFlightPlansByDrone(String droneId)
            throws DroneNotFoundException {
        logger.info("getFlightPlansByDrone(droneId=" + droneId + ")");

        if (!drones.containsKey(droneId)) {
            logger.error("getFlightPlansByDrone: drone not found, id=" + droneId);
            throw new DroneNotFoundException("Drone not found: " + droneId);
        }

        List<FlightPlan> result = sortedCopy(plansByDrone.get(droneId));

        logger.info("getFlightPlansByDrone: drone " + droneId + " has " + result.size() + " plans");
        return result;
    }

    @Override
    public synchronized Drone getDrone(String id) {
        logger.info("getDrone(id=" + id + ")");
        Drone drone = drones.get(id);
        logger.info("getDrone: " + drone);
        return drone;
    }

    @Override
    public synchronized Pilot getPilot(String id) {
        logger.info("getPilot(id=" + id + ")");
        Pilot pilot = pilots.get(id);
        logger.info("getPilot: " + pilot);
        return pilot;
    }

    @Override
    public synchronized int numDrones() {
        logger.info("numDrones()");
        int ret = drones.size();
        logger.info("numDrones: " + ret);
        return ret;
    }

    @Override
    public synchronized void clear() {
        logger.info("clear()");
        drones.clear();
        pilots.clear();
        almacen.clear();
        plansByDrone.clear();
        plansByPilot.clear();
        logger.info("clear: all data removed");
    }

    private boolean hasOverlap(List<FlightPlan> plans, LocalDateTime start, LocalDateTime end) {
        logger.debug("hasOverlap(start=" + start + ", end=" + end + ")");
        if (plans == null) {
            return false;
        }
        for (FlightPlan p : plans) {
            if (p.overlaps(start, end)) {
                logger.debug("hasOverlap: overlaps with " + p);
                return true;
            }
        }
        logger.debug("hasOverlap: no overlap");
        return false;
    }

    private void addToIndex(Map<String, List<FlightPlan>> index, String key, FlightPlan plan) {
        logger.debug("addToIndex(key=" + key + ", plan=" + plan.getId() + ")");
        List<FlightPlan> list = index.get(key);
        if (list == null) {
            list = new ArrayList<>();
            index.put(key, list);
        }
        list.add(plan);
    }

    private List<FlightPlan> sortedCopy(List<FlightPlan> plans) {
        logger.debug("sortedCopy()");
        List<FlightPlan> result = new ArrayList<>();
        if (plans != null) {
            result.addAll(plans);
        }
        Collections.sort(result, Comparator.comparing(FlightPlan::getStartDate));
        return result;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
