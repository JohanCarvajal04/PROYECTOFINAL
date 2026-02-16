package com.app.uteq.Services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CConfigurationRequest;
import com.app.uteq.Dtos.ConfigurationResponse;
import com.app.uteq.Dtos.UConfigurationRequest;
import com.app.uteq.Repository.IConfigurationsRepository;
import com.app.uteq.Services.IConfigurationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfigurationServiceImpl implements IConfigurationService {
    private final IConfigurationsRepository repository;

    @Override
    public void createConfiguration(CConfigurationRequest request) {
        repository.spiConfiguration(
                request.getProfilepicturepath(),
                request.getSignaturepath(),
                request.getEnable_sms(),
                request.getEnable_email(),
                request.getEnable_whatsapp(),
                request.getNotificationfrequency()
        );
    }

    @Override
    public void updateConfiguration(UConfigurationRequest request) {
        repository.spuConfiguration(
                request.getIdconfiguration(),
                request.getProfilepicturepath(),
                request.getSignaturepath(),
                request.getEnable_sms(),
                request.getEnable_email(),
                request.getEnable_whatsapp(),
                request.getNotificationfrequency()
        );
    }

    @Override
    public void deleteConfiguration(Integer idconfiguration) {
        repository.spdConfiguration(idconfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigurationResponse> listConfiguration() {
        List<Object[]> rows = repository.fnListConfigurations();

        return rows.stream().map(r -> new ConfigurationResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toBool(r[3]),
                toBool(r[4]),
                toBool(r[5]),
                toStr(r[6])
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }

    private Boolean toBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(v.toString());
    }
}
