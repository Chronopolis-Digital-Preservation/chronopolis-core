package org.chronopolis.ingest.controller;

import org.chronopolis.ingest.IngestController;
import org.chronopolis.ingest.PageWrapper;
import org.chronopolis.ingest.exception.ForbiddenException;
import org.chronopolis.ingest.models.ReplicationConfigUpdate;
import org.chronopolis.ingest.models.filter.StorageRegionFilter;
import org.chronopolis.ingest.repository.dao.PagedDao;
import org.chronopolis.ingest.support.FileSizeFormatter;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.QNode;
import org.chronopolis.rest.entities.storage.QStagingStorage;
import org.chronopolis.rest.entities.storage.QStorageRegion;
import org.chronopolis.rest.entities.storage.ReplicationConfig;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.create.RegionCreate;
import org.chronopolis.rest.models.enums.DataType;
import org.chronopolis.rest.models.enums.StorageType;
import org.chronopolis.rest.models.enums.StorageUnit;
import org.chronopolis.rest.models.update.RegionUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.Optional;

/**
 * View controller for the StorageRegion pages
 *
 * @author shake
 */
@Controller
public class StorageRegionUIController extends IngestController {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private final Logger log = LoggerFactory.getLogger(StorageRegionUIController.class);

    private final PagedDao dao;

    @Autowired
    public StorageRegionUIController(PagedDao dao) {
        this.dao = dao;
    }

    /**
     * Show a list of all StorageRegions
     *
     * @param model     the model for the controller
     * @param principal the security principal of the user
     * @param filter    the parameters to filter on
     * @return the template for listing StorageRegions
     */
    @GetMapping("/regions")
    public String getRegions(Model model,
                             Principal principal,
                             @ModelAttribute(value = "filter") StorageRegionFilter filter) {
        Page<StorageRegion> regions = dao.findPage(QStorageRegion.storageRegion, filter);
        PageWrapper<StorageRegion> pages = new PageWrapper<>(regions,
                "/regions", filter.getParameters());

        model.addAttribute("regions", regions);
        model.addAttribute("pages", pages);
        model.addAttribute("storageTypes", StorageType.values());
        model.addAttribute("formatter", new FileSizeFormatter());
        // enum types as well

        return "storage_region/regions";
    }

    /**
     * Return a form for creating a StorageRegion
     *
     * @param model     the model for the controller
     * @param principal the principal of the user
     * @return the template to create a StorageRegion
     */
    @GetMapping("/regions/create")
    public String createRegionForm(Model model, Principal principal, RegionCreate regionCreate) {
        appendFormAttributes(model, regionCreate);
        return "storage_region/create";
    }

    /**
     * Process a request to create a StorageRegion
     *
     * @param model         the model of the controller
     * @param principal     the principal of the user
     * @param regionCreate  the RegionCreate form
     * @param bindingResult the form validation result
     * @return the newly created StorageRegion
     */
    @PostMapping("/regions")
    public String createRegion(Model model,
                               Principal principal,
                               @Valid RegionCreate regionCreate,
                               BindingResult bindingResult) throws ForbiddenException {
        if (bindingResult.hasErrors()) {
            appendFormAttributes(model, regionCreate);
            return "storage_region/create";
        }

        Node owner;
        if (hasRoleAdmin() || principal.getName().equalsIgnoreCase(regionCreate.getNode())) {
            owner = dao.findOne(QNode.node, QNode.node.username.eq(regionCreate.getNode()));
        } else {
            throw new ForbiddenException("User does not have permissions to create this resource");
        }

        StorageRegion region = new StorageRegion();
        Double capacity = regionCreate.normalizedCapacity();
        region.setNote(regionCreate.getNote());
        region.setDataType(regionCreate.getDataType());
        region.setStorageType(regionCreate.getStorageType());
        region.setCapacity(capacity.longValue());
        region.setNode(owner);

        ReplicationConfig config = new ReplicationConfig();
        config.setServer(regionCreate.getReplicationServer());
        config.setPath(regionCreate.getReplicationPath());
        config.setUsername(regionCreate.getReplicationUser());
        config.setRegion(region);

        region.setReplicationConfig(config);
        dao.save(region);

        return "redirect:/regions/" + region.getId();
    }

    /**
     * Return the form for updating a StorageRegion
     *
     * @param model      the model for the controller
     * @param principal  the principal of the user
     * @param id         the id of the StorageRegion
     * @param regionEdit the values of the form
     * @return the template for editing
     */
    @GetMapping("/regions/{id}/edit")
    public String editRegionForm(Model model,
                                 Principal principal,
                                 @PathVariable("id") Long id,
                                 RegionUpdate regionEdit) {
        StorageRegion region = dao.findOne(QStorageRegion.storageRegion, QStorageRegion.storageRegion.id.eq(id));

        BigDecimal bdCapacity = new BigDecimal(region.getCapacity());
        FileSizeFormatter formatter = new FileSizeFormatter();
        String[] capacityPair = formatter.format(bdCapacity).split(" ");

        regionEdit.setCapacity(Math.round(Double.valueOf(capacityPair[0])));
        regionEdit.setStorageUnit(StorageUnit.valueOf(capacityPair[1]));
        regionEdit.setDataType(region.getDataType());
        regionEdit.setNote(region.getNote());
        regionEdit.setStorageType(region.getStorageType());

        model.addAttribute("region", region);
        model.addAttribute("dataTypes", DataType.values());
        model.addAttribute("storageTypes", StorageType.values());
        model.addAttribute("storageUnits", StorageUnit.values());
        model.addAttribute("regionEdit", regionEdit);
        return "storage_region/edit";
    }

    /**
     * Update a StorageRegion with the values of the regionEdit form
     *
     * @param model         the model of the controller
     * @param principal     the principal of the user
     * @param id            the id of the StorageRegion
     * @param regionEdit    the form
     * @param bindingResult the result of validation
     * @return the updated StorageRegion
     * @throws ForbiddenException if the user does not have permission to update the resource
     */
    @PostMapping("/regions/{id}/edit")
    public String editRegion(Model model,
                             Principal principal,
                             @PathVariable("id") Long id,
                             @Valid RegionUpdate regionEdit,
                             BindingResult bindingResult) throws ForbiddenException {
        StorageRegion region = dao.findOne(QStorageRegion.storageRegion, QStorageRegion.storageRegion.id.eq(id));

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors()
                    .forEach(error -> log.info("{}:{}",
                            error.getField(), error.getDefaultMessage()));

            model.addAttribute("region", region);
            model.addAttribute("dataTypes", DataType.values());
            model.addAttribute("storageTypes", StorageType.values());
            model.addAttribute("storageUnits", StorageUnit.values());
            model.addAttribute("regionEdit", regionEdit);
            return "storage_region/edit";
        }

        if (!hasRoleAdmin() && !principal.getName()
                .equalsIgnoreCase(region.getNode().getUsername())) {
            throw new ForbiddenException("User does not have permissions to create this resource");
        }

        int exponent = regionEdit.getStorageUnit().getPower();
        region.setNote(regionEdit.getNote());
        region.setDataType(regionEdit.getDataType());
        region.setStorageType(regionEdit.getStorageType());
        Double capacity = regionEdit.getCapacity() * Math.pow(1000, exponent);
        region.setCapacity(capacity.longValue());

        dao.save(region);
        return "redirect:/regions/" + id;
    }

    /**
     * Append basic attributes to a model for use as form data
     *
     * @param model        the model of the controller
     * @param regionCreate the previous form data
     */
    private void appendFormAttributes(Model model, RegionCreate regionCreate) {
        model.addAttribute("nodes", dao.findAll(QNode.node));
        model.addAttribute("dataTypes", DataType.values());
        model.addAttribute("storageUnits", StorageUnit.values());
        model.addAttribute("storageTypes", StorageType.values());
        model.addAttribute("regionCreate", regionCreate);
    }

    /**
     * Retrieve information for a single StorageRegion
     *
     * @param model     the model for the controller
     * @param principal the security principal of the user
     * @param id        the id of the StorageRegion
     * @return the template for displaying a StorageRegion
     */
    @GetMapping("/regions/{id}")
    public String getRegion(Model model, Principal principal, @PathVariable("id") Long id) {

        StorageRegion region = dao.findOne(QStorageRegion.storageRegion, QStorageRegion.storageRegion.id.eq(id));
        BigDecimal bdCapacity = new BigDecimal(region.getCapacity());

        Optional<Long> usedRaw = getUsedSpace(region);
        FileSizeFormatter formatter = new FileSizeFormatter();
        String capacity = formatter.format(bdCapacity);
        String used = formatter.format(usedRaw.orElse(0L));
        int percent = usedRaw.map(BigDecimal::new)
                .map(ur -> ur.divide(bdCapacity, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .intValue())
                .orElse(0);
        model.addAttribute("region", region);
        model.addAttribute("capacity", capacity);
        model.addAttribute("used", used);
        model.addAttribute("percent", percent);
        return "storage_region/region";
    }

    /**
     * Get the amount of space used by a StorageRegion
     *
     * @param region the storage region
     * @return the used space
     */
    private Optional<Long> getUsedSpace(StorageRegion region) {
        QStagingStorage storage = QStagingStorage.stagingStorage;
        return Optional.ofNullable(dao.getJPAQueryFactory().selectFrom(storage)
                .select(storage.size.sum())
                .where(storage.region.eq(region), storage.active.isTrue())
                .fetchOne());
    }

    /**
     * Update the replication configuration information for a StorageRegion
     * <p>
     * Constraints still to do:
     * - if a user is not an admin && not the owning node -> 403
     *
     * @param model     the model for the controller
     * @param principal the security principal of the user
     * @param id        the id of the StorageRegion
     * @param update    the replication configuration information
     * @return the template for displaying a StorageRegion
     */
    @PostMapping("/regions/{id}/config")
    public String updateRegionConfig(Model model,
                                     Principal principal,
                                     @PathVariable("id") Long id,
                                     ReplicationConfigUpdate update) throws ForbiddenException {
        StorageRegion region = dao.findOne(QStorageRegion.storageRegion, QStorageRegion.storageRegion.id.eq(id));
        Node owner = region.getNode();

        if (!hasRoleAdmin() && !principal.getName().equalsIgnoreCase(owner.getUsername())) {
            throw new ForbiddenException("User does not have permissions to update this resource");
        }

        ReplicationConfig config = region.getReplicationConfig();
        config.setPath(update.getPath());
        config.setServer(update.getServer());
        config.setUsername(update.getUsername());
        dao.save(region);

        model.addAttribute("region", region);
        return "redirect:/regions/" + region.getId();
    }

    /**
     * Placeholder for a Storage Statistics page
     *
     * @return storage template
     */
    @GetMapping("/storage")
    public String storage() {
        return "storage/index";
    }
}
