package org.chronopolis.rest.entities;

import com.google.common.collect.ImmutableSet;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.entities.depositor.QDepositor;
import org.chronopolis.rest.entities.storage.QStorageRegion;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.BagStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.HashSet;

/**
 * Oh boy
 *
 * @author shake
 */
@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JPAContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BagEntityTest {

    private final Long LONG_VALUE = 1L;
    private final String CREATOR = "bag-entity-test";
    private final String TEST_PATH = "test-path";

    @Autowired
    private EntityManager entityManager;

    private Node ncar;
    private Node umiacs;
    private Depositor depositor;
    private StorageRegion storageRegion;

    @Before
    @SuppressWarnings("Duplicates")
    public void initFromDb() {
        Assert.assertNotNull(entityManager);

        JPAQueryFactory qf = new JPAQueryFactory(entityManager);
        storageRegion = qf.selectFrom(QStorageRegion.storageRegion)
                .where(QStorageRegion.storageRegion.id.eq(1L))
                .fetchOne();
        depositor = qf.selectFrom(QDepositor.depositor)
                .where(QDepositor.depositor.namespace.eq("test-depositor"))
                .fetchOne();
        ncar = qf.selectFrom(QNode.node)
                .where(QNode.node.username.eq("ncar"))
                .fetchOne();
        umiacs = qf.selectFrom(QNode.node)
                .where(QNode.node.username.eq("umiacs"))
                .fetchOne();

        Assert.assertNotNull(storageRegion);
        Assert.assertNotNull(depositor);
    }

    @Test
    public void testBagPersistTests() {
        final String BAG_NAME = "test-bag-persist";

        JPAQueryFactory qf = new JPAQueryFactory(entityManager);
        Bag persist = new Bag();
        persist.setName(BAG_NAME);
        persist.setCreator(CREATOR);
        persist.setSize(LONG_VALUE);
        persist.setDepositor(depositor);
        persist.setTotalFiles(LONG_VALUE);
        persist.setStatus(BagStatus.DEPOSITED);

        StagingStorage bagStore =
                new StagingStorage(storageRegion, LONG_VALUE, LONG_VALUE, TEST_PATH, true);
        StagingStorage tokenStore =
                new StagingStorage(storageRegion, LONG_VALUE, LONG_VALUE, TEST_PATH, true);
        persist.setBagStorage(ImmutableSet.of(bagStore));
        persist.setTokenStorage(ImmutableSet.of(tokenStore));
        persist.setDistributions(new HashSet<>());
        persist.addDistribution(ncar, BagDistributionStatus.DISTRIBUTE);
        persist.addDistribution(umiacs, BagDistributionStatus.DEGRADED);

        entityManager.persist(persist);

        Bag fetch = qf.selectFrom(QBag.bag)
                .where(QBag.bag.name.eq(BAG_NAME))
                .fetchOne();

        Assert.assertNotNull(fetch);
        Assert.assertEquals(persist, fetch);
        Assert.assertNotEquals(0, persist.getId());
        Assert.assertEquals(1, fetch.getBagStorage().size());
        Assert.assertEquals(1, fetch.getTokenStorage().size());
        Assert.assertEquals(2, fetch.getDistributions().size());

    }

    @Test
    public void testBagMergeTests() {
        final String BAG_NAME = "test-bag-merge";

        JPAQueryFactory qf = new JPAQueryFactory(entityManager);
        Bag bag = new Bag();
        // set basic fields which need to be init
        // leave out bagStorage, tokenStorage, and distributions even though they're lateinits
        // in order to test if we can persist without setting them (we can as long as we refresh)
        bag.setName(BAG_NAME);
        bag.setCreator(CREATOR);
        bag.setSize(LONG_VALUE);
        bag.setDepositor(depositor);
        bag.setTotalFiles(LONG_VALUE);
        bag.setStatus(BagStatus.DEPOSITED);

        // persist + refresh just in case
        entityManager.persist(bag);
        entityManager.refresh(bag);

        // setup Staging entities to merge
        StagingStorage bagStore =
                new StagingStorage(storageRegion, LONG_VALUE, LONG_VALUE, TEST_PATH, true);
        StagingStorage bagStoreInactive =
                new StagingStorage(storageRegion, LONG_VALUE, LONG_VALUE, TEST_PATH, false);
        StagingStorage tokenStore =
                new StagingStorage(storageRegion, LONG_VALUE, LONG_VALUE, TEST_PATH, true);

        bag.getBagStorage().add(bagStore);
        bag.getBagStorage().add(bagStoreInactive);
        bag.getTokenStorage().add(tokenStore);
        bag.addDistribution(ncar, BagDistributionStatus.REPLICATE);
        bag.addDistribution(umiacs, BagDistributionStatus.DISTRIBUTE);
        entityManager.merge(bag);

        // fetch and asserts
        Bag fetchedBag = qf.selectFrom(QBag.bag)
                .where(QBag.bag.name.eq(BAG_NAME))
                .fetchOne();

        Assert.assertNotEquals(0L, bag.getId());
        Assert.assertNotNull(fetchedBag);
        Assert.assertEquals(bag, fetchedBag);
        Assert.assertEquals(2, fetchedBag.getBagStorage().size());
        Assert.assertEquals(1, fetchedBag.getTokenStorage().size());
        Assert.assertEquals(2, fetchedBag.getDistributions().size());
    }

}