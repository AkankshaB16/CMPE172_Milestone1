package edu.sjsu.cmpe172.careconnect.ai;

import com.google.genai.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "spring.ai.google.genai.api-key"
    )
    public ChatClient careConnectChatClient(
            @Value("${spring.ai.google.genai.api-key}")
            String apiKey,

            @Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}")
            String model
    ) {
        Client genAiClient = Client
                .builder()
                .apiKey(apiKey)
                .build();

        GoogleGenAiChatOptions options =
                GoogleGenAiChatOptions
                        .builder()
                        .model(model)
                        .temperature(0.2)
                        .build();

        ToolCallingManager toolCallingManager =
                ToolCallingManager
                        .builder()
                        .build();

        GoogleGenAiChatModel chatModel =
                GoogleGenAiChatModel
                        .builder()
                        .genAiClient(genAiClient)
                        .defaultOptions(options)
                        .toolCallingManager(toolCallingManager)
                        .build();

        return ChatClient
                .builder(chatModel)
                .build();
    }
}