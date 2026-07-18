package com.example.resolveit.service;

import com.example.resolveit.model.SystemSetting;
import com.example.resolveit.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository settingRepository;

    public String getSetting(String key, String defaultValue) {
        return settingRepository.getValue(key, defaultValue);
    }

    public void updateSetting(String key, String value) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElse(new SystemSetting(key, value, "System Configuration"));
        setting.setSettingValue(value);
        settingRepository.save(setting);
    }

    public List<SystemSetting> getAllSettings() {
        return settingRepository.findAll();
    }
    
    public int getSlaHours() {
        try {
            return Integer.parseInt(getSetting("SLA_HOURS", "48"));
        } catch (NumberFormatException e) {
            return 48;
        }
    }
}
