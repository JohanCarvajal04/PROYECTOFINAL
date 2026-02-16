package com.app.uteq.Dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    private Integer idRole;
    private String roleName;
    private String roleDescription;
    private List<PermissionInfo> permissions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionInfo {
        private Integer idPermission;
        private String code;
        private String description;
    }
}
