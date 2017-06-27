package org.chronopolis.ingest.repository.dao;

import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.TestApplication;
import org.chronopolis.ingest.models.UserRequest;
import org.chronopolis.ingest.models.UserRole;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test methods in the user service to ensure correct behavior
 *
 * Created by shake on 6/11/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class UserServiceTest extends IngestTest {

    private final String EXISTING = "umiacs";
    private final String NEW_USER = "new-user";
    private final String PASSWORD = "new-password";

    @Autowired
    UserService service;

    @Test
    public void testCreateExistingUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setRole(UserRole.ROLE_USER);
        request.setNode(false);
        request.setUsername(EXISTING);
        request.setPassword(PASSWORD);
        service.createUser(request);

        UserDetails details = service.getUser(EXISTING);
        Assert.assertNotEquals(PASSWORD, details.getPassword());
    }

    @Test
    public void testCreateNewUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setRole(UserRole.ROLE_USER);
        request.setNode(false);
        request.setUsername(NEW_USER);
        request.setPassword(PASSWORD);
        service.createUser(request);

        UserDetails details = service.getUser(NEW_USER);
        Assert.assertNotNull(details);
        Assert.assertEquals(NEW_USER, details.getUsername());
    }
}