package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.FlightPlan;
import edu.upc.dsa.domain.models.Pilot;
import edu.upc.dsa.domain.repository.DroneManager;
import edu.upc.dsa.domain.repository.DroneManagerImpl;
import edu.upc.dsa.domain.exceptions.DuplicateEntityException;
import edu.upc.dsa.domain.exceptions.PilotNotFoundException;
import edu.upc.dsa.domain.transfer.TOMapper;
import edu.upc.dsa.domain.transfer.FlightPlanTO;
import edu.upc.dsa.domain.transfer.PilotTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Api(value = "/pilots", description = "Endpoint to Pilots Service")
@Path("/pilots")
public class PilotsService {

    private final DroneManager dm;

    public PilotsService() {
        this.dm = DroneManagerImpl.getInstance();
        ServiceSupport.loadDemoDataIfEmpty(this.dm);
    }

    @POST
    @ApiOperation(value = "Add a pilot", notes = "id and name are mandatory; flight hours start at 0")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Pilot created", response = PilotTO.class),
            @ApiResponse(code = 400, message = "Invalid request (id or name missing)"),
            @ApiResponse(code = 409, message = "A pilot with this id already exists")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPilot(PilotTO request) {
        if (request == null) {
            return ServiceSupport.error(400, "Request body is mandatory");
        }
        try {
            Pilot pilot = this.dm.addPilot(request.getId(), request.getName(), request.getSurname());
            return Response.status(201).entity(TOMapper.toTO(pilot)).build();
        } catch (IllegalArgumentException e) {
            return ServiceSupport.error(400, e.getMessage());
        } catch (DuplicateEntityException e) {
            return ServiceSupport.error(409, e.getMessage());
        }
    }

    @GET
    @Path("/{id}/flightplans")
    @ApiOperation(value = "List the flight plans assigned to a pilot",
            notes = "Chronological order")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = FlightPlanTO.class,
                    responseContainer = "List"),
            @ApiResponse(code = 404, message = "Pilot not found")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFlightPlansByPilot(@PathParam("id") String id) {
        try {
            List<FlightPlanTO> result = new ArrayList<>();
            for (FlightPlan fp : this.dm.getFlightPlansByPilot(id)) {
                result.add(TOMapper.toTO(fp));
            }
            GenericEntity<List<FlightPlanTO>> entity =
                    new GenericEntity<List<FlightPlanTO>>(result) {};
            return Response.status(200).entity(entity).build();
        } catch (PilotNotFoundException e) {
            return ServiceSupport.error(404, e.getMessage());
        }
    }
}
