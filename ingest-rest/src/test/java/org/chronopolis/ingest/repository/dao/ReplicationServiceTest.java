package org.chronopolis.ingest.repository.dao;

import java.util.List;

import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.JpaContext;
import org.chronopolis.ingest.models.filter.BagFilter;
import org.chronopolis.ingest.models.filter.ReplicationFilter;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.projections.PartialBag;
import org.chronopolis.rest.entities.projections.ReplicationView;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.QNode;
import org.chronopolis.rest.entities.QReplication;
import org.chronopolis.rest.entities.Replication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

import static org.chronopolis.ingest.JpaContext.CREATE_SCRIPT;
import static org.chronopolis.ingest.JpaContext.DELETE_SCRIPT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JpaContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReplicationServiceTest extends IngestTest {

    @Autowired
    private EntityManager entityManager;

    private ReplicationDao service;

    @Before
    public void setup() {
        service = new ReplicationDao(entityManager);

        List<Node> nodes = service.findAll(QNode.node);
        List<Bag> bags = service.findAll(QBag.bag);

        for (Bag bag : bags) {
            for (Node node : nodes) {
                service.create(bag, node);
            }
        }
    }


    private final Logger log = LoggerFactory.getLogger(ReplicationServiceTest.class);

    @Test
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = CREATE_SCRIPT),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = DELETE_SCRIPT)
    })
    public void replicationGetPageTest() {
        ReplicationFilter filter = new ReplicationFilter();
        filter.setPageSize(new Long(3));
        filter.setPage(0);
        Page<Replication> page = service.findPage(QReplication.replication, filter);

        Assert.assertEquals(4, page.getTotalElements());

        long elemSize = page.getNumberOfElements();
        log.info("fetched first page size: {}", elemSize);
        Assert.assertEquals(3, elemSize);

        filter.setPage(1);
        page = service.findPage(QReplication.replication, filter);
        elemSize = page.getNumberOfElements();
        log.info("fetched second/last page size: {}", elemSize);
        Assert.assertEquals(1, elemSize);
    }
}