package edu.sjsu.cmpe172.careconnect.ai;

import edu.sjsu.cmpe172.careconnect.model.UserAccount;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiAssistantService {

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AppointmentAiTools appointmentTools;

    public AiAssistantService(
            ObjectProvider<ChatClient> chatClientProvider,
            AppointmentAiTools appointmentTools
    ) {
        this.chatClientProvider = chatClientProvider;
        this.appointmentTools = appointmentTools;
    }

    public String chat(
            UserAccount user,
            String message,
            List<String> history
    ) {
        ChatClient client = chatClientProvider.getIfAvailable();

        if (client == null) {
            return "Gemini is not configured yet. "
                    + "Please set GEMINI_API_KEY to use the appointment assistant.";
        }

        String conversation = String.join(
                "\n",
                history == null ? List.of() : history
        );

        String systemPrompt =
                "You are CareConnect's appointment assistant. "
                        + "The logged-in user is "
                        + user.name()
                        + " with customer ID "
                        + user.userId()
                        + ". Act only for this user. "
                        + "Use the appointment tools for availability, "
                        + "appointments, booking, and cancellation. "
                        + "For bookings, always show a summary first "
                        + "and wait for explicit confirmation before writing. "
                        + "If a slot is unavailable, offer alternatives. "
                        + "Previous conversation:\n"
                        + conversation;

        String answer = appointmentTools.asLoggedInCustomer(
                user.userId(),
                () -> client.prompt()
                        .system(systemPrompt)
                        .user(message)
                        .tools(appointmentTools)
                        .call()
                        .content()
        );

        return answer == null
                ? "I could not generate a response."
                : answer;
    }

    public static List<String> appendHistory(
            List<String> history,
            String userMessage,
            String assistantAnswer
    ) {
        List<String> updated = new ArrayList<>(
                history == null ? List.of() : history
        );

        updated.add("Patient: " + userMessage);
        updated.add("Assistant: " + assistantAnswer);

        if (updated.size() > 12) {
            return updated.subList(
                    updated.size() - 12,
                    updated.size()
            );
        }

        return updated;
    }
}