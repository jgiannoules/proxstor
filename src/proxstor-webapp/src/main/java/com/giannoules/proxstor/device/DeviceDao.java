package com.giannoules.proxstor.device;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
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

    /*
     * converts vertex into Device object
     *
     * assumes sanity check already performed on vertex
     */
    private Device vertexToDevice(Vertex v) {
        if (v == null) {
            return null;
        }
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

    /*
     * test Vertex for Device-ness
     */
    private boolean validDeviceVertex(Vertex v) {
        return (v != null) && v.getProperty("_type").equals("device");
    }

    /*
     * test device id for Device-ness
     */
    private boolean validDeviceId(String devId) {
        return (devId != null) && validDeviceVertex(ProxStorGraph.instance.getVertex(devId));
    }

    /*
     * tests for validity of devId ownership by userId
     * returns false if:
     *   1 devId does not map to any graph vertex
     *   2 userId does not map to any graph vertex
     *   3 devId is not vertex of type Device
     *   4 userId is not vertex of type User
     *   5 User is not Owner of Device     
     */
    private boolean validUserDevice(String userId, String devId) {
        if ((userId == null) || (devId == null)) {
            return false;
        }
        Device d = getDeviceById(devId);
        if (d == null) {    // conditions 1 & 3
            return false;
        }
        if (UserDao.instance.getUser(userId) == null) { // conditions 2 & 4
            return false;
        }
        for (Edge e : ProxStorGraph.instance.getVertex(devId).getEdges(IN, "owns")) {
            if (e.getVertex(OUT).getId().equals(userId)) {
                return true;
            }
        }
        return false; // condition 5
    }

    /*
     * abstract away setting of Vertex Device type
     */
    private void setVertexToDeviceType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "device");
        }
    }

    /*
     * returns Device stored under devId
     *
     * returns null if:
     *   - devId does not map to any graph vertex
     *   - vertex is not of type device
     *
     */
    public Device getDeviceById(String devId) {
        if (devId == null) {
            return null;
        }
        Vertex v = ProxStorGraph.instance.getVertex(devId);
        if ((v != null) && validDeviceVertex(v)) {
            return vertexToDevice(v);
        }
        return null;
    }

    
    /*
     * returns all devices in database with description desc
     */
    public List<Device> getDevicesByDescription(String desc) {
        List<Device> devices = new ArrayList<>();
        GraphQuery q = ProxStorGraph.instance.getGraph().query();
        q.has("_type", "device");
        q.has("description", desc);
        for (Vertex v : q.vertices()) {
            if (validDeviceVertex(v)) {
                devices.add(vertexToDevice(v));
            }
        }
        return devices;
    }
    
    /*
     * returns Device devId owned by User userId
     *
     * returns null if for any reason the userId is invalid,
     * devId is invalid, or device isn't owned by user     
     */
    public Device getUserDevice(String userId, String devId) {
        if (validUserDevice(userId, devId)) {
            return getDeviceById(devId);
        }
        return null;
    }

    public Collection<Device> getAllUserDevices(String userId) {
        if (userId == null) {
            return null;
        }
        Vertex v = ProxStorGraph.instance.getVertex(userId);
        List<Device> devices = new ArrayList<>();
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

    /*
     * insert new Device into Graph associated with User userId
     *
     * input is Device to be added. Note that the deviceId will be ignored
     *
     * returns Device object with correct deviceId reflecting object ID assigned
     * by underlying graph; 
     * otherwise null if
     *    - userId is invalid
     */
    public Device addUserDevice(String userId, Device d) {
        if ((userId == null) || (d == null) || (UserDao.instance.getUser(userId) == null)) {
            return null;
        }
        Vertex out = ProxStorGraph.instance.getVertex(userId);
        Vertex in = ProxStorGraph.instance.newVertex();
        in.setProperty("description", d.getDescription());
        setVertexToDeviceType(in);
        d.setDevId(in.getId().toString());
        ProxStorGraph.instance.newEdge(out, in, "owns");
        ProxStorGraph.instance.commit();
        return d;
    }

    /*
     * updates Device based on Device's devId if userId Owns
     *
     * returns true if the Device's devId is valid device
     * return false if the Device's devId is not valid device
     */
    public boolean updateUserDevice(String userId, Device d) {
        if ((userId == null) || (d == null) || (d.getDevId() == null)) {
            return false;
        }
        if (validUserDevice(userId, d.getDevId())) {
            Vertex v = ProxStorGraph.instance.getVertex(d.getDevId());
            v.setProperty("description", d.getDescription());
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

    /*
     * updates Device based on Device's devId
     *
     * returns true if the Device's devId is valid device
     * return false if the Device's devId is not valid device
     */
    public boolean _updateDevice(Device d) {
        if ((d == null) || (d.getDevId() == null)) {
            return false;
        }
        if (validDeviceId(d.getDevId())) {
            Vertex v = ProxStorGraph.instance.getVertex(d.getDevId());
            v.setProperty("description", d.getDescription());
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

    /* 
     * remove devId from graph
     *
     * returns true upon success
     * returns false if devId was not a Device
     */
    public boolean _deleteDevice(String devId) {
        if ((devId != null) && (validDeviceId(devId))) {
            ProxStorGraph.instance.getVertex(devId).remove();
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }

    /* 
     * remove devId from graph if userId Owns it
     *
     * returns true upon success
     * returns false if devId was not a Device
     */
    public boolean deleteUserDevice(String userId, String devId) {
        if (validUserDevice(userId, devId)) {
            ProxStorGraph.instance.getVertex(devId).remove();
            ProxStorGraph.instance.commit();
            return true;
        }
        return false;
    }
}
