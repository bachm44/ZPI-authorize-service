package com.zpi.api.authCode.authenticationRequest.audit;

import com.zpi.api.common.dto.UserDTO;
import com.zpi.domain.rest.analysis.twoFactor.AnalysisRequest;
import com.zpi.domain.rest.analysis.twoFactor.AuditUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuditMetadataDTO {
    private final IpInfoDTO ipInfo;
    private final DeviceInfoDTO deviceInfo;

    public AnalysisRequest toDomain(UserDTO user) {
        return new AnalysisRequest(deviceInfo.toDomain(), ipInfo.toDomain(), new AuditUser(user.getEmail()));
    }
}
