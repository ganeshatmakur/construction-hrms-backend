package com.hrms.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

 
@Configuration
public class JacksonConfig {

    @Bean
      ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register Java 8+ date/time module
        mapper.registerModule(new JavaTimeModule());
        
        // Use ISO-8601 format for dates (2026-05-25, not timestamps)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Set UTC timezone
        mapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")));
        
        // Only include non-null values in serialization
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Pretty print JSON (helpful for debugging)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}
