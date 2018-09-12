package org.chronopolis.ingest.support;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.JpaContext;
import org.chronopolis.ingest.repository.dao.PagedDAO;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.QBagFile;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JpaContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:sql/create.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:sql/delete.sql")
})
public class BagFileCSVProcessorTest extends IngestTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testReadValidCsv() throws URISyntaxException {
        final String BAG_NAME = "bag-2";
        final URL csvRoot = ClassLoader.getSystemClassLoader().getResource("csv");

        PagedDAO dao = new PagedDAO(entityManager);
        Bag bag = dao.findOne(QBag.bag, QBag.bag.name.eq(BAG_NAME));
        Path toCsv = Paths.get(csvRoot.toURI()).resolve("valid.csv");

        BagFileCSVProcessor processor = new BagFileCSVProcessor(dao);
        ResponseEntity response = processor.apply(bag.getId(), toCsv);

        JPAQueryFactory factory = new JPAQueryFactory(entityManager);
        long count = factory.selectFrom(QBagFile.bagFile)
                .where(QBagFile.bagFile.bag.name.eq(BAG_NAME))
                .fetchCount();

        // manifest + tagmanifest + 8 data files = 10
        Assert.assertEquals(10, count);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Ignore
    public void testReadInvalidCsv() throws URISyntaxException {
        final String BAG_NAME = "bag-2";
        final URL csvRoot = ClassLoader.getSystemClassLoader().getResource("csv");

        PagedDAO dao = new PagedDAO(entityManager);
        Bag bag = dao.findOne(QBag.bag, QBag.bag.name.eq(BAG_NAME));
        Path toCsv = Paths.get(csvRoot.toURI()).resolve("invalid.csv");

        BagFileCSVProcessor processor = new BagFileCSVProcessor(dao);
        processor.apply(bag.getId(), toCsv);

        JPAQueryFactory factory = new JPAQueryFactory(entityManager);
        long count = factory.selectFrom(QBagFile.bagFile)
                .where(QBagFile.bagFile.bag.name.eq(BAG_NAME))
                .fetchCount();

        // System.out.println(count);

        // manifest + tagmanifest + 8 data files = 10
        // Assert.assertEquals(10, count);
    }

}