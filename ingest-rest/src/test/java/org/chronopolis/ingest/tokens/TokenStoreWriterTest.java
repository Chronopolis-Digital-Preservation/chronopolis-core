package org.chronopolis.ingest.tokens;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.chronopolis.common.storage.Posix;
import org.chronopolis.common.storage.TokenStagingProperties;
import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.JpaContext;
import org.chronopolis.ingest.repository.dao.TokenDao;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.storage.Fixity;
import org.chronopolis.rest.entities.storage.QStagingStorage;
import org.chronopolis.rest.entities.storage.QStorageRegion;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.BagStatus;
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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.chronopolis.ingest.JpaContext.CREATE_SCRIPT;
import static org.chronopolis.ingest.JpaContext.DELETE_SCRIPT;
import static org.chronopolis.ingest.repository.dao.StagingDao.DISCRIMINATOR_TOKEN;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JpaContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = CREATE_SCRIPT),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = DELETE_SCRIPT)
})
public class TokenStoreWriterTest extends IngestTest {

    // Beans created by spring
    @Autowired private EntityManager entityManager;

    // Our search services which we need to create
    private TokenDao dao;

    private TokenStagingProperties properties;

    @Before
    public void setup() {
        dao = new TokenDao(entityManager);

        URL bags = ClassLoader.getSystemClassLoader().getResource("bags");
        properties = new TokenStagingProperties()
                .setPosix(new Posix().setId(1L).setPath(bags.getFile()));
    }

    private Bag findBag(String depositor, String name) {
        return dao.findOne(QBag.bag,
                QBag.bag.depositor.namespace.eq(depositor).and(QBag.bag.name.eq(name)));
    }

    @Test
    public void testWriteTokens() {
        final String name = "bag-3";
        final String depositor = "test-depositor";
        // Init the TokenStoreWriter and run it
        Bag b = findBag(depositor, name);
        StorageRegion region = dao.findOne(QStorageRegion.storageRegion,
                QStorageRegion.storageRegion.id.eq(properties.getPosix().getId()));
        String stage = properties.getPosix().getPath();
        TokenStoreWriter writer = new TokenStoreWriter(b, region, properties, dao);
        writer.run();

        // Refresh the bag from the db
        Bag updated = findBag(depositor, name);

        // pull the storage
        JPAQueryFactory qf = new JPAQueryFactory(entityManager);
        StagingStorage tokenStorage = qf.selectFrom(QStagingStorage.stagingStorage)
                .where(QStagingStorage.stagingStorage.bag.eq(updated)
                        .and(QStagingStorage.stagingStorage.file.dtype.eq(DISCRIMINATOR_TOKEN)))
                .fetchOne();

        Assert.assertNotNull(updated.getStorage());
        Assert.assertFalse(updated.getStorage().isEmpty());
        Assert.assertNotNull(tokenStorage);
        // fixity asserts
        Assert.assertNotNull(tokenStorage.getFile().getFixities());
        Assert.assertFalse(tokenStorage.getFile().getFixities().isEmpty());

        // path assert
        Path tokens = Paths.get(stage, tokenStorage.getPath());
        Assert.assertTrue(java.nio.file.Files.exists(tokens));
        Assert.assertEquals(tokens.toFile().length(), tokenStorage.getSize());
        Assert.assertEquals(1, tokenStorage.getTotalFiles());

        // combination fixity + path (check that it was recorded correctly)
        Set<Fixity> fixities = tokenStorage.getFile().getFixities();
        HashCode hash;
        try {
            hash = Files.asByteSource(tokens.toFile()).hash(Hashing.sha256());
            boolean fixityMatch = fixities.stream()
                    .anyMatch(fixity -> fixity.getValue().equalsIgnoreCase(hash.toString()));
            Assert.assertTrue(fixityMatch);
        } catch (IOException e) {
            // ughhhh
        }

        Assert.assertEquals(BagStatus.TOKENIZED, updated.getStatus());
    }
}