package org.chronopolis.ingest.models.filter;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;

import org.chronopolis.ingest.models.Paged;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.models.enums.BagStatus;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Data binding for query params when filtering on Bags
 *
 * Created by shake on 6/15/17.
 */
public class BagFilter extends Paged {

    private final QBag bag = QBag.bag;
    private final BooleanBuilder builder = new BooleanBuilder();

    private String name;
    private String creator;
    private String depositor;
    private List<BagStatus> status;
    private ZonedDateTime updatedBefore;

    private final LinkedListMultimap<String, String> parameters = LinkedListMultimap.create();

    public String getName() {
        return name;
    }

    public BagFilter setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
            parameters.put("name", name);
            builder.and(bag.name.eq(name));
        }
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public BagFilter setCreator(String creator) {
        if (creator != null && !creator.isEmpty()) {
            this.creator = creator;
            parameters.put("creator", creator);
            builder.and(bag.creator.eq(creator));
        }
        return this;
    }

    public String getDepositor() {
        return depositor;
    }

    public BagFilter setDepositor(String depositor) {
        if (depositor != null && !depositor.isEmpty()) {
            this.depositor = depositor;
            parameters.put("depositor", depositor);
            builder.and(bag.depositor.namespace.eq(depositor));
        }
        return this;
    }

    public List<BagStatus> getStatus() {
        return status;
    }

    public BagFilter setStatus(List<BagStatus> status) {
        if (status != null && !status.isEmpty()) {
            this.status = status;
            status.forEach(bagStatus -> parameters.put("status", bagStatus.name()));
            builder.and(bag.status.in(status));
        }
        return this;
    }

    @Override
    public Multimap<String, String> getParameters() {
        parameters.putAll(super.getParameters());
        return Multimaps.filterValues(parameters, (value) -> (value != null && !value.isEmpty()));
    }

    /**
     * Get updateBefore
     * @return ZonedDateTime
     */
    public ZonedDateTime getUpdateBefore() {
        return updatedBefore;
    }

    /**
     * Set filter query for update before searching
     * @param updatedBefore
     * @return BagFilter
     */
    public BagFilter setUpdateBefore(ZonedDateTime updatedBefore) {
        if (updatedBefore != null) {
            this.updatedBefore = updatedBefore;
            builder.and(bag.updatedAt.before(updatedBefore));
        }
        return this;
    }

    @Override
    public BooleanBuilder getQuery() {
        return builder;
    }

    @Override
    public OrderSpecifier getOrderSpecifier() {
        Order dir = getDirection();
        OrderSpecifier orderSpecifier;

        switch (getOrderBy()) {
            case "createdAt":
                orderSpecifier = new OrderSpecifier<>(dir, bag.createdAt);
                break;
            case "updatedAt":
                orderSpecifier = new OrderSpecifier<>(dir, bag.updatedAt);
                break;
            case "status":
                orderSpecifier = new OrderSpecifier<>(dir, bag.status);
                break;
            case "depositor":
                orderSpecifier = new OrderSpecifier<>(dir, bag.depositor.namespace);
                break;
            case "name":
                orderSpecifier = new OrderSpecifier<>(dir, bag.name);
                break;
            case "size":
                orderSpecifier = new OrderSpecifier<>(dir, bag.size);
                break;
            default:
                orderSpecifier = new OrderSpecifier<>(dir, bag.id);
                break;
        }

        return orderSpecifier;
    }

}
