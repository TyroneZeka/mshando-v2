package com.mshando.biddingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;

/**
 * Validation configuration for the Bidding Service.
 * 
 * Configures Bean Validation (JSR-303/JSR-380) for
 * request validation, method parameter validation,
 * and custom validation rules.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Configuration
public class ValidationConfig {

    /**
     * Configures the JSR-303 validator factory.
     * Enables validation annotations on request bodies,
     * method parameters, and entity fields.
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        
        // Don't set message source - let Spring use defaults
        // factoryBean.setValidationMessageSource(null); // This causes the error
        
        return factoryBean;
    }

    /**
     * Enables method-level validation using @Validated annotation.
     * Allows validation of service method parameters and return values.
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }

    /**
     * Primary validator bean for programmatic validation.
     * Can be injected into services for manual validation operations.
     */
    @Bean
    public Validator validatorBean() {
        return validator();
    }
}
