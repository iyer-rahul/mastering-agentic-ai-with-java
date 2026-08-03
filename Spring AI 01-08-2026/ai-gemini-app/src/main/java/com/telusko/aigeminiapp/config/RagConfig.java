package com.telusko.aigeminiapp.config;

import com.telusko.aigeminiapp.rag.KnowledgeBase;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig
{
    @Bean
    ApplicationRunner ingestKnowledge(KnowledgeBase knowledgeBase) {
        return args -> knowledgeBase.loadIfEmpty();
    }
}
