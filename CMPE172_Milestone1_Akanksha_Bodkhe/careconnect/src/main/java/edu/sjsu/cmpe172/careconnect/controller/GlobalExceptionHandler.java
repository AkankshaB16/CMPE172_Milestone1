package edu.sjsu.cmpe172.careconnect.controller;

import edu.sjsu.cmpe172.careconnect.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingService.SlotConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(
            BookingService.SlotConflictException exception,
            Model model
    ) {
        return createErrorPage(
                model,
                409,
                exception.getMessage()
        );
    }

    @ExceptionHandler(AppController.WrongRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(
            AppController.WrongRoleException exception,
            Model model
    ) {
        return createErrorPage(
                model,
                403,
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorized(
            IllegalStateException exception,
            Model model
    ) {
        return createErrorPage(
                model,
                401,
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(
            IllegalArgumentException exception,
            Model model
    ) {
        return createErrorPage(
                model,
                400,
                exception.getMessage()
        );
    }

    private String createErrorPage(
            Model model,
            int status,
            String message
    ) {
        model.addAttribute("status", status);
        model.addAttribute("message", message);

        return "error";
    }
}
