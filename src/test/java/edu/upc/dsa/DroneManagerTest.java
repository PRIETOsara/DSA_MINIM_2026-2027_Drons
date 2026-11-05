package edu.upc.dsa;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;
import edu.upc.dsa.domain.models.Position;
import edu.upc.dsa.domain.repository.DroneManager;
import edu.upc.dsa.domain.repository.DroneManagerImpl;
import edu.upc.dsa.domain.exceptions.DroneNotFoundException;
import edu.upc.dsa.domain.exceptions.DroneNotOperativeException;
import edu.upc.dsa.domain.exceptions.DuplicateEntityException;
import edu.upc.dsa.domain.exceptions.FlightPlanConflictException;
import edu.upc.dsa.domain.exceptions.PilotNotFoundException;
import edu.upc.dsa.domain.exceptions.AlmacenVacioException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class DroneManagerTest {
    private static final double DELTA = 0.0001;

    private DroneManager dm;
    private List<Position> route;

    // Drones: D1 3h, D2 7h, D3 0h. Pilots: P1 5h, P2 5h, P3 0h
    @Before
    public void setUp() throws Exception {
        dm = DroneManagerImpl.getInstance();
        dm.clear();
        route = Arrays.asList(new Position(41.38, 2.17), new Position(41.27, 1.98));

        dm.addDrone("D1", "Halcon", "DJI", "Phantom 4");
        dm.addDrone("D2", "Aguila", "Parrot", "Anafi");
        dm.addDrone("D3", "Gaviota", "Autel", "Evo II");
        dm.addPilot("P1", "Sara", "Prieto");
        dm.addPilot("P2", "Joan", "Garcia");
        dm.addPilot("P3", "Marta", "Soler");

        dm.createFlightPlan("D1", "P1", at(2026, 1, 10, 10), 3, route);
        dm.createFlightPlan("D2", "P2", at(2026, 1, 10, 10), 5, route);
        dm.createFlightPlan("D2", "P1", at(2026, 1, 11, 9), 2, route);
    }

    @After
    public void tearDown() {
        dm.clear();
    }

    private static LocalDateTime at(int y, int m, int d, int h) {
        return LocalDateTime.of(y, m, d, h, 0);
    }

    @Test
    public void testAddDrone() throws Exception {
        Assert.assertEquals(3, dm.numDrones());

        Drone d = dm.addDrone("D4", "Cuervo", "Yuneec", "Typhoon H");

        Assert.assertEquals(4, dm.numDrones());
        Assert.assertSame(d, dm.getDrone("D4"));
        Assert.assertEquals("Cuervo", d.getName());
        Assert.assertEquals("Yuneec", d.getManufacturer());
        Assert.assertEquals("Typhoon H", d.getModel());
        Assert.assertEquals(0, d.getFlightHours(), DELTA);
        Assert.assertFalse(d.isInMaintenance());
    }

    @Test(expected = DuplicateEntityException.class)
    public void testAddDroneDuplicated() throws Exception {
        dm.addDrone("D1", "Otro", "DJI", "Mini");
    }

    @Test
    public void testAddPilot() throws Exception {
        Pilot p = dm.addPilot("P4", "Lluis", "Vidal");

        Assert.assertEquals(4, dm.getPilotsByFlightHours().size());
        Assert.assertSame(p, dm.getPilot("P4"));
        Assert.assertEquals("Vidal", p.getSurname());
        Assert.assertEquals(0, p.getFlightHours(), DELTA);
    }

    @Test
    public void testDronesByFlightHoursDescending() {
        List<Drone> drones = dm.getDronesByFlightHours();

        Assert.assertEquals(3, drones.size());
        Assert.assertEquals("D2", drones.get(0).getId());
        Assert.assertEquals("D1", drones.get(1).getId());
        Assert.assertEquals("D3", drones.get(2).getId());
        for (int i = 1; i < drones.size(); i++) {
            Assert.assertTrue(drones.get(i - 1).getFlightHours() >= drones.get(i).getFlightHours());
        }
    }

    @Test
    public void testPilotsByFlightHoursDescending() {
        List<Pilot> pilots = dm.getPilotsByFlightHours();

        Assert.assertEquals(3, pilots.size());
        for (int i = 1; i < pilots.size(); i++) {
            Assert.assertTrue(pilots.get(i - 1).getFlightHours() >= pilots.get(i).getFlightHours());
        }
        Assert.assertEquals("P3", pilots.get(2).getId());
        Assert.assertEquals(5, pilots.get(0).getFlightHours(), DELTA);
    }

    @Test
    public void testCreateFlightPlan() throws Exception {
        FlightPlan fp = dm.createFlightPlan("D3", "P3", at(2026, 2, 1, 10), 3, route);

        Assert.assertNotNull(fp.getId());
        Assert.assertEquals("D3", fp.getDroneId());
        Assert.assertEquals("P3", fp.getPilotId());
        Assert.assertEquals(at(2026, 2, 1, 13), fp.getEndDate());
        Assert.assertEquals(2, fp.getPositions().size());
        Assert.assertEquals(3, dm.getDrone("D3").getFlightHours(), DELTA);
        Assert.assertEquals(3, dm.getPilot("P3").getFlightHours(), DELTA);
        Assert.assertEquals(1, dm.getFlightPlansByDrone("D3").size());
    }

    @Test
    public void testCreateFlightPlanDroneOverlap() throws Exception {
        // D1 is busy 10:00-13:00 on 2026-01-10
        try {
            dm.createFlightPlan("D1", "P3", at(2026, 1, 10, 12), 2, route);
            Assert.fail("FlightPlanConflictException expected");
        } catch (FlightPlanConflictException e) {
            // expected
        }
        Assert.assertEquals(3, dm.getDrone("D1").getFlightHours(), DELTA);
        Assert.assertEquals(0, dm.getPilot("P3").getFlightHours(), DELTA);
        Assert.assertEquals(1, dm.getFlightPlansByDrone("D1").size());

        // A plan starting exactly when the previous one ends is allowed
        dm.createFlightPlan("D1", "P3", at(2026, 1, 10, 13), 1, route);
        Assert.assertEquals(2, dm.getFlightPlansByDrone("D1").size());
    }

    @Test(expected = FlightPlanConflictException.class)
    public void testCreateFlightPlanPilotOverlap() throws Exception {
        // P1 is busy 10:00-13:00 on 2026-01-10, D3 is free
        dm.createFlightPlan("D3", "P1", at(2026, 1, 10, 11), 1, route);
    }

    @Test
    public void testCreateFlightPlanDroneInAlmacen() throws Exception {
        dm.storeDroneForMaintenance("D3");

        try {
            dm.createFlightPlan("D3", "P3", at(2026, 2, 1, 10), 1, route);
            Assert.fail("DroneNotOperativeException expected");
        } catch (DroneNotOperativeException e) {
            // expected
        }
        Assert.assertEquals(0, dm.getFlightPlansByDrone("D3").size());

        // Once repaired, the drone is operative again
        dm.repairDrone();
        dm.createFlightPlan("D3", "P3", at(2026, 2, 1, 10), 1, route);
        Assert.assertEquals(1, dm.getFlightPlansByDrone("D3").size());
    }

    @Test(expected = DroneNotFoundException.class)
    public void testCreateFlightPlanDroneNotFound() throws Exception {
        dm.createFlightPlan("DX", "P1", at(2026, 3, 1, 10), 1, route);
    }

    @Test(expected = PilotNotFoundException.class)
    public void testCreateFlightPlanPilotNotFound() throws Exception {
        dm.createFlightPlan("D1", "PX", at(2026, 3, 1, 10), 1, route);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFlightPlanInvalidDuration() throws Exception {
        dm.createFlightPlan("D3", "P3", at(2026, 3, 1, 10), 0, route);
    }

    @Test
    public void testFlightPlansByPilot() throws Exception {
        List<FlightPlan> plans = dm.getFlightPlansByPilot("P1");

        Assert.assertEquals(2, plans.size());
        Assert.assertEquals("D1", plans.get(0).getDroneId());
        Assert.assertEquals("D2", plans.get(1).getDroneId());
        Assert.assertTrue(plans.get(0).getStartDate().isBefore(plans.get(1).getStartDate()));

        Assert.assertEquals(1, dm.getFlightPlansByPilot("P2").size());
        Assert.assertEquals(0, dm.getFlightPlansByPilot("P3").size());
    }

    @Test(expected = PilotNotFoundException.class)
    public void testFlightPlansByPilotNotFound() throws Exception {
        dm.getFlightPlansByPilot("PX");
    }

    @Test
    public void testFlightPlansByDrone() throws Exception {
        Assert.assertEquals(2, dm.getFlightPlansByDrone("D2").size());
        Assert.assertEquals(1, dm.getFlightPlansByDrone("D1").size());
        Assert.assertEquals(0, dm.getFlightPlansByDrone("D3").size());
    }

    @Test
    public void testAlmacenLifo() throws Exception {
        dm.storeDroneForMaintenance("D1");
        dm.storeDroneForMaintenance("D3");
        dm.storeDroneForMaintenance("D2");
        Assert.assertTrue(dm.getDrone("D1").isInMaintenance());
        Assert.assertTrue(dm.getDrone("D2").isInMaintenance());
        Assert.assertTrue(dm.getDrone("D3").isInMaintenance());

        // The last one stored is the closest to the door
        Assert.assertEquals("D2", dm.repairDrone().getId());
        Assert.assertFalse(dm.getDrone("D2").isInMaintenance());
        Assert.assertTrue(dm.getDrone("D3").isInMaintenance());
        Assert.assertEquals("D3", dm.repairDrone().getId());
        Assert.assertEquals("D1", dm.repairDrone().getId());
        Assert.assertFalse(dm.getDrone("D1").isInMaintenance());
        Assert.assertFalse(dm.getDrone("D3").isInMaintenance());
    }

    @Test(expected = AlmacenVacioException.class)
    public void testRepairEmptyAlmacen() throws Exception {
        dm.repairDrone();
    }

    @Test(expected = DroneNotFoundException.class)
    public void testStoreDroneNotFound() throws Exception {
        dm.storeDroneForMaintenance("DX");
    }

    @Test(expected = DroneNotOperativeException.class)
    public void testStoreDroneTwice() throws Exception {
        dm.storeDroneForMaintenance("D1");
        dm.storeDroneForMaintenance("D1");
    }
}
