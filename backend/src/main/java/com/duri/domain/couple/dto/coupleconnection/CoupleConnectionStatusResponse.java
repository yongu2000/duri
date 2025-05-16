package com.duri.domain.couple.dto.coupleconnection;

import com.duri.domain.couple.constant.CoupleConnectionStatus;
import com.duri.domain.couple.entity.CoupleConnection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CoupleConnectionStatusResponse {

    private String requesterName;
    private String respondentName;
    private CoupleConnectionStatus status;

    public static CoupleConnectionStatusResponse from(CoupleConnection coupleConnection) {
        return CoupleConnectionStatusResponse.builder()
            .requesterName(coupleConnection.getRequester().getName())
            .respondentName(coupleConnection.getRespondent().getName())
            .status(coupleConnection.getStatus())
            .build();
    }
}
