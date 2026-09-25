package com.hyperbrains.hms.config;

import java.time.Duration;
import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        var ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Object.class,
                Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries())
            )
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build()
        );
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, com.hyperbrains.hms.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, com.hyperbrains.hms.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, com.hyperbrains.hms.domain.User.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Authority.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.User.class.getName() + ".authorities");
            createCache(cm, com.hyperbrains.hms.domain.Patient.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Department.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Appointment.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Visit.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.VitalSigns.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Consultation.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Consultation.class.getName() + ".diagnoseses");
            createCache(cm, com.hyperbrains.hms.domain.Diagnosis.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Diagnosis.class.getName() + ".consultationses");
            createCache(cm, com.hyperbrains.hms.domain.HospitalService.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.LabTest.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.RadiologyExam.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.DiagnosticOrder.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Result.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Referral.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Prescription.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.PrescriptionLine.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Drug.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Dispense.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.DispenseLine.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Bill.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.BillLineItem.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.Payment.class.getName());
            createCache(cm, com.hyperbrains.hms.domain.AuditLog.class.getName());
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
