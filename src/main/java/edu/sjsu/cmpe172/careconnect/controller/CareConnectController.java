package edu.sjsu.cmpe172.careconnect.controller;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import edu.sjsu.cmpe172.careconnect.dto.HomeDto;
import edu.sjsu.cmpe172.careconnect.service.CareConnectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class CareConnectController {
    private final CareConnectService careConnectService;

    public CareConnectController(CareConnectService careConnectService) {
        this.careConnectService = careConnectService;
    }

    @GetMapping
    public HomeDto home() {
        return careConnectService.getHome();
    }

    @GetMapping("slots")
    public List<AvailableSlotDto> availableSlots() {
        return careConnectService.getAvailableSlots();
    }
}
