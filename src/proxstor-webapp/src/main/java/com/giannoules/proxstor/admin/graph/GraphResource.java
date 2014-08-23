package com.giannoules.proxstor.admin.graph;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.ProxStorGraphDatabaseAlreadyRunning;
import com.giannoules.proxstor.ProxStorGraphDatabaseNotRunningException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/*
 *
 * administer the graph instance held in ProxStorGraph
 * 
 *  - create (@POST)
 *  - retrieve status (@GET)
 *  - shutdown (@DELETE) 
 */
public class GraphResource {

    /*
     * return basic status & configuration information on running graph
     *
     * success - returns 200 (OK)
     * failure - returns 503 (Service Unavailable)
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getGraph() {
        if (!ProxStorGraph.instance.isRunning()) {
            return Response.status(503).entity("no running graph instance").build();
        }
        return Response.ok().entity(ProxStorGraph.instance.toString()).build();
    }

    /*
     * shutdown running Graph instance
     * DELETE HttpMethod is the closest match to the concept of "stopping"
     *
     * success - returns 200 (OK)
     * failure - returns 404 (Not Found)
     */
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteGraph() {        
        try {
            ProxStorGraph.instance.shutdown();
            return Response.ok().entity("graph instance shutdown complete").build();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(GraphResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).entity("no running graph instance").build();
        }        
    }

    /*
     * create a new Graph instance using the form parameters as entries into
     * a Map to be used as configuration
     *  
     * Note: using x-www-form-urlencoded for simplicity 
     * 
     * converts MultiValueMap form params into a Map<String, String>
     * and uses ProxStorGraph enum's .createGraph(Map) method
     *
     * success - returns 200 (Ok)
     * failure - returns 500 (Internal Server Error) if unable to start graph instance
     * failure - returns 403 (Forbidden) if graph instance already running
     */
    @POST    
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postGraph(MultivaluedMap<String, String> formParams) {
        try {
            Map<String, String> conf = new HashMap<>();
            Iterator<String> it = formParams.keySet().iterator();
            while (it.hasNext()) {
                String theKey = it.next();
                conf.put(theKey, formParams.getFirst(theKey));
            }
            ProxStorGraph.instance.start(conf);
            if (ProxStorGraph.instance.isRunning()) {
                return Response.ok().entity("graph instance now running").build();
            } else {
                return Response.serverError().entity("unable to create graph instance").build();
            }            
        } catch (ProxStorGraphDatabaseAlreadyRunning ex) {
            Logger.getLogger(GraphResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(403).entity(ex.getMessage()).build();
        }
    }
    
}
