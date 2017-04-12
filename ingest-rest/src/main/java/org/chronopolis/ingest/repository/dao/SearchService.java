package org.chronopolis.ingest.repository.dao;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import org.chronopolis.ingest.repository.criteria.SearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Map;

/**
 * Generalized service for access database objects. Uses SearchCriteria to build queries, as well
 * as offers a basic save method.
 *
 * Created by shake on 1/24/17.
 */
@Transactional
public class SearchService<T, I extends Serializable, E extends JpaRepository<T, I> & QueryDslPredicateExecutor<T>> {

    private final E e;

    public SearchService(E e) {
        this.e = e;
    }

    public T find(SearchCriteria sc) {
        Predicate predicate = buildPredicate(sc);
        return e.findOne(predicate);
    }

    public Page<T> findAll(SearchCriteria sc, Pageable pageable) {
        Predicate predicate = buildPredicate(sc);
        return e.findAll(predicate, pageable);
    }

    public Predicate buildPredicate(SearchCriteria sc) {
        Map<Object, BooleanExpression> criteria = sc.getCriteria();
        BooleanBuilder builder = new BooleanBuilder();
        for (Object o : criteria.keySet()) {
            builder.and(criteria.get(o));
        }

        return builder.getValue();
    }

    public void save(T t) {
        e.save(t);
    }
}