package org.chronopolis.rest.entities.serializers;

import com.fasterxml.jackson.databind.JsonSerializer;

/**
 * @author shake
 */
public abstract class EntityToModelSerializer<T, R> extends JsonSerializer<T> {

    abstract R modelOf(T t);
}
