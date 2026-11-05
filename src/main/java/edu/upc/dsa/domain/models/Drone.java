package edu.upc.dsa.domain.models;

public class Drone {
    private String id;
    private String name;
    private String manufacturer;
    private String model;
    private double flightHours;
    private boolean inMaintenance;

    public Drone(String id, String name, String manufacturer, String model)
    {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.model = model;
        this.flightHours = 0;
        this.inMaintenance = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public double getFlightHours() {
        return flightHours;
    }

    public void addFlightHours(double hours) {
        this.flightHours += hours;
    }

    public boolean isInMaintenance() {
        return inMaintenance;
    }

    public void setInMaintenance(boolean inMaintenance) {
        this.inMaintenance = inMaintenance;
    }

    @Override
    public String toString()
    {
        return "Drone [id=" + id + ", name=" + name + ", manufacturer=" + manufacturer
                + ", model=" + model + ", flightHours=" + flightHours
                + ", inMaintenance=" + inMaintenance + "]";
    }
}
