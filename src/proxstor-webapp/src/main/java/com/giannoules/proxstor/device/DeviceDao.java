package com.giannoules.proxstor.device;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.user.User;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 * Data Access Object to database-persistent store of Devices
 *
 * @TODO implement caching
 *
 * Currently uses basic low-level Blueprints API
 *
 */
public enum DeviceDao {

    instance;

    private DeviceDao() {
    }

    public Device vertexToDevice(Vertex v) {
        Device d = new Device();
        d.setDescription((String) v.getProperty("description"));
        Object id = v.getId();
        if (id instanceof Long) {
            d.setDevId(Long.toString((Long) v.getId()));
        } else {
            d.setDevId(v.getId().toString());
        }
        return d;
    }

    public Device getDevice(String devId) {
        Vertex v = ProxStorGraph.instance.getVertex(devId);
        if (v != null) {
            return vertexToDevice(v);
        }
        return null;
    }

    public Collection<Device> getAllUserDevices(String userId) {
        List<Device> devices = new ArrayList<>();
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        for (Edge e : v.getEdges(OUT, "owns")) {
            devices.add(DeviceDao.instance.vertexToDevice(e.getVertex(IN)));
        }
        return devices;
    }

    /*
     * returns *all* Devices in database, independent of the owning User
     *
     * warning: use of this might mean you are violating the contract that
     *          devices exists as a relationship from a single user
     *              User --OWNS--> Device
     */
    public Collection<Device> getAllDevices() {
        List<Device> devices = new ArrayList<>();
        for (Vertex v : ProxStorGraph.instance.getGraph().getVertices("_type", "device")) {
            devices.add(vertexToDevice(v));
        }
        return devices;
    }

    public Device addDevice(User u, Device d) {
        if (d.getDevId() == null) {
            Vertex out = ProxStorGraph.instance.getVertex(u.getUserId());

            Device newDev = new Device(d.getDescription());
            Vertex in = ProxStorGraph.instance.newVertex();
            in.setProperty("description", newDev.getDescription());
            newDev.setDevId(in.getId().toString());
            in.setProperty("_type", "device");

            ProxStorGraph.instance.newEdge(out, in, "owns");

            ProxStorGraph.instance.commit();
            return newDev;
        } else {
            return null;
        }
    }

    public boolean updateDevice(Device d) {
        Vertex v = ProxStorGraph.instance.getVertex(d.getDevId());
        if (v != null) {
            v.setProperty("description", d.getDescription());
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

    public boolean deleteDevice(String userId) {
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        if (v != null) {
            v.remove();
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

    public boolean validDeviceId(String userId, String devId) {
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        return (v != null) && (v.getProperty("_type").equals("device"));
    }
}
