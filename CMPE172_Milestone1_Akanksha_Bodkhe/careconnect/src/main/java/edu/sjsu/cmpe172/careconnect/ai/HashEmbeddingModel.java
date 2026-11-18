package edu.sjsu.cmpe172.careconnect.ai;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HashEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSIONS = 128;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();

        for (int i = 0; i < request.getInstructions().size(); i++) {
            float[] vector = createEmbedding(
                    request.getInstructions().get(i)
            );

            embeddings.add(new Embedding(vector, i));
        }

        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return createEmbedding(document.getText());
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    private float[] createEmbedding(String text) {
        float[] vector = new float[DIMENSIONS];

        String[] words = text
                .toLowerCase()
                .split("\\W+");

        for (String word : words) {
            if (!word.isBlank()) {
                int index = Math.floorMod(
                        word.hashCode(),
                        DIMENSIONS
                );

                vector[index] += 1.0f;
            }
        }

        return vector;
    }
}