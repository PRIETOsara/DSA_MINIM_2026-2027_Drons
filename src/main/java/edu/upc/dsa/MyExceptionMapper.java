package edu.upc.dsa;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.utils.Exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = Logger.getLogger(MyExceptionMapper.class);

    @Override
    public Response toResponse(Exception ex) {
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }
        logger.error("Unexpected error", ex);
        return Response.status(500).entity(Exceptions.getStackTraceAsString(ex)).type("text/plain")
                .build();
    }
}
