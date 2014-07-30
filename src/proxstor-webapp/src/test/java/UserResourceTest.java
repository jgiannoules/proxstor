/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import com.giannoules.proxstor.ProxStorApp;
import com.giannoules.proxstor.user.User;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author James_Giannoules
 */
public class UserResourceTest extends JerseyTest {
    
    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return ProxStorApp.createApp();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(ProxStorApp.createMoxyJsonResolver());
    }

    @Test
    public void testGet() {
        final WebTarget target = target("users");
        final User testUser = target.request(MediaType.APPLICATION_JSON_TYPE).get(User.class);

        assertEquals(testUser, null);
    }

    @Test
    public void roundTripTest() {
        final WebTarget target = target("users");
        final User testUser = target.
                request(MediaType.APPLICATION_JSON_TYPE).
                post(Entity.entity(new User("first", "last", "address"), MediaType.APPLICATION_JSON_TYPE), User.class);

        assertEquals(testUser, new User("first", "last", "address"));
    }
}
