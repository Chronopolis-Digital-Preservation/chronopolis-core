package org.chronopolis.ingest;

import org.chronopolis.ingest.api.StagingController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackageClasses = {
        IngestSettings.class,
        StagingController.class
})
@EntityScan(basePackages = "org.chronopolis.rest.models")
@EnableAutoConfiguration
public class TestApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(TestApplication.class));
    }

    @Override
    public void run(final String... args) throws Exception {

    }
}