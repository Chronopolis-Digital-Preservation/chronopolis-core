package org.chronopolis.ingest.api;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.ingest.IngestController;
import org.chronopolis.ingest.models.RegionCreate;
import org.chronopolis.ingest.models.filter.StorageRegionFilter;
import org.chronopolis.ingest.repository.NodeRepository;
import org.chronopolis.ingest.repository.StorageRegionRepository;
import org.chronopolis.ingest.repository.criteria.StorageRegionSearchCriteria;
import org.chronopolis.ingest.repository.dao.SearchService;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.storage.ReplicationConfig;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;

/**
 * API methods to query StorageRegions
 *
 * Created by shake on 7/11/17.
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController extends IngestController {
    private final Logger log = LoggerFactory.getLogger(StorageController.class);

    private NodeRepository nodes;
    private SearchService<StorageRegion, Long, StorageRegionRepository> service;

    @Autowired
    public StorageController(NodeRepository nodes, SearchService<StorageRegion, Long, StorageRegionRepository> service) {
        this.nodes = nodes;
        this.service = service;
    }

    /**
     * Retrieve a StorageRegion by its id
     *
     * @param id the id of the StorageRegion
     * @return the StorageRegion
     */
    @GetMapping("{id}")
    public StorageRegion getRegion(@PathVariable("id") Long id) {
        StorageRegionSearchCriteria criteria = new StorageRegionSearchCriteria();
        criteria.withId(id);
        return service.find(criteria);
    }

    /**
     * Retrieve all StorageRegions
     *
     * @param filter The query parameters to filter on
     * @return all StorageRegions
     */
    @GetMapping
    public Page<StorageRegion> getRegions(@RequestAttribute StorageRegionFilter filter) {
        StorageRegionSearchCriteria criteria = new StorageRegionSearchCriteria()
                .withType(filter.getType())
                .withNodeName(filter.getName())
                .withCapacityLessThan(filter.getCapacityLess())
                .withCapacityGreaterThan(filter.getCapacityGreater());

        // blehh we should really just create our own PageRequest since we can
        return service.findAll(criteria, createPageRequest(ImmutableMap.of("page", filter.getPage().toString()), new HashMap<>()));
    }

    /**
     * Create a StorageRegion for a node
     *
     * todo: 404 if the node does not exist
     * todo: some type of identifier (local??) for storage regions?
     *       should this be included in the create call?
     * todo: test persistence of ReplicationConfig
     *
     * @param create the request containing the information about the SR
     * @return the newly created StorageRegion
     */
    @PostMapping
    public ResponseEntity<StorageRegion> createRegion(Principal principal, @RequestBody RegionCreate create) {
        ResponseEntity<StorageRegion> entity = ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();

        // Good enough I suppose
        if (hasRoleAdmin() || principal.getName().equalsIgnoreCase(create.getNode())) {
            // Can throw a 404
            Node node = nodes.findByUsername(create.getNode());

            StorageRegion region = new StorageRegion();
            region.setCapacity(create.getCapacity())
                    .setNode(node)
                    .setType(create.getType())
                    .setReplicationConfig(new ReplicationConfig()
                            .setPath(create.getReplicationPath())
                            .setServer(create.getReplicationServer())
                            .setUsername(create.getReplicationUser()));
            service.save(region);
            entity = ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(region);
        }

        return entity;
    }

}
