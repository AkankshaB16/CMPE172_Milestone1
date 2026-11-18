package edu.sjsu.cmpe172.careconnect.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private final SimpleVectorStore vectorStore;
    private final ObjectProvider<ChatClient> chatClientProvider;

    private final List<Document> knowledge = List.of(
            new Document(
                    "CareConnect clinic hours are Monday through Friday, "
                            + "8:00 AM to 5:00 PM Pacific Time."
            ),

            new Document(
                    "Patients may cancel a booked appointment before its "
                            + "end time. The cancellation must belong to the "
                            + "logged-in patient."
            ),

            new Document(
                    "General Consultation lasts 30 minutes and costs $75. "
                            + "Physical Therapy Evaluation lasts 60 minutes "
                            + "and costs $100. Preventive Health Visit lasts "
                            + "45 minutes and costs $90."
            ),

            new Document(
                    "Patients must be logged in to view their appointments. "
                            + "The assistant cannot access or change another "
                            + "patient's appointments."
            ),

            new Document(
                    "A slot can be booked by only one patient. CareConnect "
                            + "checks the transactional booking guard before "
                            + "committing a booking."
            )
    );

    public KnowledgeBaseService(
            HashEmbeddingModel embeddingModel,
            ObjectProvider<ChatClient> chatClientProvider
    ) {
        this.vectorStore = SimpleVectorStore
                .builder(embeddingModel)
                .build();

        this.chatClientProvider = chatClientProvider;
    }

    @PostConstruct
    public void ingestKnowledge() {
        vectorStore.add(knowledge);
    }

    public List<String> findSources(String question) {
        return vectorStore
                .similaritySearch(
                        SearchRequest
                                .builder()
                                .query(question)
                                .topK(3)
                                .build()
                )
                .stream()
                .map(Document::getText)
                .toList();
    }

    public String answer(String question) {
        List<String> matches = findSources(question);

        if (matches.isEmpty()) {
            return "I could not find that information in the "
                    + "CareConnect policy and FAQ.";
        }

        ChatClient client = chatClientProvider.getIfAvailable();

        if (client == null) {
            return "According to the CareConnect FAQ: "
                    + String.join(" ", matches);
        }

        String context = String.join("\n", matches);

        String response = client
                .prompt()
                .system(
                        "Answer only using the supplied CareConnect FAQ. "
                                + "If the answer is not included, say you "
                                + "do not know. Keep the answer concise."
                )
                .user(
                        "FAQ context:\n"
                                + context
                                + "\n\nQuestion: "
                                + question
                )
                .call()
                .content();

        return response == null || response.isBlank()
                ? "I could not generate a grounded answer."
                : response;
    }
}