package edu.upc.dsa.domain.transfer;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;
import edu.upc.dsa.domain.models.Position;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class TOMapper {
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private TOMapper() {
    }

    public static LocalDateTime parseDate(String text) {
        return LocalDateTime.parse(text, FORMATTER);
    }

    public static String formatDate(LocalDateTime date) {
        return date.format(FORMATTER);
    }

    public static DroneTO toTO(Drone d) {
        DroneTO dto = new DroneTO();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setManufacturer(d.getManufacturer());
        dto.setModel(d.getModel());
        dto.setFlightHours(d.getFlightHours());
        dto.setInMaintenance(d.isInMaintenance());
        return dto;
    }

    public static PilotTO toTO(Pilot p) {
        PilotTO dto = new PilotTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSurname(p.getSurname());
        dto.setFlightHours(p.getFlightHours());
        return dto;
    }

    public static FlightPlanTO toTO(FlightPlan fp) {
        FlightPlanTO dto = new FlightPlanTO();
        dto.setId(fp.getId());
        dto.setDroneId(fp.getDroneId());
        dto.setPilotId(fp.getPilotId());
        dto.setStartDate(formatDate(fp.getStartDate()));
        dto.setEndDate(formatDate(fp.getEndDate()));
        dto.setDurationHours(fp.getDurationHours());
        List<PositionTO> positions = new ArrayList<>();
        for (Position p : fp.getPositions()) {
            positions.add(new PositionTO(p.getLatitude(), p.getLongitude()));
        }
        dto.setPositions(positions);
        return dto;
    }

    public static List<Position> toPositions(List<PositionTO> dtos) {
        List<Position> positions = new ArrayList<>();
        for (PositionTO p : dtos) {
            positions.add(new Position(p.getLatitude(), p.getLongitude()));
        }
        return positions;
    }
}
