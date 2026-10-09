package edu.sjsu.cmpe172.careconnect.controller;

import edu.sjsu.cmpe172.careconnect.dto.SlotForm;
import edu.sjsu.cmpe172.careconnect.model.UserAccount;
import edu.sjsu.cmpe172.careconnect.repository.ServiceRepository;
import edu.sjsu.cmpe172.careconnect.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/app")
public class AppController {

    private final BookingService bookingService;
    private final ServiceRepository serviceRepository;

    public AppController(
            BookingService bookingService,
            ServiceRepository serviceRepository
    ) {
        this.bookingService = bookingService;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public String home(
            HttpSession session,
            Model model
    ) {
        model.addAttribute(
                "user",
                session.getAttribute("user")
        );

        return "home";
    }

    @GetMapping("/slots")
    public String slots(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session,
            Model model
    ) {
        model.addAttribute(
                "user",
                session.getAttribute("user")
        );

        model.addAttribute(
                "slots",
                bookingService.browse(
                        providerId,
                        serviceId,
                        date,
                        page,
                        10
                )
        );

        model.addAttribute("providerId", providerId);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("date", date);
        model.addAttribute("page", page);

        return "slots";
    }

    @PostMapping("/book/{slotId}")
    public String book(
            @PathVariable Long slotId,
            HttpSession session
    ) {
        UserAccount user = requireCustomer(session);

        bookingService.book(
                user.userId(),
                slotId
        );

        return "redirect:/app/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation(
            HttpSession session,
            Model model
    ) {
        model.addAttribute(
                "user",
                currentUser(session)
        );

        return "confirmation";
    }

    @GetMapping("/appointments")
    public String appointments(
            HttpSession session,
            Model model
    ) {
        UserAccount user = currentUser(session);

        model.addAttribute("user", user);

        if (user.isCustomer()) {
            model.addAttribute(
                    "appointments",
                    bookingService.customerAppointments(
                            user.userId()
                    )
            );
        } else if (user.isProvider()) {
            model.addAttribute(
                    "appointments",
                    bookingService.providerAppointments(
                            user.userId()
                    )
            );
        } else {
            model.addAttribute(
                    "appointments",
                    java.util.List.of()
            );
        }

        return "appointments";
    }

    @PostMapping("/appointments/{appointmentId}/cancel")
    public String cancel(
            @PathVariable Long appointmentId,
            HttpSession session
    ) {
        UserAccount user = requireCustomer(session);

        bookingService.cancel(
                user.userId(),
                appointmentId
        );

        return "redirect:/app/appointments";
    }

    @GetMapping("/provider/slots/new")
    public String newSlot(
            HttpSession session,
            Model model
    ) {
        UserAccount user = requireProvider(session);

        model.addAttribute("user", user);
        model.addAttribute("slotForm", new SlotForm());
        model.addAttribute(
                "services",
                serviceRepository.findAll()
        );

        return "new-slot";
    }

    @PostMapping("/provider/slots")
    public String createSlot(
            @Valid @ModelAttribute SlotForm form,
            HttpSession session
    ) {
        UserAccount user = requireProvider(session);

        bookingService.createProviderSlot(
                user.userId(),
                form
        );

        return "redirect:/app/slots";
    }

    @PostMapping("/provider/slots/{slotId}/remove")
    public String removeSlot(
            @PathVariable Long slotId,
            HttpSession session
    ) {
        UserAccount user = requireProvider(session);

        bookingService.removeProviderSlot(
                user.userId(),
                slotId
        );

        return "redirect:/app/slots";
    }

    private UserAccount currentUser(
            HttpSession session
    ) {
        Object user = session.getAttribute("user");

        if (!(user instanceof UserAccount)) {
            throw new IllegalStateException(
                    "Please log in first"
            );
        }

        return (UserAccount) user;
    }

    private UserAccount requireCustomer(
            HttpSession session
    ) {
        UserAccount user = currentUser(session);

        if (!user.isCustomer()) {
            throw new WrongRoleException(
                    "Customer access required"
            );
        }

        return user;
    }

    private UserAccount requireProvider(
            HttpSession session
    ) {
        UserAccount user = currentUser(session);

        if (!user.isProvider() && !user.isAdmin()) {
            throw new WrongRoleException(
                    "Provider access required"
            );
        }

        return user;
    }

    public static class WrongRoleException
            extends RuntimeException {

        public WrongRoleException(String message) {
            super(message);
        }
    }
}