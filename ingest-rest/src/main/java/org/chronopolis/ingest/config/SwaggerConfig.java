package org.chronopolis.ingest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * SpringFox Swagger2 configuration
 * @author lsitu
 *
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Autowired
    private BuildProperties buildProperties;

    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        String version = buildProperties.getVersion();

        return new ApiInfoBuilder()
                .title(buildProperties.getName())
                .description("Chronopolis Ingest API")
                .version(version)
                .build();
    }
}