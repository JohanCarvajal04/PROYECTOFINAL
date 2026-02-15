package com.app.uteq.Services;

import com.app.uteq.Dtos.CPermissionRequest;
import com.app.uteq.Dtos.PermissionResponse;
import com.app.uteq.Dtos.UPermissionRequest;

import java.util.List;

public interface IPermissionsService {
    void createPermission(CPermissionRequest request);
    void updatePermission(UPermissionRequest request);
    void deletePermission(Integer idpermission);
    List<PermissionResponse> listPermission();
}
