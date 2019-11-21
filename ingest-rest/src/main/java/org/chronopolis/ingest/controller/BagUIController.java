package org.chronopolis.ingest.controller;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.chronopolis.ingest.IngestController;
import org.chronopolis.ingest.PageWrapper;
import org.chronopolis.ingest.models.BagUpdate;
import org.chronopolis.ingest.models.filter.BagFilter;
import org.chronopolis.ingest.repository.dao.BagDao;
import org.chronopolis.ingest.repository.dao.ReplicationDao;
import org.chronopolis.ingest.repository.dao.StagingDao;
import org.chronopolis.ingest.repository.dao.TokenDao;
import org.chronopolis.ingest.support.BagCreateResult;
import org.chronopolis.ingest.support.FileSizeFormatter;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.QAceToken;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.QBagFile;
import org.chronopolis.rest.entities.QNode;
import org.chronopolis.rest.entities.QReplication;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.storage.QStorageRegion;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.models.create.BagCreate;
import org.chronopolis.rest.models.enums.BagStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.Valid;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.chronopolis.ingest.repository.dao.StagingDao.DISCRIMINATOR_BAG;
import static org.chronopolis.ingest.repository.dao.StagingDao.DISCRIMINATOR_TOKEN;


/**
 * Controller for handling bag/replication related requests
 * <p>
 * Created by shake on 4/17/15.
 * lsitu 10/22/19.
 */
@Controller
public class BagUIController extends IngestController {
    private final Logger log = LoggerFactory.getLogger(BagUIController.class);

    private final BagDao dao;
    private final TokenDao tokenDao;
    private final StagingDao stagingService;
    private final ReplicationDao replicationDao;

    @Autowired
    public BagUIController(BagDao dao,
                           TokenDao tokenDao, StagingDao stagingService,
                           ReplicationDao replicationDao) {
        this.dao = dao;
        this.tokenDao = tokenDao;
        this.stagingService = stagingService;
        this.replicationDao = replicationDao;
    }

    /**
     * Retrieve information about all bags
     *
     * @param model     the view model
     * @param principal authentication information
     * @return page listing all bags
     */
    @GetMapping("/bags")
    public String getBags(Model model, Principal principal,
                          @ModelAttribute(value = "filter") BagFilter filter) {
        Page<Bag> bags = dao.findPage(QBag.bag, filter);
        PageWrapper<Bag> pages = new PageWrapper<>(bags, "/bags", filter.getParameters());
        model.addAttribute("bags", bags);
        model.addAttribute("pages", pages);
        model.addAttribute("statuses", BagStatus.Companion.statusByGroup());

        return "collections/collections";
    }

    /**
     * Get information about a single bag
     * <p>
     * todo: constants for these things
     * todo: token count async
     *
     * @param model the view model
     * @param id    the id of the bag
     * @return page showing the individual bag
     */
    @GetMapping("/bags/{id}")
    public String getBag(Model model, Principal principal, @PathVariable("id") Long id) {
        FileSizeFormatter formatter = new FileSizeFormatter();
        Bag bag = dao.findOne(QBag.bag, QBag.bag.id.eq(id));
        Optional<StagingStorage> activeBagStorage =
                stagingService.activeStorageForBag(bag, DISCRIMINATOR_BAG);
        Optional<StagingStorage> activeTokenStorage =
                stagingService.activeStorageForBag(bag, DISCRIMINATOR_TOKEN);

        model.addAttribute("formatter", formatter);
        model.addAttribute("bag", bag);
        model.addAttribute("replications",
                replicationDao.findAll(QReplication.replication, QReplication.replication.bag.id.eq(id)));
        model.addAttribute("statuses", Arrays.asList(BagStatus.values()));
        model.addAttribute("tokens", tokenCount(id));
        activeBagStorage.ifPresent(s -> model.addAttribute("activeBagStorage", s));
        activeTokenStorage.ifPresent(s -> model.addAttribute("activeTokenStorage", s));

        return "collections/collection";
    }

    /**
     * Handler for updating a bag
     * <p>
     * todo: constraint on updating the bag as a non-admin
     * todo: tostring for BagUpdate
     *
     * @param model  the view model
     * @param id     id of the bag to update
     * @param update the updated information
     * @return page showing the individual bag
     */
    @PostMapping("/bags/{id}")
    public String updateBag(Model model,
                            Principal principal,
                            @PathVariable("id") Long id,
                            BagUpdate update) {
        Bag bag = dao.findOne(QBag.bag, QBag.bag.id.eq(id));
        bag.setStatus(update.getStatus());
        dao.save(bag);

        model.addAttribute("bags", bag);
        model.addAttribute("statuses", Arrays.asList(BagStatus.values()));
        model.addAttribute("tokens", tokenCount(id));

        return "collections/collection";
    }

    /**
     * Retrieve the page for adding bags
     *
     * @param model the view model
     * @return page to add a bag
     */
    @RequestMapping(value = "/bags/add", method = RequestMethod.GET)
    public String addBag(Model model, Principal principal) {
        model.addAttribute("nodes", dao.findAll(QNode.node));
        model.addAttribute("regions", replicationDao.findAll(QStorageRegion.storageRegion));
        return "collections/add";
    }

    /**
     * Handler for adding bags
     *
     * @param request the request containing the bag name, depositor, and location
     * @return redirect to the bags page
     */
    @RequestMapping(value = "/bags/add", method = RequestMethod.POST)
    public String addBag(Principal principal, @Valid BagCreate request) {
        BagCreateResult result = dao.processRequest(principal.getName(), request);
        return result.getBag()
                .map(bag -> "redirect:/bags/" + bag.getId())
                .orElse("redirect:/bags/add");
    }

    @GetMapping(value = "/bags/{id}/download/files", produces = "text/plain")
    public ResponseEntity<StreamingResponseBody> getFiles(@PathVariable("id") Long id) {
        JPAQueryFactory queryFactory = dao.getJPAQueryFactory();
        List<String> fetch = queryFactory.select(QBagFile.bagFile.filename)
                .from(QBagFile.bagFile)
                .where(QBagFile.bagFile.bag.id.eq(id)).fetch();
        String bag = queryFactory.from(QBag.bag)
                .select(QBag.bag.name)
                .where(QBag.bag.id.eq(id))
                .fetchOne();

        StreamingResponseBody stream = outputStream -> {
            for (String filename : fetch) {
                outputStream.write(filename.getBytes(Charset.defaultCharset())); // force UTF-8?
                outputStream.write("\n".getBytes(Charset.defaultCharset()));
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=" + bag + "-filelist.txt")
                .body(stream);
    }

    @GetMapping(value = "/bags/{id}/download/tokens", produces = "text/plain")
    public ResponseEntity<StreamingResponseBody> getBagTokens(@PathVariable("id") Long id) {
        String bag = dao.getJPAQueryFactory()
                .from(QBag.bag)
                .select(QBag.bag.name)
                .where(QBag.bag.id.eq(id))
                .fetchOne();
        StreamingResponseBody stream = outputStream -> tokenDao.writeToStream(id, outputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=" + bag + "-tokenstore.txt")
                .body(stream);
    }

    /**
     * Get the stuck collections
     *
     * @return the collections page
     */
    @GetMapping("/bags/stuck")
    public String getStuckBags(Model model, @ModelAttribute(value = "filter") BagFilter filter) {
        Page<Bag> stuckBags = dao.findStuckBags(QBag.bag, filter);

        PageWrapper<Bag> pages = new PageWrapper<>(stuckBags, "/bags/stuck", filter.getParameters());

        model.addAttribute("stuck", true);
        model.addAttribute("bags", stuckBags);
        model.addAttribute("pages", pages);
        model.addAttribute("statuses", BagStatus.Companion.statusByGroup());

        return "collections/collections";
    }

    // sup

    private Long tokenCount(Long bagId) {
        JPAQueryFactory queryFactory = replicationDao.getJPAQueryFactory();
        return queryFactory.selectFrom(QAceToken.aceToken)
                .where(QAceToken.aceToken.bag.id.eq(bagId))
                .fetchCount();
    }


}
