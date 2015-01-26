package org.chronopolis.ingest.api;

import org.chronopolis.ingest.ChronPackager;
import org.chronopolis.ingest.IngestSettings;
import org.chronopolis.ingest.exception.NotFoundException;
import org.chronopolis.ingest.repository.BagRepository;
import org.chronopolis.ingest.repository.NodeRepository;
import org.chronopolis.ingest.repository.ReplicationRepository;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.BagStatus;
import org.chronopolis.rest.models.IngestRequest;
import org.chronopolis.rest.models.Node;
import org.chronopolis.rest.models.Replication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Map;

import static org.chronopolis.ingest.api.Params.PAGE;
import static org.chronopolis.ingest.api.Params.PAGE_SIZE;

/**
 * Created by shake on 11/5/14.
 */
@RestController
@RequestMapping("/api")
public class StagingController {
    Logger log = LoggerFactory.getLogger(StagingController.class);

    @Autowired
    BagRepository bagRepository;

    @Autowired
    NodeRepository nodeRepository;

    @Autowired
    ReplicationRepository replicationRepository;

    @Autowired
    IngestSettings ingestSettings;

    @RequestMapping(value = "bags", method = RequestMethod.GET)
    public Iterable<Bag> getBags(Principal principal,
                                 @RequestParam Map<String, String> params) {
        Integer pageNum = params.containsKey(PAGE)
                ? Integer.parseInt(params.get(PAGE))
                : -1;
        Integer pageSize = params.containsKey(PAGE_SIZE)
                ? Integer.parseInt(params.get(PAGE_SIZE))
                : 20;

        if (pageNum != -1) {
            return bagRepository.findAll(new PageRequest(pageNum, pageSize));
        }

        return bagRepository.findByStatus(BagStatus.STAGED);
    }

    @RequestMapping(value = "bags/{bag-id}", method = RequestMethod.GET)
    public Bag getBag(Principal principal, @PathVariable("bag-id") Long bagId) {
        Bag bag = bagRepository.findOne(bagId);
        if (bag == null) {
            throw new NotFoundException("bag/" + bagId);
        }
        return bag;
    }

    @RequestMapping(value = "bags", method = RequestMethod.PUT)
    public Bag stageBag(Principal principal, @RequestBody IngestRequest request) {
        String name = request.getName();
        String depositor = request.getDepositor();

        // First check if the bag exists
        Bag bag = bagRepository.findByNameAndDepositor(name, depositor);

        if (bag != null) {
            log.debug("Bag {} exists from depositor {}, skipping creation", name, depositor);
            return bag;
        }

        log.debug("Creating bag {} for depositor {}", name, depositor);
        // If not, create the bag + tokens, then save it
        ChronPackager packager = new ChronPackager(request.getName(),
                request.getLocation(),
                request.getDepositor(),
                ingestSettings);
        bag = packager.packageForChronopolis();
        bagRepository.save(bag);

        Path bagPath = Paths.get(ingestSettings.getBagStage(),
                                 bag.getLocation());
        Path tokenPath = Paths.get(ingestSettings.getTokenStage(),
                                   bag.getTokenLocation());

        // Set up where nodes will pull from
        String user = ingestSettings.getExternalUser();
        String server = ingestSettings.getStorageServer();
        String tokenStore = new StringBuilder(user)
                .append("@").append(server)
                .append(":").append(tokenPath.toString())
                .toString();
        String bagLocation = new StringBuilder(user)
                .append("@").append(server)
                .append(":").append(bagPath.toString())
                .toString();


        for (Node node : nodeRepository.findAll()) {
            log.trace("Creating replication object for {}", node.getUsername());
            Replication replication = new Replication(node, bag, bagLocation, tokenStore);
            replication.setProtocol("rsync");
            replicationRepository.save(replication);
        }

        return bag;
    }

}