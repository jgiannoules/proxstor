package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.Locality;
import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.EnvironmentalType;
import com.giannoules.proxstor.api.User;
import java.util.Date;
import java.util.UUID;
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

/**
 * Unit tests for ProxStorConnector's Locality-centric methods
 * 
 * @author James Giannoules
 */
public class LocalityConnectorTester  {

    private static ProxStorConnector conn;
    private static String invalidId;
    private Locality goodLocality;
    private Location goodLocation;
    private Environmental goodEnvironmental;
    private User goodUser;
    private Device goodDevice;
    
    public LocalityConnectorTester() {        
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector(ConnectionSettings.ConnectionString);
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
        Environmental s = new Environmental();
        s.setDescription("WiFi in Shopping Mall");
        s.setType(EnvironmentalType.WIFI_BSSID);
        s.setIdentifier(UUID.randomUUID().toString());        
        goodEnvironmental = conn.addEnvironmental(goodLocation.getLocId(), s);        
        assertNotNull(goodEnvironmental.getEnvironmentalId());
        assertEquals(s, goodEnvironmental);
        goodUser = conn.addUser(new User("first", "last", "f_last@msn.com"));
        assertNotNull(goodUser);
        goodDevice = new Device();
        goodDevice.setDescription("Mobile Thing for JUnit");        
        goodDevice.setManufacturer("FashionableBrand");
        goodDevice.setModel("X1000");
        goodDevice.setOs("RandomOS");
        goodDevice.setSerialNum(UUID.randomUUID().toString());
        goodDevice = conn.addDevice(goodUser.getUserId(), goodDevice);                
        assertNotNull(goodDevice);
        Locality loc = new Locality();
        loc.setActive(true);
        loc.setDeviceId(goodDevice.getDevId());
        loc.setManual(false);
        loc.setEnvironmentalId(goodEnvironmental.getEnvironmentalId());
        goodLocality = conn.addLocality(loc);
        assertNotNull(goodLocality);
        loc.setArrival(goodLocality.getArrival());
        loc.setLocalityId(goodLocality.getLocalityId());
        loc.setLocationId(goodLocality.getLocationId());
        assertEquals(goodLocality, loc);
        invalidId = goodLocality.getLocalityId() + 1;
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
        Locality l = conn.getLocality(goodLocality.getLocalityId());
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
        assertTrue(conn.deleteLocality(goodLocality.getLocalityId()));
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
        m.setEnvironmentalId(goodEnvironmental.getEnvironmentalId());
        Locality l = conn.addLocality(m);
        assertNotNull(l);
        assertNotNull(l.getLocalityId());
        m.setArrival(l.getArrival());
        m.setLocalityId(l.getLocalityId());
        m.setLocationId(l.getLocationId());
        assertEquals(l, m);        
    }    
  
}
