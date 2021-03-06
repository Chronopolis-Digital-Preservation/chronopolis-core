package org.chronopolis.ingest.repository.dao;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.chronopolis.ingest.exception.NotFoundException;
import org.chronopolis.ingest.models.filter.ReplicationFilter;
import org.chronopolis.ingest.support.ReplicationCreateResult;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.BagDistribution;
import org.chronopolis.rest.entities.BagDistributionStatus;
import org.chronopolis.rest.entities.BagFile;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.QBagDistribution;
import org.chronopolis.rest.entities.QDataFile;
import org.chronopolis.rest.entities.QNode;
import org.chronopolis.rest.entities.QReplication;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.TokenStore;
import org.chronopolis.rest.entities.depositor.QDepositor;
import org.chronopolis.rest.entities.projections.ReplicationView;
import org.chronopolis.rest.entities.storage.QStagingStorage;
import org.chronopolis.rest.entities.storage.ReplicationConfig;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.models.create.ReplicationCreate;
import org.chronopolis.rest.models.enums.ReplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.repository.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.chronopolis.ingest.repository.dao.StagingDao.DISCRIMINATOR_BAG;
import static org.chronopolis.ingest.repository.dao.StagingDao.DISCRIMINATOR_TOKEN;

public class ReplicationDao extends PagedDao {

    private static final String DEFAULT_USER = "chronopolis";

    private final Logger log = LoggerFactory.getLogger(ReplicationDao.class);

    public ReplicationDao(EntityManager em) {
        super(em);
    }

    /**
     * Create a {@link Replication} based on the {@code bagId} and {@code nodeId} in order to find
     * the {@link Bag} and {@link Node} to distribute data to.
     *
     * If either the {@code bagId} or {@code nodeId} do not exist, throw a {@link NotFoundException}
     *
     * The {@link Bag} must have {@link StagingStorage} created for both the {@link BagFile} and
     * {@link TokenStore} which are staged for distribution. If neither exist, a
     * {@link ReplicationCreateResult} will be returned with {@link ReplicationCreateResult#errors}
     * filled out for the appropriate error.
     *
     * @param bagId  the id of the bag to replicate
     * @param nodeId the id of the node to replicate to
     * @return the newly created replication
     * @throws NotFoundException if the bag or node do not exist
     */
    public ReplicationCreateResult create(Long bagId, Long nodeId) {
        log.info("Processing replication create request for Bag {}", bagId);

        // Get our db resources
        Bag bag = findOne(QBag.bag, QBag.bag.id.eq(bagId));
        Node node = findOne(QNode.node, QNode.node.id.eq(nodeId));

        // todo: ReplicationCreateResult instead of NotFoundException?
        if (bag == null) {
            throw new NotFoundException("Bag " + bagId);
        } else if (node == null) {
            throw new NotFoundException("Node " + nodeId);
        }

        return create(bag, node);
    }

    /**
     * Create a new replication for the Node (user) based on the Bag Id
     * If a replication already exists (and is not terminated), return it instead of creating a new one
     *
     * @param request The request to create a replication for
     * @return the newly created replication
     * @throws NotFoundException if the bag or node do not exist
     */
    public ReplicationCreateResult create(ReplicationCreate request) {
        return create(request.getBagId(), request.getNodeId());
    }

    /**
     * Create a replication with a Bag and Node which have already been pulled from the DB.
     * If parameters are met for bag staging storage, continue on by passing along information to
     * a function which will query for token staging storage and continue the replication create process.
     * If active bag storage does not exist or does not have any associated fixity values, return
     * a ReplicationCreateResult with errors outlining the problems.
     *
     * @param bag  The bag to create a replication for
     * @param node The node to send the replication to
     * @return the result of creating the replication
     */
    public ReplicationCreateResult create(final Bag bag, final Node node) {
        Optional<StagingStorage> bagStorage = queryStorage(bag.getId(), DISCRIMINATOR_BAG);
        return bagStorage.filter(staging -> !staging.getFile().getFixities().isEmpty())
                .map(staging -> createReplicationString(staging, true))
                .map(staging -> withBagStorage(bag, node, staging))
                .orElseGet(() -> new ReplicationCreateResult(ImmutableList
                        .of("Problem with BagStorage. Either no active storage or fixities.")));
    }

    /**
     * Complete creation of a Replication with active Bag storage. If active token storage
     * does not exist or does not have an associated fixity values, return a ReplicationCreateResult
     * with errors outlining the problems.
     *
     * @param bag     the bag being replicated
     * @param node    the node receiving the replication
     * @param bagLink the link for replicating the bag
     * @return the result of creating the replication
     */
    private ReplicationCreateResult withBagStorage(Bag bag, Node node, String bagLink) {
        return queryStorage(bag.getId(), DISCRIMINATOR_TOKEN)
                .filter(staging -> !staging.getFile().getFixities().isEmpty())
                .map(staging -> createReplicationString(staging, false))
                .map(tokenLink -> withTokenStorage(bag, node, bagLink, tokenLink))
                .orElseGet(() -> new ReplicationCreateResult(ImmutableList
                        .of("Problem with TokenStorage. Either no active storage or fixities.")));
    }

    /**
     * Complete creation of a Replication, with active Bag and Token storage
     *
     * @param bag       the bag being replicated
     * @param node      the node receiving the replication
     * @param bagLink   the link for replicating the bag
     * @param tokenLink the link for replicating the token store
     * @return the result of creating the replication
     */
    private ReplicationCreateResult withTokenStorage(Bag bag,
                                                     Node node,
                                                     String bagLink,
                                                     String tokenLink) {
        createDist(bag, node);
        List<Replication> ongoing = findAll(QReplication.replication,
                QReplication.replication.bag.id.eq(bag.getId())
                        .and(QReplication.replication.node.username.eq(node.getUsername())
                                .and(QReplication.replication.status.in(ReplicationStatus.Companion.active()))));

        Replication action = new Replication(ReplicationStatus.PENDING,
                node, bag, bagLink, tokenLink, "rsync", null, null);

        // So... the protocol field needs to be looked at during the next update
        // basically we have a field which is authoritative for both links, even though the
        // bag and token are likely in different staging areas. Either we'll want separate
        // protocol fields, separate replications, or some other way of doing this. Needs thinking.
        action.setProtocol("rsync");

        // iterate through our ongoing replications and search for a non terminal replication
        // Partial index this instead?
        //       create unique index "one_repl" on replications(node_id) where status == ''...
        if (ongoing.size() != 0) {
            for (Replication replication : ongoing) {
                ReplicationStatus status = replication.getStatus();
                if (status.isOngoing()) {
                    log.info("Found ongoing replication for {} to {}, ignoring create request",
                            bag.getName(), node.getUsername());
                    action = replication;
                }
            }
        } else {
            log.info("Created new replication request for {} to {}",
                    bag.getName(), node.getUsername());
        }

        save(action);
        return new ReplicationCreateResult(action);
    }


    /**
     * Retrieve a StagingStorage entity for a bag
     *
     * @param bagId         the id of the bag
     * @param discriminator the discriminator to join on (either BAG or TOKEN_STORE)
     * @return the StagingStorage entity, wrapped in an Optional in the event none exist
     */
    private Optional<StagingStorage> queryStorage(Long bagId, String discriminator) {
        log.trace("[Bag-{}] Querying storage", bagId);
        QBag b = QBag.bag;
        QStagingStorage storage = QStagingStorage.stagingStorage;

        /*
         * The query we want to mimic
         * SELECT s.id, s.path, s.size, ...
         * FROM staging_storage s
         *   JOIN bag_storage AS bs
         *   ON bs.staging_id = s.id AND bs.bag_id = 12
         * WHERE s.active = 't';
         *
         * Maybe there's a way to do it without the join? All we're doing is getting the staging_storage...
         * SELECT s.id, s.path, s.size, ...
         * FROM staging_storage s
         * WHERE s.active = 't' AND s.id = (SELECT staging_id FROM bag_storage AS b WHERE b.bag_id = ?1);
         *
         * what we end up with seems like a pretty suboptimal query; if needed we can look into it
         * might be easier to execute native sql than fiddle with querydsl in that case
         */
        JPAQueryFactory factory = getJPAQueryFactory();
        JPAQuery<StagingStorage> query = factory.from(b)
                .innerJoin(b.storage, storage)
                .where(storage.bag.id.eq(bagId)
                        .and(storage.active.isTrue()
                        .and(storage.file.dtype.eq(discriminator))))
                .select(storage);

        return Optional.ofNullable(query.fetchFirst());
    }

    /**
     * Build a string for replication based off the storage for the object
     * <p>
     *
     * @param storage The storage to replication from
     * @return The string for the replication
     */
    public static String createReplicationString(StagingStorage storage, Boolean trailingSlash) {
        ReplicationConfig config;

        storage.getRegion();
        config = storage.getRegion().getReplicationConfig();

        final String user = config.getUsername() != null ? config.getUsername() : DEFAULT_USER;
        final String server = config.getServer();
        final String root = config.getPath();

        Path path = Paths.get(root, storage.getPath());
        // inline this?
        return buildLink(user, server, path, trailingSlash);
    }

    /**
     * Get or create the BagDistribution for a node
     *
     * @param bag  the bag being distributed
     * @param node the node being distributed to
     */
    private void createDist(Bag bag, Node node) {
        BagDistribution bagDistribution;
        QBagDistribution qBagDist = QBagDistribution.bagDistribution;
        bagDistribution = findOne(qBagDist, qBagDist.bag.eq(bag).and(qBagDist.node.eq(node)));

        if (bagDistribution == null) {
            bag.addDistribution(node, BagDistributionStatus.DISTRIBUTE);
            // not sure if this is the best place for this...
            save(bag);
        }
    }

    private static String buildLink(String user, String server, Path file, Boolean trailingSlash) {
        return user +
                "@" + server +
                ":" + file.toString() + (trailingSlash ? "/" : "");
    }

    /**
     * Query the database for a {@link Replication} projected on to a {@link ReplicationView}
     *
     * @param id the id of the {@link Replication}
     * @return the {@link ReplicationView} projection
     */
    public ReplicationView findReplicationAsView(Long id) {
        QReplication replication = QReplication.replication;
        // not a fan of transforming this into a map then getting the id but.. not sure how to
        // do it otherwise
        return createViewQuery()
                .where(QReplication.replication.id.eq(id))
                .transform(GroupBy.groupBy(replication.id).as(replicationProjection()))
                .get(id);
    }

    /**
     * Query the database for a set of {@link Replication}s and project the results on to
     * {@link ReplicationView}s.
     *
     * @param filter the {@link ReplicationFilter} containing the query parameters
     * @return a {@link Page} containing the results of the query
     */
    public Page<ReplicationView> findViewsAsPage(ReplicationFilter filter) {
        QReplication replication = QReplication.replication;
        JPAQuery<?> query = createViewQuery()
                .where(filter.getQuery())
                .orderBy(filter.getOrderSpecifier());
        JPAQuery<Replication> count = getJPAQueryFactory()
                .selectFrom(replication)
                .where(filter.getQuery());

        List<ReplicationView> replications = query.transform(GroupBy.groupBy(replication.id).list(replicationProjection()));

        int page = filter.getPage();
        long pageSize = filter.getPageSize();

        List<ReplicationView> pagedResults = new ArrayList<>();
        if (page*pageSize < replications.size()) {
            long fromIndex = page*pageSize;
            long toIndex = fromIndex + pageSize > replications.size() ? replications.size() : fromIndex + pageSize;

            pagedResults.addAll(replications.subList((int)fromIndex, (int)toIndex));
        }

        return PageableExecutionUtils.getPage(
                pagedResults,
                filter.createPageRequest(),
                count::fetchCount);
    }

    private JPAQuery<?> createViewQuery() {
        QReplication replication = QReplication.replication;
        QBag bag = QBag.bag;
        QNode node = QNode.node;
        QNode distributionNode = new QNode(DISTRIBUTION_IDENTIFIER);
        QBagDistribution distribution = QBagDistribution.bagDistribution;
        QStagingStorage staging = QStagingStorage.stagingStorage;
        QDataFile file = QDataFile.dataFile;
        return getJPAQueryFactory().from(replication)
                .innerJoin(replication.node, node)
                // CompleteBag projection joins
                .innerJoin(replication.bag, bag)
                .innerJoin(bag.depositor, QDepositor.depositor)
                .leftJoin(bag.distributions, distribution)
                .on(distribution.status.eq(BagDistributionStatus.REPLICATE))
                .leftJoin(distribution.node, distributionNode)
                .leftJoin(bag.storage, staging)
                .on(staging.active.isTrue())
                .leftJoin(staging.file, file);
    }
}
