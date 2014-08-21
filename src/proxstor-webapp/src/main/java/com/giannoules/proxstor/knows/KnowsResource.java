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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("knows")
public class KnowsResource {
    
    @Path("{userid: [0-9]*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getKnowsUsers(@PathParam("userid") String userId) {
        List<User> knows = new ArrayList<>();
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        for (Edge e : v.getEdges(IN, "knows")) {
            knows.add(UserDao.instance.getUser(e.getVertex(OUT)));
        }
        return knows;
    }
}
