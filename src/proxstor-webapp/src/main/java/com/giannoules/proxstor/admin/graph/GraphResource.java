package com.giannoules.proxstor.admin.graph;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.ProxStorGraphDatabaseAlreadyRunning;
import com.giannoules.proxstor.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.Result;
import com.giannoules.proxstor.Status;
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

/*
 *
 * administer the graph instance held in ProxStorGraph
 * 
 *  - create (@POST)
 *  - retrieve status (@GET)
 *  - shutdown (@DELETE)
 * 
 * @TODO return appropriate httpstatus
 * 
 */
public class GraphResource {

    
    /*
     * return basic status & configuration information on running graph
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result getGraph() {
        Result res = new Result();
        res.setStatus(Status.NO_STATUS);
        res.setMessage(ProxStorGraph.instance.toString());
        res.setData(ProxStorGraph.instance.getFeatures());
        return res;
    }

    /*
     * shutdown running Graph instance
     * DELETE HttpMethod is the closest match to the concept of "stopping"
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)    
    public Result deleteGraph() {        
        Result stat = new Result();
        try {
            ProxStorGraph.instance.shutdown();
            stat.setStatus(Status.SUCCESS);
            stat.setMessage("shutdown complete");
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(GraphResource.class.getName()).log(Level.SEVERE, null, ex);
            stat.setStatus(Status.FAILURE);
            stat.setMessage("Graph instance not running!");
        }
        return stat;
    }

    /*
     * create a new Graph instance using the form parameters as entries into
     * a Map to be used as configuration
     *  
     * Note: using x-www-form-urlencoded for simplicity
     * 
     * converts MultiValueMap form params into a Map<String, String>
     * and uses ProxStorGraph enum's .createGraph(Map) method
     */
    @POST    
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Result postGraph(MultivaluedMap<String, String> formParams) {
        Result stat;
        try {
            Map<String, String> conf = new HashMap<>();
            Iterator<String> it = formParams.keySet().iterator();
            while (it.hasNext()) {
                String theKey = it.next();
                conf.put(theKey, formParams.getFirst(theKey));
            }
            ProxStorGraph.instance.start(conf);
            stat = new Result(Status.SUCCESS, "graph has been started", "");
        } catch (ProxStorGraphDatabaseAlreadyRunning ex) {
            Logger.getLogger(GraphResource.class.getName()).log(Level.SEVERE, null, ex);
            stat = new Result(Status.FAILURE, ex.getMessage(),"");
        }
        return stat;
    }

    /*
     * this is a more ideal implementation using arbitrarily large Maps
     *
     * @TODO (low priority) consume XML map
     */
     /*
     @POST
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.APPLICATION_XML)
     public MyHashMapObject<String, String> postGraph(
             MyHashMapObject<String, String> anotherMap) {        
        return anotherMap;
     }
     */
    
}
