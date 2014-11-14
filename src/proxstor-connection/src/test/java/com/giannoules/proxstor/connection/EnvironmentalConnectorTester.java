package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Location;
import com.giannoules.proxstor.api.LocationType;
import com.giannoules.proxstor.api.Environmental;
import com.giannoules.proxstor.api.EnvironmentalType;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for ProxStorConnector's Environmental-centric methods
 * 
 * @author James Giannoules
 */
public class EnvironmentalConnectorTester {
    
    private static ProxStorConnector conn;
    private static String invalidId;
    private Location goodLocation;
    private Environmental goodEnvironmental;
    private Environmental goodEnvironmental2;
    
    public EnvironmentalConnectorTester() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector(ConnectionSettings.ConnectionString);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /*
     * create one location with two environmentals
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
        
        s = new Environmental();
        s.setDescription("BLE Device in Bathroom");
        s.setType(EnvironmentalType.BLE_UUID);
        s.setIdentifier(UUID.randomUUID().toString());
        
        goodEnvironmental2 = conn.addEnvironmental(goodLocation.getLocId(), s);
        
        assertNotNull(goodEnvironmental2.getEnvironmentalId());
        assertEquals(s, goodEnvironmental2);        
    }
    
    @After
    public void tearDown() {
    }
    
    /*
     * add a third environmental to goodLocation
     * - expect environmental to be reflected with environmentalId added
     */
    @Test
    public void addEnvironmental() {
        Environmental s = new Environmental();
        s.setDescription("WiFi in Coffee Shop");
        s.setType(EnvironmentalType.WIFI_BSSID);
        s.setIdentifier(UUID.randomUUID().toString());
        Environmental s2 = conn.addEnvironmental(goodLocation.getLocId(), s);
        assertNotNull(s2.getEnvironmentalId());
        assertEquals(s, s2);
    }
    
    /*
     * add blank environmental to goodLocation and invalidLocation
     * - expect null for both
     */
    @Test
    public void addEnvironmentalInvalid() {
        Environmental s = new Environmental();
        assertNull(conn.addEnvironmental(goodLocation.getLocId(), s));
        assertNull(conn.addEnvironmental(invalidId, goodEnvironmental));
    }
    
    /*
     * update goodEnvironmental
     * - expect true
     */
    @Test
    public void updateEnvironmental() {
        goodEnvironmental.setDescription("updated description");
        assertTrue(conn.updateEnvironmental(goodLocation.getLocId(), goodEnvironmental));
    }
    
    /*
     * update goodEnvironmental in invalidLocation
     * update new Environmental in goodLocation
     * update new Environmental in invalidLocation
     * - expect false for all
     */    
    @Test
    public void updateEnvironmentalInvalid() {
        assertFalse(conn.updateEnvironmental(invalidId, goodEnvironmental));
        assertFalse(conn.updateEnvironmental(goodLocation.getLocId(), new Environmental()));
        assertFalse(conn.updateEnvironmental(invalidId, new Environmental()));        
    }
    
    /*
     * get all environmentals in goodLocation
     * - expect {goodEnvironmental, goodEnvironmental2}
     * get all environmentals from new Location
     * - expect {}
     */
    @Test
    public void getEnvironmentals() {
        Collection<Environmental> environmentals;
        environmentals = conn.getEnvironmentals(goodLocation.getLocId());
        assertNotNull(environmentals);
        assertEquals(environmentals.size(), 2);
        assertTrue(environmentals.contains(goodEnvironmental));
        assertTrue(environmentals.contains(goodEnvironmental2));
        
        Location l = new Location();
        l.setAddress("12345 Main St.");
        l.setDescription("My Favorite Burger Joint");
        l.setLatitude(30.267153);
        l.setLatitude(-97.7430608);
        l.setType(LocationType.BUSINESS);
        l = conn.addLocation(l);
        assertNotNull(l.getLocId());
        environmentals = conn.getEnvironmentals(l.getLocId());
        assertEquals(environmentals, Collections.EMPTY_LIST);
    }    
    
    /*
     * get environmentals in invalid location
     * - expect null
     */
    @Test
    public void getEnvironmentalsInvalid() {
        assertNull(conn.getEnvironmentals(invalidId));        
    }
    
    /*
     * delete goodEnvironmental and goodEnvironmental2 from goodLocation
     * - expect true
     * delete goodEnvironmental and goodEnvironmental2 from goodLocation (again)
     * - expect false
     */
    @Test
    public void deleteEnvironmental() {
        assertTrue(conn.deleteEnvironmental(goodLocation.getLocId(), goodEnvironmental.getEnvironmentalId()));
        assertTrue(conn.deleteEnvironmental(goodLocation.getLocId(), goodEnvironmental2.getEnvironmentalId()));
        assertFalse(conn.deleteEnvironmental(goodLocation.getLocId(), goodEnvironmental.getEnvironmentalId()));
        assertFalse(conn.deleteEnvironmental(goodLocation.getLocId(), goodEnvironmental2.getEnvironmentalId()));
    }
    
    /*
     * delete goodEnvironmental from invalidId
     * delete goodEnvironmental2 from invalidId
     * delete invalidId from goodLocation
     * - expect false for all
     */
    @Test
    public void deleteEnvironmentalInvalid() {
        assertFalse(conn.deleteEnvironmental(invalidId, goodEnvironmental.getEnvironmentalId()));
        assertFalse(conn.deleteEnvironmental(invalidId, goodEnvironmental2.getEnvironmentalId()));
        assertFalse(conn.deleteEnvironmental(goodLocation.getLocId(), invalidId));
    }
    
}
