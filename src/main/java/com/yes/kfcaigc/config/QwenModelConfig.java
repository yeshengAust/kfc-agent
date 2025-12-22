package com.yes.kfcaigc.config;

import com.yes.kfcaigc.repository.SystemConfigRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QwenModelConfig {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Bean
    public ChatLanguageModel qwenChatModel() {
        // 从数据库读取配置
        String baseUrl = systemConfigRepository.getConfigValue("qwen.api.base-url", 
                "https://dashscope.aliyuncs.com/compatible-mode/v1");
        String apiKey = systemConfigRepository.getConfigValue("qwen.api.key");
        String modelName = systemConfigRepository.getConfigValue("qwen.api.model-name", "qwen-plus");
        String temperatureStr = systemConfigRepository.getConfigValue("qwen.api.temperature", "0.1");
        
        Double temperature = Double.parseDouble(temperatureStr);
        
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }
}


