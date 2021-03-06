package org.chronopolis.replicate.config;

import okhttp3.OkHttpClient;

import org.chronopolis.common.ace.AceCollections;
import org.chronopolis.common.ace.AceConfiguration;
import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.storage.BucketBroker;
import org.chronopolis.common.storage.PreservationProperties;
import org.chronopolis.common.storage.PreservationPropertiesValidator;
import org.chronopolis.replicate.ReplicationProperties;
import org.chronopolis.replicate.batch.Submitter;
import org.chronopolis.replicate.batch.TransferFactory;
import org.chronopolis.replicate.batch.ace.AceFactory;
import org.chronopolis.replicate.support.SmtpProperties;
import org.chronopolis.replicate.support.SmtpReporter;
import org.chronopolis.rest.api.IngestApiProperties;
import org.chronopolis.rest.api.IngestGenerator;
import org.chronopolis.rest.api.OkBasicInterceptor;
import org.chronopolis.rest.api.ServiceGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for the beans used by the replication-shell
 * <p>
 * Created by shake on 4/16/14.
 */
@Configuration
@EnableConfigurationProperties({
        SmtpProperties.class,
        IngestApiProperties.class,
        PreservationProperties.class,
        ReplicationProperties.class,
        AceConfiguration.class})
public class ReplicationConfig {

    @Value("${debug.retrofit:NONE}")
    public String retrofitLogLevel;

    @Value("${ace.timeout:5}")
    public Long timeout;

    /**
     * Retrofit adapter for interacting with the ACE REST API
     *
     * @param configuration the ACE AM configuration properties
     * @return the AceService for connecting to ACE
     */
    @Bean
    public AceService aceService(AceConfiguration configuration) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new OkBasicInterceptor(
                        configuration.getUsername(),
                        configuration.getPassword()))
                .readTimeout(timeout, TimeUnit.MINUTES)
                .writeTimeout(timeout, TimeUnit.MINUTES)
                .build();

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(configuration.getAm())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return restAdapter.create(AceService.class);
    }

    @Bean
    public TransferFactory transferFactory(ThreadPoolExecutor io,
                                           ServiceGenerator generator,
                                           ReplicationProperties properties) {
        return new TransferFactory(io, generator.files(), generator.replications(), properties);
    }

    @Bean
    public AceFactory aceFactory(AceService ace,
                                 ThreadPoolExecutor http,
                                 ServiceGenerator generator,
                                 AceConfiguration configuration) {
        return new AceFactory(ace, generator, configuration, http);
    }

    /**
     * ServiceGenerator for creating services which can send requests to the Ingest REST API
     *
     * @param properties the API properties for configuration
     * @return the ServiceGenerator
     */
    @Bean
    public ServiceGenerator serviceGenerator(IngestApiProperties properties) {
        return new IngestGenerator(properties);
    }

    /**
     * The main replication submission bean
     *
     * @param ace        the service to connect to the ACE-AM REST API
     * @param reporter   the {@link SmtpReporter} used to send reports
     * @param properties the configuration for... general replication properties
     * @param generator  the ServiceGenerator to use for creating Ingest API services
     * @param broker     the BucketBroker for handling distribution of replications into Buckets
     * @return A {@link Submitter} which tracks bags submitted for replication
     */
    @Bean
    public Submitter submitter(AceService ace,
                               SmtpReporter reporter,
                               AceFactory aceFactory,
                               TransferFactory transferFactory,
                               ReplicationProperties properties,
                               ServiceGenerator generator,
                               BucketBroker broker) {
        return new Submitter(
                ace,
                reporter,
                broker,
                generator,
                aceFactory,
                transferFactory,
                properties,
                http()
        );
    }

    @Bean
    public ThreadPoolExecutor http() {
        return new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Bean
    public ThreadPoolExecutor io(ReplicationProperties properties) {
        return new ThreadPoolExecutor(properties.getMaxFileTransfers(),
                properties.getMaxFileTransfers(),
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());
    }

    @Bean
    public SmtpReporter reporter(SmtpProperties properties) {
        return new SmtpReporter(properties);
    }

    /**
     * Our BucketBroker to determine placement of Replications
     *
     * @param preservationProperties the properties containing our Storage Spaces
     * @return the BucketBroker
     */
    @Bean
    public BucketBroker bucketBroker(PreservationProperties preservationProperties) {
        return BucketBroker.fromProperties(preservationProperties);
    }

    /**
     * Validator to make sure the PreservationProperties contain storage areas exist, can be
     * read from, and can be written to.
     *
     * @return the PreservationPropertiesValidator
     */
    @Bean
    static Validator configurationPropertiesValidator() {
        return new PreservationPropertiesValidator();
    }

    /**
     * Retrofit adapter for interacting with the ACE REST API to retrieve collections
     *
     * @param configuration the ACE AM configuration properties
     * @return the AceCollections for connecting to ACE
     */
    @Bean
    public AceCollections aceCollectionsService(AceConfiguration configuration) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new OkBasicInterceptor(
                        configuration.getUsername(),
                        configuration.getPassword()))
                .readTimeout(timeout, TimeUnit.MINUTES)
                .writeTimeout(timeout, TimeUnit.MINUTES)
                .build();

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(configuration.getAm())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return restAdapter.create(AceCollections.class);
    }
}
