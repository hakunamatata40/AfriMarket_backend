package com.example.AfriMarket_backend.service;

import com.example.AfriMarket_backend.model.SystemSetting;
import com.example.AfriMarket_backend.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemSettingService {

    private final SystemSettingRepository repo;

    public SystemSettingService(SystemSettingRepository repo) {
        this.repo = repo;
    }

    public List<SystemSetting> findAll() {
        return repo.findAll();
    }

    public String getValue(String key, String defaultValue) {
        return repo.findByKey(key).map(SystemSetting::getValue).orElse(defaultValue);
    }

    @Transactional
    public void upsert(String key, String value) {
        SystemSetting s = repo.findByKey(key).orElse(new SystemSetting(key, value, ""));
        s.setValue(value);
        repo.save(s);
    }

    @Transactional
    public void saveAll(java.util.Map<String, String> params) {
        params.forEach(this::upsert);
    }
}
