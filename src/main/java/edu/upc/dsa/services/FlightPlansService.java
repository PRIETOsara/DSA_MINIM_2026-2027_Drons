package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.repository.DroneManager;
import edu.upc.dsa.domain.repository.DroneManagerImpl;
import edu.upc.dsa.domain.exceptions.DroneNotFoundException;
import edu.upc.dsa.domain.exceptions.DroneNotOperativeException;
import edu.upc.dsa.domain.exceptions.FlightPlanConflictException;
import edu.upc.dsa.domain.exceptions.PilotNotFoundException;
import edu.upc.dsa.domain.transfer.TOMapper;
import edu.upc.dsa.domain.transfer.FlightPlanTO;
import edu.upc.dsa.domain.transfer.RequestFlightPlanTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Api(value = "/flightplans", description = "Endpoint to Flight Plans Service")
@Path("/flightplans")
public class FlightPlansService {

    private final DroneManager dm;

    public FlightPlansService() {
        this.dm = DroneManagerImpl.getInstance();
        ServiceSupport.loadDemoDataIfEmpty(this.dm);
    }

    @POST
    @ApiOperation(value = "Create a flight plan",
            notes = "startDate format: yyyy-MM-dd'T'HH:mm. The estimated hours are added to the "
                    + "flight hours of the drone and of the pilot.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Flight plan created", response = FlightPlanTO.class),
            @ApiResponse(code = 400, message = "Invalid request (missing fields, bad date, "
                    + "duration <= 0, no positions)"),
            @ApiResponse(code = 404, message = "Drone or pilot not found"),
            @ApiResponse(code = 409, message = "Drone or pilot busy in that interval, "
                    + "or drone in maintenance")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFlightPlan(RequestFlightPlanTO request) {
        if (request == null || request.getStartDate() == null || request.getPositions() == null) {
            return ServiceSupport.error(400, "droneId, pilotId, startDate, durationHours and "
                    + "positions are mandatory");
        }
        try {
            LocalDateTime start = TOMapper.parseDate(request.getStartDate());
            FlightPlan plan = this.dm.createFlightPlan(request.getDroneId(), request.getPilotId(),
                    start, request.getDurationHours(), TOMapper.toPositions(request.getPositions()));
            return Response.status(201).entity(TOMapper.toTO(plan)).build();
        } catch (DateTimeParseException e) {
            return ServiceSupport.error(400, "startDate must have the format "
                    + TOMapper.DATE_PATTERN);
        } catch (IllegalArgumentException e) {
            return ServiceSupport.error(400, e.getMessage());
        } catch (DroneNotFoundException | PilotNotFoundException e) {
            return ServiceSupport.error(404, e.getMessage());
        } catch (DroneNotOperativeException | FlightPlanConflictException e) {
            return ServiceSupport.error(409, e.getMessage());
        }
    }
}
