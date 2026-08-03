package com.telusko.aigeminiapp.config;

import org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaudeCompatibilityConfig {

    @Bean
    static BeanPostProcessor removeAnthropicTemperature() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof AnthropicChatProperties properties) {
                    properties.getOptions().setTemperature(null);
                }
                return bean;
            }
        };
    }
}
