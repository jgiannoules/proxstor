package com.giannoules.proxstor.device;

import com.giannoules.proxstor.ProxStorGraph;
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

    public Device getDevice(String userId, String devId) {
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        if (v != null) {
            return vertexToDevice(v);
        }
        return null;
    }

    public Collection<Device> getAllDevices(String userId) {
        List<Device> users = new ArrayList<>();
        for (Vertex v : ProxStorGraph.instance.getGraph().getVertices("_type", "device")) {
            users.add(vertexToDevice(v));
        }
        return Collections.EMPTY_SET;
    }

    public Device addDevice(Device d) {
        if (d.getDevId() == null) {
            Device newDev = new Device(d.getDescription());
            Vertex v = ProxStorGraph.instance.newVertex();
            v.setProperty("description", newDev.getDescription());
            v.setProperty("_type", "device");
            ProxStorGraph.instance.commit();
            newDev.setDevId(v.getId().toString());
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
