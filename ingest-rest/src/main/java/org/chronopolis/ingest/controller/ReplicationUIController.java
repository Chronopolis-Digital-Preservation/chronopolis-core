package org.chronopolis.ingest.controller;

import org.chronopolis.ingest.IngestController;
import org.chronopolis.ingest.PageWrapper;
import org.chronopolis.ingest.exception.NotFoundException;
import org.chronopolis.ingest.models.ReplicationCreate;
import org.chronopolis.ingest.models.ReplicationUpdate;
import org.chronopolis.ingest.models.filter.BagFilter;
import org.chronopolis.ingest.models.filter.ReplicationFilter;
import org.chronopolis.ingest.repository.dao.ReplicationDao;
import org.chronopolis.ingest.support.ReplicationCreateResult;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.QNode;
import org.chronopolis.rest.entities.QReplication;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.models.enums.ReplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.querydsl.core.types.dsl.BooleanExpression;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for handling replication related requests
 * <p>
 * Created by shake on 4/17/15.
 * lsitu 10/22/19.
 */
@Controller
public class ReplicationUIController extends IngestController {
    private final Logger log = LoggerFactory.getLogger(ReplicationUIController.class);

    private final ReplicationDao dao;

    @Autowired
    public ReplicationUIController(ReplicationDao dao) {
        this.dao = dao;
    }

    /**
     * Get all replications
     *
     * @param model     the view model
     * @param principal authentication information
     * @return the page listing all replications
     */
    @RequestMapping(value = "/replications", method = RequestMethod.GET)
    public String getReplications(Model model, Principal principal,
                                  @ModelAttribute(value = "filter") ReplicationFilter filter) {
        Page<Replication> replications = dao.findPage(QReplication.replication, filter);

        model.addAttribute("replications", replications);
        model.addAttribute("statuses", ReplicationStatus.Companion.statusByGroup());
        model.addAttribute("pages", new PageWrapper<>(replications,
                "/replications",
                filter.getParameters()));
        model.addAttribute("pageSize", String.valueOf(filter.getPageSize()));

        return "replications/replications";
    }

    @RequestMapping(value = "/replications/{id}", method = RequestMethod.GET)
    public String getReplication(Model model, Principal principal, @PathVariable("id") Long id) {
        Replication replication = dao.findOne(QReplication.replication, QReplication.replication.id.eq(id));
        // Not found if null
        model.addAttribute("replication", replication);

        return "replications/replication";
    }

    /**
     * Get all replications
     * If admin, return a list of all replications
     * else return a list for the given user
     *
     * @param model     the viewmodel
     * @param principal authentication information
     * @return the replications/add page
     */
    @RequestMapping(value = "/replications/add", method = RequestMethod.GET)
    public String addReplications(Model model, Principal principal) {
        model.addAttribute("bags", dao.findPage(QBag.bag, new BagFilter()));
        model.addAttribute("nodes", dao.findAll(QNode.node));
        return "replications/add";
    }

    /**
     * Handle a request to create a replication from the Bag.id page
     *
     * @param model the model of the response
     * @param bag   the bag id to create replications for
     * @return the create replication form
     */
    @RequestMapping(value = "/replications/create", method = RequestMethod.GET)
    public String createReplicationForm(Model model,
                                        Principal principal,
                                        @RequestParam("bag") Long bag) {
        model.addAttribute("bag", bag);
        if (hasRoleAdmin()) {
            model.addAttribute("nodes", dao.findAll(QNode.node));
        } else {
            List<Node> nodes = new ArrayList<>();
            Node node = dao.findOne(QNode.node, QNode.node.username.eq(principal.getName()));
            if (node != null) {
                nodes.add(node);
            }
            model.addAttribute("nodes", nodes);
        }
        return "replications/create";
    }

    /**
     * Create multiple replications
     * <p>
     * Todo: ReplicationCreate -> ReplicationCreateMultiple
     *
     * @param principal the security principal of the user
     * @param form      the ReplicationCreate for to create many replications
     * @return the replications list view
     */
    @RequestMapping(value = "/replications/create", method = RequestMethod.POST)
    public String createReplications(Principal principal,
                                     @ModelAttribute("form") ReplicationCreate form) {
        final Long bag = form.getBag();
        form.getNodes().forEach(nodeId -> {
            ReplicationCreateResult result = dao.create(bag, nodeId);
            if (!result.getErrors().isEmpty()) {
                log.warn("[Bag-{}] ReplicationCreate errors {}", bag, result.getErrors());
            }
        });
        return "redirect:/replications/";
    }

    /**
     * Handler for adding bags
     *
     * @param request the request containing the bag name, depositor, and location
     * @return redirect to all replications
     */
    @RequestMapping(value = "/replications/add", method = RequestMethod.POST)
    public String addReplication(Model model, Principal principal,
                                 org.chronopolis.rest.models.create.ReplicationCreate request) {
        ReplicationCreateResult result = dao.create(request);

        Optional<Replication> repl = result.getResult();
        if (repl.isPresent() && result.getErrors().isEmpty()) {
            return "redirect:/replications/" + repl.get().getId();
        } else {
            String errorMessage = String.join("; ", result.getErrors());
            log.error("Replication create error: {}.", errorMessage);

            model.addAttribute("bagId", request.getBagId());
            model.addAttribute("nodeId", request.getNodeId());
            model.addAttribute("bags", dao.findPage(QBag.bag, new BagFilter()));
            model.addAttribute("nodes", dao.findAll(QNode.node));
            model.addAttribute("message", "Replication create error: " + errorMessage);
            return "/replications/add";
        }
    }

   /**
     * Handle a request to edit a replication from the Bag.id page
     *
     * @param model the model of the response
     * @param bag   the bag id to create replications for
     * @return the create replication form
     */
    @RequestMapping(value = "/replications/{id}/edit", method = RequestMethod.GET)
    public String editReplicationForm(Model model,
                                      Principal principal,
                                      @PathVariable("id") Long id) {
        Replication replication = dao.findOne(QReplication.replication, QReplication.replication.id.eq(id));

        model.addAttribute("replication", replication);
        model.addAttribute("statuses", ReplicationStatus.values());
        model.addAttribute("statusDelete", ReplicationStatus.PENDING);

        if (hasRoleAdmin()) {
            model.addAttribute("nodes", dao.findAll(QNode.node));
        } else {
            List<Node> nodes = new ArrayList<>();
            Node node = dao.findOne(QNode.node, QNode.node.username.eq(principal.getName()));
            if (node != null) {
                nodes.add(node);
            }
            model.addAttribute("nodes", nodes);
        }

        return "replications/edit";
    }

    /**
     * Handle a request to edit a replication from the Bag.id page
     *
     * @param model the model of the response
     * @param bag   the bag id to create replications for
     * @return the create replication form
     */
    @PostMapping("/replications/{id}/edit")
    public String updateReplication(Model model,
                                    Principal principal,
                                    @PathVariable("id") Long id,
                                    ReplicationUpdate replicationEdit,
                                    RedirectAttributes redirectAttributes) {
        BooleanExpression query = QReplication.replication.id.eq(id);

        // If a user is not an admin, make sure we only search for THEIR replications
        if (!hasRoleAdmin()) {
            query = query.and(QReplication.replication.node.username.eq(principal.getName()));
        }

        Replication update = dao.findOne(QReplication.replication, query);

        if (update == null) {
            throw new NotFoundException("Replication not found: " + id + ".");
        }

        String message = "";
        try {

            if (replicationEdit.getStatus().isClientStatus()) {
                update.setStatus(replicationEdit.getStatus());

                dao.save(update);

                message = "Replication for collection " + update.getBag().getName() + " updated successfully!";
                redirectAttributes.addFlashAttribute("message", message);

                return "redirect:/replications/" + id;
            } else {
                message = "Replication status " + replicationEdit.getStatus() + " update is not allowed. "
                        + "The following client status will be allowed: "
                        + "STARTED, SUCCESS, FAILURE, ACE_AUDITING, ACE_TOKEN_LOADED, ACE_REGISTERED.";
            }
        } catch(Exception ex) {
            message = "Error replication update: " + ex.getMessage();
            log.error(message, ex);
        }

        model.addAttribute("message", message);
        model.addAttribute("replication", update);
        model.addAttribute("statuses", ReplicationStatus.values());

        if (hasRoleAdmin()) {
            model.addAttribute("nodes", dao.findAll(QNode.node));
        } else {
            model.addAttribute("replication", update);
            model.addAttribute("statuses", ReplicationStatus.values());

            if (hasRoleAdmin()) {
                model.addAttribute("nodes", dao.findAll(QNode.node));
            } else {
                List<Node> nodes = new ArrayList<>();
                Node n = dao.findOne(QNode.node, QNode.node.username.eq(principal.getName()));
                if (n != null) {
                    nodes.add(n);
                }
                model.addAttribute("nodes", nodes);
            }
        }

        return "replications/edit";
    }

    /**
     * Handle a request to cancel/delete a replication from the Bag.id page
     *
     * @param model the model of the response
     * @param bag   the bag id to create replications for
     * @return the create replication form
     */
    @GetMapping("/replications/{id}/delete")
    public String deleteReplication(Model model,
                                    Principal principal,
                                    @PathVariable("id") Long id,
                                    RedirectAttributes redirectAttributes) {
        String message = "";
        Replication replication = dao.findOne(QReplication.replication, QReplication.replication.id.eq(id));

        try {
            // only allow deleting replication with pending status?
            if (replication.getStatus().equals(ReplicationStatus.PENDING)) {
                dao.delete(replication);

                message = "Replication for collection " + replication.getBag().getName() + " deleted!";
                redirectAttributes.addFlashAttribute("message", message);
                return "redirect:/replications";
            } else {
                message = "Cancel operation is not allowed for replication status " + replication.getStatus() + ".";
            }
        } catch(Exception ex) {
            message = ex.getMessage();
            log.error("Error deletion: " + message, ex);
        }

        model.addAttribute("message", message);
        model.addAttribute("replication", replication);
        model.addAttribute("statuses", ReplicationStatus.values());

        if (hasRoleAdmin()) {
            model.addAttribute("nodes", dao.findAll(QNode.node));
        } else {
            model.addAttribute("replication", replication);
            model.addAttribute("statuses", ReplicationStatus.values());

            if (hasRoleAdmin()) {
                model.addAttribute("nodes", dao.findAll(QNode.node));
            } else {
                List<Node> nodes = new ArrayList<>();
                Node n = dao.findOne(QNode.node, QNode.node.username.eq(principal.getName()));
                if (n != null) {
                    nodes.add(n);
                }
                model.addAttribute("nodes", nodes);
            }
        }

        return "replications/edit";
    }
}
