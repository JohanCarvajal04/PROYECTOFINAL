package com.app.uteq.Services;

import com.app.uteq.Dtos.CRejectionReasonRequest;
import com.app.uteq.Dtos.RejectionReasonResponse;
import com.app.uteq.Dtos.URejectionReasonRequest;

import java.util.List;

public interface IRejectionReasonsService {
    void createRejectreason(CRejectionReasonRequest request);
    void updateRejectreason(URejectionReasonRequest request);
    void deleteRejectreason(Integer idrejectionreason);
    List<RejectionReasonResponse> listRejectreason(Boolean onlyActive);
}
