package com.giannoules.proxstor.sensor;

import com.giannoules.proxstor.ProxStorGraph;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.giannoules.proxstor.location.LocationDao;
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

public enum SensorDao {

    instance;

    private SensorDao() {
    }

    /*
     * converts vertex into Sensor object
     *
     * assumes sanity check already performed on vertex
     */
    private Sensor vertexToSensor(Vertex v) {
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
     * test Vertex for Sensor-ness
     */
    private boolean validSensorVertex(Vertex v) {
        return (v != null) && v.getProperty("_type").equals("sensor");
    }

    /*
     * test sensorid for Sensor-ness
     */
    private boolean validSensorId(String sensorId) {
        try {
            return (sensorId != null) && validSensorVertex(ProxStorGraph.instance.getVertex(sensorId));
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
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
    private boolean validLocationSensor(String locId, String sensorId) {
        if ((locId == null) || (sensorId == null)) {
            return false;
        }
        try {
            Sensor s = getSensorById(sensorId);
            if (s == null) {    // conditions 1 & 3
                return false;
            }
            if (LocationDao.instance.getLocationById(locId) == null) { // conditions 2 & 4
                return false;
            }
            for (Edge e : ProxStorGraph.instance.getVertex(sensorId).getEdges(IN, "contains")) {
                if (e.getVertex(OUT).getId().equals(locId)) {
                    return true;
                }
            }
            return false; // condition 5
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /*
     * abstract away setting of Vertex Sensor type
     */
    private void setVertexToSensorType(Vertex v) {
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
    public Sensor getSensorById(String sensorId) {
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
        if ((v != null) && validSensorVertex(v)) {
            return vertexToSensor(v);
        }
        return null;
    }

    /*
     * returns all sensor in database with description desc
     */
    public List<Sensor> getSensorsByDescription(String desc) {
        List<Sensor> sensors = new ArrayList<>();
        GraphQuery q;
        try {
            q = ProxStorGraph.instance._query();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        q.has("_type", "sensor");
        q.has("description", desc);
        for (Vertex v : q.vertices()) {
            if (validSensorVertex(v)) {
                sensors.add(vertexToSensor(v));
            }
        }
        return sensors;
    }

    /*
     * returns Sensor sensorId contained within Location locId
     *
     * returns null if for any reason the locId is invalid,
     * sensorId is invalid, or sensor isn't contained within location     
     */
    public Sensor getLocationSensor(String locId, String sensorId) {
        if (validLocationSensor(locId, sensorId)) {
            return getSensorById(sensorId);
        }
        return null;
    }

    /*
     * @TODO too fragile - need to validate locIds
     */
    public Collection<Sensor> getAllLocationSensors(String locId) {
        if (locId == null) {
            return null;
        }
        Vertex v;
        try {
            v = ProxStorGraph.instance.getVertex(locId);
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        List<Sensor> sensors = new ArrayList<>();
        for (Edge e : v.getEdges(OUT, "contains")) {
            sensors.add(SensorDao.instance.vertexToSensor(e.getVertex(IN)));
        }
        return sensors;
    }
  
    /*
     * find all matching Sensors based on partially specified Sensor
     */
    public Collection<Sensor> getMatchingSensors(Sensor partial) {
        List<Sensor> sensors = new ArrayList<>();
        if ((partial.getSensorId() != null) && (!partial.getSensorId().isEmpty())) {
            sensors.add(getSensorById(partial.getSensorId()));
            return sensors;
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
            if (validSensorVertex(v)) {
                sensors.add(vertexToSensor(v));
            }
        }
        return sensors;
    }

    /*
     * returns *all* Sensors in database, independent of the Locations
     *
     * warning: use of this might mean you are violating the contract that
     *          Sensors exists as a relationship from a single Location
     *              Location --CONTAINS--> Sensor
     */
    public Collection<Sensor> getAllSensors() {
        List<Sensor> sensors = new ArrayList<>();
        try {
            for (Vertex v : ProxStorGraph.instance.getVertices("_type", "sensor")) {
                sensors.add(vertexToSensor(v));
            }
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return sensors;
    }

    /*
     * insert new Sensor into Graph associated with Location locId
     *
     * input is Sensor to be added. Note that the locId will be ignored.
     *
     * returns Sensor object with correct sensorId reflecting object ID assigned
     * by underlying graph; 
     * otherwise null if
     *    - locId is invalid
     */
    public Sensor addLocationSensor(String locId, Sensor s) {
        if ((locId == null) || (s == null) || (LocationDao.instance.getLocationById(locId) == null)) {
            return null;
        }
        try {
            Vertex out = ProxStorGraph.instance.getVertex(locId);
            Vertex in = ProxStorGraph.instance.addVertex();
            in.setProperty("description", s.getDescription());
            in.setProperty("type", s.getType().toString());
            setVertexToSensorType(in);
            s.setSensorId(in.getId().toString());
            ProxStorGraph.instance.addEdge(out, in, "contains");
            ProxStorGraph.instance.commit();
            return s;
        } catch (ProxStorGraphDatabaseNotRunningException | ProxStorGraphNonExistentObjectID ex) {
            Logger.getLogger(SensorDao.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /*
     * updates Sensor based on Sensor's sensorId if locId contains Sensor
     *
     * returns true if the Sensor's sensorId is valid sensor
     * return false if the Sensor's sensorId is not valid sensor
     *
     * @TODO this needs to validate relationship
     */
    public boolean updateLocationSensor(String locId, Sensor s) {
        if ((locId == null) || (s == null) || (s.getSensorId() == null)) {
            return false;
        }
        if (validLocationSensor(locId, s.getSensorId())) {
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
     * updates Sensor based on Sensor's sensorId
     *
     * returns true if the Sensor's sensorId is valid sensor
     * return false if the Sensor's sensorId is not valid sensor
     */
    public boolean _updateSensor(Sensor s) {
        if ((s == null) || (s.getSensorId() == null)) {
            return false;
        }
        if (validSensorId(s.getSensorId())) {
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
     * remove sensorId from graph
     *
     * returns true upon success
     * returns false if sensorId was not a Sensor
     */
    public boolean _deleteSensor(String sensorId) {
        if ((sensorId != null) && (validSensorId(sensorId))) {
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
     * remove sensorId from graph if Location contains it
     *
     * returns true upon success
     * returns false if sensorId was not a Sensor
     */
    public boolean deleteLocationSensor(String locId, String sensorId) {
        if (validLocationSensor(locId, sensorId)) {
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
}
