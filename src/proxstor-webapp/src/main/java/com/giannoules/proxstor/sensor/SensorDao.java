package com.giannoules.proxstor.sensor;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.InvalidLocationId;
import com.giannoules.proxstor.exception.InvalidParameter;
import com.giannoules.proxstor.exception.InvalidSensorId;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.exception.SensorNotContainedWithinLocation;
import com.giannoules.proxstor.location.LocationDao;
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

public enum SensorDao {

    instance;

    private SensorDao() {
    }

    /**
     * Get a specific sensorId contained in a particular lcdId. The sensor id
     * must exists as a valid sensor, the locid must exists as a valid location,
     * and the 'within' relationship must exists from location to sensor.
     *
     * @param locId String representation of location id
     * @param sensorId String representation of sensor id
     *
     * @return Instance of matching Sensor
     *
     * @throws InvalidSensorId if the sensorId is invalid
     * @throws InvalidLocationId if the locId is invalid
     * @throws SensorNotContainedWithinLocation if the sensor isn't within the
     * location
     */
    public Sensor getLocationSensor(String locId, String sensorId) throws SensorNotContainedWithinLocation, InvalidSensorId, InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        validOrException(sensorId);
        if (isLocationSensor(locId, sensorId)) {
            return get(sensorId);
        } else {
            throw new SensorNotContainedWithinLocation();
        }
    }

    /**
     * Returns all the sensors within the location
     *
     * @param locId String representation of locaiton id
     *
     * @return Collection of Sensor objects contained in location, or null if
     * none
     *
     * @throws InvalidLocationId If the locId is invalid
     */
    public Collection<Sensor> getAllLocationSensors(String locId) throws InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(locId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        List<Sensor> sensors = new ArrayList<>();
        for (Edge e : v.getEdges(OUT, "contains")) {
            sensors.add(SensorDao.instance.toSensor(e.getVertex(IN)));
        }
        return sensors;
    }

    /**
     * Find and returns all matching sensors based on partially specified Sensor
     * <p>
     * If the partially specified sensor includes a sensorId field then that one
     * property is used to match. If the sensorId is not present, then all other
     * properties are used using a GraphQuery to match. This means passing in a
     * sensoId will allow you to retrieve a specific sensor and bypass the
     * normal requirement that sensor retrieval requires knowledge of the
     * location containing it.
     * <p>
     * Note that no exceptions are thrown. An invalid sensorId is handled a
     * simply a null response.
     *
     * @param partial Partially completed Sensor object
     *
     * @return Collection of Sensor objects matching partial, or null if none
     */
    public Collection<Sensor> getMatching(Sensor partial) {
        List<Sensor> sensors = new ArrayList<>();
        if ((partial.getSensorId() != null) && (!partial.getSensorId().isEmpty())) {
            try {
                validOrException(partial.getSensorId());
                sensors.add(get(partial.getSensorId()));
                return sensors;
            } catch (InvalidSensorId ex) {
                // invalid sensorId is not an exception, it is just no match condition
                Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "sensor");
        if ((partial.getDescription() != null) && (!partial.getDescription().isEmpty())) {
            q.has("description", partial.getDescription());
        }
        if (partial.getType() != null) {
            q.has("type", partial.getType().toString());
        }
        for (Vertex v : q.vertices()) {
            if (valid(v)) {
                sensors.add(toSensor(v));
            }
        }
        return sensors;
    }

    /**
     * Insert new SEnsor instance into Graph associated with Location locId
     *
     * @param locId String representation of the Location containing the Sensor
     * @param s Sensor to be added. Note that the sensorId will be ignored
     *
     * @return Sensor object with correct sensorId reflecting object ID assigned
     * by underlying graph;
     *
     * @throws InvalidLocationId If the locId parameter does not match a valid
     * location
     */
    public Sensor add(String locId, Sensor s) throws InvalidLocationId, InvalidParameter {
        LocationDao.instance.validOrException(locId);
        if ((s.getDescription() == null) || (s.getType() == null)) {
            throw new InvalidParameter();
        }
        try {
            Vertex out = ProxStorGraph.instance.getVertex(locId);
            Vertex in = ProxStorGraph.instance.addVertex();
            in.setProperty("description", s.getDescription());
            in.setProperty("type", s.getType().toString());
            setType(in);
            s.setSensorId(in.getId().toString());
            ProxStorGraph.instance.addEdge(out, in, "contains");
            ProxStorGraph.instance.commit();
            return s;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Updates (modifies) an existing sensor in the database. The sensor must be
     * in a 'Within' relationship with the Location. All fields from the Sensor
     * parameter will overwrite the fields of the original Sensor. The sensor id
     * will remain the same.
     *
     * @param locId String representation of the Location containing the sensor
     * @param s Updated Sensor object
     *
     * @return true if the update was successful; false if a database error was
     * encountered
     *
     * @throws InvalidLocationId If the locId parameter does not represent a
     * valid location
     * @throws InvalidSensorId If the sensorId within the s parameter does not
     * represent a valid sensor
     * @throws SensorNotContainedWithinLocation If the s.sensorId and locId are
     * valid, but the sensor is not within the location.
     */
    public boolean update(String locId, Sensor s) throws SensorNotContainedWithinLocation, InvalidSensorId, InvalidLocationId {
        LocationDao.instance.validOrException(locId);
        validOrException(s.getSensorId());
        if (!isLocationSensor(locId, s.getSensorId())) {
            throw new SensorNotContainedWithinLocation();
        }        
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(s.getSensorId());
            if (s.getDescription() != null) {
                v.setProperty("description", s.getDescription());
            }
            if (s.getType() != null) {
                v.setProperty("type", s.getType().toString());
            }
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Remove a Sensor from the database. The location must have a 'within'
     * relationship to the sensor for the operation to succeed.
     *
     * @param locId String representation of the location id containing the
     * sensor
     * @param sensorId String representation of the sensor to be removed
     *
     * @return true if the operation succeeds; false if communication error to
     * the database
     *
     * @throws InvalidSensorId If sensorId parameter is not a valid sensor in
     * the graph
     * @throws InvalidLocationId If locId parameter is not a valid location in
     * the graph
     * @throws SensorNotContainedWithinLocation If the locId and sensorId are
     * valid, but the sensor is not within the location.
     */
    public boolean delete(String locId, String sensorId) throws InvalidSensorId, InvalidLocationId, SensorNotContainedWithinLocation {
        LocationDao.instance.validOrException(locId);
        validOrException(sensorId);
        if (!isLocationSensor(locId, sensorId)) {
            throw new SensorNotContainedWithinLocation();
        }
        try {
            ProxStorGraph.instance.getVertex(sensorId).remove();
            ProxStorGraph.instance.commit();
            return true;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Helper method which accepts a location id strings and either returns
     * nothing to the caller or throws an InvalidSensorId exception if any of
     * the sensor id are invalid.
     *
     * @param sensorIds Variadic list of sensor id String representations
     *
     * @throws InvalidSensorId If any of the sensorId String parameters are not
     * valid sensors
     */
    public void validOrException(String... sensorIds) throws InvalidSensorId {
        if (!valid(sensorIds)) {
            throw new InvalidSensorId();
        }
    }

    // ----> BEGIN private methods <----
    /*
     * converts vertex into Sensor object
     *
     * assumes sanity check already performed on vertex
     */
    private Sensor toSensor(Vertex v) {
        if (v == null) {
            return null;
        }
        Sensor s = new Sensor();
        s.setDescription((String) v.getProperty("description"));
        s.setType(SensorType.valueOf((String) v.getProperty("type")));
        Object id = v.getId();
        if (id instanceof Long) {
            s.setSensorId(Long.toString((Long) v.getId()));
        } else {
            s.setSensorId(v.getId().toString());
        }
        return s;
    }

    /*
     * abstract away setting of Vertex Sensor type
     */
    private void setType(Vertex v) {
        if (v != null) {
            v.setProperty("_type", "sensor");
        }
    }

    /*
     * returns Sensor stored under sensorId
     *
     * returns null if:
     *   - sensorId does not map to any graph vertex
     *   - vertex is not of type sensor
     *
     */
    private Sensor get(String sensorId) {
        if (sensorId == null) {
            return null;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(sensorId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if ((v != null) && valid(v)) {
            return toSensor(v);
        }
        return null;
    }

    /* 
     * remove sensorId from graph
     *
     * returns true upon success
     * returns false if sensorId was not a Sensor
     */
    private boolean delete(String sensorId) {
        if ((sensorId != null) && (valid(sensorId))) {
            try {
                ProxStorGraph.instance.getVertex(sensorId).remove();
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

    /*
     * updates Sensor based on Sensor's sensorId
     *
     * returns true if the Sensor's sensorId is valid sensor
     * return false if the Sensor's sensorId is not valid sensor
     */
    public boolean update(Sensor s) {
        if ((s == null) || (s.getSensorId() == null)) {
            return false;
        }
        if (valid(s.getSensorId())) {
            try {
                Vertex v = ProxStorGraph.instance.getVertex(s.getSensorId());
                v.setProperty("description", s.getDescription());
                v.setProperty("type", s.getType().toString());
                ProxStorGraph.instance.commit();
                return true;
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

    /*
     * test Vertex for Sensor-ness
     */
    private boolean valid(Vertex... vertices) {
        for (Vertex v : vertices) {
            if ((v == null) || !v.getProperty("_type").equals("sensor")) {
                return false;
            }
        }
        return true;
    }

    /*
     * test sensorid for Sensor-ness
     */
    private boolean valid(String... ids) {
        for (String id : ids) {
            try {
                if ((id == null) || !valid(ProxStorGraph.instance.getVertex(id))) {
                    return false;
                }
            } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
                Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    /*
     * tests for validity of sensorId conatined by locId
     *
     * returns false if:
     *   1 sensorId does not map to any graph vertex
     *   2 locId does not map to any graph vertex
     *   3 sensorId is not vertex of type Sensor
     *   4 locId is not vertex of type Location
     *   5 Location is not container of Sensor     
     */
    private boolean isLocationSensor(String locId, String sensorId) {
        try {
            SensorDao.instance.validOrException(sensorId);
            LocationDao.instance.validOrException(locId);
        } catch (InvalidParameter ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            for (Edge e : ProxStorGraph.instance.getVertex(sensorId).getEdges(IN, "contains")) {
                if (e.getVertex(OUT).getId().equals(locId)) {
                    return true;
                }
            }
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false; // condition 5        
    }

}
