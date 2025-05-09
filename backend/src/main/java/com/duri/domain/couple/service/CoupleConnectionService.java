package com.duri.domain.couple.service;

import static com.duri.domain.couple.constant.CoupleConnectionStatus.ACCEPT;
import static com.duri.domain.couple.constant.CoupleConnectionStatus.CANCEL;
import static com.duri.domain.couple.constant.CoupleConnectionStatus.PENDING;
import static com.duri.domain.couple.constant.CoupleConnectionStatus.REJECT;
import static com.duri.domain.couple.constant.CoupleRedisKey.COUPLE_CONNECTION_CODE_TO_USERID_KEY;
import static com.duri.domain.couple.constant.CoupleRedisKey.COUPLE_CONNECTION_USERID_TO_CODE_KEY;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.couple.constant.CoupleConnectionStatus;
import com.duri.domain.couple.controller.CoupleConnectionWebSocketController;
import com.duri.domain.couple.dto.CoupleConnectionCodeResponse;
import com.duri.domain.couple.dto.CoupleConnectionSendRequest;
import com.duri.domain.couple.dto.CoupleConnectionStatusResponse;
import com.duri.domain.couple.entity.Couple;
import com.duri.domain.couple.entity.CoupleConnection;
import com.duri.domain.couple.exception.ExistingCoupleConnectionException;
import com.duri.domain.couple.exception.InvalidCoupleConnectionCodeException;
import com.duri.domain.couple.exception.InvalidCoupleConnectionException;
import com.duri.domain.couple.repository.CoupleConnectionRepository;
import com.duri.domain.couple.repository.CoupleRepository;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.service.UserService;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class CoupleConnectionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;
    private final CoupleConnectionRepository coupleConnectionRepository;
    private final CoupleRepository coupleRepository;
    private final CoupleConnectionWebSocketController coupleConnectionWebSocketController;

    public CoupleConnectionCodeResponse getCode(CustomUserDetails userDetails) {
        // 유저 ID 에 대한 코드가 존재하면 불러오기
        String code = redisTemplate.opsForValue()
            .get(COUPLE_CONNECTION_USERID_TO_CODE_KEY + String.valueOf(userDetails.getId()));

        // 유저 ID 에 대한 코드가 없다면
        if (code == null) {
            // 겹치는 코드가 없을 때 까지 코드 생성
            do {
                code = generateCodeFromUUID();
            } while (Boolean.TRUE.equals(redisTemplate.hasKey(
                COUPLE_CONNECTION_CODE_TO_USERID_KEY + code)));

            // 코드 : 유저 ID 생성
            redisTemplate.opsForValue().set(
                COUPLE_CONNECTION_CODE_TO_USERID_KEY + code,
                String.valueOf(userDetails.getId()),
                Duration.ofDays(1)
            );

            // 유저 ID : 코드 생성
            redisTemplate.opsForValue().set(
                COUPLE_CONNECTION_USERID_TO_CODE_KEY + String.valueOf(userDetails.getId()),
                code,
                Duration.ofDays(1)
            );
        }
        return new CoupleConnectionCodeResponse(code);
    }

    public CoupleConnectionStatusResponse connect(CustomUserDetails userDetails,
        CoupleConnectionSendRequest request) {
        // 요청자
        User requester = userDetails.getUser();

        // 인증코드로 상대방 유저 ID 받아오기
        String respondentUserId = redisTemplate.opsForValue()
            .get(COUPLE_CONNECTION_CODE_TO_USERID_KEY + request.getCode());
        // 인증코드가 존재하지 않을 경우
        if (respondentUserId == null) {
            throw new InvalidCoupleConnectionCodeException();
        }
        // 자기 자신의 인증코드를 입력한 경우
        if (respondentUserId.equals(String.valueOf(requester.getId()))) {
            throw new InvalidCoupleConnectionCodeException();
        }

        User respondent = userService.findById(Long.valueOf(respondentUserId));

        Optional<CoupleConnection> coupleConnection = coupleConnectionRepository.findByRequesterAndRespondent(
            requester, respondent);

        // 이미 Pending 요청이 존재 할 경우
        if (coupleConnection.isPresent() && coupleConnection.get().getStatus().equals(PENDING)) {
            throw new ExistingCoupleConnectionException();
        }

        // Pending 외의 요청(CANCEL)이 존재 할 경우
        coupleConnection.ifPresent(connection -> connection.changeStatus(PENDING));

        if (coupleConnection.isEmpty()) {
            CoupleConnection connection = CoupleConnection.builder()
                .requester(requester)
                .respondent(respondent)
                .status(PENDING)
                .build();
            coupleConnectionRepository.save(connection);

            coupleConnectionWebSocketController.sendConnectionStatusRetrieveMessage(respondent);

            return CoupleConnectionStatusResponse.of(connection);
        }

        // WebSocket 커플 연결 요청 보내기
        coupleConnectionWebSocketController.sendConnectionStatusRetrieveMessage(respondent);

        //응답자(오른쪽)
        //
        //-> FCM 이용해서 푸시 알람 생성

        return CoupleConnectionStatusResponse.of(coupleConnection.get());
    }

    public Void confirmConnectionStatus(CustomUserDetails userDetails) {
        CoupleConnection coupleConnection = coupleConnectionRepository.findByRequesterOrRespondent(
                userDetails.getUser(), userDetails.getUser())
            .orElseThrow(InvalidCoupleConnectionException::new);
        coupleConnectionRepository.delete(coupleConnection);

        return null;
    }

    public CoupleConnectionStatusResponse getSentConnectionStatus(CustomUserDetails userDetails) {
        Optional<CoupleConnection> connection = coupleConnectionRepository.findByRequester(
            userDetails.getUser());

        if (connection.isEmpty()) {
            return null;
        }
        CoupleConnection coupleConnection = connection.get();
        CoupleConnectionStatus status = coupleConnection.getStatus();

        if (status.equals(PENDING) || status.equals(REJECT)) {
            return CoupleConnectionStatusResponse.of(coupleConnection);
        }

        if (status.equals(ACCEPT)) {
            coupleConnectionRepository.delete(coupleConnection);
            return CoupleConnectionStatusResponse.of(coupleConnection);
        }
        return null;
    }

    public CoupleConnectionStatusResponse getReceivedConnectionStatus(
        CustomUserDetails userDetails) {
        Optional<CoupleConnection> connection = coupleConnectionRepository.findByRespondent(
            userDetails.getUser());

        if (connection.isEmpty()) {
            return null;
        }
        CoupleConnection coupleConnection = connection.get();
        CoupleConnectionStatus status = coupleConnection.getStatus();

        if (status.equals(PENDING) || status.equals(CANCEL)) {
            return CoupleConnectionStatusResponse.of(coupleConnection);
        }

        return null;
    }

    public Void rejectConnection(CustomUserDetails userDetails) {
        User respondent = userDetails.getUser();
        CoupleConnection connection = coupleConnectionRepository.findByRespondent(respondent)
            .orElseThrow(
                InvalidCoupleConnectionException::new);
        connection.changeStatus(REJECT);

        // WebSocket 연결 거부 메세지 보내기
        coupleConnectionWebSocketController.sendConnectionStatusRetrieveMessage(
            connection.getRequester());

        // FCM 상대방이 요청 거절했다는 푸시 보내기

        return null;
    }

    public Void acceptConnection(CustomUserDetails userDetails) {
        User respondent = userDetails.getUser();
        CoupleConnection connection = coupleConnectionRepository.findByRespondent(respondent)
            .orElseThrow(
                InvalidCoupleConnectionException::new);
        connection.changeStatus(ACCEPT);
        Couple couple = Couple.of(connection.getRequester(), connection.getRespondent());

        coupleRepository.save(couple);

        // WebSocket 메인페이지로 이동시키는 메세지 보내기
        coupleConnectionWebSocketController.sendConnectionStatusRetrieveMessage(
            connection.getRequester());

        // FCM 상대방이 요청 수락했다는 푸시 보내기

        return null;
    }

    public Void cancelConnection(CustomUserDetails userDetails) {
        User requester = userDetails.getUser();
        CoupleConnection connection = coupleConnectionRepository.findByRequester(requester)
            .orElseThrow(
                InvalidCoupleConnectionException::new);
        connection.changeStatus(CANCEL);

        // WebSocket 상대방이 요청 취소했다는 메세지 보내기
        coupleConnectionWebSocketController.sendConnectionStatusRetrieveMessage(
            connection.getRespondent());

        // FCM 상대방이 요청 취소했다는 푸시 보내기

        return null;
    }

    private String generateCodeFromUUID() {
        UUID uuid = UUID.randomUUID();
        // UUID 를 A-Z까지 표현하도록 36진수로 변환 후 앞 8자리만 사용
        return new BigInteger(uuid.toString().replace("-", ""), 16).toString(36).toUpperCase()
            .substring(0, 8);
    }

}
