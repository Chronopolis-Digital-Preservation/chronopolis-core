package org.chronopolis.ingest.tokens;

import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.JpaContext;
import org.chronopolis.ingest.repository.dao.PagedDao;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.serializers.BagSerializer;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.tokenize.ManifestEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;

import static org.chronopolis.ingest.JpaContext.CREATE_SCRIPT;
import static org.chronopolis.ingest.JpaContext.DELETE_SCRIPT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * Test the DatabasePredicate to ensure filtering works correctly when retrieving work
 *
 * @author shake
 */
@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JpaContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = CREATE_SCRIPT),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = DELETE_SCRIPT)
})
public class DatabasePredicateTest extends IngestTest {

    @Autowired
    private EntityManager entityManager;

    private PagedDao dao;
    private DatabasePredicate predicate;
    private BagSerializer serializer;

    @Before
    public void setup() {
        dao = new PagedDao(entityManager);
        predicate = new DatabasePredicate(dao);
        serializer = new BagSerializer();
    }

    @Test
    public void test() {
        final String digest = "test-digest";
        final String fileExists = "/manifest-sha256.txt";
        final String fileNotExists = "/data/hello_other_world";
        org.chronopolis.rest.entities.Bag be = dao.findOne(QBag.bag, QBag.bag.name.eq("bag-3"));

        final Bag bag = serializer.modelFor(be);
        // an unfortunate side effect of immutability + java :(
        final Bag invalidId = bag.copy(999L,
                bag.getSize(),
                bag.getTotalFiles(),
                bag.getBagStorage(),
                bag.getTokenStorage(),
                bag.getCreatedAt(),
                bag.getUpdatedAt(),
                bag.getName(),
                bag.getCreator(),
                bag.getDepositor(),
                bag.getStatus(),
                bag.getReplicatingNodes());

        ManifestEntry exists = new ManifestEntry(bag, fileExists, digest);
        ManifestEntry bagNotExists = new ManifestEntry(invalidId, fileNotExists, digest);
        ManifestEntry tokenNotExists = new ManifestEntry(bag, fileNotExists, digest);

        Assert.assertFalse(predicate.test(exists));
        Assert.assertFalse(predicate.test(bagNotExists));
        Assert.assertTrue(predicate.test(tokenNotExists));
    }

    @Test
    public void testNullChecks() {
        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(new ManifestEntry(null, "file", "test-digest")));
    }
}