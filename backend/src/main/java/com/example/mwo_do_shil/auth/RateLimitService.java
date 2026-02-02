package com.example.mwo_do_shil.auth;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {
    private final Firestore firestore;
    private final int USER_DAILY_LIMIT = 3;
    private final int SYSTEM_DAILY_LIMIT = 600;
    // 상수로 선언해두면 오타 방지 및 재사용에 좋습니다.
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public String getTodayString() {
        // 서버 위치와 상관없이 항상 한국 날짜를 반환합니다.
        return LocalDate.now(KST).toString();
    }
    // 결과 전달을 위한 DTO
    public record RateLimitInfo(boolean isAllowed, int remainingCount, String message) {}

    // ✅ 1. 호출 전: 한도 체크만 (증가 X)
    public boolean checkRateLimit(String uid) {
        String today = getTodayString();
        try {
            return firestore.runTransaction(tx -> {
                DocumentReference systemRef = firestore.collection("system_limits").document("global");
                DocumentReference userRef = firestore.collection("usage_limits").document(uid);

                DocumentSnapshot systemSnap = tx.get(systemRef).get();
                DocumentSnapshot userSnap = tx.get(userRef).get();

                // 시스템 한도 체크
                if (isLimitExceeded(systemSnap, today, SYSTEM_DAILY_LIMIT)) {
                    log.warn("시스템 호출량 초과: {}", today);
                    return false;
                }

                // 사용자 한도 체크
                int currentUsage = getCurrentUsage(userSnap, today);
                return currentUsage < USER_DAILY_LIMIT;
            }).get();
        } catch (Exception e) {
            log.error("Rate limit 체크 실패: {}", uid, e);
            return false;
        }
    }

    // ✅ 2. 호출 성공 후: 카운트 증가
    public void incrementUsage(String uid) {
        String today = getTodayString();
        try{
            firestore.runTransaction(tx -> {
                DocumentReference systemRef = firestore.collection("system_limits").document("global");
                DocumentReference userRef = firestore.collection("usage_limits").document(uid);

                DocumentSnapshot systemSnap = tx.get(systemRef).get();
                DocumentSnapshot userSnap = tx.get(userRef).get();

                // 시스템 카운트 증가
                incrementCount(tx, systemRef, systemSnap, today);
                // 사용자 카운트 증가
                incrementCount(tx, userRef, userSnap, today);

                return null;
            });
        }catch (Exception e) {
            log.error("Rate limit 체크 실패: {}", uid, e.getMessage());
        }
    }

    // 호출량 조회 전용 메서드 (프론트엔드 초기 진입용)
    public int getRemainingUsage(String uid) {
        try {
            String today = getTodayString();
            DocumentSnapshot userSnap = firestore.collection("usage_limits").document(uid).get().get();

            int currentUsage = getCurrentUsage(userSnap, today);
            log.info("호출량 조회 성공 {e ={}}",currentUsage);
            return currentUsage;
        } catch (Exception e) {
            log.error("호출량 조회 에러 {e ={}}",e.getMessage());
            return 0;
        }
    }

    // 사용자의 호출량을 조회
    private int getCurrentUsage(DocumentSnapshot snap, String today) {
        // 오늘의 count만 고려
        if (snap.exists() && today.equals(snap.getString("last_date"))) {
            Long count = snap.getLong("count");
            return count != null ? count.intValue() : 0;
        }
        return 0;
    }

    // 사용자의 호출 제한
    private boolean isLimitExceeded(DocumentSnapshot snap, String today, int limit) {
        return getCurrentUsage(snap, today) >= limit;
    }

    public void incrementCount(Transaction tx, DocumentReference ref, DocumentSnapshot snap, String today) {
        // 현재 시간 기준으로 7일 뒤에 삭제되도록 설정 (2일 유지 -> 어차피 하루동안의 호출량을 제한하는 기능을 가지므로)
        // TTL 설정.
        long expireTimestamp = System.currentTimeMillis() / 1000 + (60 * 60 * 24 * 2);
        log.info("추천 api 호출 성공 후 incrementCount() 호출");

        if (!snap.exists() || !today.equals(snap.getString("last_date"))) {
            tx.set(ref, Map.of(
                    "count", 1,
                    "last_date", today,
                    "expire_at", expireTimestamp // TTL용 필드 추가
            ));
        } else {
            tx.update(ref,
                    "count", FieldValue.increment(1),
                    "expire_at", expireTimestamp // 활동이 있을 때마다 만료 시간 갱신 (선택사항)
            );
        }
    }
}