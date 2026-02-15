package com.app.uteq.Services;

import com.app.uteq.Dtos.CConfigurationRequest;
import com.app.uteq.Dtos.ConfigurationResponse;
import com.app.uteq.Dtos.UConfigurationRequest;

import java.util.List;

public interface IConfigurationService {
    void createConfiguration(CConfigurationRequest request);
    void updateConfiguration(UConfigurationRequest request);
    void deleteConfiguration(Integer idconfiguration);
    List<ConfigurationResponse> listConfiguration();
}
