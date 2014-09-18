package com.giannoules.proxstor.connection;

import com.giannoules.proxstor.api.Device;
import com.giannoules.proxstor.api.User;
import java.util.Collection;
import java.util.Iterator;
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

/* 
 * unit tests for proxstorconnector's Device-centric methods
 */

public class DeviceConnectorTester {
    
    private static ProxStorConnector conn;
    private static Integer invalidUserId;
    private static Integer invalidDeviceId;
    private Device goodDevice;
    private static User goodUser;
    
    public DeviceConnectorTester() {        
    }
    
    @BeforeClass
    public static void setUpClass() {
        conn = new ProxStorConnector("http://localhost:8080/api");        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /*
     * always setup a "good user" with known fields then associated a known
     * "good device", also with known fields
     *
     * based on the IDs returned calculate IDs which do not exist in db
     */
    @Before
    public void setUp() {
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
        invalidUserId = Integer.parseInt(goodUser.getUserId()) + 1;
        invalidDeviceId = Integer.parseInt(goodDevice.getDevId()) + 1;
    }
    
    @After
    public void tearDown() {
    }
    
    /*
     * add simple device
     *  - expect new device devID != null
     *  - expect that new device == goodDevice
     */
    @Test
    public void addDevice() {
        Device d = new Device();
        d.setDescription("Mobile Thing for JUnit");        
        d.setManufacturer("FashionableBrand");
        d.setModel("X1000");
        d.setOs("RandomOS");
        d.setSerialNum(UUID.randomUUID().toString());
        Device d2 = conn.addDevice(Integer.parseInt(goodUser.getUserId()), goodDevice);   
        assertNotNull(d2.getDevId());
        d.setDevId(d2.getDevId());
        assertEquals(d, d2);
    }
    
    /*
     * add empty device
     *  - expect null return from connector
     */
    @Test
    public void addInvalidDevice() {
        Device d = conn.addDevice(Integer.parseInt(goodUser.getUserId()), new Device());
        assertNull(d);        
    }
    
    /*
     * get specific gooddevice from gooduser
     *  - expect gooddevice to be returned
     */
    @Test
    public void getDevice() {
        Device d = conn.getDevice(Integer.parseInt(goodUser.getUserId()), 
                Integer.parseInt(goodDevice.getDevId()));
        assertEquals(goodDevice, d);
    }
    
    /*
     * get all devices belonging to goodUser
     *  - first add another instnace of the goodDevice
     *  - expect two devices to be returned
     *  - expect both to be goodDevices
     */
    @Test
    public void getUserDevices() {
        Device d = conn.addDevice(Integer.parseInt(goodUser.getUserId()), goodDevice);   
        assertNotNull(d.getDevId());
        Device d2 = conn.addDevice(Integer.parseInt(goodUser.getUserId()), goodDevice);   
        assertNotNull(d.getDevId());
        Collection<Device> devices = conn.getDevices(Integer.parseInt(goodUser.getUserId()));
        assertNotNull(devices);
        assertEquals(devices.size(), 3);
        assertTrue(devices.contains(goodDevice));
        assertTrue(devices.contains(d));
        assertTrue(devices.contains(d2));
    }
    
    /*
     * attempt to retrieve invalid device on invalid user
     * attempt to retrieve invlalid device on valid user
     * - expect null in both cases
     */
    @Test
    public void getInvalidDevice() {
        assertNull(conn.getDevice(invalidUserId, invalidDeviceId));
        assertNull(conn.getDevice(Integer.parseInt(goodUser.getUserId()), invalidDeviceId));
    }
    
    /*
     * change goodDevice description and update
     * - expect true
     */
    @Test
    public void updateDevice() {
        goodDevice.setDescription("Something else");
        assertTrue(conn.updateDevice(Integer.parseInt(goodUser.getUserId()), goodDevice));
    }
    
    /*
     * attempt to update a device, but pass in unconfigured device
     * - expect false
     */
    @Test
    public void updateInvalidDevice() {
        Device d = new Device();
        assertFalse(conn.updateDevice(Integer.parseInt(goodUser.getUserId()), d));
    }
   
    /*
     * delete a valid device from a valid user
     * - expect true
     */
    @Test
    public void deleteDevice() {
        assertTrue(conn.deleteDevice(Integer.parseInt(goodUser.getUserId()), Integer.parseInt(goodDevice.getDevId())));
    }
    
    /*
     * delete an invalid device from a valid user
     * delete a valid device from an invalid user
     * delete a valid device from a valid user - but wrong combination
     * - expect false in all cases
     */
    @Test
    public void deleteInvalidDevice() {
        assertFalse(conn.deleteDevice(Integer.parseInt(goodUser.getUserId()), invalidDeviceId));
        assertFalse(conn.deleteDevice(invalidUserId, Integer.parseInt(goodDevice.getDevId())));
        assertFalse(conn.deleteDevice(Integer.parseInt(goodDevice.getDevId()), Integer.parseInt(goodUser.getUserId())));
    }
}
