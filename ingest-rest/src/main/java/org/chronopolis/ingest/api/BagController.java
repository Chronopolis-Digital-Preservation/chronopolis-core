package org.chronopolis.ingest.api;

import org.chronopolis.ingest.IngestController;
import org.chronopolis.ingest.exception.NotFoundException;
import org.chronopolis.ingest.models.Paged;
import org.chronopolis.rest.models.update.BagUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.chronopolis.ingest.models.filter.BagFilter;
import org.chronopolis.ingest.repository.dao.BagDao;
import org.chronopolis.ingest.support.BagCreateResult;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.projections.CompleteBag;
import org.chronopolis.rest.entities.projections.PartialBag;
import org.chronopolis.rest.models.create.BagCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST Controller for controlling actions associated with bags
 * <p>
 * Created by shake on 11/5/14.
 */
@RestController
@RequestMapping("/api/bags")
public class BagController extends IngestController {
    private final Logger log = LoggerFactory.getLogger(BagController.class);

    private final BagDao dao;

    @Autowired
    public BagController(BagDao dao) {
        this.dao = dao;
    }

    /**
     * Retrieve all the bags we know about
     *
     * @param filter Query parameters used for searching
     * @return all bags matching the query parameters
     */
    @GetMapping
    public Iterable<PartialBag> getBags(@ModelAttribute BagFilter filter,
                                        @RequestParam(required=false, defaultValue = Paged.DEFAULT_PAGE_SIZE + "") Long pageSize,
                                        @RequestParam(required=false, defaultValue = "0") Integer page) {
        filter.setPageSize(pageSize);
        filter.setPage(page);

        return dao.findViewAsPage(filter);
    }

    /**
     * Retrieve information about a single bag
     *
     * @param id the bag id to retrieve
     * @return the bag specified by the id
     */
    @GetMapping("/{id}")
    public CompleteBag getBag(@PathVariable("id") Long id) {
        CompleteBag bag = dao.findCompleteView(id);

        if (bag == null) {
            throw new NotFoundException("bag/" + id);
        }

        return bag;
    }

    /**
     * Notification that a bag exists and is ready to be ingested into Chronopolis
     *
     * @param principal authentication information
     * @param request   the request containing the bag name, depositor, and location of the bag
     * @return HTTP 201 with the created Bag
     *         HTTP 400 if the request is not valid (depositor, region)
     *         HTTP 401 if the user is not authenticated
     *         HTTP 403 if the user is not authorized to create
     */
    @PostMapping
    public ResponseEntity<?> stageBag(Principal principal, @RequestBody BagCreate request) {
        BagCreateResult result = dao.processRequest(principal.getName(), request);
        return result.getResponseEntity();
    }

    /**
     * Handler for updating a bag
     * <p>
     * todo: constraint on updating the bag as a non-admin
     *
     * @param id     id of the bag to update
     * @param update the updated information
     * @return page showing the individual bag
     */
    @PutMapping("/{id}")
    public ResponseEntity<Bag> updateBag(Principal principal,
                            @PathVariable("id") Long id,
                            @RequestBody BagUpdate update) {
        Bag bag = dao.findOne(QBag.bag, QBag.bag.id.eq(id));

        if (bag == null) {
            throw new NotFoundException("Bag " + id);
        }

        bag.setStatus(update.getStatus());
        dao.save(bag);

        log.info("Updated bag {} ({}) status: {} to {}.", bag.getName(), bag.getId(), bag.getStatus(), update.getStatus());

        return ResponseEntity.ok(bag);
    }
}
