package org.chronopolis.ingest.api;

import org.chronopolis.ingest.repository.criteria.SearchCriteria;
import org.chronopolis.ingest.repository.dao.ReplicationService;
import org.chronopolis.ingest.repository.dao.StagingService;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.storage.Fixity;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.FixityUpdate;
import org.chronopolis.rest.models.RStatusUpdate;
import org.chronopolis.rest.models.ReplicationStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the replication controller
 * <p>
 * Things we need to setup before hand:
 * - Bag for the replication
 * - Node who is replicating
 * - Possibly node who does not own the replication
 * - SecurityContext (probably a better way to handle this but the static method usage kind of hurts us)
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ReplicationController.class)
public class ReplicationControllerTest extends ControllerTest {

    private final String CORRECT_TAG_FIXITY = "tag-fixity";
    private final String CORRECT_TOKEN_FIXITY = "token-fixity";
    private final String INVALID_FIXITY = "fxity";

    private ReplicationController controller;

    @MockBean private StagingService staging;
    @MockBean private ReplicationService service;

    @Before
    public void setup() {
        controller = new ReplicationController(staging, service);
        setupMvc(controller);
    }

    @Test
    public void testReplications() throws Exception {
        when(service.findAll(any(SearchCriteria.class), any(Pageable.class))).thenReturn(null);
        mvc.perform(get("/api/replications?node=umiacs").principal(authorizedPrincipal))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    public void testFindReplication() throws Exception {
        when(service.find(any(SearchCriteria.class))).thenReturn(new Replication(node(), bag()));
        mvc.perform(get("/api/replications/{id}", 4L).principal(authorizedPrincipal))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    public void testCorrectUpdate() throws Exception {
        when(staging.activeStorageForBag(any(Bag.class), eq(QBag.bag.tokenStorage)))
                .thenReturn(Optional.of(stagingStorage(CORRECT_TOKEN_FIXITY)));
        setupPut("/api/replications/{id}/tokenstore", 4L, new FixityUpdate(CORRECT_TOKEN_FIXITY))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.receivedTokenFixity").value(CORRECT_TOKEN_FIXITY));
    }

    @Test
    public void testClientUpdate() throws Exception {
        setupPut("/api/replications/{id}/status", 4L, new RStatusUpdate(ReplicationStatus.STARTED))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.status").value("STARTED"));
    }

    @Test
    public void testInvalidTokenFixity() throws Exception {
        when(staging.activeStorageForBag(any(Bag.class), eq(QBag.bag.tokenStorage)))
                .thenReturn(Optional.of(stagingStorage(CORRECT_TAG_FIXITY)));
        setupPut("/api/replications/{id}/tokenstore", 4L, new FixityUpdate(INVALID_FIXITY))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.status").value("FAILURE_TOKEN_STORE"))
                .andExpect(jsonPath("$.receivedTokenFixity").value(INVALID_FIXITY));
    }

    @Test
    public void testInvalidTagFixity() throws Exception {
        when(staging.activeStorageForBag(any(Bag.class), eq(QBag.bag.bagStorage)))
                .thenReturn(Optional.of(stagingStorage(CORRECT_TOKEN_FIXITY)));
        setupPut("/api/replications/{id}/tagmanifest", 4L, new FixityUpdate(INVALID_FIXITY))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.status").value("FAILURE_TAG_MANIFEST"))
                .andExpect(jsonPath("$.receivedTagFixity").value(INVALID_FIXITY));
    }

    public <T> ResultActions setupPut(String uri, Long id, T obj) throws Exception {
        when(service.find(any(SearchCriteria.class))).thenReturn(new Replication(node(), bag()));
        authenticateUser();
        return mvc.perform(
                put(uri, id)
                        .with(user(user)) // any way to streamline some of this I wonder?
                        .principal(authorizedPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(obj)))
                .andDo(print());
    }

    private Bag bag() {
        StorageRegion region = new StorageRegion();
        region.setId(1L);
        Bag bag = new Bag("test-bag", "test-depositor");
        bag.setId(1L);
        bag.setBagStorage(new StagingStorage().addFixity(
                new Fixity().setAlgorithm("test-algorithm")
                        .setValue(CORRECT_TAG_FIXITY)
                        .setCreatedAt(ZonedDateTime.now()))
                .setRegion(region));
        bag.setTokenStorage(new StagingStorage().addFixity(
                new Fixity().setAlgorithm("test-algorithm")
                        .setValue(CORRECT_TOKEN_FIXITY)
                        .setCreatedAt(ZonedDateTime.now()))
                .setRegion(region));
        return bag;
    }

    private StagingStorage stagingStorage(String value) {
        return new StagingStorage().addFixity(
                new Fixity().setAlgorithm("test-algorithm")
                .setValue(value)
                .setCreatedAt(ZonedDateTime.now()))
        ;
    }


    private Node node() {
        return new Node("user", "password");
    }

}
