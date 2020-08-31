package org.chronopolis.ingest;

import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

/**
 * ActiveProfileResolver for test
 * @author lsitu
 */
public class EnvironmentActiveProfileResolver implements ActiveProfilesResolver {
    public static String ACTICE_PROFILE_KEY = "spring.profiles.active";
    private final DefaultActiveProfilesResolver resolver = new DefaultActiveProfilesResolver();

    @Override
    public String[] resolve(Class<?> clazz) {
        return System.getProperties().containsKey(ACTICE_PROFILE_KEY)
                ? System.getProperty(ACTICE_PROFILE_KEY).split("\\s*,\\s*")
                : resolver.resolve(clazz);
    }
}
