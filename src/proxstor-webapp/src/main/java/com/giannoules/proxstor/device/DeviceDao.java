package com.giannoules.proxstor.device;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.DeviceNotOwnedByUser;
import com.giannoules.proxstor.exception.InvalidDeviceId;
import com.giannoules.proxstor.exception.InvalidUserId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.user.UserDao;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Data Access Object to database-persistent store of Devices
 *
 * @TODO implement caching
 * @TODO owns -> uses
 *
 * Currently uses basic low-level Blueprints API
 *
 */
public enum DeviceDao {

    instance;

    private DeviceDao() {
    }

    /*
     * returns Device devId owned by User userId
     *
     * throws InvalidUserId if the userId is invalid
     * throws InvalidDeivceId if the devId is invalid
     * throws DeviceNotOwnedByUser if the device isn't owned by the user
     */
    public Device getUserDevice(String userId, String devId) throws InvalidDeviceId, InvalidUserId, DeviceNotOwnedByUser {
        UserDao.instance.validOrException(userId);
        validOrException(devId);        
        if (isUserDev(userId, devId)) {
            return DeviceDao.this.get(devId);
        } else {
            throw new DeviceNotOwnedByUser();
        }
    }

    /*
     * returns all the devices owned by the userId
     *
     * throws InvalidUserId if the userId is invalid 
     */
    public Collection<Device> getAllUserDevices(String userId) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(userId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        List<Device> devices = new ArrayList<>();
        for (Edge e : v.getEdges(OUT, "uses")) {
            devices.add(DeviceDao.instance.toDevice(e.getVertex(IN)));
        }
        return devices;
    }
    
    /*
     * find all matching Devices based on partially specified Device
     *
     * passing in a devId will allow you to retrieve a specific device and
     * no further filtering based on the other fields (if present) will be
     * done.
     *
     */
    public Collection<Device> get(Device partial) {
        List<Device> devices = new ArrayList<>();
        if ((partial.getId() != null) && (!partial.getId().isEmpty())) {
            try {                
                validOrException(partial.getId());
                devices.add(DeviceDao.this.get(partial.getId()));
                return devices;  
            } catch (InvalidDeviceId ex) {
                // invalid devId is not an exception, it is just no match condition
                Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }                                  
        }
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "device");
        if ((partial.getDescription() != null) && (!partial.getDescription().isEmpty())) {
            q.has("description", partial.getDescription());
        }
        for (Vertex v : q.vertices()) {
            if (valid(v)) {
                devices.add(toDevice(v));
            }
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
     
     */
    public Device add(String userId, Device d) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        try {
            Vertex out = ProxStorGraph.instance.getVertex(userId);
            Vertex in = ProxStorGraph.instance.addVertex();
            in.setProperty("description", d.getDescription());
            setType(in);
            d.setId(in.getId().toString());
            ProxStorGraph.instance.addEdge(out, in, "uses");
            ProxStorGraph.instance.commit();
            return d;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /*
     * updates Device based on Device's devId if userId Uses
     *    
     */
    public boolean update(String userId, Device d) throws InvalidUserId, InvalidDeviceId, DeviceNotOwnedByUser {
        UserDao.instance.validOrException(userId);
        validOrException(d.getId());
        if (!isUserDev(userId, d.getId())) {
            throw new DeviceNotOwnedByUser();
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(d.getId());
            v.setProperty("description", d.getDescription());
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /* 
     * remove devId from graph if userId Uses it
     *
     * returns true upon success
     * returns false if devId was not a Device
     */
    public boolean delete(String userId, String devId) throws InvalidUserId, InvalidDeviceId, DeviceNotOwnedByUser {
        UserDao.instance.validOrException(userId);
        validOrException(devId);
        if (!isUserDev(userId, devId)) {
            throw new DeviceNotOwnedByUser();
        }
        try {
            ProxStorGraph.instance.getVertex(devId).remove();
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void validOrException(String ... devIds) throws InvalidDeviceId {
        if (!valid(devIds)) {
            throw new InvalidDeviceId();
        }
    }
    
    // ----> BEGIN private methods <----
   
    
    /*
     * converts vertex into Device object
     *
     * assumes sanity check already performed on vertex
     */
    private Device toDevice(Vertex v) {
        if (v == null) {
            return null;
        }
        Device d = new Device();
        d.setDescription((String) v.getProperty("description"));
        Object id = v.getId();
        if (id instanceof Long) {
            d.setId(Long.toString((Long) v.getId()));
        } else {
            d.setId(v.getId().toString());
        }
        return d;
    }

    /*
     * abstract away setting of Vertex Device type
     */
    private void setType(Vertex v) {
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
    private Device get(String devId) {
        if (devId == null) {
            return null;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(devId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if ((v != null) && valid(v)) {
            return toDevice(v);
        }
        return null;
    }
    
    /* 
     * remove devId from graph
     *
     * returns true upon success
     * returns false if devId was not a Device
     */
    private boolean delete(String devId) {
        if ((devId != null) && (valid(devId))) {
            try {
                ProxStorGraph.instance.getVertex(devId).remove();
                ProxStorGraph.instance.commit();
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
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
    private boolean update(Device d) {
        if ((d == null) || (d.getId() == null)) {
            return false;
        }
        if (valid(d.getId())) {
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(d.getId());
                v.setProperty("description", d.getDescription());
                ProxStorGraph.instance.commit();
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            return true;
        }
        return false;
    }
    
    /*
     * test Vertex for Device-ness
     */
    private boolean valid(Vertex ... vertices) {
        for (Vertex v : vertices) {
            if ((v == null) || !v.getProperty("_type").equals("device")) {
                return false;
            }
        }
        return true;
    }

    /*
     * test device id for Device-ness
     */
    private boolean valid(String ... ids) {
        for (String id : ids) {
            try {
                if ((id == null) || !valid(ProxStorGraph.instance.getVertex(id))) {
                    return false;
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
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
    private boolean isUserDev(String userId, String devId) {
        if ((userId == null) || (devId == null)) {
            return false;
        }
        Device d = DeviceDao.this.get(devId);
        if (d == null) {    // conditions 1 & 3
            return false;
        }
        try {
            try {
                if (UserDao.instance.get(userId) == null) { // conditions 2 & 4
                    return false;
                }
             // This can be collapsed into simple UserDao.getUserDevice DeviceDao.getUserDevice with exception catches
            } catch (InvalidUserId ex) {
                Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (Edge e : ProxStorGraph.instance.getVertex(devId).getEdges(IN, "uses")) {
                if (e.getVertex(OUT).getId().equals(userId)) {
                    return true;
                }
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false; // condition 5        
    }

}
