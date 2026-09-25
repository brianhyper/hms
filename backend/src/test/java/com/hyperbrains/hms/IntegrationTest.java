package com.hyperbrains.hms;

import com.hyperbrains.hms.config.AsyncSyncConfiguration;
import com.hyperbrains.hms.config.DatabaseTestcontainer;
import com.hyperbrains.hms.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        HmsApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.hyperbrains.hms.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}
