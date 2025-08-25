package com.mshando.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * Configuration for Thymeleaf template processing.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class TemplateConfig {

    /**
     * String template resolver for processing templates from database
     */
    @Bean
    public StringTemplateResolver stringTemplateResolver() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false); // Disable caching for dynamic templates
        return templateResolver;
    }

    /**
     * Template engine for processing notification templates
     */
    @Bean
    @Primary
    public TemplateEngine notificationTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(stringTemplateResolver());
        return templateEngine;
    }
}
