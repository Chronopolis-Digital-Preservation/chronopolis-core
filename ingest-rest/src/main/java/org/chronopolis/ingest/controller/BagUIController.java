package org.chronopolis.ingest.controller;

import org.chronopolis.ingest.IngestController;
import org.chronopolis.ingest.PageWrapper;
import org.chronopolis.ingest.exception.BadRequestException;
import org.chronopolis.ingest.exception.ForbiddenException;
import org.chronopolis.ingest.exception.NotFoundException;
import org.chronopolis.ingest.models.BagUpdate;
import org.chronopolis.ingest.models.ReplicationCreate;
import org.chronopolis.ingest.models.filter.BagFilter;
import org.chronopolis.ingest.models.filter.ReplicationFilter;
import org.chronopolis.ingest.repository.NodeRepository;
import org.chronopolis.ingest.repository.StorageRegionRepository;
import org.chronopolis.ingest.repository.TokenRepository;
import org.chronopolis.ingest.repository.criteria.BagSearchCriteria;
import org.chronopolis.ingest.repository.criteria.ReplicationSearchCriteria;
import org.chronopolis.ingest.repository.criteria.StorageRegionSearchCriteria;
import org.chronopolis.ingest.repository.dao.BagService;
import org.chronopolis.ingest.repository.dao.ReplicationService;
import org.chronopolis.ingest.repository.dao.SearchService;
import org.chronopolis.ingest.support.ReplicationCreateResult;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.storage.Fixity;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.BagStatus;
import org.chronopolis.rest.models.IngestRequest;
import org.chronopolis.rest.models.ReplicationRequest;
import org.chronopolis.rest.models.ReplicationStatus;
import org.chronopolis.rest.models.storage.FixityCreate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for handling bag/replication related requests
 * <p>
 * Created by shake on 4/17/15.
 */
@Controller
public class BagUIController extends IngestController {
    private final Logger log = LoggerFactory.getLogger(BagUIController.class);
    private final Logger access = LoggerFactory.getLogger("access-log");
    private final Integer DEFAULT_PAGE_SIZE = 20;
    private final Integer DEFAULT_PAGE = 0;

    private final BagService bagService;
    private final ReplicationService replicationService;
    private final TokenRepository tokenRepository;
    private final NodeRepository nodeRepository;
    private final SearchService<StorageRegion, Long, StorageRegionRepository> regions;

    @Autowired
    public BagUIController(BagService bagService,
                           ReplicationService replicationService,
                           TokenRepository tokenRepository,
                           NodeRepository nodeRepository,
                           SearchService<StorageRegion, Long, StorageRegionRepository> regions) {
        this.bagService = bagService;
        this.replicationService = replicationService;
        this.tokenRepository = tokenRepository;
        this.nodeRepository = nodeRepository;
        this.regions = regions;
    }

    /**
     * Retrieve information about all bags
     *
     * @param model     - the view model
     * @param principal - authentication information
     * @return page listing all bags
     */
    @RequestMapping(value = "/bags", method = RequestMethod.GET)
    public String getBags(Model model, Principal principal,
                          @ModelAttribute(value = "filter") BagFilter filter) {
        access.info("[GET /bags] - {}", principal.getName());

        BagSearchCriteria criteria = new BagSearchCriteria()
                .nameLike(filter.getName())
                .depositorLike(filter.getDepositor())
                .withStatuses(filter.getStatus());

        Sort.Direction direction = (filter.getDir() == null) ? Sort.Direction.ASC : Sort.Direction.fromStringOrNull(filter.getDir());
        Sort s = new Sort(direction, filter.getOrderBy());
        Page<Bag> bags = bagService.findAll(criteria, new PageRequest(filter.getPage(), DEFAULT_PAGE_SIZE, s));

        PageWrapper<Bag> pages = new PageWrapper<>(bags, "/bags", filter.getParameters());
        model.addAttribute("bags", bags);
        model.addAttribute("pages", pages);
        model.addAttribute("statuses", BagStatus.statusByGroup());

        return "bags";
    }

    /**
     * Get information about a single bag
     *
     * @param model - the view model
     * @param id    - the id of the bag
     * @return page showing the individual bag
     */
    @RequestMapping(value = "/bags/{id}", method = RequestMethod.GET)
    public String getBag(Model model, Principal principal, @PathVariable("id") Long id) {
        access.info("[GET /bags/{}] - {}", id, principal.getName());

        BagSearchCriteria bsc = new BagSearchCriteria().withId(id);
        ReplicationSearchCriteria rsc = new ReplicationSearchCriteria().withBagId(id);

        model.addAttribute("bag", bagService.find(bsc));
        model.addAttribute("replications", replicationService.findAll(rsc,
                new PageRequest(DEFAULT_PAGE, DEFAULT_PAGE_SIZE)));
        model.addAttribute("statuses", Arrays.asList(BagStatus.values()));
        model.addAttribute("tokens", tokenRepository.countByBagId(id));

        return "bag";
    }

    /**
     * Handler for updating a bag
     * <p>
     * todo: constraint on updating the bag as a non-admin
     * todo: tostring for BagUpdate
     *
     * @param model  - the viewmodel
     * @param id     - id of the bag to update
     * @param update - the updated information
     * @return page showing the individual bag
     */
    @RequestMapping(value = "/bags/{id}", method = RequestMethod.POST)
    public String updateBag(Model model, Principal principal, @PathVariable("id") Long id, BagUpdate update) {
        access.info("[POST /bags/{}] - {}", id, principal.getName());
        access.info("POST parameters - {};{}", update.getLocation(), update.getStatus());

        BagSearchCriteria criteria = new BagSearchCriteria().withId(id);
        Bag bag = bagService.find(criteria);
        bag.setStatus(update.getStatus());
        bagService.save(bag);

        model.addAttribute("bags", bag);
        model.addAttribute("statuses", Arrays.asList(BagStatus.values()));
        model.addAttribute("tokens", tokenRepository.countByBagId(id));

        return "bag";
    }

    /**
     * Invert the active flag for Storage in a Bag
     *
     * @param principal the principal of the user
     * @param id        the id of the bag
     * @param storageId the id of the stagingStorage
     * @return the bag
     */
    @GetMapping("/bags/{id}/storage/{storageId}/activate")
    public String updateBagStorage(Principal principal,
                                   @PathVariable("id") Long id,
                                   @PathVariable("storageId") Long storageId) throws ForbiddenException {
        access.info("[GET /bags/{}/storage/{}/activate] - {}", id, storageId, principal.getName());

        BagSearchCriteria criteria = new BagSearchCriteria().withId(id);
        Bag bag = bagService.find(criteria);
        if (bag == null) {
            throw new NotFoundException("Bag does not exist");
        }

        StagingStorage storage = findStorageForBag(bag, storageId);

        // could do a null check here...
        StorageRegion region = storage.getRegion();
        String owner = region.getNode().getUsername();
        if (!hasRoleAdmin() && !owner.equalsIgnoreCase(principal.getName())) {
            throw new ForbiddenException("User is not allowed to update this resource");
        }

        storage.setActive(!storage.isActive());
        bagService.save(bag);
        return "redirect:/bags/" + id;
    }

    /**
     * Update a fixity for a Bag's Storage Area
     * <p>
     * This is kind of a patch job at the moment to help with doing things through the ui
     * For now: if the fixity is null send to a create page
     *          else remove the fixity
     *
     * @param model     the model for the controller
     * @param principal the security principal of the user
     * @param id        the id of the bag
     * @param storageId the id of the storage area
     * @return
     * @throws ForbiddenException
     */
    @GetMapping("/bags/{id}/storage/{storageId}/fixity")
    public String updateStorageFixity(Model model,
                                      Principal principal,
                                      @PathVariable("id") Long id,
                                      @PathVariable("storageId") Long storageId) throws ForbiddenException {
        access.info("[GET /bags/{}/storage/{}/fixity] - {}", id, storageId, principal.getName());
        String template = "redirect:/bags/" + id;

        BagSearchCriteria criteria = new BagSearchCriteria().withId(id);
        Bag bag = bagService.find(criteria);
        if (bag == null) {
            throw new NotFoundException("Bag does not exist!");
        }

        StagingStorage storage = findStorageForBag(bag, storageId);

        // could do a null check here...
        StorageRegion region = storage.getRegion();
        String owner = region.getNode().getUsername();
        if (!hasRoleAdmin() && !owner.equalsIgnoreCase(principal.getName())) {
            throw new ForbiddenException("User is not allowed to update this resource");
        }

        if (storage.getFixities().isEmpty()) {
            model.addAttribute("bag", bag);
            model.addAttribute("storage", storage);
            template = "fixity/create";
        } else {
            // this should be for an individual fixity value but w/e
            log.info("[{}] Removing Fixities", bag.getDepositor() + "::" + bag.getName());
        }

        return template;
    }

    /**
     * Create a Fixity for a given Bag and StagingStorage
     * todo: tostring for FixityCreate
     *
     * @param principal the principal of the user
     * @param id        the id of the Bag
     * @param storageId the id of the StagingStorage
     * @param create    the FixityCreate information
     * @return the updated Bag
     * @throws ForbiddenException if the user does not have permissions to edit the Bag
     */
    @PostMapping("/bags/{id}/storage/{storageId}/fixity")
    public String createStorageFixity(Principal principal,
                                      @PathVariable("id") Long id,
                                      @PathVariable("storageId") Long storageId,
                                      FixityCreate create) throws ForbiddenException {
        access.info("[POST /bags/{}/storage/{}/fixity] - {}", id, storageId, principal.getName());
        access.info("POST parameters - {};{}", create.getAlgorithm(), create.getValue());
        String template = "redirect:/bags/" + id;

        BagSearchCriteria criteria = new BagSearchCriteria().withId(id);
        Bag bag = bagService.find(criteria);
        if (bag == null) {
            throw new NotFoundException("Bag does not exist!");
        }

        StagingStorage storage = findStorageForBag(bag, storageId);

        // could do a null check here...
        StorageRegion region = storage.getRegion();
        String owner = region.getNode().getUsername();
        if (!hasRoleAdmin() && !owner.equalsIgnoreCase(principal.getName())) {
            throw new ForbiddenException("User is not allowed to update this resource");
        }

        Fixity fixity = new Fixity();
        fixity.setStorage(storage);
        fixity.setValue(create.getValue());
        fixity.setAlgorithm(create.getAlgorithm());
        fixity.setCreatedAt(ZonedDateTime.now());
        storage.getFixities().add(fixity);
        bagService.save(bag);

        return "redirect:/bags/" + id;
    }

    private StagingStorage findStorageForBag(Bag bag, Long storageId) {
        StagingStorage storage;
        // todo: through the db
        if (bag.getBagStorage() != null
                && bag.getBagStorage().getId().equals(storageId)) {
            storage = bag.getBagStorage();
        } else if (bag.getTokenStorage() != null
                && bag.getTokenStorage().getId().equals(storageId)) {
            storage = bag.getTokenStorage();
        } else {
            // should have a related ExceptionHandler
            throw new RuntimeException("Invalid Storage Id");
        }
        return storage;
    }

    /**
     * Retrieve the page for adding bags
     *
     * @param model - the view model
     * @return page to add a bag
     */
    @RequestMapping(value = "/bags/add", method = RequestMethod.GET)
    public String addBag(Model model, Principal principal) {
        access.info("[GET /bags/add] - {}", principal.getName());
        model.addAttribute("nodes", nodeRepository.findAll());
        return "addbag";
    }

    /**
     * Handler for adding bags
     *
     * @param request - the request containing the bag name, depositor, and location
     * @return redirect to the bags page
     */
    @RequestMapping(value = "/bags/add", method = RequestMethod.POST)
    public String addBag(Principal principal, IngestRequest request) {
        access.info("[POST /bags/add] - {}", principal.getName());
        access.info("Post parameters: {};{}", request.getDepositor(), request.getName());
        Long regionId = request.getStorageRegion();

        StorageRegion region = regions.find(new StorageRegionSearchCriteria()
                .withId(regionId));
        if (region == null) {
            throw new BadRequestException("Bad Request: StorageRegion "
                    + regionId
                    + " not found!");
        }

        Set<Node> replicatingNodes = replicatingNodes(request.getReplicatingNodes());
        Bag bag = bagService.create(principal.getName(), request, region, replicatingNodes);

        return "redirect:/bags/" + bag.getId();
    }

    private Set<Node> replicatingNodes(List<String> nodeNames) {
        Set<Node> replicatingNodes = new HashSet<>();
        if (nodeNames == null) {
            nodeNames = new ArrayList<>();
        }

        for (String name : nodeNames) {
            Node node = nodeRepository.findByUsername(name);
            if (node != null) {
                replicatingNodes.add(node);
            } else {
                log.warn("Node {} not found for distribution of bag!", name);
            }
        }

        return replicatingNodes;
    }

    //
    // Replication stuff

    /**
     * Get all replications
     * If admin, return a list of all replications
     * else return a list for the given user
     *
     * @param model     - the viewmodel
     * @param principal - authentication information
     * @return the page listing all replications
     */
    @RequestMapping(value = "/replications", method = RequestMethod.GET)
    public String getReplications(Model model, Principal principal,
                                  @ModelAttribute(value = "filter") ReplicationFilter filter) {
        access.info("[GET /replications] - {}", principal.getName());

        Page<Replication> replications;
        ReplicationSearchCriteria criteria = new ReplicationSearchCriteria()
                .bagNameLike(filter.getBag())
                .nodeUsernameLike(filter.getNode())
                .withStatuses(filter.getStatus());

        Sort.Direction direction = (filter.getDir() == null) ? Sort.Direction.ASC : Sort.Direction.fromStringOrNull(filter.getDir());
        Sort s = new Sort(direction, filter.getOrderBy());
        replications = replicationService.findAll(criteria, new PageRequest(filter.getPage(), DEFAULT_PAGE_SIZE, s));

        model.addAttribute("replications", replications);
        model.addAttribute("statuses", ReplicationStatus.statusByGroup());
        model.addAttribute("pages", new PageWrapper<>(replications, "/replications", filter.getParameters()));

        return "replications";
    }

    @RequestMapping(value = "/replications/{id}", method = RequestMethod.GET)
    public String getReplication(Model model, Principal principal, @PathVariable("id") Long id) {
        access.info("[GET /replications/{}] - {}", id, principal.getName());
        ReplicationSearchCriteria criteria = new ReplicationSearchCriteria()
                .withId(id);

        Replication replication = replicationService.find(criteria);
        log.info("Found replication {}::{}", replication.getId(), replication.getNode().getUsername());
        model.addAttribute("replication", replication);

        return "replication";
    }

    /**
     * Get all replications
     * If admin, return a list of all replications
     * else return a list for the given user
     *
     * @param model     - the viewmodel
     * @param principal - authentication information
     * @return the addreplication page
     */
    @RequestMapping(value = "/replications/add", method = RequestMethod.GET)
    public String addReplications(Model model, Principal principal) {
        access.info("[GET /replications/add] - {}", principal.getName());
        model.addAttribute("bags", bagService.findAll(new BagSearchCriteria(), new PageRequest(0, 100)));
        model.addAttribute("nodes", nodeRepository.findAll());
        return "addreplication";
    }

    /**
     * Handle a request to create a replication from the Bag.id page
     *
     * @param model the model of the response
     * @param bag   the bag id to create replications for
     * @return the create replication form
     */
    @RequestMapping(value = "/replications/create", method = RequestMethod.GET)
    public String createReplicationForm(Model model, Principal principal, @RequestParam("bag") Long bag) {
        access.info("[GET /replications/create] - {}", principal.getName());
        model.addAttribute("bag", bag);
        if (hasRoleAdmin()) {
            model.addAttribute("nodes", nodeRepository.findAll());
        } else {
            List<Node> nodes = new ArrayList<>();
            Node node = nodeRepository.findByUsername(principal.getName());
            if (node != null) {
                nodes.add(node);
            }
            model.addAttribute("nodes", nodes);
        }
        return "replications/create";
    }

    @RequestMapping(value = "/replications/create", method = RequestMethod.POST)
    public String createReplications(Principal principal, @ModelAttribute("form") ReplicationCreate form) {
        access.info("[POST /replications/create] - {}", principal.getName());
        final Long bag = form.getBag();
        form.getNodes().forEach(nodeId -> replicationService.create(bag, nodeId));
        return "redirect:/replications/";
    }

    /**
     * Handler for adding bags
     *
     * @param request - the request containing the bag name, depositor, and location
     * @return redirect to all replications
     */
    @RequestMapping(value = "/replications/add", method = RequestMethod.POST)
    public String addReplication(Principal principal, ReplicationRequest request) {
        access.info("[POST /replications/add] - {}", principal.getName());
        ReplicationCreateResult result = replicationService.create(request);

        // todo: find a way to display errors backwards
        String template = result.getResult()
                .map(repl -> "redirect:/replications/" + repl.getId())
                .orElse("redirect:/replications/create");
        return template;
    }

}
