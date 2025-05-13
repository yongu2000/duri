package com.duri.domain.couple.service;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.dto.coupleconnection.CoupleConnectionCodeResponse;
import com.duri.domain.couple.dto.coupleconnection.CoupleConnectionSendRequest;
import com.duri.domain.couple.dto.coupleconnection.CoupleConnectionStatusResponse;

public interface CoupleConnectionService {

    CoupleConnectionCodeResponse getCode(CustomUserDetails userDetails);

    CoupleConnectionStatusResponse connect(CustomUserDetails userDetails,
        CoupleConnectionSendRequest request);

    Void confirmConnectionStatus(CustomUserDetails userDetails);

    CoupleConnectionStatusResponse getSentConnectionStatus(CustomUserDetails userDetails);

    CoupleConnectionStatusResponse getReceivedConnectionStatus(
        CustomUserDetails userDetails);

    Void rejectConnection(CustomUserDetails userDetails);

    Void acceptConnection(CustomUserDetails userDetails);

    Void cancelConnection(CustomUserDetails userDetails);
}
