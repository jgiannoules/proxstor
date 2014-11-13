package com.giannoules.proxstor.admin.testing;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.User;
import com.giannoules.proxstor.device.DeviceDao;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.user.UserDao;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TestingDevicesResource {

    /**
     * using reservoir sampling return count devices randomly selected from
     * database
     *
     * @param count
     * @return
     */
    @Path("/retrieve/{count}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveDevices(@PathParam("count") Integer count) {
        List<Device> devices = getDevicesReservoirSampling(count);
        return Response.ok((Device[]) devices.toArray(new Device[devices.size()])).build();
    }

    private List<Device> getDevicesReservoirSampling(Integer count) {
        Integer k;
        Integer i;
        int j;
        Random r = new Random();
        k = count;
        List<Device> R = new ArrayList<>();
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(UserDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "device");
        q.limit(1000000);
        Iterator<Vertex> it = q.vertices().iterator();
        i = k;
        while ((i > 0) && (it.hasNext())) {
            R.add(DeviceDao.instance.get(it.next().getId().toString()));
            i--;
        }
        i = k + 1;
        while (it.hasNext()) {
            j = r.nextInt(i);
            if (j < k) {
                R.remove(j);
                R.add(DeviceDao.instance.get(it.next().getId().toString()));
            } else {
                it.next();
            }
            i++;
        }
        return R;
    }
}
