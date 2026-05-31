package com.example.AfriMarket_backend.service;

import com.example.AfriMarket_backend.model.RelayPoint;
import com.example.AfriMarket_backend.model.Zone;
import com.example.AfriMarket_backend.model.enums.RelayStatus;
import com.example.AfriMarket_backend.repository.RelayPointRepository;
import com.example.AfriMarket_backend.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelayService {

    private final RelayPointRepository relayPointRepository;
    private final ZoneRepository zoneRepository;

    public RelayService(RelayPointRepository relayPointRepository, ZoneRepository zoneRepository) {
        this.relayPointRepository = relayPointRepository;
        this.zoneRepository = zoneRepository;
    }

    public List<RelayPoint> findAll() {
        return relayPointRepository.findAll();
    }

    public RelayPoint findById(Long id) {
        return relayPointRepository.findById(id).orElseThrow(() -> new RuntimeException("Relais introuvable"));
    }

    public List<Zone> findAllZones() {
        return zoneRepository.findAll();
    }

    @Transactional
    public RelayPoint save(RelayPoint relay) {
        return relayPointRepository.save(relay);
    }

    @Transactional
    public void updateStatus(Long id, RelayStatus status) {
        RelayPoint relay = findById(id);
        relay.setStatus(status);
        relayPointRepository.save(relay);
    }

    @Transactional
    public void delete(Long id) {
        relayPointRepository.deleteById(id);
    }
}
