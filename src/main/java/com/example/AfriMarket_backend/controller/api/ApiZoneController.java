package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.ZoneDto;
import com.example.AfriMarket_backend.model.enums.RelayStatus;
import com.example.AfriMarket_backend.repository.RelayPointRepository;
import com.example.AfriMarket_backend.repository.ZoneRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class ApiZoneController {

    private final ZoneRepository zoneRepository;
    private final RelayPointRepository relayRepository;

    public ApiZoneController(ZoneRepository zoneRepository, RelayPointRepository relayRepository) {
        this.zoneRepository = zoneRepository;
        this.relayRepository = relayRepository;
    }

    @GetMapping("/zones")
    public ResponseEntity<?> getAllZones() {
        return ResponseEntity.ok(
                zoneRepository.findAll().stream().map(ZoneDto::from).collect(Collectors.toList())
        );
    }

    @GetMapping("/relays")
    public ResponseEntity<?> getRelays(@RequestParam(required = false) Long zoneId) {
        var relays = zoneId != null
                ? relayRepository.findAll().stream()
                        .filter(r -> r.getStatus() == RelayStatus.ACTIVE
                                && r.getZone() != null
                                && r.getZone().getId().equals(zoneId))
                        .collect(Collectors.toList())
                : relayRepository.findByStatus(RelayStatus.ACTIVE);
        return ResponseEntity.ok(relays.stream().map(ZoneDto.RelayDto::from).collect(Collectors.toList()));
    }
}
