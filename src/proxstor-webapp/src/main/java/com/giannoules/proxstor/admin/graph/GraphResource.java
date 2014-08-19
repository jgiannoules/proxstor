/*
 * Copyright (c) 2014, jim
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.giannoules.proxstor.admin.graph;

import com.giannoules.proxstor.ProxStorGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author jim
 * 
 * administer the graph instance held in ProxStorGraph
 * 
 *  - create (@POST)
 *  - retrieve status (@GET)
 *  - shutdown (@DELETE)
 * 
 */
@Path("/admin/graph")
public class GraphResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getGraph() {
        if (ProxStorGraph.instance.isRunning()) {
            return ProxStorGraph.instance.toString();
        }
        return "Graph instance not running!";
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteGraph() {
        if (ProxStorGraph.instance.isRunning()) {
            String msg = ProxStorGraph.instance.toString();
            ProxStorGraph.instance.shutdown();
            return msg + " shutdown.";
        }
        return "Graph instance not running!";
    }

    /*
     * using x-www-form-urlencoded for simplicity
     * 
     * converts MultiValueMap form params into a Map<String, String>
     * and uses ProxStorGraph enum's .createGraph(Map) method
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postGraph(MultivaluedMap<String, String> formParams) {
        Map<String, String> conf = new HashMap<>();
        Iterator<String> it = formParams.keySet().iterator();
        while (it.hasNext()) {
            String theKey = it.next();
            conf.put(theKey, formParams.getFirst(theKey));
        }
        ProxStorGraph.instance.createGraph(conf);
        return ProxStorGraph.instance.toString();
    }

    /*
     * this is a more ideal implementation using arbitrarily large Maps
     * @TODO get this working !?
     */
     @POST
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.APPLICATION_XML)
     public MyHashMapObject<String, String> postGraph(
             MyHashMapObject<String, String> anotherMap) {        
        return anotherMap;
     }
  
}
