package org.chronopolis.ingest.repository.dao;

import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.JpaContext;
import org.chronopolis.rest.kot.entities.Node;
import org.chronopolis.rest.kot.entities.QNode;
import org.chronopolis.rest.kot.entities.depositor.Depositor;
import org.chronopolis.rest.kot.entities.depositor.DepositorContact;
import org.chronopolis.rest.kot.entities.depositor.QDepositor;
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
import java.util.List;

/**
 * Tests for common operations for a Depositor
 *
 * @author shake
 */
@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JpaContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DepositorDAOTest extends IngestTest {

    private final String ncar = "ncar";
    private final String sdsc = "sdsc";
    private final String ucsd = "ucsd";
    private final String umiacs = "umiacs";
    private final String namespace = "test-depositor";
    private final QDepositor qDepositor = QDepositor.depositor;

    @Autowired
    private EntityManager entityManager;

    private PagedDAO dao;

    @Before
    public void setup() {
        dao = new PagedDAO(entityManager);
    }

    @Test
    public void newDepositor() {
        // old depositor
        org.chronopolis.rest.entities.QDepositor old = org.chronopolis.rest.entities.QDepositor.depositor;
        QDepositor nu = QDepositor.depositor;

        // error... but why
        // depositor doesn't conform to PersistableEntity....
        dao.findAll(nu);

        Depositor depositor = new Depositor();
        depositor.setNamespace("new-namespace");
        depositor.setSourceOrganization("new-source-organization");
        depositor.setOrganizationAddress("new-organization-address");

        depositor.setContacts(new HashSet<>());
        depositor.getContacts().add(new DepositorContact("new-name", "new-phone", "new-email"));
        depositor.getContacts().add(new DepositorContact("new-name-2", "new-phone-2", "new-email-2"));
        // dao.findAll(QNode.node).forEach(depositor::addNodeDistribution);

        // dao.save(depositor);

        Depositor saved = dao.findOne(qDepositor, qDepositor.namespace.eq("new-namespace"));
        Assert.assertNotNull(saved);
        Assert.assertEquals("new-source-organization", saved.getSourceOrganization());
        Assert.assertEquals("new-organization-address", saved.getOrganizationAddress());
        Assert.assertEquals(4, saved.getNodeDistributions().size());
        Assert.assertEquals(2, saved.getContacts().size());
    }

    @Test
    public void testDeleteAllReplicatingNodes() {
        List<Node> all = dao.findAll(QNode.node, QNode.node.username.in(umiacs, ncar, sdsc));
        Depositor one = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        // todo: need to add convenience methods for adding and removing contacts/nodes
        all.forEach(one::removeNodeDistribution);
        dao.save(one);

        Depositor saved = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        Assert.assertEquals(0, saved.getNodeDistributions().size());
    }

    @Test
    public void testAddOneNode() {
        Depositor one = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        dao.save(one);
        Node ucsdNode = dao.findOne(QNode.node, QNode.node.username.eq(ucsd));
        one.addNodeDistribution(ucsdNode);
        dao.save(one);

        Depositor saved = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        Assert.assertEquals(4, saved.getNodeDistributions().size());
    }

    @Test
    public void testRemoveAndAddNodes() {
        Depositor one = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        dao.findAll(QNode.node,
                QNode.node.username.in(umiacs, ncar, sdsc))
                .forEach(one::removeNodeDistribution);
        dao.save(one);

        dao.findAll(QNode.node).forEach(one::addNodeDistribution);
        dao.save(one);

        Depositor saved = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        Assert.assertEquals(4, saved.getNodeDistributions().size());
    }

    @Test
    public void testAddContact() {
        DepositorContact contact = new DepositorContact("new-name", "new-phone", "new-email");
        Depositor one = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        one.addContact(contact);
        dao.save(one);

        Depositor saved = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        Assert.assertEquals(1, saved.getContacts().size());
        Assert.assertTrue(saved.getContacts().contains(contact));
    }

    @Test
    public void testAddContacts() {
        DepositorContact contact = new DepositorContact("new-name", "new-phone", "new-email");
        DepositorContact contact2 = new DepositorContact("new-name-2", "new-phone-2", "new-email-2");

        Depositor one = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        one.getContacts().add(contact);
        one.getContacts().add(contact2);

        Depositor saved = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        Assert.assertEquals(2, saved.getContacts().size());
        Assert.assertTrue(saved.getContacts().contains(contact));
        Assert.assertTrue(saved.getContacts().contains(contact2));
    }

}
