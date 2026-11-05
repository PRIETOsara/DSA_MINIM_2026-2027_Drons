package edu.upc.dsa.domain.models;

public class Pilot {
    private String id;
    private String name;
    private String surname;
    private double flightHours;

    public Pilot(String id, String name, String surname)
    {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.flightHours = 0;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getSurname()
    {
        return surname;
    }

    public double getFlightHours()
    {
        return flightHours;
    }

    public void addFlightHours(double hours)
    {
        this.flightHours += hours;
    }

    @Override
    public String toString()
    {
        return "Pilot [id=" + id + ", name=" + name + ", surname=" + surname
                + ", flightHours=" + flightHours + "]";
    }
}
