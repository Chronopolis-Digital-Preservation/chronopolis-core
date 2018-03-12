package org.chronopolis.ingest.api;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.chronopolis.ingest.models.filter.BagFilter;
import org.chronopolis.ingest.models.filter.DepositorFilter;
import org.chronopolis.ingest.repository.dao.PagedDAO;
import org.chronopolis.ingest.support.Loggers;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Depositor;
import org.chronopolis.rest.entities.DepositorContact;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.QDepositor;
import org.chronopolis.rest.models.DepositorContactCreate;
import org.chronopolis.rest.models.DepositorCreate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;
import static org.chronopolis.ingest.IngestController.hasRoleAdmin;

/**
 * API implementation for Depositors
 * <p>
 * todo: POST /api/depositors/{ns}/nodes
 * todo: DELETE /api/depositors/{ns}/nodes
 * todo: POST /api/depositors/{ns}/contacts
 * todo: DELETE /api/depositors/{ns}/contacts
 *
 * @author shake
 */
@RestController
@RequestMapping("/api/depositors")
public class DepositorController {

    private final Logger log = LoggerFactory.getLogger(DepositorController.class);
    private final Logger access = LoggerFactory.getLogger(Loggers.ACCESS_LOG);

    private final PagedDAO dao;

    @Autowired
    public DepositorController(PagedDAO dao) {
        this.dao = dao;
    }

    /**
     * Retrieve all Depositors held by the Ingest Server
     *
     * @return HTTP 200 with a list of all Depositors
     *         HTTP 401 if the user is not authenticated
     */
    @GetMapping
    public ResponseEntity<Iterable<Depositor>> depositors(@ModelAttribute DepositorFilter filter) {
        return ResponseEntity.ok(dao.findPage(QDepositor.depositor, filter));
    }

    /**
     * Create a Depositor in the Ingest Server
     *
     * @param principal the security principal of the user
     * @param depositor the Depositor to create
     * @return HTTP 201 if the Depositor was created successfully with the Depositor as the response
     *         HTTP 400 if the DepositorCreate is not valid (bad phone number, missing fields, etc)
     *         HTTP 401 if the user is not authenticated
     *         HTTP 403 if the user requesting the create does not have permission
     *         HTTP 409 if the Depositor already exists
     */
    @PostMapping
    public ResponseEntity<Depositor> createDepositor(Principal principal,
                                                     @Valid @RequestBody
                                                     DepositorCreate depositor) {
        access.info("[POST /api/depositors] - {}", principal.getName());
        QDepositor qDepositor = QDepositor.depositor;

        // Default response of Forbidden if a user is not authorized to create Depositors
        ResponseEntity<Depositor> response = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (hasRoleAdmin()) {
            // check collision
            Depositor exists = dao.findOne(qDepositor,
                    qDepositor.namespace.eq(depositor.getNamespace()));

            if (exists == null) {
                Optional<Set<DepositorContact>> parsed = parseContacts(depositor.getContacts());
                Optional<Depositor> depositorOptional = parsed.map(contacts -> new Depositor()
                        .setNamespace(depositor.getNamespace())
                        .setSourceOrganization(depositor.getSourceOrganization())
                        .setOrganizationAddress(depositor.getOrganizationAddress())
                        .setContacts(contacts));
                depositorOptional.ifPresent(dao::save);

                // Even though the BadRequest is handled by Spring, we do an additional check here
                // as a sanity check. This is because a PhoneNumberException can be thrown so it
                // gives a way to handle the exception gracefully.
                response = depositorOptional.map(entity ->
                        ResponseEntity.status(HttpStatus.CREATED).body(entity))
                        .orElse(ResponseEntity.badRequest().build());
            } else {
                response = ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        return response;
    }

    /**
     * Read through DepositorContactCreate requests and return a Set of DepositorContacts if
     * the requests are valid. If a Contact is invalid, return an empty Optional. This way if
     * no Contacts were requested to be made, we can still return an empty Set.
     *
     * Note: We might want to move this somewhere else as this will likely be something we
     * need to do in multiple places
     *
     * @param creates the DepositorContactCreate requests
     * @return The DepositorContacts
     */
    private Optional<Set<DepositorContact>> parseContacts(List<DepositorContactCreate> creates) {
        PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();
        Set<DepositorContact> contacts = new HashSet<>(creates.size());
        Optional<Set<DepositorContact>> safe = Optional.of(contacts);
        try {
            for (DepositorContactCreate create : creates) {
                DepositorContactCreate.PhoneNumber proto = create.getPhoneNumber();
                Phonenumber.PhoneNumber phoneNumber =
                        numberUtil.parse(proto.getNumber(), proto.getCountryCode());
                contacts.add(new DepositorContact().setContactName(create.getName())
                        .setContactEmail(create.getEmail())
                        .setContactPhone(numberUtil.format(phoneNumber, INTERNATIONAL)));
            }
        } catch (NumberParseException exception) {
            // This shouldn't happen, but in case we're trying an unvalidated DCC
            log.error("Unable to parse number", exception);
            safe = Optional.empty();
        }

        return safe;
    }

    /**
     * Retrieve a Depositor identified by their namespace
     *
     * @param namespace the namespace of the Depositor
     * @return HTTP 200 if the Depositor is found with the Depositor as the response body
     *         HTTP 401 if the user is not authenticated
     *         HTTP 404 if the Depositor is not found
     */
    @GetMapping("/{namespace}")
    public ResponseEntity<Depositor> depositor(@PathVariable("namespace") String namespace) {
        ResponseEntity<Depositor> response = ResponseEntity.notFound().build();

        Depositor depositor = dao.findOne(QDepositor.depositor,
                new DepositorFilter().setNamespace(namespace));

        if (depositor != null) {
            response = ResponseEntity.ok(depositor);
        }

        return response;
    }

    /**
     * Retrieve all Bags which a Depositor has ownership of in Chronopolis
     *
     * @param namespace the namespace of the Depositor
     * @return HTTP 200 with a list of Bags the Depositor owns
     *         HTTP 401 if the user is not authenticated
     *         HTTP 404 if the Depositor does not exist?
     */
    @GetMapping("/{namespace}/bags")
    public ResponseEntity<Iterable<Bag>> depositorBags(@PathVariable("namespace") String namespace,
                                                       @ModelAttribute BagFilter filter) {
        // Default response if the namespace does not match a known Depositor
        ResponseEntity<Iterable<Bag>> entity = ResponseEntity.notFound().build();

        QDepositor qDepositor = QDepositor.depositor;
        Depositor depositor = dao.findOne(qDepositor, qDepositor.namespace.eq(namespace));
        if (depositor != null) {
            filter.setDepositor(namespace);
            Page<Bag> bags = dao.findPage(QBag.bag, filter);
            entity = ResponseEntity.ok(bags);
        }

        return entity;
    }

    /**
     * Retrieve a Bag which a Depositor has ownership of in Chronopolis
     *
     * @param namespace the namespace of the Depositor
     * @param bagName   the name of the Bag
     * @return HTTP 200 with the Bag as the response body
     *         HTTP 401 if the user is not authenticated
     *         HTTP 404 if the bag does not exist
     */
    @GetMapping("/{namespace}/bags/{bagName}")
    public ResponseEntity<Bag> depositorBag(@PathVariable("namespace") String namespace,
                                            @PathVariable("bagName") String bagName) {
        ResponseEntity<Bag> response = ResponseEntity.notFound().build();
        BagFilter filter = new BagFilter()
                .setName(bagName)
                .setDepositor(namespace);
        Bag bag = dao.findOne(QBag.bag, filter);
        if (bag != null) {
            response = ResponseEntity.ok(bag);
        }
        return response;
    }

}
