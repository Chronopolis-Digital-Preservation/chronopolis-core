package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.databind.JsonSerializer;

/**
 * @author shake
 */
public abstract class EntityToModelSerializer<T, R> extends JsonSerializer<T> {

    abstract R modelOf(T t);
}
