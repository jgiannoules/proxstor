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

    /**
     * Get a specific devid owned by a particular userid. The dev id must exists
     * as a valid device, the userid must exists as a valid user, and the 'uses'
     * relationship must exists from user to device.
     *
     * @param userId String representation of user id
     * @param devId String representation of device id
     * 
     * @return Instance of matching Device
     * 
     * @throws InvalidUserId if the userId is invalid
     * @throws InvalidDeviceId if the devId is invalid
     * @throws DeviceNotOwnedByUser if the device isn't owned by the user
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

    /**
     * Returns all the devices owned by the userId
     *
     * @param userId String representation of user id
     * 
     * @return Collection of Device objects owned by user, or null if none
     * 
     * @throws InvalidUserId If the userId is invalid 
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
    
    /**
     * Find and returns all matching devices based on partially specified Device
     * <p>If the partially specified device includes a devId field then that one
     * property is used to match. If the devId is not present, then all other
     * properties are used using a GraphQuery to match. This means
     * passing in a devId will allow you to retrieve a specific device and
     * bypass the normal requirement that device retrieval requires knowledge
     * of the user who uses it.
     * <p>Note that no exceptions are thrown. An invalid devId is handled a
     * simply a null response.
     * 
     * @param partial Partially completed Device object
     * 
     * @return Collection of Device objects matching partial, or null if none
     */
    public Collection<Device> getMatching(Device partial) {
        List<Device> devices = new ArrayList<>();
        if ((partial.getDevId() != null) && (!partial.getDevId().isEmpty())) {
            try {                
                validOrException(partial.getDevId());
                devices.add(DeviceDao.this.get(partial.getDevId()));
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

    /**
     * Insert new Device instance into Graph associated with User userId
     *
     * @param userId String representation of the User owning the Device
     * @param d Device to be added. Note that the deviceId will be ignored
     *
     * @return Device object with correct deviceId reflecting object ID assigned
     * by underlying graph; 
     *
     * @throws InvalidUserId If the userId parameter does not match a valid user
     */
    public Device add(String userId, Device d) throws InvalidUserId {
        UserDao.instance.validOrException(userId);
        try {
            Vertex out = ProxStorGraph.instance.getVertex(userId);
            Vertex in = ProxStorGraph.instance.addVertex();
            in.setProperty("description", d.getDescription());
            setType(in);
            d.setDevId(in.getId().toString());
            ProxStorGraph.instance.addEdge(out, in, "uses");
            ProxStorGraph.instance.commit();
            return d;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Updates (modifies) an existing device in the database. The device must
     * be in a 'Used' relationship with the User. All fields from the Device
     * parameter will overwrite the fields of the original Device. The device
     * id will remain the same.
     *
     * @param userId String representation of the User who uses the device
     * @param d Updated Device object
     * 
     * @return true if the update was successful; false if a database error was
     * encountered
     * 
     * @throws InvalidUserId If the userId parameter does not represent a valid user
     * @throws InvalidDeviceId If the devId within the d parameter does not represent
     * a valid device
     * @throws DeviceNotOwnedByUser If the d.devId and userId are valid, but the
     * device is not owner by the user.
     */
    public boolean update(String userId, Device d) throws InvalidUserId, InvalidDeviceId, DeviceNotOwnedByUser {
        UserDao.instance.validOrException(userId);
        validOrException(d.getDevId());
        if (!isUserDev(userId, d.getDevId())) {
            throw new DeviceNotOwnedByUser();
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(d.getDevId());
            v.setProperty("description", d.getDescription());
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(DeviceDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Remove a Device from the database. The user must have a 'uses' relationship
     * to the device for the operation to succeed.
     *
     * @param userId String representation of the user id who uses the device
     * @param devId String representation of the device to be removed
     * 
     * @return true if the operation succeeds; false if communication error to 
     * the database
     * 
     * @throws InvalidUserId If userId parameter is not a valid user in the graph
     * @throws InvalidDeviceId If devId parameter is not a valid device in the graph   
     * @throws DeviceNotOwnedByUser If the devId and userId are valid, but the
     * device is not owner by the user.
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

    /**
     * Helper method which accepts a device id strings and either returns
     * nothing to the caller or throws an InvalidDeviceId exception if any
     * of the device id are invalid.
     * 
     * @param devIds Variadic list of device id String representations
     * 
     * @throws InvalidDeviceId If any of the devId String parameters are not valid
     * devices
     */
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
            d.setDevId(Long.toString((Long) v.getId()));
        } else {
            d.setDevId(v.getId().toString());
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
        if ((d == null) || (d.getDevId() == null)) {
            return false;
        }
        if (valid(d.getDevId())) {
            Vertex v;
            try {
                v = ProxStorGraph.instance.getVertex(d.getDevId());
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
