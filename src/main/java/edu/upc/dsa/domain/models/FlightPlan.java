package edu.upc.dsa.domain.models;

import java.time.LocalDateTime;
import java.util.List;

public class FlightPlan {
    private String id;
    private String droneId;
    private String pilotId;
    private LocalDateTime startDate;
    private double durationHours;
    private List<Position> positions;

    public FlightPlan(String id, String droneId, String pilotId, LocalDateTime startDate,
                      double durationHours, List<Position> positions)
    {
        this.id = id;
        this.droneId = droneId;
        this.pilotId = pilotId;
        this.startDate = startDate;
        this.durationHours = durationHours;
        this.positions = positions;
    }

    public String getId()
    {
        return id;
    }

    public String getDroneId()
    {
        return droneId;
    }

    public String getPilotId()
    {
        return pilotId;
    }

    public LocalDateTime getStartDate()
    {
        return startDate;
    }

    public double getDurationHours()
    {
        return durationHours;
    }

    public List<Position> getPositions()
    {
        return positions;
    }

    public LocalDateTime getEndDate()
    {
        return startDate.plusSeconds(Math.round(durationHours * 3600));
    }

    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd)
    {
        return startDate.isBefore(otherEnd) && otherStart.isBefore(getEndDate());
    }

    @Override
    public String toString()
    {
        return "FlightPlan [id=" + id + ", droneId=" + droneId + ", pilotId=" + pilotId
                + ", startDate=" + startDate + ", durationHours=" + durationHours
                + ", positions=" + positions + "]";
    }
}
