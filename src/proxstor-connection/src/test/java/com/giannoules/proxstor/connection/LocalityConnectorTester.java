package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import com.giannoules.proxstor.api.Sensor;
import com.giannoules.proxstor.api.SensorType;
import com.giannoules.proxstor.api.User;
import java.util.Date;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/* 
 * unit tests for proxstorconnector's Locality-centric methods
 */
public class LocalityConnectorTester  {

    private static ProxStorConnector conn;
    private static Integer invalidId;
    private Locality goodLocality;
    private Location goodLocation;
    private Sensor goodSensor;
    private User goodUser;
    private Device goodDevice;
    
    public LocalityConnectorTester() {        
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector("http://localhost:8080/api");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * known good locality for each @Test
     * invalidId calculated as last localityId + 1
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
        goodUser = conn.addUser(new User("first", "last", "f_last@msn.com"));
        assertNotNull(goodUser);
        goodDevice = new Device();
        goodDevice.setDescription("Mobile Thing for JUnit");        
        goodDevice.setManufacturer("FashionableBrand");
        goodDevice.setModel("X1000");
        goodDevice.setOs("RandomOS");
        goodDevice.setSerialNum(UUID.randomUUID().toString());
        goodDevice = conn.addDevice(Integer.parseInt(goodUser.getUserId()), goodDevice);                
        assertNotNull(goodDevice);
        Locality loc = new Locality();
        loc.setActive(true);
        loc.setDeviceId(goodDevice.getDevId());
        loc.setManual(false);
        loc.setSensorId(goodSensor.getSensorId());
        goodLocality = conn.addLocality(loc);
        assertNotNull(goodLocality);
        loc.setArrival(goodLocality.getArrival());
        loc.setLocalityId(goodLocality.getLocalityId());
        loc.setLocationId(goodLocality.getLocationId());
        assertEquals(goodLocality, loc);
        invalidId = Integer.parseInt(goodLocality.getLocalityId()) + 1;
    }
    
    @After
    public void tearDown() {
    }

    /*
     * retrieve good Locality
     * - expect retrieved user equals goodLocality
     */
    @Test
    public void getLocality() {
        Locality l = conn.getLocality(Integer.parseInt(goodLocality.getLocalityId()));
        assertEquals(l, goodLocality);
    }
    
    /*
     * attempt to retrieve invalid locality id
     * - expect null
     */
    @Test
    public void getInvalidLocality() {
        assertNull(conn.getLocality(invalidId));
    }
    
    /*
     * delete goodLocality
     * - expect true
     */
    @Test
    public void deleteLocality() {
        assertTrue(conn.deleteLocality(Integer.parseInt(goodLocality.getLocalityId())));
    }
    
    /*
     * delete invalid locality id
     * - expect false
     */
    @Test
    public void deleteInvalidLocality() {
        assertFalse(conn.deleteLocality(invalidId));
    }
    
    /*
     * change a field in goodLocality and update
     * - expect true
     */
    @Test
    public void updateLocality() {
        goodLocality.setDeparture(new Date());
        goodLocality.setActive(false);
        assertTrue(conn.updateLocality(goodLocality));
    }
    
    /*
     * attempt to update an invalid locality id
     * attempt to update locality with valid id, but no other data
     * - expect false
     */
    @Test
    public void updateInvalidLocality() {
        Locality l = new Locality();
        l.setLocalityId(invalidId.toString());
        assertFalse(conn.updateLocality(l));
        l.setLocalityId(goodLocality.getLocalityId());
        assertFalse(conn.updateLocality(l));
    }

    /*
     * attempt to add a Locality without necessary data     
     * - expect false
     */
    @Test
    public void addInvalidLocality() {
        Locality l = new Locality();
        assertNull(conn.addLocality(l));
    }
    
    /*
     * attempt to add a valid locality
     * - expect localityId to be populated
     * - expect locality mirrored back properly
     */
    @Test 
    public void addValidLocality() {
        Locality m = new Locality();
        m.setActive(true);
        m.setDeviceId(goodDevice.getDevId());
        m.setSensorId(goodSensor.getSensorId());
        Locality l = conn.addLocality(m);
        assertNotNull(l);
        assertNotNull(l.getLocalityId());
        m.setArrival(l.getArrival());
        m.setLocalityId(l.getLocalityId());
        m.setLocationId(l.getLocationId());
        assertEquals(l, m);        
    }    
  
}