package edu.upc.dsa.domain.transfer;

import java.util.ArrayList;
import java.util.List;

public class RequestFlightPlanTO {
    private String droneId;
    private String pilotId;
    private String startDate;
    private double durationHours;
    private List<PositionTO> positions = new ArrayList<>();

    public RequestFlightPlanTO() {
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public String getPilotId() {
        return pilotId;
    }

    public void setPilotId(String pilotId) {
        this.pilotId = pilotId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(double durationHours) {
        this.durationHours = durationHours;
    }

    public List<PositionTO> getPositions() {
        return positions;
    }

    public void setPositions(List<PositionTO> positions) {
        this.positions = positions;
    }
}
