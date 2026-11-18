package edu.sjsu.cmpe172.careconnect.controller;

import edu.sjsu.cmpe172.careconnect.ai.AiAssistantService;
import edu.sjsu.cmpe172.careconnect.ai.AutonomousReminderService;
import edu.sjsu.cmpe172.careconnect.ai.KnowledgeBaseService;
import edu.sjsu.cmpe172.careconnect.model.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistantService assistantService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AutonomousReminderService reminderService;

    public AiController(
            AiAssistantService assistantService,
            KnowledgeBaseService knowledgeBaseService,
            AutonomousReminderService reminderService
    ) {
        this.assistantService = assistantService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.reminderService = reminderService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody ChatRequest request,
            HttpSession session
    ) {
        UserAccount user = currentCustomer(session);

        @SuppressWarnings("unchecked")
        List<String> history =
                (List<String>) session.getAttribute("aiHistory");

        String answer = assistantService.chat(
                user,
                request.message(),
                history
        );

        session.setAttribute(
                "aiHistory",
                AiAssistantService.appendHistory(
                        history,
                        request.message(),
                        answer
                )
        );

        return ResponseEntity.ok(
                Map.of(
                        "answer", answer,
                        "user", user.name()
                )
        );
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(
            @RequestBody AskRequest request
    ) {
        return Map.of(
                "answer",
                knowledgeBaseService.answer(request.question()),
                "sources",
                knowledgeBaseService.findSources(request.question())
        );
    }

    @PostMapping("/reminders/run")
    public Map<String, Object> runReminders() {
        return Map.of(
                "sent",
                reminderService.runNow()
        );
    }

    private UserAccount currentCustomer(HttpSession session) {
        Object user = session.getAttribute("user");

        if (!(user instanceof UserAccount account)) {
            throw new IllegalStateException("Please log in first.");
        }

        if (!account.isCustomer()) {
            throw new AppController.WrongRoleException(
                    "Customer access required."
            );
        }

        return account;
    }

    public record ChatRequest(String message) {
    }

    public record AskRequest(String question) {
    }
}