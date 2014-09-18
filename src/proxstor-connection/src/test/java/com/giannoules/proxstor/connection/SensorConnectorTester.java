package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.SensorType;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/* 
 * unit tests for proxstorconnector's Sensor-centric methods
 */
public class SensorConnectorTester {
    
    private static ProxStorConnector conn;
    private static Integer invalidId;
    private Location goodLocation;
    private Sensor goodSensor;
    private Sensor goodSensor2;
    
    public SensorConnectorTester() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector("http://localhost:8080/api");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * create one location with two sensors
     */
    @Before
    public void setUp() {
        Location l = new Location();
        l.setAddress("12345 Main St.");
        l.setDescription("My Favorite Burger Joint");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        goodLocation = conn.addLocation(l);
        assertNotNull(goodLocation.getLocId());
        assertEquals(l, goodLocation);
        
        Sensor s = new Sensor();
        s.setDescription("WiFi in Shopping Mall");
        s.setType(SensorType.WIFI_BSSID);
        s.setIdentifier(UUID.randomUUID().toString());
        
        goodSensor = conn.addSensor(Integer.parseInt(goodLocation.getLocId()), s);
        
        assertNotNull(goodSensor.getSensorId());
        assertEquals(s, goodSensor);
        
        s = new Sensor();
        s.setDescription("BLE Device in Bathroom");
        s.setType(SensorType.BLE_UUID);
        s.setIdentifier(UUID.randomUUID().toString());
        
        goodSensor2 = conn.addSensor(Integer.parseInt(goodLocation.getLocId()), s);
        
        assertNotNull(goodSensor2.getSensorId());
        assertEquals(s, goodSensor2);        
    }
    
    @After
    public void tearDown() {
    }
    
    /*
     * add a third sensor to goodLocation
     * - expect sensor to be reflected with sensorId added
     */
    @Test
    public void addSensor() {
        Sensor s = new Sensor();
        s.setDescription("WiFi in Coffee Shop");
        s.setType(SensorType.WIFI_BSSID);
        s.setIdentifier(UUID.randomUUID().toString());
        Sensor s2 = conn.addSensor(Integer.parseInt(goodLocation.getLocId()), s);
        assertNotNull(s2.getSensorId());
        assertEquals(s, s2);
    }
    
    /*
     * add blank sensor to goodLocation and invalidLocation
     * - expect null for both
     */
    @Test
    public void addSensorInvalid() {
        Sensor s = new Sensor();
        assertNull(conn.addSensor(Integer.parseInt(goodLocation.getLocId()), s));
        assertNull(conn.addSensor(invalidId, goodSensor));
    }
    
    /*
     * update goodSensor
     * - expect true
     */
    @Test
    public void updateSensor() {
        goodSensor.setDescription("updated description");
        assertTrue(conn.updateSensor(Integer.parseInt(goodLocation.getLocId()), goodSensor));
    }
    
    /*
     * update goodSensor in invalidLocation
     * update new Sensor in goodLocation
     * update new Sensor in invalidLocation
     * - expect false for all
     */    
    @Test
    public void updateSensorInvalid() {
        assertFalse(conn.updateSensor(invalidId, goodSensor));
        assertFalse(conn.updateSensor(Integer.parseInt(goodLocation.getLocId()), new Sensor()));
        assertFalse(conn.updateSensor(invalidId, new Sensor()));        
    }
    
    /*
     * get all sensors in goodLocation
     * - expect {goodSensor, goodSensor2}
     * get all sensors from new Location
     * - expect {}
     */
    @Test
    public void getSensors() {
        Collection<Sensor> sensors;
        sensors = conn.getSensors(Integer.parseInt(goodLocation.getLocId()));
        assertNotNull(sensors);
        assertEquals(sensors.size(), 2);
        assertTrue(sensors.contains(goodSensor));
        assertTrue(sensors.contains(goodSensor2));
        
        Location l = new Location();
        l.setAddress("12345 Main St.");
        l.setDescription("My Favorite Burger Joint");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        l = conn.addLocation(l);
        assertNotNull(l.getLocId());
        sensors = conn.getSensors(Integer.parseInt(l.getLocId()));
        assertEquals(sensors, Collections.EMPTY_LIST);
    }    
    
    /*
     * get sensors in invalid location
     * - expect null
     */
    @Test
    public void getSensorsInvalid() {
        assertNull(conn.getSensors(invalidId));        
    }
    
    /*
     * delete goodSensor and goodSensor2 from goodLocation
     * - expect true
     * delete goodSensor and goodSensor2 from goodLocation (again)
     * - expect false
     */
    @Test
    public void deleteSensor() {
        assertTrue(conn.deleteSensor(Integer.parseInt(goodLocation.getLocId()), Integer.parseInt(goodSensor.getSensorId())));
        assertTrue(conn.deleteSensor(Integer.parseInt(goodLocation.getLocId()), Integer.parseInt(goodSensor2.getSensorId())));
        assertFalse(conn.deleteSensor(Integer.parseInt(goodLocation.getLocId()), Integer.parseInt(goodSensor.getSensorId())));
        assertFalse(conn.deleteSensor(Integer.parseInt(goodLocation.getLocId()), Integer.parseInt(goodSensor2.getSensorId())));
    }
    
    /*
     * delete goodSensor from invalidId
     * delete goodSensor2 from invalidId
     * delete invalidId from goodLocation
     * - expect false for all
     */
    @Test
    public void deleteSensorInvalid() {
        assertFalse(conn.deleteSensor(invalidId, Integer.parseInt(goodSensor.getSensorId())));
        assertFalse(conn.deleteSensor(invalidId, Integer.parseInt(goodSensor2.getSensorId())));
        assertFalse(conn.deleteSensor(Integer.parseInt(goodLocation.getLocId()), invalidId));
    }
    
}
