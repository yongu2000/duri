package com.duri.config;

import com.duri.global.serializer.EncryptIdBeanSerializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomEncryption() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.setSerializerModifier(new EncryptIdBeanSerializerModifier());
            builder.modules(module);
            builder.modules(module, new JavaTimeModule());
            
        };
    }
}
