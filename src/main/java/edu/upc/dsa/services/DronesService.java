package edu.upc.dsa.services;

import edu.upc.dsa.domain.models.Drone;
import edu.upc.dsa.domain.repository.DroneManager;
import edu.upc.dsa.domain.repository.DroneManagerImpl;
import edu.upc.dsa.domain.exceptions.DuplicateEntityException;
import edu.upc.dsa.domain.transfer.DroneTO;
import edu.upc.dsa.domain.transfer.TOMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Api(value = "/drones", description = "Endpoint to Drones Service")
@Path("/drones")
public class DronesService {

    private final DroneManager dm;

    public DronesService() {
        this.dm = DroneManagerImpl.getInstance();
        ServiceSupport.loadDemoDataIfEmpty(this.dm);
    }

    @POST
    @ApiOperation(value = "Add a drone", notes = "id and name are mandatory; flight hours start at 0")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Drone created", response = DroneTO.class),
            @ApiResponse(code = 400, message = "Invalid request (id or name missing)"),
            @ApiResponse(code = 409, message = "A drone with this id already exists")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDrone(DroneTO request) {
        if (request == null) {
            return ServiceSupport.error(400, "Request body is mandatory");
        }
        try {
            Drone drone = this.dm.addDrone(request.getId(), request.getName(),
                    request.getManufacturer(), request.getModel());
            return Response.status(201).entity(TOMapper.toTO(drone)).build();
        } catch (IllegalArgumentException e) {
            return ServiceSupport.error(400, e.getMessage());
        } catch (DuplicateEntityException e) {
            return ServiceSupport.error(409, e.getMessage());
        }
    }

    @GET
    @ApiOperation(value = "List the drones sorted by flight hours (descending)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = DroneTO.class,
                    responseContainer = "List")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDrones() {
        List<DroneTO> result = new ArrayList<>();
        for (Drone d : this.dm.getDronesByFlightHours()) {
            result.add(TOMapper.toTO(d));
        }
        GenericEntity<List<DroneTO>> entity = new GenericEntity<List<DroneTO>>(result) {};
        return Response.status(200).entity(entity).build();
    }
}
