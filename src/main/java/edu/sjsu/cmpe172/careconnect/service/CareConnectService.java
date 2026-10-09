package edu.sjsu.cmpe172.careconnect.service;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import edu.sjsu.cmpe172.careconnect.dto.HomeDto;
import edu.sjsu.cmpe172.careconnect.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareConnectService {
    private final SlotRepository slotRepository;

    public CareConnectService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public HomeDto getHome() {
        int count = slotRepository.countAvailableSlots();
        return new HomeDto(
                "CareConnect",
                "Find and book clinic appointments with healthcare providers.",
                count
        );
    }

    public List<AvailableSlotDto> getAvailableSlots() {
        return slotRepository.findAvailableSlots();
    }
}
