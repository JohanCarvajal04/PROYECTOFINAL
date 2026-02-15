package com.app.uteq.Services.Impl;

import com.app.uteq.Dtos.CPermissionRequest;
import com.app.uteq.Dtos.PermissionResponse;
import com.app.uteq.Dtos.UPermissionRequest;
import com.app.uteq.Repository.IPermissionsRepository;
import com.app.uteq.Services.IPermissionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionsServiceImpl implements IPermissionsService {
    private final IPermissionsRepository repository;

    @Override
    public void createPermission(CPermissionRequest request) {
        repository.spiPermission(
                request.getCode(),
                request.getDescription()
        );
    }

    @Override
    public void updatePermission(UPermissionRequest request) {
        repository.spuPermission(
                request.getIdpermission(),
                request.getCode(),
                request.getDescription()
        );
    }

    @Override
    public void deletePermission(Integer idpermission) {
        repository.spdPermission(idpermission);
    }

    @Override
    public List<PermissionResponse> listPermission() {
        List<Object[]> rows = repository.fnListPermissions();

        return rows.stream().map(r -> new PermissionResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2])
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue(); // por si viene Long/BigInteger
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }
}
