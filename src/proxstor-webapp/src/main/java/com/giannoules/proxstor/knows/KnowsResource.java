package com.giannoules.proxstor.knows;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.user.User;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class KnowsResource {

    private String userId;
    
    public KnowsResource(String userId) {
        this.userId = userId;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUserKnows() {
        List<User> knows = new ArrayList<>();
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        for (Edge e : v.getEdges(OUT, "knows")) {
            knows.add(UserDao.instance.vertexToUser(e.getVertex(IN)));
        }
        return knows;
    }

    @Path("/{knownUserId: [0-9]*}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public boolean postUserKnows(@PathParam("knownUserId") String knownUserId) {
        Vertex out = ProxStorGraph.instance.getVertex(userId);
        Vertex in = ProxStorGraph.instance.getVertex(knownUserId);
        ProxStorGraph.instance.newEdge(out, in, "knows");
        return true;
    }

    @Path("/{knownUserId: [0-9]*}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public boolean deleteUserKnows(@PathParam("knownUserId") String knownUserId) {
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        for (Edge e : v.getEdges(OUT, "knows")) {
            if (e.getVertex(IN).getId().toString().equals(knownUserId)) {
                e.remove();
                return true;
            }
        }
        return false;
    }
}
