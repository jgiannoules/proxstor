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

/**
 *
 * administer the graph instance held in ProxStorGraph
 * 
 *  - create (@POST)
 *  - retrieve status (@GET)
 *  - shutdown (@DELETE)
 * 
 */
public class GraphResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getGraph() {
        return ProxStorGraph.instance.toString();   
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteGraph() {        
        try {
            String msg = ProxStorGraph.instance.toString();
            ProxStorGraph.instance.shutdown();
            return msg + " shutdown.";
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(GraphResource.class.getName()).log(Level.SEVERE, null, ex);
            return "Graph instance not running!";
        }        
        
    }

    /*
     * Note: using x-www-form-urlencoded for simplicity
     * 
     * converts MultiValueMap form params into a Map<String, String>
     * and uses ProxStorGraph enum's .createGraph(Map) method
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postGraph(MultivaluedMap<String, String> formParams) {
        try {
            Map<String, String> conf = new HashMap<>();
            Iterator<String> it = formParams.keySet().iterator();
            while (it.hasNext()) {
                String theKey = it.next();
                conf.put(theKey, formParams.getFirst(theKey));
            }
            ProxStorGraph.instance.start(conf);
            return ProxStorGraph.instance.toString();
        } catch (ProxStorGraphDatabaseAlreadyRunning ex) {
            Logger.getLogger(GraphResource.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }
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
