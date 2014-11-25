package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.environmental.EnvironmentalDao;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.user.UserDao;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TestingEnvironmentalsResource {

    /**
     * using reservoir sampling return count environmentals randomly selected
     * from database
     *
     * @param count
     * @return
     */
    @Path("/retrieve/{count}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveEnvironmentals(@PathParam("count") Integer count) {
        List<Environmental> environmentals = getEnvironmentalReservoirSampling(count);
        return Response.ok((Environmental[]) environmentals.toArray(new Environmental[environmentals.size()])).build();
    }

    private List<Environmental> getEnvironmentalReservoirSampling(Integer count) {
        Integer k;
        Integer i;
        int j;
        Random r = new Random();
        k = count;
        List<Environmental> R = new ArrayList<>();
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "environmental");
//        q.limit(1000000);
        Iterator<Vertex> it = q.vertices().iterator();
        i = k;
        while ((i > 0) && (it.hasNext())) {
            R.add(EnvironmentalDao.instance.get(it.next().getId().toString()));
            i--;
        }
        i = k + 1;
        while (it.hasNext()) {
            j = r.nextInt(i);
            if (j < k) {
                R.remove(j);
                R.add(EnvironmentalDao.instance.get(it.next().getId().toString()));
            } else {
                it.next();
            }
            i++;
        }
        return R;
    }
}
